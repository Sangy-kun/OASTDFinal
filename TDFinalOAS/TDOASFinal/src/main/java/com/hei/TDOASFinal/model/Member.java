package com.hei.TDOASFinal.model;

import java.util.List;

public class Member {

    private String id;
    private String firstName;
    private String lastName;
    private String birthDate;
    private String gender;
    private String address;
    private String profession;
    private Long phoneNumber;
    private String email;
    private String occupation;
    private String collectivityId;
    private List<Member> referees;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getFirstName() { return firstName; }
    public void setFirstName(String v) { this.firstName = v; }
    public String getLastName() { return lastName; }
    public void setLastName(String v) { this.lastName = v; }
    public String getBirthDate() { return birthDate; }
    public void setBirthDate(String v) { this.birthDate = v; }
    public String getGender() { return gender; }
    public void setGender(String v) { this.gender = v; }
    public String getAddress() { return address; }
    public void setAddress(String v) { this.address = v; }
    public String getProfession() { return profession; }
    public void setProfession(String v) { this.profession = v; }
    public Long getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(Long v) { this.phoneNumber = v; }
    public String getEmail() { return email; }
    public void setEmail(String v) { this.email = v; }
    public String getOccupation() { return occupation; }
    public void setOccupation(String v) { this.occupation = v; }
    public String getCollectivityId() { return collectivityId; }
    public void setCollectivityId(String v) { this.collectivityId = v; }
    public List<Member> getReferees() { return referees; }
    public void setReferees(List<Member> v) { this.referees = v; }
}