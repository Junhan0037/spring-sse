package com.sse.domain.job.service;

import com.sse.domain.job.constant.JobMode;
import com.sse.domain.job.constant.JobType;
import com.sse.domain.job.dto.JobDTO;
import com.sse.domain.job.dto.JobQueue;
import com.sse.domain.job.executor.JobExecutor;
import com.sse.domain.sse.service.SseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import static com.sse.domain.job.constant.JobStatus.ERROR;

@Service
@RequiredArgsConstructor
@Slf4j
public class JobService {

    private final JobExecutor jobExecutor;
    private final SseService sseService;

    public JobQueue getJobQueue(JobDTO jobDTO, JobType jobType) {
        return jobExecutor.getJobQueue(jobDTO.getGroupId(), jobDTO.getQueueId(), jobType);
    }

    public void executeJobQueueAsync(JobQueue jobQueue, JobMode jobMode, String groupId, String eventName) {
        jobExecutor.runJobQueueAsync(jobQueue, jobMode, (result) -> {
            if (ERROR.equals(jobQueue.getStatus())) {
                sseService.sendData(groupId, eventName, jobQueue.getErrorMsg());
            }

            log.info(result.toString());
            sseService.sendData(groupId, eventName, result);

            jobExecutor.removeJobQueue(groupId, jobQueue.getQueueId(), jobQueue.getType());
            sseService.complete(groupId, eventName);
        });
    }

}
