package com.example.demo.api.model.messages.out.battle_state;

import java.util.List;

import com.example.demo.api.model.Player;
import com.example.demo.api.model.bd.Enemy;
import com.example.demo.api.model.messages.MessageBody;

public class BattleStateUpdate_OUT extends MessageBody{

    public final static String TYPE = "BATTLE_STATE_UPDATE";
    
    @Override
    public String getMessageType(){
        return TYPE;
    }

    public List<Player> players;
    public List<Enemy> enemies;
}