package com.hei.TDOASFinal.model;

import java.util.List;

public class Collectivity {

    private String id;
    private Integer number;
    private String name;
    private String location;
    private String specialty;
    private CollectivityStructure structure;
    private List<Member> members;

    public String getId() { return id; }
    public void setId(String v) { this.id = v; }
    public Integer getNumber() { return number; }
    public void setNumber(Integer v) { this.number = v; }
    public String getName() { return name; }
    public void setName(String v) { this.name = v; }
    public String getLocation() { return location; }
    public void setLocation(String v) { this.location = v; }
    public String getSpecialty() { return specialty; }
    public void setSpecialty(String v) { this.specialty = v; }
    public CollectivityStructure getStructure() { return structure; }
    public void setStructure(CollectivityStructure v) { this.structure = v; }
    public List<Member> getMembers() { return members; }
    public void setMembers(List<Member> v) { this.members = v; }
}