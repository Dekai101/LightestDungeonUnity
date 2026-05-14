package com.example.demo.components;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.springframework.web.socket.WebSocketSession;

import com.example.demo.api.model.Player;
import com.example.demo.api.model.Room;
import com.example.demo.api.model.messages.JSONMessage;
import com.example.demo.api.model.states.State;
import com.example.demo.api.model.states.StatePickCharacter;
import com.example.demo.api.model.bd.BdPlayer;
import com.example.demo.api.model.bd.Character;
import com.example.demo.api.model.bd.CharacterService;
import com.example.demo.api.model.bd.Enemy;
import com.example.demo.api.model.bd.Item;
import com.example.demo.api.model.bd.LootService;
import com.example.demo.api.model.bd.Skill;
import com.example.demo.api.model.bd.SkillService;

import tools.jackson.databind.ObjectMapper;

import org.springframework.web.socket.TextMessage;


import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.UUID;

/**
 * Classe que representa una partida i tot el seu estat:
 *  - jugadors connectats
 *  - cua de missatges
 *  - estat intern del joc (@TODO)
 */
public class GameInstance {

    /** Les connexions dels jugadors de la partida  */
    private final List<Player> players;

    /** Les habitacions que hi ha disponibles en el mapa creat */
    private final List<Room> nextRooms;

    private Room actualRoom;

    //Services
    private final LootService lootService;
    private final CharacterService characterService;
    private final SkillService skillService;

    /** Llista de missatges per gestionaro (INBOX) */
    private final BlockingQueue<GameMessage> queue = new LinkedBlockingQueue<>();

    /** Executor de fils */
    private final ExecutorService executor;

    /** Mutex per controlar que el joc només tingui 1 fil en execució */
    private final AtomicBoolean threadIsRunning = new AtomicBoolean(false);

    /** Indica si la partida està en curs (true) o ha acabat (false) */
    private volatile boolean gameActive = true;
    
    /** Id de la partida (UUID) */
    private final String id;

    /**
     * Estat del joc
     */
    private State currentState;

    public List<Player> getPlayers(){
        return this.players;
    }

    public List<Room> getNextRooms(){
        return this.nextRooms;
    }

    public Room getActualRoom(){
        return this.actualRoom;
    }

    public void setActualRoom(Room room){
        actualRoom = room;
    }

    public List<Item> getChestLoot(){
        if(actualRoom.getType().equals("CHEST_ROOM")) {
            return lootService.generateChestLoot(actualRoom.getLevel());
        }

        return new ArrayList<>();
    }

    public List<com.example.demo.api.model.bd.Character> getCharacters(){
        return characterService.getAllCharacters();
    }

    public List<com.example.demo.api.model.bd.Character> getCharactersWithSkills(){
        return characterService.getAllCharactersWithSkills();
    }

    public Character getCharacterById(int id){
        return characterService.getCharacterById(id).get();
    }

    public List<BdPlayer> getBdPlayers(){
        return characterService.getAllPlayers();
    }

    public BdPlayer getBdPlayerById(int id){
        return characterService.getPlayerById(id).get();
    }

    public List<Enemy> getEnemies(){
        return characterService.getAllEnemies();
    }

    public List<Enemy> getEnemiesByLevel() {
        Random rand = new Random();

        List<Enemy> allEnemies = characterService.getEnemiesByLevel(actualRoom.getLevel() + 1);
        List<Enemy> enemiesChoosed = new ArrayList<>();

        int combatIdStart = (int)players.get(players.size() - 1).getId() + 1;

        for (int i = 0; i < 3; i++) {
            Enemy enemy = new Enemy(allEnemies.get(rand.nextInt(allEnemies.size())));
            enemy.setCombatId(combatIdStart + i);
            enemiesChoosed.add(enemy);
        }

        return enemiesChoosed;
    }

    private Map<Player, BdPlayer> playerCharacters = new HashMap<>();

    public Map<Player, BdPlayer> getPlayerCharacters() {
        return playerCharacters;
    }

    public List<Skill> getAllSkills(){
        return skillService.getAllSkills();
    }

    public List<Skill> getPassiveSkills(){
        return skillService.getPassiveSkills();
    }

    public List<Skill> getActiveSkills(){
        return skillService.getActiveSkills();
    }

    public Skill getSkillById(int id){
        return skillService.getSkillById(id).get();
    }

    public Skill getSkillByIdWithEffects(int id){
        return skillService.getSkillByIdWithEffects(id).get();
    }

    public void setPlayerCharacter(Player player, BdPlayer character) {
        playerCharacters.put(player, character);
    }

    public Item getItemById(int id){
        return lootService.getItemById(id).get();
    }
    
    /**
     * Permet canviar l'estat actual de la partida
     * @param newState el nou estat
     */
    public void setState(State newState){
        if(newState==null) throw new RuntimeException("No podem posar un estat null");
        currentState = newState;
    }

