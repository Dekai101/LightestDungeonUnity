package com.example.demo.api.model;

public class StatusCharacter {
    private int level;
    private int durationTurns;
    private String name;

    public StatusCharacter(String name, int level, int durationTurns) {
        this.name = name;
        this.level = level;
        this.durationTurns = durationTurns;
    }

    public int getLevel() { return level; }
    public void setLevel(int level) { this.level = level; }

    public int getDurationTurns() { return durationTurns; }
    public void setDurationTurns(int durationTurns) { this.durationTurns = durationTurns; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

}
