package com.hei.TDOASFinal.controller;

import com.hei.TDOASFinal.model.*;
import com.hei.TDOASFinal.service.ActivityService;
import com.hei.TDOASFinal.service.AttendanceService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/collectivities")
public class ActivityController {

    private final ActivityService activityService;
    private final AttendanceService attendanceService;

    public ActivityController(ActivityService activityService,
                              AttendanceService attendanceService) {
        this.activityService = activityService;
        this.attendanceService = attendanceService;
    }

    @PostMapping("/{id}/activities")
    @ResponseStatus(HttpStatus.CREATED)
    public List<Activity> create(@PathVariable String id,
                                 @RequestBody List<CreateActivity> payload) {
        return activityService.createAll(id, payload);
    }

    @GetMapping("/{id}/activities")
    public List<Activity> getAll(@PathVariable String id) {
        return activityService.getAll(id);
    }

    @PostMapping("/{id}/activities/{activityId}/attendance")
    @ResponseStatus(HttpStatus.CREATED)
    public List<Attendance> record(@PathVariable String id,
                                   @PathVariable String activityId,
                                   @RequestBody List<CreateAttendance> payload) {
        return attendanceService.recordAll(id, activityId, payload);
    }

    @GetMapping("/{id}/activities/{activityId}/attendance")
    public List<Attendance> get(@PathVariable String id,
                                @PathVariable String activityId) {
        return attendanceService.getAll(id, activityId);
    }
}