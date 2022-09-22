package com.sse.domain.job.dto;

import com.sse.domain.job.constant.JobMode;
import com.sse.domain.job.constant.JobStatus;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

import static com.sse.domain.job.constant.JobStatus.WAITING;

/**
 * 실제 실행되는 쓰레드들의 집합 (순차, 병렬 실행 가능)
 */

@Data
public class Job<T> {

    private String name;
    private List<Supplier<T>> taskList; // Supplier<T> : 함수형 인터페이스 (인자를 받지 않고 Type T 객체를 리턴)
    private JobMode mode;
    private JobStatus status;
    private CompletableFuture future;

    public Job(String name, JobMode mode) {
        this.name = name;
        this.mode = mode;
        this.status = WAITING;
        this.taskList = new ArrayList<>();
    }

    public void addTask(Supplier<T> task) {
        this.taskList.add(task);
    }

    public void addTask(Runnable runnable) {
        Supplier<T> supplier = () -> {
            runnable.run();
            return null;
        };
        taskList.add(supplier);
    }

}
