package com.example.demo.api.model.states;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import com.example.demo.api.model.Player;
import com.example.demo.api.model.bd.BdPlayer;
import com.example.demo.api.model.messages.JSONMessage;
import com.example.demo.api.model.messages.in.pick_characters.PickCharacterMessage_IN;
import com.example.demo.api.model.messages.out.characters_to_pick.CharacterInfo;
import com.example.demo.api.model.messages.out.characters_to_pick.PlayerInfo;
import com.example.demo.api.model.messages.out.characters_to_pick.Players2SelectMessage_OUT;
import com.example.demo.api.model.messages.out.generic.ActionResult_OUT;
import com.example.demo.api.model.messages.out.identify_player.PlayerIdentifier_OUT;
import com.example.demo.components.GameInstance;
import com.example.demo.components.GameMessage;

import tools.jackson.databind.ObjectMapper;

public class StatePickCharacter extends State
{
    private ObjectMapper mapper;

    Players2SelectMessage_OUT m;

    public StatePickCharacter(GameInstance game) {
        super(game);
        mapper = new ObjectMapper();
        
        List<PlayerInfo> players = new ArrayList<>();
        for (Player p:game.getPlayers()) {
            PlayerInfo p1 = new PlayerInfo(p.getId(),"Player "+p.getId());
            players.add(p1);

            //Fer el missatge per a que cada jugador sapigui qui es
            PlayerIdentifier_OUT playerinfo = new PlayerIdentifier_OUT();
            playerinfo.playerId = p.getId();
            playerinfo.playerNumber = p.getId()+1;
            game.send(p.getSession(), new JSONMessage(game.getId(), playerinfo));
        }

        List<CharacterInfo> characterInfos = new ArrayList<>();
        for (BdPlayer bdPlayer : game.getBdPlayers()) {
            characterInfos.add(new CharacterInfo(bdPlayer, -1, false));
        }

        m = new Players2SelectMessage_OUT();
        m.characters = characterInfos;
        m.players = players;

        JSONMessage gm = new JSONMessage(game.getId(), m);
        game.broadcast(gm);
    }

    @Override
    public void tick() {
        
        GameMessage message = game.pollMessage(5, TimeUnit.SECONDS);
        if(message!=null){
            JSONMessage gm = mapper.readValue(message.payload(), JSONMessage.class);
            System.out.println("TICK:"+gm.messageType);

            switch (gm.messageType) {
                case PickCharacterMessage_IN.TYPE:
                    pickCharacter(message.player(), gm);
                    break;
            
                default:
                    break;
            }
        }
    }

    private void pickCharacter(Player p, JSONMessage jsonMsg) {

        PickCharacterMessage_IN message = mapper.treeToValue(jsonMsg.data, PickCharacterMessage_IN.class);
        System.out.println("MESSAGE: Pick character:"+message.characterId);
        
        // Verifiquem l'id del jugador
        boolean idPlayerValid = p.getId() == message.playerId;

        if(!idPlayerValid){ 
            System.out.println("Missatge erroni, el jugador amb id :"+message.playerId+" no existeix.");
            // Missatge individual
            ActionResult_OUT result = new ActionResult_OUT(false, 1);
            game.send(p.getSession(), new JSONMessage(game.getId(), result) );
            return;
        }else {
            // Busquem personatge amb l'ID que volem ocupar 
            Optional<CharacterInfo> oc = m.characters.stream().filter(x -> x.getCharacter().getId() == message.characterId).findFirst();
            if(!oc.isPresent())
            {
                System.out.println("Missatge erroni, el personatge amb id :"+message.characterId+" no existeix.");
                // Missatge individual
                ActionResult_OUT result = new ActionResult_OUT(false, 1);
                game.send(p.getSession(), new JSONMessage(game.getId(), result) );
                return;

            } else {

                // Si el trobem, mirem si ja està seleccionat.
                CharacterInfo ci = oc.get();

                if(!ci.isSelected){

                    // si no està seleccionat, assignem l'id del jugador al personatge 
                    ci.selectedPlayerId = message.playerId;
                    ci.isSelected = true;
                    game.getPlayers().get(message.playerId).setBdPlayer(ci.character, 0, 0);

                    // Missatge individual
                    ActionResult_OUT result = new ActionResult_OUT(true, 0);
                    game.send(p.getSession(), new JSONMessage(game.getId(), result) );

                    // Missatge a tothom amb l'actualització de les assignacions.
                    JSONMessage ogm = new JSONMessage(game.getId(),m);
                    game.broadcast(ogm);

                    // Si tots els jugadors estan assignats, passem a l'estat següent !
                    if(m.characters.stream().filter(x -> x.isSelected).count()==game.getPlayers().size()){                    
                        game.setState(new StateMap(game));
                    }
                } else {
                    System.out.println("Character ja seleccionat");
                    ActionResult_OUT result = new ActionResult_OUT(false, 1);
                    game.send(p.getSession(), new JSONMessage(game.getId(), result) );
                }
            }     
        }
    }
}