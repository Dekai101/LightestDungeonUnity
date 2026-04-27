package com.example.demo.api.model.bd;

import jakarta.persistence.*;

@Entity
@Table(name = "Item")
public class Item {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 500)
    private String description;

    @Column(nullable = false, length = 20)
    private String quality;

    @Column(nullable = false)
    private boolean consumable;

    @Column(name = "target_type", nullable = false, length = 20)
    private String targetType;

    @Column(name = "is_aoe", nullable = false)
    private boolean isAoe;

    @Column(name = "max_uses")
    private Integer maxUses;

    @Column(name = "image_thumb", nullable = false, length = 500)
    private String imageThumb;

    public Item() {}

    public Integer getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getQuality() { return quality; }
    public boolean isConsumable() { return consumable; }
    public String getTargetType() { return targetType; }
    public boolean isAoe() { return isAoe; }
    public Integer getMaxUses() { return maxUses; }
    public String getImageThumb() { return imageThumb; }
}