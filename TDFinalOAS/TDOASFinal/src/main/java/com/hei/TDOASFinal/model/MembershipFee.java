package com.hei.TDOASFinal.model;

public class MembershipFee extends CreateMembershipFee {
    private String id;
    private String collectivityId;
    private ActivityStatus status;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getCollectivityId() { return collectivityId; }
    public void setCollectivityId(String collectivityId) { this.collectivityId = collectivityId; }
    public ActivityStatus getStatus() { return status; }
    public void setStatus(ActivityStatus status) { this.status = status; }
}
