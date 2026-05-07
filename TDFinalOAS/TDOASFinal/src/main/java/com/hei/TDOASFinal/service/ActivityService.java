package com.hei.TDOASFinal.service;

import com.hei.TDOASFinal.model.Activity;
import com.hei.TDOASFinal.model.CreateActivity;
import com.hei.TDOASFinal.repository.ActivityRepository;
import com.hei.TDOASFinal.repository.CollectivityRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Service
public class ActivityService {

    private final ActivityRepository activityRepository;
    private final CollectivityRepository collectivityRepository;

    public ActivityService(ActivityRepository activityRepository,
                           CollectivityRepository collectivityRepository) {
        this.activityRepository = activityRepository;
        this.collectivityRepository = collectivityRepository;
    }

    public List<Activity> createAll(String collectivityId, List<CreateActivity> payloads) {
        try {
            if (!collectivityRepository.existsById(collectivityId)) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Collectivity not found: " + collectivityId);
            }
            List<Activity> result = new ArrayList<>();
            for (CreateActivity payload : payloads) {
                Activity a = new Activity();
                a.setTitle(payload.getTitle());
                a.setType(payload.getType());
                a.setActivityDate(payload.getActivityDate());
                a.setIsMandatory(payload.getIsMandatory() == null || payload.getIsMandatory());
                result.add(activityRepository.save(collectivityId, a));
            }
            return result;
        } catch (SQLException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    public List<Activity> getAll(String collectivityId) {
        try {
            if (!collectivityRepository.existsById(collectivityId)) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Collectivity not found: " + collectivityId);
            }
            return activityRepository.findByCollectivityId(collectivityId);
        } catch (SQLException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }
}