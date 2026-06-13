package com.eventtracker.service;

import com.eventtracker.entity.ActivityLog;

import java.util.List;

public interface ActivityLogService {

    void log(String type, String message, String actor);

    List<ActivityLog> recent();

    long count();
}
