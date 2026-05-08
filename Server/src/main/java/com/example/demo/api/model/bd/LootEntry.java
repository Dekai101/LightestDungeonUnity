package com.example.demo.api.model.bd;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;

@Entity
@Table(name = "LootEntry")
public class LootEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer num;

    @ManyToOne
    @JoinColumn(name = "loot_table_id", nullable = false)
    @JsonIgnore
    private LootTable lootTable;

    @ManyToOne
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;

    @Column(name = "drop_chance", nullable = false)
    private Float dropChance;

    @Column(name = "min_quality", nullable = false, length = 20)
    private String minQuality;

    @Column(name = "max_quality", nullable = false, length = 20)
    private String maxQuality;

    public LootEntry() {}

    public Integer getNum() { return num; }
    public LootTable getLootTable() { return lootTable; }
    public Item getItem() { return item; }
    public Float getDropChance() { return dropChance; }
    public String getMinQuality() { return minQuality; }
    public String getMaxQuality() { return maxQuality; }
}