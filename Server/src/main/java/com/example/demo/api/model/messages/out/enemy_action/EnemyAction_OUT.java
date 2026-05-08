package com.example.demo.api.model.messages.out.enemy_action;

import com.example.demo.api.model.messages.MessageBody;
import java.util.List;

public class EnemyAction_OUT extends MessageBody {

    public static final String TYPE = "ENEMY_ACTION";

    public List<EnemyActionResult> actions;

    @Override
    public String getMessageType() { return TYPE; }

    public EnemyAction_OUT(List<EnemyActionResult> actions) {
        this.actions = actions;
    }
}