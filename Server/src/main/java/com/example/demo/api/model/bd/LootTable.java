package com.example.demo.api.model.bd;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "LootTable")
public class LootTable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @OneToOne
    @JoinColumn(name = "enemy_id", nullable = false)
    private Enemy enemy;

    @OneToMany(mappedBy = "lootTable", cascade = CascadeType.ALL)
    private List<LootEntry> entries = new ArrayList<>();

    public LootTable() {}

    public Integer getId() { return id; }
    public Enemy getEnemy() { return enemy; }
    public List<LootEntry> getEntries() { return entries; }
}