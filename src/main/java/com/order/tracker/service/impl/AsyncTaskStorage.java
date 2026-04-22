package com.order.tracker.service.impl;

import com.order.tracker.domain.AsyncTask;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

@Component
public class AsyncTaskStorage {

    private final Map<String, AsyncTask> taskStatuses = new ConcurrentHashMap<>();

    public void saveTask(final AsyncTask task) {
        taskStatuses.put(task.getTaskId(), task);
    }

    public AsyncTask getTask(final String taskId) {
        return taskStatuses.get(taskId);
    }

    public Map<String, AsyncTask> getAllTasks() {
        return taskStatuses;
    }
}
