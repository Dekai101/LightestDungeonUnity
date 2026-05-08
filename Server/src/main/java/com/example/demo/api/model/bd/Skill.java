package com.example.demo.api.model.bd;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "Skill")
public class Skill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(length = 500)
    private String description;

    @Column(name = "energy_cost", nullable = false)
    private Integer energyCost;

    @Column(nullable = false)
    private Float accuracy;

    @Column(nullable = false)
    private Integer hits;

    @Column(name = "target_type", nullable = false, length = 20)
    private String targetType;

    @Column(name = "is_aoe", nullable = false)
    private Boolean isAoe;

    @Column(name = "is_passive", nullable = false)
    private Boolean isPassive;

    @Column(name = "image_thumb", nullable = false, length = 500)
    private String imageThumb;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "SkillEffect",
        joinColumns = @JoinColumn(name = "skill_id"),
        inverseJoinColumns = @JoinColumn(name = "effect_id")
    )
    @JsonIgnore
    private List<Effect> effects = new ArrayList<>();

    public Skill() {}

    public Integer getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public Integer getEnergyCost() { return energyCost; }
    public Float getAccuracy() { return accuracy; }
    public Integer getHits() { return hits; }
    public String getTargetType() { return targetType; }
    public Boolean getIsAoe() { return isAoe; }
    public Boolean getIsPassive() { return isPassive; }
    public String getImageThumb() { return imageThumb; }
    public Effect getEffect() { return effects.get(0); }
}