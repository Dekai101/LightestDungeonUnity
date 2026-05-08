package com.example.demo.api.model.bd;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;

@Entity
@Table(name = "Entity")
@Inheritance(strategy = InheritanceType.JOINED)
public class Character {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(nullable = false)
    private Integer level;

    @Column(nullable = false)
    private Integer hp;

    @Column(name = "hp_max", nullable = false)
    private Integer hpMax;

    @Column(nullable = false)
    private Integer energy;

    @Column(name = "energy_max", nullable = false)
    private Integer energyMax;

    @Column(nullable = false)
    private Integer attack;

    @Column(nullable = false)
    private Integer defense;

    @Column(nullable = false)
    private Integer speed;

    @Column(name = "crit_chance", nullable = false)
    private Float critChance;

    @Column(name = "crit_damage", nullable = false)
    private Float critDamage;

    @Column(name = "accuracy_multiplier", nullable = false)
    private Float accuracyMultiplier;

    @Column(name = "image_thumb", nullable = false, length = 500)
    private String imageThumb;

    @Column(name = "image_full", nullable = false, length = 500)
    private String imageFull;

    @Column(length = 500)
    private String description;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "EntitySkill",
        joinColumns = @JoinColumn(name = "entity_id"),
        inverseJoinColumns = @JoinColumn(name = "skill_id")
    )
    @JsonIgnore
    private List<Skill> skills = new ArrayList<>();

    public Character() {}

    public Integer getId() { return id; }
    public String getName() { return name; }
    public Integer getLevel() { return level; }
    public Integer getHp() { return hp; }
    public Integer getHpMax() { return hpMax; }
    public Integer getEnergy() { return energy; }
    public Integer getEnergyMax() { return energyMax; }
    public Integer getAttack() { return attack; }
    public Integer getDefense() { return defense; }
    public Integer getSpeed() { return speed; }
    public Float getCritChance() { return critChance; }
    public Float getCritDamage() { return critDamage; }
    public Float getAccuracyMultiplier() { return accuracyMultiplier; }
    public String getImageThumb() { return imageThumb; }
    public String getImageFull() { return imageFull; }
    public String getDescription() { return description; }
    public List<Skill> getSkills() { return skills; }
    public void setHp(Integer hp) { this.hp = hp; }
    public void setEnergy(Integer energy) { this.energy = energy; }
}