package com.sse.domain.job.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum JobStatus {

    WAITING("WAITING"),
    RUNNING("RUNNING"),
    ERROR("ERROR"),
    COMPLETE("DONE"),
    CANCEL_REQUEST("CANCEL_REQUEST"),
    CANCELED("CANCELED");

    private final String value;

}
