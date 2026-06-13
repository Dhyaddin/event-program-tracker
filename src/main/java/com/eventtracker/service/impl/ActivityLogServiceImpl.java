package com.eventtracker.service.impl;

import com.eventtracker.entity.ActivityLog;
import com.eventtracker.repository.ActivityLogRepository;
import com.eventtracker.service.ActivityLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ActivityLogServiceImpl implements ActivityLogService {

    private final ActivityLogRepository activityLogRepository;

    @Override
    public void log(String type, String message, String actor) {
        activityLogRepository.save(ActivityLog.builder()
                .type(type)
                .message(message)
                .actor(actor)
                .build());
    }

    @Override
    public List<ActivityLog> recent() {
        return activityLogRepository.findTop20ByOrderByCreatedAtDesc();
    }

    @Override
    public long count() {
        return activityLogRepository.count();
    }
}
