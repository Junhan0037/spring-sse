package com.sse.domain.sse.dto;

import com.sse.domain.job.constant.JobStatus;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SseDTO {

    private Object result;
    private JobStatus status;

}
