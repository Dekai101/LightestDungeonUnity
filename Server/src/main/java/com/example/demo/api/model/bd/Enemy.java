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

    public Enemy() {}

    public Integer getPassiveId() { return passiveId; }
    public LootTable getLootTable() { return lootTable; }
}