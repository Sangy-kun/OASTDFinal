package com.hei.TDOASFinal.model;

public class CollectivityStructure {

    private Member president;
    private Member vicePresident;
    private Member treasurer;
    private Member secretary;

    public Member getPresident() { return president; }
    public void setPresident(Member v) { this.president = v; }
    public Member getVicePresident() { return vicePresident; }
    public void setVicePresident(Member v) { this.vicePresident = v; }
    public Member getTreasurer() { return treasurer; }
    public void setTreasurer(Member v) { this.treasurer = v; }
    public Member getSecretary() { return secretary; }
    public void setSecretary(Member v) { this.secretary = v; }
}