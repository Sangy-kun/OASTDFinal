package com.hei.TDOASFinal.model;

import java.time.LocalDate;

public class CreateActivity {
    private String title;
    private String type;
    private LocalDate activityDate;
    private Boolean isMandatory;

    public String getTitle() { return title; }
    public void setTitle(String v) { this.title = v; }
    public String getType() { return type; }
    public void setType(String v) { this.type = v; }
    public LocalDate getActivityDate() { return activityDate; }
    public void setActivityDate(LocalDate v) { this.activityDate = v; }
    public Boolean getIsMandatory() { return isMandatory; }
    public void setIsMandatory(Boolean v) { this.isMandatory = v; }
}