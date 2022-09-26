package com.sse.domain.job.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum JobType {

    PSD("psd"),
    ANO("ano"),
    PSD_ANO("psd_ano"),
    ETC("etc"),
    TEST("test");

    private final String mode;

}
