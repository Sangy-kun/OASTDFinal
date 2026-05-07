package com.hei.TDOASFinal.model;

public class CollectivityOverallStatistics {
    private CollectivityInformation collectivityInformation;
    private Integer newMembersNumber;
    private Double overallMemberCurrentDuePercentage;
    private Double globalAttendanceRate;

    public CollectivityInformation getCollectivityInformation() { return collectivityInformation; }
    public void setCollectivityInformation(CollectivityInformation v) { this.collectivityInformation = v; }
    public Integer getNewMembersNumber() { return newMembersNumber; }
    public void setNewMembersNumber(Integer v) { this.newMembersNumber = v; }
    public Double getOverallMemberCurrentDuePercentage() { return overallMemberCurrentDuePercentage; }
    public void setOverallMemberCurrentDuePercentage(Double v) { this.overallMemberCurrentDuePercentage = v; }
    public Double getGlobalAttendanceRate() { return globalAttendanceRate; }
    public void setGlobalAttendanceRate(Double v) { this.globalAttendanceRate = v; }
}