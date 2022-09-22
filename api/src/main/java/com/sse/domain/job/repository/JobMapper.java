package com.sse.domain.job.repository;

import com.sse.domain.job.dto.JobDTO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface JobMapper {

    List<String> getProcessId(JobDTO jobDTO);

    void killProcess(JobDTO jobDTO);

}
