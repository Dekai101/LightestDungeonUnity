package com.example.demo.api.model.messages.out.status_applied;

import java.util.List;

import com.example.demo.api.model.messages.MessageBody;

public class Status_Applied_OUT extends MessageBody{

    public final static String TYPE = "STATUS_APPLIED";
    
    @Override
    public String getMessageType(){
        return TYPE;
    }

    public List<StatusUpdate> updates;
}