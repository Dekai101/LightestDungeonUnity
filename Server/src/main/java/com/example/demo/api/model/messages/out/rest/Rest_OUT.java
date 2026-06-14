package com.example.demo.api.model.messages.out.rest;

import com.example.demo.api.model.messages.MessageBody;

public class Rest_OUT extends MessageBody {

    public final static String TYPE = "REST";
    
    @Override
    public String getMessageType(){
        return TYPE;
    }

    public int playerId;
    public int hpHealed;
    public int currentHp;
}
