package com.sse.domain.job.dto;

import com.sse.domain.job.constant.JobType;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class JobDTO {

    private String groupId;
    private String queueId;
    private JobType jobType;

}
