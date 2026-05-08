package com.example.demo.api.model.bd;

import jakarta.persistence.*;

@Entity
@Table(name = "Statistic")
public class Statistic {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 50)
    private String name;

    public Statistic() {}

    public Integer getId() { return id; }
    public String getName() { return name; }
}