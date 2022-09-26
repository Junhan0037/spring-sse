package com.sse.domain.member;

import com.sse.domain.job.constant.JobMode;
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
    public JobDTO findAllMembers(Member member, String eventName) {
        JobDTO jobDTO = JobDTO.builder()
                .groupId(member.getEmail())
                .queueId("-queue-" + LocalDateTime.now())
                .eventName(eventName)
                .build();

        JobType jobQueueType = ETC;
        JobMode jobQueueMode = SERIAL;
        JobQueue jobQueue = jobService.getJobQueue(jobDTO, jobQueueType);

        String jobName1 = "getAllMembersInPARALLEL";
        Job<List<Member>> job1 = new Job<>(jobName1, PARALLEL);
        job1.addTask(() -> memberRepository.findAll());
        job1.addTask(() -> memberRepository.findAll());

        String jobName2 = "getAllMembersInSERIAL";
        Job<List<Member>> job2 = new Job<>(jobName2, SERIAL);
        job2.addTask(() -> memberRepository.findAll());
        job2.addTask(() -> memberRepository.findAll());

        jobQueue.push(List.of(job1, job2));
        jobService.executeJobQueueAsync(jobQueue, jobQueueMode, jobDTO.getGroupId(), jobDTO.getEventName());

        return jobDTO;
    }

}
