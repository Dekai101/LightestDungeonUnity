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

    public BdPlayer(BdPlayer other) {

        this.setId(other.getId());
        this.setName(other.getName());
        this.setLevel(other.getLevel());

        this.setHp(other.getHp());
        this.setHpMax(other.getHpMax());

        this.setEnergy(other.getEnergy());
        this.setEnergyMax(other.getEnergyMax());

        this.setAttack(other.getAttack());
        this.setDefense(other.getDefense());
        this.setSpeed(other.getSpeed());

        this.setCritChance(other.getCritChance());
        this.setCritDamage(other.getCritDamage());
        this.setAccuracyMultiplier(other.getAccuracyMultiplier());

        this.setImageThumb(other.getImageThumb());
        this.setImageFull(other.getImageFull());
        this.setDescription(other.getDescription());

        this.setSkills(other.getSkills());

        this.xpPoints = other.xpPoints;
        this.skillPoints = other.skillPoints;
    }

    public void setXpPoints(int xp_points){
        this.xpPoints = xp_points;
    }

    public void setSkillPoints(int skill_points){
        this.skillPoints = skill_points;
    }

    public Integer getXpPoints() { return xpPoints; }
    public Integer getSkillPoints() { return skillPoints; }
}