    /**
     * Constructor de la instància de joc
     * @param players
     * @param executor
     */
    public GameInstance(List<Player> players, ExecutorService executor, LootService lootService, 
        CharacterService characterService, SkillService skillService) {
        this.id = UUID.randomUUID().toString();
        this.actualRoom = new Room(0, "START_ROOM", 0);
        this.nextRooms = new ArrayList<>();
        this.players = players;
        this.executor = executor;
        this.lootService = lootService;
        this.characterService = characterService;
        this.skillService = skillService;

        System.out.println("Starting the game instance.");
        currentState = new StatePickCharacter(this);
    }

    /** Iniciar fil de la partida */
    public void start(){
        if(threadIsRunning.compareAndSet(false, true)){
            System.out.println("The thread has been started.");
            executor.submit(this::processLoop);
        }
    }

    /** Encuar missatge d'un player */
    public void enqueue(GameMessage message) {
        queue.offer(message); 
    }
    /** retorna l'ID de la partida */
    public String getId() {
        return id;
    }

    /**
     * mètode per llegir missatge de la cua de missatges entrants de joc
     * @param time de timeout
     * @param tu
     * @return el missatge, o null si no hi ha pendents durant tot el temps estipulat.
     */
    public GameMessage pollMessage(int time, TimeUnit tu){
        try {

            return queue.poll(time, tu);

        } catch (InterruptedException e) {
            e.printStackTrace();
            return null;
        }
    }

     /**
     * mètode per llegir missatge de la cua de missatges entrants de joc (sense bloqueig)
     * @return el missatge, o null si no hi ha pendents.
     */
    public GameMessage pollMessage(){
        return queue.poll();
    }



    /** 
     * THE GAME LOOP () () () () () () () () () () () ()
     */
    private void processLoop() {
   
        while (gameActive) {
            try {
                currentState.tick();
            } catch (Exception e) {
                System.out.println("Error en el game loop:");
                e.printStackTrace();
            }
        }
    }

    /** 
     * Funció d'utilitat per enviar un missatge  genèric (que es serialitza
     *  a JSON) a una connexió concreta */
    public void send(WebSocketSession session, JSONMessage jsonMsg) {
        ObjectMapper mapper = new ObjectMapper();
        send( session, mapper.writeValueAsString(jsonMsg));
    }

    /** Funció d'utilitat per enviar un missatge a una connexió concreta */
    private void send(WebSocketSession session, String text) {
        try {
            synchronized (session) {
                session.sendMessage(new TextMessage(text));
            }
        } catch (IOException e) {
            // handle disconnect
        }
    }

    /** Permet enviar a tots els players un missatge genèric (que es serialitza a JSON) */
    public void broadcast(JSONMessage jsonMsg){
        ObjectMapper mapper = new ObjectMapper();
        broadcast(mapper.writeValueAsString(jsonMsg));
    }

    /** Funció d'utilitat per enviar un missatge de text a tots els ususaris de la partida */
    private void broadcast(String text) {
        for (Player player : players) {
            send(player.getSession(), text);
        }
    }

    public void stop() {
        gameActive = false;
    }

    //Crear següents rooms disponibles
    public void createNextRooms() {
        nextRooms.clear();

        // Si estem al nivell 4, la següent i única room és el BOSS
        if (actualRoom.getLevel() == 4) {
            nextRooms.add(new Room(1, "BOSS_ROOM", actualRoom.getLevel()+1));
            return;
        }
        
        Random random = new Random();
        //Entre 2 i 3 rooms
        int count = 2 + random.nextInt(2);

        String currentType = actualRoom.getType();

        //Si la room actual NO es ENEMY_ROOM, no pot apareixer
        List<String> availableTypes = new ArrayList<>(Arrays.asList("ENEMY_ROOM", "CHEST_ROOM", "REST_ROOM"));
        if (!currentType.equals("ENEMY_ROOM")) {
            availableTypes.remove(currentType);
        }

        //Es van afegint rooms i si es una que no es enemic ja no pot apareixer com a un altre opcio
        Set<String> usedNonEnemyTypes = new HashSet<>();

        int attempts = 0;
        while (nextRooms.size() < count && attempts < 50) {
            attempts++;

            List<String> candidates = availableTypes.stream()
                .filter(type -> type.equals("ENEMY_ROOM") || !usedNonEnemyTypes.contains(type))
                .collect(Collectors.toList());

            if (candidates.isEmpty()) break;

            String chosenType = candidates.get(random.nextInt(candidates.size()));

            if (!chosenType.equals("ENEMY_ROOM")) {
                usedNonEnemyTypes.add(chosenType);
            }

            int roomId = nextRooms.size() + 1;
            nextRooms.add(new Room(roomId, chosenType, actualRoom.getLevel()+1));
        }
    }
}