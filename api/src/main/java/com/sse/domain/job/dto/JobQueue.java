package com.sse.domain.job.dto;

import com.sse.domain.job.constant.JobStatus;
import com.sse.domain.job.constant.JobType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

import static com.sse.domain.job.constant.JobStatus.*;

/**
 * Job 들을 Queue 에 쌓아서 순차적으로 실행 (ex: (create table job : 단일 -> insert table job : 병렬 -> get data job : 병렬))
 * Work Runner 에서 실행하여 결과를 얻는 최소 단위
 */

@Data
@Builder
public class JobQueue<T> {

    private String groupId;
    private String queueId;
    private JobType type;
    private List<Job> jobList;
    private JobStatus status;
    private String errorMsg;
    private T parameter;

    public void push(Job job) {
        this.jobList.add(job);
    }

    public List<Job> getJobList() {
        return this.jobList;
    }

    public static String getNewId() {
        return LocalDateTime.now().toString();
    }

    public boolean isCanceled() {
        return CANCELED.equals(status) || CANCEL_REQUEST.equals(status) || ERROR.equals(status);
    }

}
