package com.hei.TDOASFinal.model;

public class Attendance {
    private String id;
    private String activityId;
    private Member member;
    private AttendanceStatus status;
    private Boolean isFromAnotherCollectivity;

    public String getId() { return id; }
    public void setId(String v) { this.id = v; }
    public String getActivityId() { return activityId; }
    public void setActivityId(String v) { this.activityId = v; }
    public Member getMember() { return member; }
    public void setMember(Member v) { this.member = v; }
    public AttendanceStatus getStatus() { return status; }
    public void setStatus(AttendanceStatus v) { this.status = v; }
    public Boolean getIsFromAnotherCollectivity() { return isFromAnotherCollectivity; }
    public void setIsFromAnotherCollectivity(Boolean v) { this.isFromAnotherCollectivity = v; }
}