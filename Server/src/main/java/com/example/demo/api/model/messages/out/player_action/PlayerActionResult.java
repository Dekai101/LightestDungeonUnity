package com.example.demo.api.model.messages.out.player_action;

public class PlayerActionResult {

    public int playerId;
    public String choiceType;   // "SKILL", "ITEM" o "PASS"
    public int[] targets;
    public int value;           // dany fet o HP curat
    public String statistic;
    public boolean critical;
    public boolean hit;
    public String statusApplied; // null si no n'hi ha cap

    public PlayerActionResult() {}

    public PlayerActionResult(int playerId, String choiceType, int[] targets,
                               int value, String statistic, boolean critical, boolean hit, String statusApplied) {
        this.playerId      = playerId;
        this.choiceType    = choiceType;
        this.targets       = targets;
        this.value         = value;
        this.statistic     = statistic;
        this.critical      = critical;
        this.hit           = hit;
        this.statusApplied = statusApplied;
    }
}