package com.example.demo.api.model.bd;

import jakarta.persistence.*;

@Entity
@Table(name = "Status")
public class Status {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(name = "max_level", nullable = false)
    private Integer maxLevel;

    @Column(length = 500)
    private String description;

    @Column(name = "scaling_formula", length = 250)
    private String scalingFormula;

    public Status() {}

    public Integer getId() { return id; }
    public String getName() { return name; }
    public Integer getMaxLevel() { return maxLevel; }
    public String getDescription() { return description; }
    public String getScalingFormula() { return scalingFormula; }
}