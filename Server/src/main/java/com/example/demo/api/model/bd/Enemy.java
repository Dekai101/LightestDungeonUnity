package com.example.demo.api.model.bd;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;

@Entity
@Table(name = "Enemy")
@PrimaryKeyJoinColumn(name = "entity_id")
public class Enemy extends Character {

    @Column(name = "passive_id", nullable = false)
    private Integer passiveId;

    @OneToOne(mappedBy = "enemy", cascade = CascadeType.ALL)
    @JsonIgnore
    private LootTable lootTable;

    @Transient
    private int combatId;

    public Enemy() {}

    public Enemy(Enemy other) {

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

    this.passiveId = other.passiveId;
    this.lootTable = other.lootTable;
}

    public int getCombatId(){ return combatId; }
    public Integer getPassiveId() { return passiveId; }
    public LootTable getLootTable() { return lootTable; }

    public void setCombatId(int id){ this.combatId = id; }
}