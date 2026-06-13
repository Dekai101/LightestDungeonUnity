package com.example.demo.api.model.messages.in.items_picked;

import java.util.List;

import com.example.demo.api.model.bd.Item;
import com.example.demo.api.model.messages.MessageBody;

public class ItemsPicked_IN extends MessageBody{

    public final static String TYPE = "ITEMS_PICKED";

    @Override
    public String getMessageType(){
        return TYPE;
    }

    public List<Item> items;


}