package com.hei.TDOASFinal.model;

public class CollectivityOverallStatistics {
    private CollectivityInformation collectivityInformation;
    private Integer newMembersNumber;
    private Double overallMemberCurrentDuePercentage;

    public CollectivityInformation getCollectivityInformation() { return collectivityInformation; }
    public void setCollectivityInformation(CollectivityInformation collectivityInformation) { this.collectivityInformation = collectivityInformation; }
    public Integer getNewMembersNumber() { return newMembersNumber; }
    public void setNewMembersNumber(Integer newMembersNumber) { this.newMembersNumber = newMembersNumber; }
    public Double getOverallMemberCurrentDuePercentage() { return overallMemberCurrentDuePercentage; }
    public void setOverallMemberCurrentDuePercentage(Double overallMemberCurrentDuePercentage) { this.overallMemberCurrentDuePercentage = overallMemberCurrentDuePercentage; }
}
