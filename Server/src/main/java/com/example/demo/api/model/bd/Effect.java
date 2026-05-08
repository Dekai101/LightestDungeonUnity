package com.example.demo.api.model.bd;

import jakarta.persistence.*;

@Entity
@Table(name = "Effect")
public class Effect {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "stat_id")
    private Statistic statistic;

    @Column(name = "min_flat_power")
    private Integer minFlatPower;

    @Column(name = "max_flat_power")
    private Integer maxFlatPower;

    @Column(name = "stat_multiplier")
    private Float statMultiplier;

    @ManyToOne
    @JoinColumn(name = "status_id")
    private Status status;

    @Column(name = "effect_level")
    private Integer effectLevel;

    @Column(nullable = false)
    private Float probability;

    @Column(name = "duration_turns", nullable = false)
    private Integer durationTurns;

    public Effect() {}

    public Integer getId() { return id; }
    public Statistic getStatistic() { return statistic; }
    public Integer getMinFlatPower() { return minFlatPower; }
    public Integer getMaxFlatPower() { return maxFlatPower; }
    public Float getStatMultiplier() { return statMultiplier; }
    public Status getStatus() { return status; }
    public Integer getEffectLevel() { return effectLevel; }
    public Float getProbability() { return probability; }
    public Integer getDurationTurns() { return durationTurns; }
}