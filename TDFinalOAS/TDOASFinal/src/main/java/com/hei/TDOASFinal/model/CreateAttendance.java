package com.hei.TDOASFinal.model;

public class CreateAttendance {
    private String memberId;
    private AttendanceStatus status;
    private Boolean isFromAnotherCollectivity;

    public String getMemberId() { return memberId; }
    public void setMemberId(String v) { this.memberId = v; }
    public AttendanceStatus getStatus() { return status; }
    public void setStatus(AttendanceStatus v) { this.status = v; }
    public Boolean getIsFromAnotherCollectivity() { return isFromAnotherCollectivity; }
    public void setIsFromAnotherCollectivity(Boolean v) { this.isFromAnotherCollectivity = v; }
}