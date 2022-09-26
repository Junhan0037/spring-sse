package com.sse.domain.job.executor;

import com.sse.domain.job.constant.JobMode;
import com.sse.domain.job.constant.JobType;
import com.sse.domain.job.dto.Job;
import com.sse.domain.job.dto.JobGroup;
import com.sse.domain.job.dto.JobQueue;
import com.sse.util.SseObjectUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.PreDestroy;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static com.sse.domain.job.constant.JobMode.PARALLEL;
import static com.sse.domain.job.constant.JobStatus.*;

/**
 * JobQueue 관리 (생성, 삭제, 실행, 중지 ...)
 *
 * Sync : 동기 (Block) 응답까지 대기
 * Async : 비동기 (Non-Block) 백그라운드에 넘기고 다른 처리
 * Serial : 순차 (단일 쓰레드)
 * Parallel : 병렬 (병렬 쓰레드)
 */

@Slf4j
@Service
public class JobExecutor {

    private Map<String, JobGroup> jobGroupMap = new ConcurrentHashMap<>(); // Multi-Thread 환경에서 사용할 수 있는 Map
    private ThreadPoolExecutor threadPoolExecutor = (ThreadPoolExecutor) Executors.newCachedThreadPool();

    @PreDestroy
    public void destroy() {
        threadPoolExecutor.shutdown();
    }

    // =================================================================================================================
    // =================================================== JobGroup ====================================================
    // =================================================================================================================
    public JobGroup getJobGroup(String groupId) {
        if (!jobGroupMap.containsKey(groupId)) {
            JobGroup jobGroup = getNewJobGroup(groupId);
            jobGroupMap.put(groupId, jobGroup);
        }

        return jobGroupMap.get(groupId);
    }

    private JobGroup getNewJobGroup(String groupId) {
        Map<String, JobQueue> jobQueueMap = new ConcurrentHashMap<>();

        return JobGroup.builder()
                .groupId(groupId)
                .jobQueueMap(jobQueueMap)
                .build();
    }

    public void removeAllJobGroup() {
        Set<String> groupIdSet = jobGroupMap.keySet();
        for (String groupId : groupIdSet) {
            removeJobGroup(groupId);
        }
    }

    public void removeJobGroup(String groupId) {
        cancelJobGroup(groupId, true);
        jobGroupMap.remove(groupId);
    }

    public void cancelJobGroup(String groupId, boolean mayInterruptIfRunning) {
        Map<String, JobQueue> jobQueueMap = getJobGroup(groupId).getJobQueueMap();
        Set<String> jobQueueKeySet = jobQueueMap.keySet();

        for (String key : jobQueueKeySet) {
            JobQueue queue = jobQueueMap.get(key);
            cancelJobQueue(groupId, queue.getQueueId(), queue.getType(), mayInterruptIfRunning);
        }
    }

    // =================================================================================================================
    // =================================================== JobQueue ====================================================
    // =================================================================================================================
    /**
     * JobQueue 로직 Non-Block 처리
     * Consumer<T> : 1개의 Type T 인자를 받고 리턴 값이 없는 함수형 인터페이스
     */
    public void runJobQueueAsync(JobQueue jobQueue, JobMode jobQueueMode, Consumer<Map<String, Object>> fn) {
        CompletableFuture.runAsync(() -> {
            Map<String, Object> result = runJobQueueSync(jobQueue, jobQueueMode);
            fn.accept(result);
        }, threadPoolExecutor);
    }

    /**
     * JobQueue 로직 Block 처리
     */
    public Map<String, Object> runJobQueueSync(JobQueue jobQueue, JobMode jobQueueMode) {
        boolean isParallelMode = PARALLEL.equals(jobQueueMode);
        jobQueue.setStatus(RUNNING);

        Map<String, Object> result = null;

        try {
            result = isParallelMode ? runJobQueueSyncInParallel(jobQueue) : runJobQueueSyncInSerial(jobQueue);
            if (CANCEL_REQUEST.equals(jobQueue.getStatus())) {
                jobQueue.setStatus(CANCELED);
                jobQueue.getJobList().forEach(job -> job.setStatus(CANCELED));
            } else if (!ERROR.equals(jobQueue.getStatus())) {
                jobQueue.setStatus(COMPLETE);
            }
        } catch (Exception e) {
            handleException(jobQueue, e);
        }

        return result;
    }

    /**
     * JobQueue 로직 Block 처리 -> Job 순차 처리
     */
    private Map<String, Object> runJobQueueSyncInSerial(JobQueue jobQueue) throws Exception {
        Map<String, Object> resultMap = new HashMap<>();
        List<Job> jobList = jobQueue.getJobList();

        for (Job job : jobList) {
            Object result = runJob(job); // Job 실행
            if (SseObjectUtil.isNotEmpty(result)) {
                resultMap.put(job.getName(), result);
            }
            job.setStatus(COMPLETE);
        }

        return resultMap;
    }

