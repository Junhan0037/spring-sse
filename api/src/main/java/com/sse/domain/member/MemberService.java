package com.sse.domain.member;

import com.sse.domain.job.dto.Job;
import com.sse.domain.job.dto.JobDTO;
import com.sse.domain.job.dto.JobQueue;
import com.sse.domain.job.service.JobService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import static com.sse.domain.job.constant.JobMode.PARALLEL;
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
    public void findAllMembers(Member member) {
        JobDTO jobDTO = JobDTO.builder()
                .groupId(member.getEmail())
                .queueId("testQueueId")
                .build();

        JobQueue<?> jobQueue = jobService.getJobQueue(jobDTO, ETC);

        Job<?> job = jobService.getJob("getAllMembers", PARALLEL);
        job.addTask(() -> memberRepository.findAll());
        job.addTask(() -> memberRepository.findAll());

        jobQueue.push(job);

        jobService.executeJobQueueAsync(jobQueue, jobDTO);
    }

}
