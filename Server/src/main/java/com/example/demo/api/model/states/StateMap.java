package com.example.demo.api.model.states;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.parsing.NullSourceExtractor;

import com.example.demo.api.model.Player;
import com.example.demo.api.model.Room;
import com.example.demo.api.model.messages.JSONMessage;
import com.example.demo.api.model.messages.in.pick_characters.PickCharacterMessage_IN;
import com.example.demo.api.model.messages.in.select_room.SelectRoomMessage_IN;
import com.example.demo.api.model.messages.out.generic.ActionResult_OUT;
import com.example.demo.api.model.messages.out.show_map.ShowMapMessage_OUT;
import com.example.demo.components.GameInstance;
import com.example.demo.components.GameMessage;
import com.example.demo.api.model.bd.Item;

import tools.jackson.databind.ObjectMapper;

public class StateMap extends State
{
    private ObjectMapper mapper;

    private ShowMapMessage_OUT msg;

    public StateMap(GameInstance game) {
        super(game);
        mapper = new ObjectMapper();

        game.createNextRooms();

        msg = new ShowMapMessage_OUT();
        msg.rooms = game.getNextRooms();

        JSONMessage gm = new JSONMessage(game.getId(), msg);
        game.broadcast(gm);
    }


    @Override
    public void tick() {
        GameMessage message = game.pollMessage(1, TimeUnit.MILLISECONDS);

        if(message!=null){
            JSONMessage gm = mapper.readValue(message.payload(), JSONMessage.class);
            System.out.println("TICK: "+gm.messageType);

            switch (gm.messageType) {
                case SelectRoomMessage_IN.TYPE:
                    select_room(message.player(), gm);
                    break;
            
                default:
                    break;
            }
        }
    }


    private void select_room(Player p, JSONMessage jsonmsg){
        SelectRoomMessage_IN message = mapper.treeToValue(jsonmsg.data, SelectRoomMessage_IN.class);

        System.out.println("MESSAGE: Selected Room: "+message.room.getType());

        if(message.room.getId() < 0){
            System.out.println("Missatge erroni: L'id del room no pot ser menor a 0");

            ActionResult_OUT result = new ActionResult_OUT(false, 1);
            game.send(p.getSession(), new JSONMessage(game.getId(), result) );
            return;
        }

        boolean isNextRoom = false;

        for (Room nextRoom : game.getNextRooms()) {
            if(message.room.getId() == nextRoom.getId() &&
                message.room.getType().equals(nextRoom.getType()) && message.room.getLevel() == nextRoom.getLevel()){
                    isNextRoom = true;
                    break;
            }
        }

        if(!isNextRoom){
            System.out.println("Missatge erroni: El room no coincideix amb els rooms disponibles");
            System.out.println("Mira si l'id, el tipus i/o el nivell del room es correcte");

            ActionResult_OUT result = new ActionResult_OUT(false, 1);
            game.send(p.getSession(), new JSONMessage(game.getId(), result) );
            return;
        } else {
            ActionResult_OUT result = new ActionResult_OUT(true, 0);
            game.send(p.getSession(), new JSONMessage(game.getId(), result) );
            
            game.setActualRoom(message.room);

            switch (message.room.getType()) {
                case "CHEST_ROOM":
                    game.setState(new StateChestRoom(game));
                    break;

                case "ENEMY_ROOM":
                    game.setState(new StateMap(game));
                    System.out.println("Enemy room selected: TO DO");
                    break;

                case "REST_ROOM":
                    game.setState(new StateRestRoom(game));
                    break;

                case "BOSS_ROOM":
                    game.setState(new StateMap(game));
                    System.out.println("Boss room selected: TO DO");
                    break;
            }
        }
    }
}