package com.sse.domain.job.executor;

import com.sse.core.exception.BaseException;
import com.sse.domain.job.dto.Job;
import com.sse.domain.job.dto.JobQueue;
import com.sse.util.SseObjectUtil;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import static com.sse.core.exception.ErrorType.USER_NOT_EXISTS;
import static com.sse.domain.job.constant.JobMode.PARALLEL;
import static com.sse.domain.job.constant.JobMode.SERIAL;
import static com.sse.domain.job.constant.JobStatus.*;
import static com.sse.domain.job.constant.JobType.TEST;
import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@ExtendWith(SpringExtension.class)
class JobExecutorTest {

    @InjectMocks
    private JobExecutor jobExecutor;

    @Test
    @DisplayName("runJob() :: parallel (병렬처리인 경우 :: 멀티 쓰레드)")
    @SneakyThrows
    public void runJobInParallelTest() {
        List<Integer> expectedList = new ArrayList<>();

        String jobName = "JOB_PARALLEL";
        Job<Integer> parallelJob = new Job<>(jobName, PARALLEL);

        IntStream.range(0, 100).forEach(num -> {
            expectedList.add(num);
            parallelJob.addTask(() -> {
                int randomTime = (int) (Math.random() * 10) * 10;
                try {
                    Thread.sleep(randomTime);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return num;
            });
        });

        List<Integer> actualList = (List<Integer>) jobExecutor.runJob(parallelJob);

        log.debug("Finish!! >>> {}", actualList);

        assertArrayEquals(expectedList.stream().sorted().toArray(), actualList.stream().sorted().toArray());
    }

    @Test
    @DisplayName("runJob() :: serial (순차처리인 경우 :: 단일 쓰레드)")
    @SneakyThrows
    public void runJobInSerialTest() {
        List<Integer> expectedList = new ArrayList<>();

        String jobName = "JOB_SERIAL";
        Job<Integer> serialJob = new Job<>(jobName, SERIAL);

        IntStream.range(0, 100).forEach(num -> {
            expectedList.add(num);
            serialJob.addTask(() -> {
                int randomTime = (int) (Math.random() * 10) * 10;
                try {
                    Thread.sleep(randomTime);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return num;
            });
        });

        List<Integer> actualList = (List<Integer>) jobExecutor.runJob(serialJob);

        log.debug("Finish!! >>> {}", actualList);

        assertArrayEquals(expectedList.stream().sorted().toArray(), actualList.stream().sorted().toArray());
    }

    @Test
    @DisplayName("runJob() :: runnable 형태 테스트 (return 이 없는 Task)")
    @SneakyThrows
    public void runJobRunnableTest() {
        String jobName = "JOB_RUNNABLE";
        Job runnableJob = new Job(jobName, SERIAL);

        IntStream.range(0, 10).forEach(num -> {
            int randomTime = (int) (Math.random() * 10) * 10;
            runnableJob.addTask(() -> {
                try {
                    Thread.sleep(randomTime);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });
        });

        Object result = jobExecutor.runJob(runnableJob);

        assertFalse(SseObjectUtil.isNotEmpty(result));
    }

    @Test
    @DisplayName("runJobQueueSync():: serial & parallel")
    public void runJobQueueSyncTest() {
        String groupId = "ADMIN";
        String queueId = "TEST_QUEUE";
        Map<String, List<Object>> expectedMap = new HashMap<>();

        // 1. Job Queue 생성
        JobQueue jobQueue = jobExecutor.getJobQueue(groupId, queueId, TEST);

        // 2. Job 생성
        String createTableJobName = "CREATE_TABLE";
        expectedMap.put(createTableJobName, new ArrayList<>());
        Job<Integer> createTableJob = new Job<>(createTableJobName, SERIAL);

        IntStream.range(0, 100).forEach(num -> {
            expectedMap.get(createTableJobName).add(num);
            createTableJob.addTask(() -> {
                assertEquals(RUNNING, jobQueue.getStatus());
                int randomTime = (int) (Math.random() * 10) * 10;
                try {
                    Thread.sleep(randomTime);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return num;
            });
        });

        String insertTableJobName = "INSERT_TABLE";
        expectedMap.put(insertTableJobName, new ArrayList<>());
        Job<String> insertTableJob = new Job<>(insertTableJobName, PARALLEL);

        IntStream.range(0, 100).forEach(num -> {
            expectedMap.get(insertTableJobName).add(String.valueOf(num));
            insertTableJob.addTask(() -> {
                assertEquals(RUNNING, jobQueue.getStatus());
                int randomTime = (int) (Math.random() * 10) * 10;
                try {
                    Thread.sleep(randomTime);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return String.valueOf(num);
            });
        });

        String selectTableJobName = "SELECT_TABLE";
        expectedMap.put(selectTableJobName, new ArrayList<>());
        Job<String> selectTableJob = new Job<>(selectTableJobName, PARALLEL);

        IntStream.range(0, 100).forEach(num -> {
            expectedMap.get(selectTableJobName).add(String.valueOf(num));
            selectTableJob.addTask(() -> {
                assertEquals(RUNNING, jobQueue.getStatus());
                int randomTime = (int) (Math.random() * 10) * 10;
                try {
                    Thread.sleep(randomTime);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return String.valueOf(num);
            });
        });

        // 3. Job Queue 추가
        jobQueue.push(List.of(createTableJob, insertTableJob, selectTableJob));

        // 4-1. 실행 (Serial)
        Map<String, Object> actualInSerial = jobExecutor.runJobQueueSync(jobQueue, SERIAL);
        assertArrayEquals(expectedMap.get(createTableJobName).stream().sorted().toArray(), ((List<Object>) actualInSerial.get(createTableJobName)).stream().sorted().toArray());
        assertArrayEquals(expectedMap.get(insertTableJobName).stream().sorted().toArray(), ((List<Object>) actualInSerial.get(insertTableJobName)).stream().sorted().toArray());
        assertArrayEquals(expectedMap.get(selectTableJobName).stream().sorted().toArray(), ((List<Object>) actualInSerial.get(selectTableJobName)).stream().sorted().toArray());
        assertEquals(COMPLETE, jobQueue.getStatus());

        // 4-2. 실행 (Parallel)
        Map<String, Object> actualInParallel = jobExecutor.runJobQueueSync(jobQueue, PARALLEL);
        assertArrayEquals(expectedMap.get(createTableJobName).stream().sorted().toArray(), ((List<Object>) actualInParallel.get(createTableJobName)).stream().sorted().toArray());
        assertArrayEquals(expectedMap.get(insertTableJobName).stream().sorted().toArray(), ((List<Object>) actualInParallel.get(insertTableJobName)).stream().sorted().toArray());
        assertArrayEquals(expectedMap.get(selectTableJobName).stream().sorted().toArray(), ((List<Object>) actualInParallel.get(selectTableJobName)).stream().sorted().toArray());
        assertEquals(COMPLETE, jobQueue.getStatus());

        // 5. JobQueue 삭제
        jobExecutor.removeJobQueue(jobQueue);
    }

    @Test
    @DisplayName("runJobQueueSync():: 예외처리 테스트")
    public void runJobQueueSyncThrowTest() {
        String groupId = "ADMIN";
        String queueId = "THROW_QUEUE";

        // 1. Job Queue 생성
        JobQueue jobQueue = jobExecutor.getJobQueue(groupId, queueId, TEST);

        Job throwJob = new Job("THROW_JOB", SERIAL);
        throwJob.addTask(() -> {
            throw new BaseException(USER_NOT_EXISTS);
        });

        jobQueue.push(throwJob);
        jobExecutor.runJobQueueSync(jobQueue, SERIAL);

        assertEquals(ERROR, jobQueue.getStatus());
        assertTrue(jobQueue.getErrorMsg().contains(USER_NOT_EXISTS.getMessage()));

        jobExecutor.removeJobQueue(jobQueue);
    }

    @Test
    @DisplayName("runJobQueueAsync() :: async test")
    public void runJobQueueAsyncTest() throws InterruptedException {
        String groupId = "ADMIN";
        String queueId = "TEST_QUEUE";
        Map<String, List<Object>> expectedMap = new HashMap<>();

        // 1. Job Queue 생성
        JobQueue jobQueue = jobExecutor.getJobQueue(groupId, queueId, TEST);

        // 2. Job 생성
        String createTableJobName = "CREATE_JOB";
        expectedMap.put(createTableJobName, new ArrayList<>());
        Job<Integer> createTableJob = new Job<>(createTableJobName, SERIAL);

        IntStream.range(0, 100).forEach(num -> {
            expectedMap.get(createTableJobName).add(num);
            createTableJob.addTask(() -> {
                assertEquals(RUNNING, jobQueue.getStatus());
                int randomTime = (int) (Math.random() * 10) * 10;
                try {
                    Thread.sleep(randomTime);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return num;
            });
        });

        jobQueue.push(createTableJob);

        jobExecutor.runJobQueueAsync(jobQueue, SERIAL, (actual) -> {
            assertArrayEquals(expectedMap.get(createTableJobName).stream().sorted().toArray(), ((List<Object>) actual.get(createTableJobName)).stream().sorted().toArray());
            assertEquals(COMPLETE, jobQueue.getStatus());
        });

        Thread.sleep(10000);
    }

    @Test
    @DisplayName("cancelJobQueue() :: job queue cancel")
    public void cancelJobQueueTest() {
        String groupId = "ADMIN";
        String queueId = "TEST_QUEUE";
        JobQueue jobQueue = jobExecutor.getJobQueue(groupId, queueId, TEST);

        String jobName = "JOB";

        IntStream.range(0, 5).forEach(seq -> {
            Job<Integer> job = new Job<>(jobName + seq, PARALLEL);
            IntStream.range(0, 10).forEach(num -> {
                if (num == 5) {
                    job.addTask(() -> {
                        jobExecutor.cancelJobQueue(jobQueue, true);
                        return num;
                    });
                } else {
                    job.addTask(() -> {
                        int randomTime = (int) (Math.random() * 10) * 10;
                        try {
                            Thread.sleep(randomTime);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        return num;
                    });
                }
            });
            jobQueue.push(job);
        });

        jobExecutor.runJobQueueSync(jobQueue, PARALLEL);
        assertEquals(CANCELED, jobQueue.getStatus());

        jobExecutor.removeJobQueue(jobQueue);
    }

}
