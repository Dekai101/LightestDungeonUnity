package com.example.demo.api.model.messages.out.loot;

import java.util.List;

import com.example.demo.api.model.bd.Item;
import com.example.demo.api.model.messages.MessageBody;

public class ShowEnemyLoot_OUT extends MessageBody{
    public final static String TYPE = "ENEMY_LOOT";
    
    @Override
    public String getMessageType(){
        return TYPE;
    }

    public ShowEnemyLoot_OUT(List<Item> loot) {
        this.loot = loot;
    }

    public ShowEnemyLoot_OUT() {
    }


    public List<Item> loot;
}
