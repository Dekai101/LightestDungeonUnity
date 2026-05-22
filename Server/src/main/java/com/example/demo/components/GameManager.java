package com.example.demo.components;

import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import com.example.demo.api.model.Player;
import com.example.demo.api.model.bd.CharacterService;
import com.example.demo.api.model.bd.LootService;
import com.example.demo.api.model.bd.SkillService;

/**
 * Gestor principal de partides.
 * 
 * - Guarda jugadors en espera
 * - Crea noves partides quan hi ha 3 jugadors
 * - Gestiona múltiples GameInstance simultànies
 * - Redirigeix missatges WS a la seva partida
 */
@Component
public class GameManager {

    /** Id incremental de jugadors */
    private long lastPlayerId = 0;

    /** Pool de fils */
    private final ExecutorService executor;

    /** Jugadors esperant partida */
    private final Queue<WebSocketSession> waitingPlayers = new ConcurrentLinkedQueue<>();

    /** Sessió -> partida */
    private final Map<WebSocketSession, GameInstance> sessionToGame = new ConcurrentHashMap<>();

    /** Sessió -> player */
    private final Map<WebSocketSession, Player> sessionToPlayer = new ConcurrentHashMap<>();

    /** gameId -> GameInstance */
    private final Map<String, GameInstance> games = new ConcurrentHashMap<>();

    // Serveis
    private final LootService lootService;
    private final CharacterService characterService;
    private final SkillService skillService;

    public GameManager(LootService lootService, CharacterService characterService, SkillService skillService) {
        // Pots canviar-ho si vols limitar fils
        this.executor = Executors.newCachedThreadPool();

        this.lootService = lootService;
        this.characterService = characterService;
        this.skillService = skillService;
    }

    /**
     * Nova connexió websocket
     */
    public void onConnect(WebSocketSession session) {

        System.out.println("Jugador connectat: " + session.getId());

        waitingPlayers.add(session);

        System.out.println("Jugadors esperant: " + waitingPlayers.size());

        tryStartGame();
    }

    /**
     * Missatge rebut d'un jugador
     */
    public void handleIncoming(WebSocketSession session, String message) {
        GameInstance game = sessionToGame.get(session);

        if (game != null) {

            Player player = sessionToPlayer.get(session);

            game.enqueue(new GameMessage(player, message));
        }
    }

    /**
     * Intenta crear totes les partides possibles
     */
    private synchronized void tryStartGame() {

        while (waitingPlayers.size() >= 3) {

            List<Player> players = List.of(
                    new Player(lastPlayerId++, waitingPlayers.poll()),
                    new Player(lastPlayerId++, waitingPlayers.poll()),
                    new Player(lastPlayerId++, waitingPlayers.poll())
            );

            GameInstance game = new GameInstance(players, executor, lootService, characterService, skillService);

            String gameId = game.getId();

            for (Player p : players) {
                sessionToGame.put(p.getSession(), game);
                sessionToPlayer.put(p.getSession(), p);
            }

            games.put(gameId, game);

            System.out.println("Nova partida creada: " + gameId);

            game.start();
        }
    }

    /**
     * Elimina partida acabada
     */
    public void removeGame(String gameId) {

        GameInstance game = games.remove(gameId);

        if (game != null) {

            for (Player p : game.getPlayers()) {
                sessionToGame.remove(p.getSession());
                sessionToPlayer.remove(p.getSession());
            }

            System.out.println("Partida eliminada: " + gameId);
        }
    }

    /**
     * Desconnexió d'un jugador
     */
    public void onDisconnect(WebSocketSession session) {

        waitingPlayers.remove(session);

        GameInstance game = sessionToGame.get(session);

        if (game != null) {
            game.onPlayerDisconnect(session);
        }

        sessionToGame.remove(session);
        sessionToPlayer.remove(session);

        System.out.println("Jugador desconnectat: " + session.getId());
    }
}