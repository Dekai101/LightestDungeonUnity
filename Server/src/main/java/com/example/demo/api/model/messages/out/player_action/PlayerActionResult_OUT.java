package com.example.demo.api.model.messages.out.player_action;

import com.example.demo.api.model.messages.MessageBody;
import java.util.List;

public class PlayerActionResult_OUT extends MessageBody {

    public static final String TYPE = "PLAYER_ACTION_RESULT";

    public List<PlayerActionResult> results;

    @Override
    public String getMessageType() { return TYPE; }

    public PlayerActionResult_OUT(List<PlayerActionResult> results) {
        this.results = results;
    }
}