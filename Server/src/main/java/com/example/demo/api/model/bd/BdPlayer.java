package com.example.demo.api.model.bd;

import jakarta.persistence.*;

@Entity
@Table(name = "Player")
@PrimaryKeyJoinColumn(name = "entity_id")
public class BdPlayer extends Character {

    @Column(name = "xp_points", nullable = false)
    private Integer xpPoints;

    @Column(name = "skill_points", nullable = false)
    private Integer skillPoints;

    public BdPlayer() {}

    public void setXpPoints(int xp_points){
        this.xpPoints = xp_points;
    }

    public void setSkillPoints(int skill_points){
        this.skillPoints = skill_points;
    }

    public Integer getXpPoints() { return xpPoints; }
    public Integer getSkillPoints() { return skillPoints; }
}