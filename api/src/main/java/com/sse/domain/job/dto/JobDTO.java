package com.sse.domain.job.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class JobDTO {

    private String groupId;
    private String queueId;
    private String pid;

}
