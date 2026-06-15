package com.example.demo.api.model.states;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import com.example.demo.api.model.Player;
import com.example.demo.api.model.messages.JSONMessage;
import com.example.demo.api.model.messages.in.room_cleared.RoomCleared_IN;
import com.example.demo.api.model.messages.out.generic.ActionResult_OUT;
import com.example.demo.api.model.messages.out.loot.ShowChestLoot_OUT;
import com.example.demo.api.model.messages.out.loot.ShowInventory_OUT;
import com.example.demo.components.GameInstance;
import com.example.demo.components.GameMessage;

import tools.jackson.databind.ObjectMapper;

public class StateChestRoom extends State
{
    private ObjectMapper mapper;

    private ShowChestLoot_OUT msg;

    private HashMap<Player, Boolean> playersCleared;

    public StateChestRoom(GameInstance game) {
        super(game);
        mapper = new ObjectMapper();

        //Ficar el valor dels jugadors en false per saber que no han acabat la room
        playersCleared = new HashMap<>();
        for (int i = 0; i < game.getPlayers().size(); i++) {
            playersCleared.put(game.getPlayers().get(i), false);
        }

        msg = new ShowChestLoot_OUT();
        msg.loot = game.getChestLoot();

        JSONMessage gm = new JSONMessage(game.getId(), msg);
        game.broadcast(gm);
                        
        if(msg.loot != null){
            game.addItemsToInventory(msg.loot);
        } else {
            System.out.println("No items picked");
        }
        game.broadcast(new JSONMessage(game.getId(), new ShowInventory_OUT(game.getInventory().values())));
    }


    @Override
    public void tick() {
        GameMessage message = game.pollMessage(1, TimeUnit.MILLISECONDS);

        if(message!=null){
            JSONMessage gm = mapper.readValue(message.payload(), JSONMessage.class);
            System.out.println("TICK: "+gm.messageType);

            switch (gm.messageType) {
                case RoomCleared_IN.TYPE:
                    waitAllPlayers(message.player(), gm);
                    break;
                default:
                    break;
            }
        }
    }

    private void waitAllPlayers(Player p, JSONMessage jsonMsg) {

        RoomCleared_IN message = mapper.treeToValue(jsonMsg.data, RoomCleared_IN.class);
        System.out.println("MESSAGE: Chest Room Cleared");
        
        // Verifiquem l'id del jugador
        boolean idPlayerValid = p.getId() == message.playerId;

        if(!idPlayerValid){ 
            System.out.println("Missatge erroni, el jugador amb id :"+message.playerId+" no ets tu o no existeix.");
            // Missatge individual
            ActionResult_OUT result = new ActionResult_OUT(false, 1);
            game.send(p.getSession(), new JSONMessage(game.getId(), result) );
            return;
        }

        // Missatge individual
        ActionResult_OUT result = new ActionResult_OUT(true, 0);
        game.send(p.getSession(), new JSONMessage(game.getId(), result) );

        if(playersCleared.containsKey(p)){
            playersCleared.put(p, true);
        } else {
            System.out.println("Error: aquest player no ha d'estar al joc");
        }

        // Si tots els jugadors estan assignats, passem a l'estat següent
        boolean allCleared = true;

        for (Player player : playersCleared.keySet()) {
            if(!playersCleared.get(player).booleanValue()){
                allCleared = false;
            }
        }

        if(allCleared){
            game.setState(new StateMap(game));
        }
    }
}
