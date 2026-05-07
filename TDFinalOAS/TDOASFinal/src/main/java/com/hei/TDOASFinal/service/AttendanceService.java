package com.hei.TDOASFinal.service;

import com.hei.TDOASFinal.model.Attendance;
import com.hei.TDOASFinal.model.CreateAttendance;
import com.hei.TDOASFinal.repository.ActivityRepository;
import com.hei.TDOASFinal.repository.AttendanceRepository;
import com.hei.TDOASFinal.repository.CollectivityRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Service
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final ActivityRepository activityRepository;
    private final CollectivityRepository collectivityRepository;

    public AttendanceService(AttendanceRepository attendanceRepository,
                             ActivityRepository activityRepository,
                             CollectivityRepository collectivityRepository) {
        this.attendanceRepository = attendanceRepository;
        this.activityRepository = activityRepository;
        this.collectivityRepository = collectivityRepository;
    }

    public List<Attendance> recordAll(String collectivityId, String activityId,
                                      List<CreateAttendance> payloads) {
        try {
            if (!collectivityRepository.existsById(collectivityId)) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Collectivity not found: " + collectivityId);
            }
            activityRepository.findById(activityId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "Activity not found: " + activityId));

            List<Attendance> result = new ArrayList<>();
            for (CreateAttendance input : payloads) {
                if (attendanceRepository.existsByActivityAndMember(activityId, input.getMemberId())) {
                    throw new ResponseStatusException(HttpStatus.CONFLICT,
                            "Attendance already recorded for member: " + input.getMemberId());
                }
                result.add(attendanceRepository.save(activityId, input));
            }
            return result;
        } catch (SQLException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    public List<Attendance> getAll(String collectivityId, String activityId) {
        try {
            if (!collectivityRepository.existsById(collectivityId)) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Collectivity not found: " + collectivityId);
            }
            activityRepository.findById(activityId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "Activity not found: " + activityId));
            return attendanceRepository.findByActivityId(activityId);
        } catch (SQLException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }
}