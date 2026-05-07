package com.hei.TDOASFinal.model;

public class CollectivityLocalStatistics {
    private MemberDescription memberDescription;
    private Double earnedAmount;
    private Double unpaidAmount;
    private Double attendanceRate;

    public MemberDescription getMemberDescription() { return memberDescription; }
    public void setMemberDescription(MemberDescription v) { this.memberDescription = v; }
    public Double getEarnedAmount() { return earnedAmount; }
    public void setEarnedAmount(Double v) { this.earnedAmount = v; }
    public Double getUnpaidAmount() { return unpaidAmount; }
    public void setUnpaidAmount(Double v) { this.unpaidAmount = v; }
    public Double getAttendanceRate() { return attendanceRate; }
    public void setAttendanceRate(Double v) { this.attendanceRate = v; }
}