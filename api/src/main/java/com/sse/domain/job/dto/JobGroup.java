package com.sse.domain.job.dto;

import com.sse.domain.job.constant.JobStatus;
import com.sse.domain.job.constant.JobType;
import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.Map;

import static com.sse.domain.job.constant.JobStatus.WAITING;

/**
 * 유저의 모든 Job 들의 집합 (ex. 어떤 유저의 모든 실행 중인 쓰레드)
 */

@Data
@Builder
public class JobGroup {

    private String groupId;
    private JobStatus status;
    private Map<String, JobQueue> jobQueueMap;

    public JobQueue getJobQueue(String queueId) {
        return jobQueueMap.get(queueId);
    }

    public JobQueue getJobQueue(String groupId, String queueId, JobType jobQueueType) {
        if (!jobQueueMap.containsKey(queueId)) {
            JobQueue jobQueue = getNewJobQueue(groupId, queueId, jobQueueType);
            jobQueue.setStatus(WAITING);
            jobQueueMap.put(getQueueMapKey(jobQueueType, queueId), jobQueue);
        }

        return jobQueueMap.get(getQueueMapKey(jobQueueType, queueId));
    }

    public void removeJobQueue(String queueId) {
        this.jobQueueMap.remove(queueId);
    }

    private JobQueue getNewJobQueue(String groupId, String queueId, JobType jobQueueType) {
        return JobQueue.builder()
                .groupId(groupId)
                .queueId(queueId)
                .jobList(new ArrayList<>())
                .type(jobQueueType)
                .build();
    }

    private String getQueueMapKey(JobType jobQueueType, String queueId) {
        return jobQueueType.toString() + queueId;
    }

}
