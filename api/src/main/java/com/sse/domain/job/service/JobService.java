package com.sse.domain.job.service;

import com.sse.domain.job.constant.JobMode;
import com.sse.domain.job.constant.JobType;
import com.sse.domain.job.dto.Job;
import com.sse.domain.job.dto.JobDTO;
import com.sse.domain.job.dto.JobQueue;
import com.sse.domain.job.executor.JobExecutor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import static com.sse.domain.job.constant.JobMode.SERIAL;
import static com.sse.domain.job.constant.JobStatus.ERROR;

@Service
@RequiredArgsConstructor
@Slf4j
public class JobService {

    private final JobExecutor jobExecutor;

    public Job<?> getJob(String jobName, JobMode mode) {
        return new Job<>(jobName, mode);
    }

    public JobQueue<?> getJobQueue(JobDTO jobDTO, JobType jobType) {
        JobQueue<?> jobQueue = jobExecutor.getJobQueue(jobDTO.getGroupId(), jobDTO.getQueueId(), jobType);
//        jobQueue.setParameter();
        return jobQueue;
    }

    public void executeJobQueueAsync(JobQueue jobQueue, JobDTO jobDTO) {
        jobExecutor.runJobQueueAsync(jobQueue, SERIAL, (result) -> {
            if (ERROR.equals(jobQueue.getStatus())) {
                log.info("JobQueue status is ERROR!!!");
            }

            jobExecutor.removeJobQueue(jobDTO.getGroupId(), jobQueue.getQueueId(), jobQueue.getType());

            log.info(result.toString());
        });
    }

}
