package com.example.demo.api.model.messages.out.enemy_action;

public class EnemyActionResult {

    public int enemyId;
    public String skillName;
    public int targetPlayerId;
    public int damage;
    public boolean critical;
    public boolean hit;
    public String statusApplied;

    public EnemyActionResult() {}

    public EnemyActionResult(int enemyId, String skillName, int targetPlayerId,
                              int damage, boolean critical, boolean hit, String statusApplied) {
        this.enemyId        = enemyId;
        this.skillName      = skillName;
        this.targetPlayerId = targetPlayerId;
        this.damage         = damage;
        this.critical       = critical;
        this.hit            = hit;
        this.statusApplied  = statusApplied;
    }
}