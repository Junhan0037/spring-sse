package com.sse.domain.member;

import com.sse.domain.job.constant.JobType;
import com.sse.domain.job.dto.Job;
import com.sse.domain.job.dto.JobDTO;
import com.sse.domain.job.dto.JobQueue;
import com.sse.domain.job.service.JobService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static com.sse.domain.job.constant.JobMode.PARALLEL;
import static com.sse.domain.job.constant.JobMode.SERIAL;
import static com.sse.domain.job.constant.JobType.ETC;

@Service
@Transactional
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final JobService jobService;

    /**
     * Non-Block, Async + 병렬 처리
     */
    public JobDTO findAllMembers(Member member) {
        JobDTO jobDTO = JobDTO.builder()
                .groupId(member.getEmail())
                .queueId("testQueueId-" + LocalDateTime.now())
                .build();

        JobType jobType = ETC;
        JobQueue jobQueue = jobService.getJobQueue(jobDTO, jobType);

        String jobName = "getAllMembers";
        Job<List<Member>> job = new Job<>(jobName, PARALLEL);
        job.addTask(() -> memberRepository.findAll());
        job.addTask(() -> memberRepository.findAll());

        jobQueue.push(job);

        String eventName = jobType + jobDTO.getQueueId();
        jobService.executeJobQueueAsync(jobQueue, SERIAL, jobDTO.getGroupId(), eventName);

        jobDTO.setJobType(jobType);
        return jobDTO;
    }

}
