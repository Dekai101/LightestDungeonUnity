package com.example.demo.api.model.messages.out.status_applied;

public class StatusUpdate {
    public int targetId;
    public String statusName;
    public float value;
    public String statisticName;
    public int pendingTurns;
    public int level;

    public StatusUpdate() {}

    public StatusUpdate(int targetId, String statusName, float value, String statisticName, int pendingTurns, int level) {
        this.targetId = targetId;
        this.statusName = statusName;
        this.value = value;
        this.statisticName = statisticName;
        this.pendingTurns = pendingTurns;
        this.level = level;
    }
}