    /**
     * JobQueue 로직 Block 처리 -> Job 병렬 처리
     */
    private Map<String, Object> runJobQueueSyncInParallel(JobQueue jobQueue) throws Exception {
        Map<String, Object> resultMap = new ConcurrentHashMap<>();
        List<Job> jobList = jobQueue.getJobList();
        List<CompletableFuture> futureList = new ArrayList<>();

        for (Job job : jobList) {
            CompletableFuture future = CompletableFuture.supplyAsync(() -> {
                Object result = null;

                if (CANCEL_REQUEST.equals(jobQueue.getStatus()) || ERROR.equals(jobQueue.getStatus())) {
                    return null;
                }
                try {
                    result = runJob(job);
                    if (SseObjectUtil.isNotEmpty(result)) {
                        resultMap.put(job.getName(), result);
                    }
                    job.setStatus(COMPLETE);
                } catch (Exception e) {
                    handleException(jobQueue, e);
                }
                return result;
            }, threadPoolExecutor);

            futureList.add(future);
        }

        CompletableFuture<Void> allFuture = CompletableFuture.allOf(futureList.toArray(new CompletableFuture[futureList.size()]));

        allFuture.get(); // Job 실행

        return resultMap;
    }

    public void cancelJobQueue(String groupId, String queueId, JobType type, boolean mayInterruptIfRunning) {
        JobQueue jobQueue = getJobGroup(groupId).getJobQueue(type.toString() + queueId);

        if (Objects.isNull(jobQueue) || !RUNNING.equals(jobQueue.getStatus())) return;

        List<Job> jobList = jobQueue.getJobList();
        jobQueue.setStatus(CANCEL_REQUEST);

        for (Job job : jobList) {
            job.setStatus(CANCEL_REQUEST);
            CompletableFuture future = job.getFuture();
            if (Objects.nonNull(future)) {
                future.cancel(mayInterruptIfRunning);
            }
        }
    }

    public JobQueue getJobQueue(String groupId, String queueId, JobType jobQueueType) {
        JobGroup jobGroup = getJobGroup(groupId);
        return jobGroup.getJobQueue(groupId, queueId, jobQueueType);
    }

    public void removeJobQueue(JobQueue jobQueue) {
        removeJobQueue(jobQueue.getGroupId(), jobQueue.getQueueId(), jobQueue.getType());
    }

    public void removeJobQueue(String groupId, String queueId, JobType type) {
        cancelJobQueue(groupId, queueId, type, true);
        getJobGroup(groupId).removeJobQueue(queueId);
    }

    // =================================================================================================================
    // ===================================================== Job =======================================================
    // =================================================================================================================
    /**
     * Job 의 Task 처리
     */
    private Object runJob(Job job) throws Exception {
        boolean isParallelMode = PARALLEL.equals(job.getMode());
        List<CompletableFuture> futureList = isParallelMode ? runJobInParallel(job) : runJobInSerial(job);
        CompletableFuture<Void> allFuture = CompletableFuture.allOf(futureList.toArray(new CompletableFuture[futureList.size()]));

        allFuture.get(); // 병렬 Job 실행

        List<Object> result = futureList.stream()
                .map(CompletableFuture::join) // join() : CompletableFuture 가 끝나기를 기다리는 Blocking 메소드
                .collect(Collectors.toList());

        return result.size() == 1 ? result.get(0) : result;
    }

    /**
     * Job 의 Task 순차 처리
     */
    private List<CompletableFuture> runJobInSerial(Job job) {
        List<Supplier> taskList = job.getTaskList();
        List<CompletableFuture> futureList = new ArrayList<>();

        int idx = 0;
        for (Supplier task : taskList) {
            CompletableFuture future;
            if (idx == 0) {
                future = CompletableFuture.supplyAsync(() -> {
                    if (CANCEL_REQUEST.equals(job.getStatus())) {
                        return null;
                    }
                    return task.get(); // Task 실행
                }, threadPoolExecutor);
            } else {
                future = futureList.get(idx - 1).thenApply((capture) -> { // thenApply() : 앞 Task 수행이 끝난 뒤 수행
                    if (CANCEL_REQUEST.equals(job.getStatus())) {
                        return null;
                    }
                    return task.get(); // Task 실행
                });
            }
            futureList.add(future);
            idx++;
        }

        return futureList;
    }

    /**
     * Job 의 Task 병렬 처리
     */
    private List<CompletableFuture> runJobInParallel(Job job) {
        List<Supplier> taskList = job.getTaskList();
        List<CompletableFuture> futureList = new ArrayList<>();

        for (Supplier task : taskList) {
            CompletableFuture future;

            future = CompletableFuture.supplyAsync(() -> {
                if (CANCEL_REQUEST.equals(job.getStatus())) {
                    return null;
                }
                return task.get();
            }, threadPoolExecutor);

            futureList.add(future);
        }

        return futureList;
    }

    // =================================================================================================================
    // ================================================== Exception ====================================================
    // =================================================================================================================
    private void handleException(JobQueue jobQueue, Exception e) {
        if (isForceCancel(e)) {
            jobQueue.setStatus(CANCELED);
        } else {
            setErrorStatus(jobQueue, e);
        }
    }

    private boolean isForceCancel(Exception e) {
        if (StringUtils.isEmpty(e.getCause().getMessage())) return false;
        return e.getCause().getMessage().contains("java.sql.SQLRecoverableException");
    }

    private void setErrorStatus(JobQueue jobQueue, Exception e) {
        jobQueue.setStatus(ERROR);
        jobQueue.setErrorMsg(e.getMessage());
        e.printStackTrace();
    }

}
