package com.sse.domain.job.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum JobMode {

    SERIAL, // 순차
    PARALLEL; // 병렬

}
