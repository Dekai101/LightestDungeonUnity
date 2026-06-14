package com.example.demo.api.model.messages.out.loot;

import java.util.Collection;
import java.util.List;

import com.example.demo.api.model.bd.Item;
import com.example.demo.api.model.messages.MessageBody;

public class ShowInventory_OUT extends MessageBody{
    public final static String TYPE = "INVENTORY_LOOT";
    
    @Override
    public String getMessageType(){
        return TYPE;
    }

    public ShowInventory_OUT(Collection<Item> loot) {
        this.loot = loot;
    }

    public ShowInventory_OUT(Item[] loot) {
        this.loot = List.of(loot);
    }

    public ShowInventory_OUT() {
    }

    public Collection<Item> loot;
}
