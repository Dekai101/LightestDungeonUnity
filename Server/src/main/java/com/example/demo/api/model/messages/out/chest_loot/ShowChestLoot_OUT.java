package com.example.demo.api.model.messages.out.chest_loot;

import java.util.List;

import com.example.demo.api.model.bd.Item;
import com.example.demo.api.model.messages.MessageBody;

public class ShowChestLoot_OUT extends MessageBody {

    public final static String TYPE = "CHEST_LOOT";
    
    @Override
    public String getMessageType(){
        return TYPE;
    }

    public List<Item> loot;
}
