package com.example.demo.api.model.messages.out.enemies;

import java.util.List;

import com.example.demo.api.model.bd.Enemy;
import com.example.demo.api.model.messages.MessageBody;

public class ShowEnemies_OUT extends MessageBody {

    public final static String TYPE = "ENEMIES_2_FIGHT";
    
    @Override
    public String getMessageType(){
        return TYPE;
    }

    public List<Enemy> enemies;
}
