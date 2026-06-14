package com.example.demo.api.model.states;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.example.demo.api.model.Player;
import com.example.demo.api.model.StatusCharacter;
import com.example.demo.api.model.bd.BdPlayer;
import com.example.demo.api.model.bd.Character;
import com.example.demo.api.model.bd.Effect;
import com.example.demo.api.model.bd.Enemy;
import com.example.demo.api.model.bd.Item;
import com.example.demo.api.model.bd.LootEntry;
import com.example.demo.api.model.bd.Skill;
import com.example.demo.api.model.messages.JSONMessage;
import com.example.demo.api.model.messages.in.items_picked.ItemsPicked_IN;
import com.example.demo.api.model.messages.in.players_turn.PlayerTurn;
import com.example.demo.api.model.messages.in.players_turn.PlayersTurn_IN;
import com.example.demo.api.model.messages.in.room_cleared.RoomCleared_IN;
import com.example.demo.api.model.messages.out.enemy_action.EnemyAction_OUT;
import com.example.demo.api.model.messages.out.enemy_action.EnemyActionResult;
import com.example.demo.api.model.messages.out.battle_state.BattleStateUpdate_OUT;
import com.example.demo.api.model.messages.out.enemies.ShowEnemies_OUT;
import com.example.demo.api.model.messages.out.generic.ActionResult_OUT;
import com.example.demo.api.model.messages.out.loot.ShowEnemyLoot_OUT;
import com.example.demo.api.model.messages.out.loot.ShowInventory_OUT;
import com.example.demo.api.model.messages.out.player_action.PlayerActionResult;
import com.example.demo.api.model.messages.out.player_action.PlayerActionResult_OUT;
import com.example.demo.api.model.messages.out.status_applied.StatusUpdate;
import com.example.demo.api.model.messages.out.status_applied.Status_Applied_OUT;
import com.example.demo.components.GameInstance;
import com.example.demo.components.GameMessage;

import tools.jackson.databind.ObjectMapper;

public class StateEnemyRoom extends State {

    private ObjectMapper mapper;
    private Random random = new Random();

    private List<Enemy> enemies;

    private List<Item> enemyLoot;

    private boolean alreadyPicked = false;

    private List<Player> memPlayers;

    private Map<Player, PlayerTurn> pendingTurns = new HashMap<>();

    private HashMap<Player, Boolean> playersCleared = new HashMap<>();

    private List<PlayerActionResult> results = new ArrayList<>();

    public StateEnemyRoom(GameInstance game) {
        super(game);
        mapper = new ObjectMapper();

        enemyLoot = new ArrayList<>();

        alreadyPicked = false;

        enemies = game.getEnemiesByLevel();

        memPlayers = new ArrayList<>();

        for (Player p : game.getPlayers()) {
            p.getCharacter().setEnergy(p.getCharacter().getEnergyMax());
            playersCleared.put(p, false);
            memPlayers.add(new Player(p));
        }

        ShowEnemies_OUT msg = new ShowEnemies_OUT();
        msg.enemies = enemies;
        game.broadcast(new JSONMessage(game.getId(), msg));
    }

    @Override
    public void tick() {
        try {
            GameMessage message = game.pollMessage(1, TimeUnit.MILLISECONDS);
            if (message != null) {
                JSONMessage gm = mapper.readValue(message.payload(), JSONMessage.class);
                System.out.println("TICK: " + gm.messageType);

                switch (gm.messageType) {
                    case PlayersTurn_IN.TYPE:
                        if(enemies.stream().allMatch(e -> e.getHp() <= 0)){
                            break;
                        }
                        makeTurn(message.player(), gm);
                        break;
                    case RoomCleared_IN.TYPE:
                        waitAllPlayers(message.player(), gm);
                        break;
                    case ItemsPicked_IN.TYPE:
                        if(!enemies.stream().allMatch(e -> e.getHp() <= 0)){
                            game.broadcast(new JSONMessage(game.getId(), new ActionResult_OUT(false, 5)));
                            break;
                        }
                        if(alreadyPicked){
                            game.broadcast(new JSONMessage(game.getId(), new ActionResult_OUT(false, 6)));
                            break;
                        }

                        List<Item> items = new ArrayList<>();

                        for(Integer itemId : mapper.treeToValue(gm.data, ItemsPicked_IN.class).items){
                            Item item = enemyLoot.stream()
                                .filter(i -> i.getId() == itemId)
                                .findFirst()
                                .orElse(null);

                            if(item != null){
                                items.add(item);
                            }
                        }
                        if(!items.isEmpty()){
                            alreadyPicked = true;
                            game.addItemsToInventory(items);
                            game.broadcast(new JSONMessage(game.getId(), new ShowInventory_OUT(game.getInventory().values())));
                        } else {
                            System.out.println("No items picked");
                        }
                        
                        break;
                    default:
                        break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void waitAllPlayers(Player p, JSONMessage jsonMsg) {
        RoomCleared_IN message = mapper.treeToValue(jsonMsg.data, RoomCleared_IN.class);
        System.out.println("MESSAGE: Enemy Room Cleared");

        boolean idPlayerValid = p.getId() == message.playerId;

        if (!idPlayerValid) {
            System.out.println("Missatge erroni, el jugador amb id :" + message.playerId + " no ets tu.");
            game.send(p.getSession(), new JSONMessage(game.getId(), new ActionResult_OUT(false, 1)));
            return;
        }

        game.send(p.getSession(), new JSONMessage(game.getId(), new ActionResult_OUT(true, 0)));

        if (playersCleared.containsKey(p)) {
            playersCleared.put(p, true);
        } else {
            System.out.println("Error: aquest player no ha d'estar al joc");
        }

        boolean allCleared = playersCleared.values().stream().allMatch(v -> v);
        if (allCleared) {
            for (Player player : memPlayers) {
                game.getPlayers().stream()
                    .filter(pl -> pl.getId() == player.getId())
                    .findFirst()
                    .ifPresent(pl -> pl.getCharacter().setHp(player.getCharacter().getHp()));
            }
            for (Player player : game.getPlayers()){
                player.getCharacter().setEnergy(player.getCharacter().getEnergyMax());
            }
            
            game.setState(new StateMap(game));
        }
    }

    private void makeTurn(Player p, JSONMessage jsonMsg) {
        PlayersTurn_IN message = mapper.treeToValue(jsonMsg.data, PlayersTurn_IN.class);

        boolean idPlayerValid = message.players.stream().anyMatch(e -> e.player.getId() == p.getId());

        if (!idPlayerValid) {
            System.out.println("Missatge erroni, el jugador no existeix al joc.");
            game.send(p.getSession(), new JSONMessage(game.getId(), new ActionResult_OUT(false, 1)));
            return;
        }

        boolean isPlayerAlive = true;
        for(Player pl : game.getPlayers()){
            if(pl.getId() == p.getId()){
                if(pl.getCharacter().getHp() <= 0){
                    isPlayerAlive = false;
                    break;
                }
            }
        }

        if (!isPlayerAlive) {
            System.out.println("Missatge erroni, el jugador es mort.");
            game.send(p.getSession(), new JSONMessage(game.getId(), new ActionResult_OUT(false, 2)));
            return;
        }

        boolean isEnemyAlive = true;
        for(Enemy en : enemies){
            if(en.getId() == message.players.get(0).target.getId()){
                if(en.getHp() <= 0){
                    isEnemyAlive = false;
                    break;
                }
            }
        }

        if (!isEnemyAlive) {
            System.out.println("Missatge erroni, l'enemic es mort.");
            game.send(p.getSession(), new JSONMessage(game.getId(), new ActionResult_OUT(false, 3)));
            return;
        }

        game.send(p.getSession(), new JSONMessage(game.getId(), new ActionResult_OUT(true, 0)));

        if (message.players != null && !message.players.isEmpty()) {
            pendingTurns.put(p, message.players.get(0));
        }

        if (pendingTurns.size() < memPlayers.size()) {
            System.out.println("Esperant jugadors... (" + pendingTurns.size() + "/" + memPlayers.size() + ")");
            return;
        }

        System.out.println("Tots els jugadors han triat. Processant torn...");

        List<PlayerActionResult> playerResults = processPlayerTurns();
        List<EnemyActionResult> enemyResults   = processEnemyTurns();

        Status_Applied_OUT statusApplied = processStatusEffects();

        BattleStateUpdate_OUT battle_state = new BattleStateUpdate_OUT();
        battle_state.enemies = enemies;
        battle_state.players = memPlayers;

        game.broadcast(new JSONMessage(game.getId(), new PlayerActionResult_OUT(playerResults)));
        game.broadcast(new JSONMessage(game.getId(), new EnemyAction_OUT(enemyResults)));
        game.broadcast(new JSONMessage(game.getId(), battle_state));
        game.broadcast(new JSONMessage(game.getId(), statusApplied));

        pendingTurns.clear();

        boolean allEnemiesDead = enemies.stream().allMatch(e -> e.getHp() <= 0);
        boolean allPlayersDead = memPlayers.stream().allMatch(p2 -> p2.getCharacter().getHp() <= 0);
        
        if (allEnemiesDead) {
            System.out.println("Tots els enemics han mort. Sala netejada!");
            Random rand = new Random();
            List<Item> items = new ArrayList<>();

            for (Enemy en : enemies) {
                for (LootEntry loot : en.getLootTable().getEntries()) {

                    double roll = rand.nextDouble();

                    if (roll < loot.getDropChance()) {
                        System.out.println("Dropped: " + loot.getItem().getName());
                        items.add(loot.getItem());
                    }
                }
            }
            enemyLoot = items;
            game.broadcast(new JSONMessage(game.getId(), new ShowEnemyLoot_OUT(items)));
            return;
        } else if (allPlayersDead) {
            System.out.println("Tots els jugadors han mort. Game Over.");
            game.stop();
            return;
        }

        //Energy recovering system
        for (Player player : memPlayers) {
            int energyRecover = player.getCharacter().getEnergyMax() / 4;
            player.getCharacter().setEnergy(player.getCharacter().getEnergy()+energyRecover);
            if(player.getCharacter().getEnergy() > player.getCharacter().getEnergyMax()){
                player.getCharacter().setEnergy(player.getCharacter().getEnergyMax());
            }
        }
    }

    private List<PlayerActionResult> processPlayerTurns() {
        results = new ArrayList<>();

        for (Map.Entry<Player, PlayerTurn> entry : pendingTurns.entrySet()) {
            if (entry.getKey().getCharacter().hasStatus("stunned")) {

                results.add(new PlayerActionResult(
                    (int)entry.getKey().getId(),
                    "STUNNED",
                    -1,
                    0,
                    "",
                    false,
                    false,
                    null
                ));

                continue;
            }

            PlayerTurn turn = entry.getValue();

            if (!turn.choiceMade || turn.player == null) {
                int pid = turn.player != null ? turn.player.getId() : -1;
                results.add(new PlayerActionResult(pid, "PASS", -1, 0, "", false, false, null));
                continue;
            }

            if ("SKILL".equals(turn.choiceType) && turn.skillCasted != null) {
                processSkillAction(turn);
            } else if ("ITEM".equals(turn.choiceType) && turn.itemUsed != null) {
                if(game.getInventory().values().contains(turn.itemUsed)){
                    processItemAction(turn);
                } else {
                    System.out.println("Item not found in inventory");
                    results.add(new PlayerActionResult(turn.player.getId(), "PASS", -1, 0, "", false, false, null));
                }
            } else {
                results.add(new PlayerActionResult(turn.player.getId(), "PASS", -1, 0, "", false, false, null));
            }
        }

        return results;
    }

    private void processSkillAction(PlayerTurn turn) {
        System.out.println("Id del attacker: " + turn.player.getId());
        System.out.println("Id del target: "   + turn.target.getId());
        System.out.println("Id de la skill: "  + turn.skillCasted.getId());

        Player realPlayer = memPlayers.stream()
            .filter(pl -> pl.getId() == turn.player.getId()).findFirst().orElse(null);

        if (realPlayer == null) {
            System.out.println("Player not found");
            return;
        }

        BdPlayer attacker = game.getBdPlayerById(realPlayer.getCharacter().getId());
        Skill skill       = game.getSkillByIdWithEffects(turn.skillCasted.getId());

        boolean anyCrit      = false;
        boolean anyHit       = false;
        String statisticName = "";
        String statusApplied = null;

        int[] myTargetsId = new int[3];
        int[] bdTargetsId = new int[3];

        if (skill.getIsAoe() != null && skill.getIsAoe()) {
            if ("ENEMY".equals(skill.getTargetType())) {
                myTargetsId = enemies.stream()
                        .filter(en -> en.getHp() > 0)
                        .mapToInt(Enemy::getCombatId)
                        .toArray();

                bdTargetsId = enemies.stream()
                        .filter(en -> en.getHp() > 0)
                        .mapToInt(Enemy::getId)
                        .toArray();
            } else {
                myTargetsId = memPlayers.stream()
                        .filter(pl -> pl.getCharacter().getHp() > 0)
                        .mapToInt(p -> (int) p.getId())
                        .toArray();

                bdTargetsId = memPlayers.stream()
                        .filter(pl -> pl.getCharacter().getHp() > 0)
                        .mapToInt(p -> p.getCharacter().getId())
                        .toArray();
            }
        } else {
            if ("ENEMY".equals(skill.getTargetType())) {
                int i = 0;
                for (Enemy en : enemies) {
                    if (en.getHp() > 0 && en.getCombatId() == turn.target.getId()) {
                        myTargetsId[i] = en.getCombatId();
                        bdTargetsId[i] = en.getId();
                        i++;
                    }
                }
            } else {
                int i = 0;
                for (Player pl : memPlayers) {
                    if (pl.getCharacter().getHp() > 0 && pl.getId() == turn.target.getId()) {
                        myTargetsId[i] = (int) pl.getId();
                        bdTargetsId[i] = pl.getCharacter().getId();
                        i++;
                    }
                }
            }
        }

        if (bdTargetsId.length == 0) {
            results.add(new PlayerActionResult(attacker.getId(), "SKILL", -1, 0, "", false, false, null));
            return;
        }

        for (Effect effect : skill.getEffects()) {
            for (int i = 0; i < bdTargetsId.length; i++) {
                for (int j = 0; j < skill.getHits(); j++) {
                    int totalValue = 0;

                    boolean hit = random.nextFloat() < (skill.getAccuracy() * attacker.getAccuracyMultiplier());
                    if (!hit) continue;
                    anyHit = true;

                    boolean crit = random.nextFloat() < attacker.getCritChance();
                    if (crit) anyCrit = true;

                    if (random.nextFloat() < effect.getProbability()) {
                        if (effect.getStatus() != null) {
                            statusApplied = effect.getStatus().getName();
                            for (Player player : memPlayers) {
                                if (player.getId() == myTargetsId[i]) {
                                    if(player.getCharacter().hasStatus(effect.getStatus().getName())){
                                        int level = player.getCharacter().getStatusLevel(statusApplied);
                                        int turns = player.getCharacter().getDurationTurns(statusApplied);
                                        
                                        if(level < effect.getEffectLevel()){
                                            player.getCharacter().removeStatus(statusApplied);
                                            player.getCharacter().addStatusEffect(statusApplied, effect.getEffectLevel(), effect.getDurationTurns());
                                            System.out.println("Status level of new effect is bigger, applying new status effect");
                                        } else if(level == effect.getEffectLevel() && turns < effect.getDurationTurns()){
                                            player.getCharacter().removeStatus(statusApplied);
                                            player.getCharacter().addStatusEffect(statusApplied, effect.getEffectLevel(), effect.getDurationTurns());
                                            System.out.println("Turns of new effect is bigger, applying new status effect");
                                        }
                                    } else{
                                        player.getCharacter().addStatusEffect(statusApplied, effect.getEffectLevel(), effect.getDurationTurns());
                                    }
                                }
                            }
                            for (Enemy enemy : enemies) {
                                if (enemy.getCombatId() == myTargetsId[i]) {
                                    if(enemy.hasStatus(effect.getStatus().getName())){
                                        int level = enemy.getStatusLevel(statusApplied);
                                        int turns = enemy.getDurationTurns(statusApplied);
                                        
                                        if(level < effect.getEffectLevel()){
                                            enemy.removeStatus(statusApplied);
                                            enemy.addStatusEffect(statusApplied, effect.getEffectLevel(), effect.getDurationTurns());
                                            System.out.println("Status level of new effect is bigger, applying new status effect");
                                        } else if(level == effect.getEffectLevel() && turns < effect.getDurationTurns()){
                                            enemy.removeStatus(statusApplied);
                                            enemy.addStatusEffect(statusApplied, effect.getEffectLevel(), effect.getDurationTurns());
                                            System.out.println("Turns of new effect is bigger, applying new status effect");
                                        }
                                    } else{
                                        enemy.addStatusEffect(statusApplied, effect.getEffectLevel(), effect.getDurationTurns());
                                    }
                                }
                            }
                            continue;
                        }
                    }

                    if (effect.getStatistic() == null) continue;
                    statisticName = effect.getStatistic().getName();

                    if ("attack".equals(statisticName)) {

                        if (effect.getMinFlatPower() != null && effect.getMaxFlatPower() != null) {
                            int flatComponent = randomBetween(effect.getMinFlatPower(), effect.getMaxFlatPower());
                            int rawDamage = flatComponent;

                            if (crit) rawDamage = (int)(rawDamage * attacker.getCritDamage());

                            int finalDamage = Math.max(1, rawDamage - getDefenseOf(bdTargetsId[i]));

                            statisticName = "hp";
                            
                            for (Player player : memPlayers) {
                                if(player.getId() == myTargetsId[i]){
                                    player.getCharacter().setHp(player.getCharacter().getHp()-finalDamage);
                                }
                            }
                            for (Enemy enemy : enemies) {
                                if(enemy.getCombatId() == myTargetsId[i]){
                                    enemy.setHp(enemy.getHp()-finalDamage);
                                }
                            }

                            totalValue -= finalDamage;
                        } 
                        else if (effect.getStatMultiplier() != null) {
                            float currentAttack = getStatOf(bdTargetsId[i],"attack");

                            int attackChange = (int)(currentAttack * effect.getStatMultiplier());

                            for (Player player : memPlayers) {
                                if(player.getId() == myTargetsId[i]){
                                    player.getCharacter().setAttack(player.getCharacter().getAttack()+attackChange);
                                }
                            }
                            for (Enemy enemy : enemies) {
                                if(enemy.getCombatId() == myTargetsId[i]){
                                    enemy.setAttack(enemy.getAttack()+attackChange);
                                }
                            }

                            totalValue += attackChange;
                        }
                    } else if ("hp".equals(statisticName)) {

                        int tHpMax = getHpMaxOf(bdTargetsId[i]);
                        int value  = 0;

                        if (effect.getMinFlatPower() != null && effect.getMaxFlatPower() != null) {
                            int flatComponent = randomBetween(effect.getMinFlatPower(), effect.getMaxFlatPower());
                            value = flatComponent + (int) (tHpMax * 0.05f);
                        } else if (effect.getStatMultiplier() != null) {
                            value = (int) (tHpMax * effect.getStatMultiplier());
                        }

                        for (Player player : memPlayers) {
                            if(player.getId() == myTargetsId[i]){
                                player.getCharacter().setHp(player.getCharacter().getHp()+value);
                            }
                        }
                        for (Enemy enemy : enemies) {
                            if(enemy.getCombatId() == myTargetsId[i]){
                                enemy.setHp(enemy.getHp()+value);
                            }
                        }

                        totalValue += value;

                    } else if ("accuracy_multiplier".equals(statisticName)) {

                        float mult = effect.getStatMultiplier() != null ? effect.getStatMultiplier() : 0f;
                        float value = (mult * 100);

                        for (Player player : memPlayers) {
                            if(player.getId() == myTargetsId[i]){
                                player.getCharacter().setAccuracyMultiplier(player.getCharacter().getAccuracyMultiplier()+value);
                            }
                        }
                        for (Enemy enemy : enemies) {
                            if(enemy.getCombatId() == myTargetsId[i]){
                                enemy.setAccuracyMultiplier(enemy.getAccuracyMultiplier()+value);
                            }
                        } 

                        totalValue += value;

                    } else {

                        float statValue = getStatOf(bdTargetsId[i], statisticName);
                        float mult = effect.getStatMultiplier() != null ? effect.getStatMultiplier() : 0f;
                        float value = (statValue * mult);

                        for (Player player : memPlayers) {
                            if(player.getId() == myTargetsId[i]){
                                player.getCharacter().setStat(statisticName, value);
                            }
                        }
                        for (Enemy enemy : enemies) {
                            if(enemy.getCombatId() == myTargetsId[i]){
                                enemy.setStat(statisticName, value);
                            }
                        } 

                        totalValue += value;
                    }

                    results.add(new PlayerActionResult(
                        (int)realPlayer.getId(), "SKILL", myTargetsId[i],
                        totalValue, statisticName, anyCrit, anyHit, statusApplied
                    ));
                }
            }
        }
    }

    private void processItemAction(PlayerTurn turn) {
        System.out.println("Id del attacker: " + turn.player.getId());
        System.out.println("Id del target: "   + turn.target.getId());
        System.out.println("Id de l'item: "    + turn.itemUsed.getId());

        Player realPlayer = memPlayers.stream()
            .filter(pl -> pl.getId() == turn.player.getId()).findFirst().orElse(null);

        if (realPlayer == null) {
            System.out.println("Player not found");
            return;
        }

        BdPlayer attacker = game.getBdPlayerById(realPlayer.getCharacter().getId());

        Item item = game.getItemById(turn.itemUsed.getId());

        boolean anyCrit      = false;
        boolean anyHit       = true;
        String statisticName = "";
        String statusApplied = null;

        int[] myTargetsId = new int[3];
        int[] bdTargetsId = new int[3];

        if (item.isAoe()) {
            if ("ENEMY".equals(item.getTargetType())) {
                myTargetsId = enemies.stream()
                        .filter(en -> en.getHp() > 0)
                        .mapToInt(Enemy::getCombatId)
                        .toArray();

                bdTargetsId = enemies.stream()
                        .filter(en -> en.getHp() > 0)
                        .mapToInt(Enemy::getId)
                        .toArray();
            } else {
                myTargetsId = memPlayers.stream()
                        .filter(pl -> pl.getCharacter().getHp() > 0)
                        .mapToInt(p -> (int) p.getId())
                        .toArray();

                bdTargetsId = memPlayers.stream()
                        .filter(pl -> pl.getCharacter().getHp() > 0)
                        .mapToInt(p -> p.getCharacter().getId())
                        .toArray();
            }
        } else {
            if ("ENEMY".equals(item.getTargetType())) {
                int i = 0;
                for (Enemy en : enemies) {
                    if (en.getHp() > 0 && en.getCombatId() == turn.target.getId()) {
                        myTargetsId[i] = en.getCombatId();
                        bdTargetsId[i] = en.getId();
                        i++;
                    }
                }
            } else {
                int i = 0;
                for (Player pl : memPlayers) {
                    if (pl.getCharacter().getHp() > 0 && pl.getId() == turn.target.getId()) {
                        myTargetsId[i] = (int) pl.getId();
                        bdTargetsId[i] = pl.getCharacter().getId();
                        i++;
                    }
                }
            }
        }

        if (bdTargetsId.length == 0) {
            results.add(new PlayerActionResult(attacker.getId(), "ITEM", -1, 0, "", false, false, null));
            return;
        }

        for (Effect effect : item.getEffects()) {
            for (int i = 0; i < bdTargetsId.length; i++) {
                int totalValue = 0;

                if (random.nextFloat() < effect.getProbability()){
                    if (effect.getStatus() != null) {
                        statusApplied = effect.getStatus().getName();
                        for (Player player : memPlayers) {
                            if (player.getId() == myTargetsId[i]) {
                                if(player.getCharacter().hasStatus(effect.getStatus().getName())){
                                    int level = player.getCharacter().getStatusLevel(statusApplied);
                                    int turns = player.getCharacter().getDurationTurns(statusApplied);
                                        
                                    if(level < effect.getEffectLevel()){
                                        player.getCharacter().removeStatus(statusApplied);
                                        player.getCharacter().addStatusEffect(statusApplied, effect.getEffectLevel(), effect.getDurationTurns());
                                        System.out.println("Status level of new effect is bigger, applying new status effect");
                                    } else if(level == effect.getEffectLevel() && turns < effect.getDurationTurns()){
                                        player.getCharacter().removeStatus(statusApplied);
                                        player.getCharacter().addStatusEffect(statusApplied, effect.getEffectLevel(), effect.getDurationTurns());
                                        System.out.println("Turns of new effect is bigger, applying new status effect");
                                    }
                                } else{
                                    player.getCharacter().addStatusEffect(statusApplied, effect.getEffectLevel(), effect.getDurationTurns());
                                }
                            }
                        }
                        for (Enemy enemy : enemies) {
                            if (enemy.getCombatId() == myTargetsId[i]) {
                                if(enemy.hasStatus(effect.getStatus().getName())){
                                    int level = enemy.getStatusLevel(statusApplied);
                                    int turns = enemy.getDurationTurns(statusApplied);
                                    
                                    if(level < effect.getEffectLevel()){
                                        enemy.removeStatus(statusApplied);
                                        enemy.addStatusEffect(statusApplied, effect.getEffectLevel(), effect.getDurationTurns());
                                        System.out.println("Status level of new effect is bigger, applying new status effect");
                                    } else if(level == effect.getEffectLevel() && turns < effect.getDurationTurns()){
                                        enemy.removeStatus(statusApplied);
                                        enemy.addStatusEffect(statusApplied, effect.getEffectLevel(), effect.getDurationTurns());
                                        System.out.println("Turns of new effect is bigger, applying new status effect");
                                    }
                                } else{
                                    enemy.addStatusEffect(statusApplied, effect.getEffectLevel(), effect.getDurationTurns());
                                }
                            }
                        }
                        continue;
                    }
                }

                if (effect.getStatistic() == null) continue;
                statisticName = effect.getStatistic().getName();

                if ("attack".equals(statisticName)) {
                    if (effect.getMinFlatPower() != null && effect.getMaxFlatPower() != null) {
                        int flatComponent = randomBetween(effect.getMinFlatPower(), effect.getMaxFlatPower());
                        int rawDamage = flatComponent;

                        int finalDamage = Math.max(1, rawDamage - getDefenseOf(bdTargetsId[i]));

                        statisticName = "hp";

                        for (Player player : memPlayers) {
                            if(player.getId() == myTargetsId[i]){
                                player.getCharacter().setHp(player.getCharacter().getHp()-finalDamage);
                            }
                        }
                        for (Enemy enemy : enemies) {
                            if(enemy.getCombatId() == myTargetsId[i]){
                                enemy.setHp(enemy.getHp()-finalDamage);
                            }
                        }

                        totalValue -= finalDamage;
                    } 
                    else if (effect.getStatMultiplier() != null) {
                        float currentAttack = getStatOf(bdTargetsId[i],"attack");

                        int attackChange = (int)(currentAttack * effect.getStatMultiplier());

                        for (Player player : memPlayers) {
                            if(player.getId() == myTargetsId[i]){
                                player.getCharacter().setAttack(player.getCharacter().getAttack()+attackChange);
                            }
                        }
                        for (Enemy enemy : enemies) {
                            if(enemy.getCombatId() == myTargetsId[i]){
                                enemy.setAttack(enemy.getAttack()+attackChange);
                            }
                        }

                        totalValue += attackChange;
                    }
                } else if ("hp".equals(statisticName)) {

                    int tHpMax = getHpMaxOf(bdTargetsId[i]);
                    int value  = 0;

                    if (effect.getMinFlatPower() != null && effect.getMaxFlatPower() != null) {
                        int flatComponent = randomBetween(effect.getMinFlatPower(), effect.getMaxFlatPower());
                        value = flatComponent + (int) (tHpMax * 0.05f);
                    } else if (effect.getStatMultiplier() != null) {
                        value = (int) (tHpMax * effect.getStatMultiplier());
                    }

                    for (Player player : memPlayers) {
                        if(player.getId() == myTargetsId[i]){
                            player.getCharacter().setHp(player.getCharacter().getHp()+value);
                        }
                    }
                    for (Enemy enemy : enemies) {
                        if(enemy.getCombatId() == myTargetsId[i]){
                            enemy.setHp(enemy.getHp()+value);
                        }
                    }

                    totalValue += value;

                } else if ("accuracy_multiplier".equals(statisticName)) {

                    float mult = effect.getStatMultiplier() != null ? effect.getStatMultiplier() : 0f;
                    float value = (mult * 100);

                    for (Player player : memPlayers) {
                        if(player.getId() == myTargetsId[i]){
                            player.getCharacter().setAccuracyMultiplier(player.getCharacter().getAccuracyMultiplier()+value);
                        }
                    }
                    for (Enemy enemy : enemies) {
                        if(enemy.getCombatId() == myTargetsId[i]){
                            enemy.setAccuracyMultiplier(enemy.getAccuracyMultiplier()+value);
                        }
                    } 

                    totalValue += value;

                } else {

                    float statValue = getStatOf(bdTargetsId[i], statisticName);
                    float mult      = effect.getStatMultiplier() != null ? effect.getStatMultiplier() : 0f;
                    float value = (statValue * mult);

                    for (Player player : memPlayers) {
                        if(player.getId() == myTargetsId[i]){
                            player.getCharacter().setStat(statisticName, value);
                        }
                    }
                    for (Enemy enemy : enemies) {
                        if(enemy.getCombatId() == myTargetsId[i]){
                            enemy.setStat(statisticName, value);
                        }
                    }

                    totalValue += value;
                }
                
                results.add(new PlayerActionResult(
                    (int)realPlayer.getId(), "ITEM", myTargetsId[i],
                    totalValue, statisticName, anyCrit, anyHit, statusApplied
                ));
            }
        }
        
        game.getInventory().values().stream().filter(i -> i.getId() == item.getId()).findFirst().get().use();
        Item it = game.getInventory().values().stream().filter(i -> i.getId() == item.getId()).findFirst().get();

        if(it.getMaxUses() <= 0){
            game.getInventory().values().remove(it);
        }
    }

    private List<EnemyActionResult> processEnemyTurns() {
        List<EnemyActionResult> results = new ArrayList<>();

        List<Player> alivePlayers = memPlayers.stream()
            .filter(p -> p.getCharacter().getHp() > 0)
            .collect(Collectors.toList());

        for (Enemy e : enemies) {
            if (e.hasStatus("stunned")) {
                results.add(new EnemyActionResult(
                    e.getCombatId(), "STUNNED",
                    -1, 0, false, false, null
                ));
                continue;
            }

            Enemy enemy = game.getEnemyWithSkills(e.getId());

            if (enemy.getHp() <= 0) continue;
            if (alivePlayers.isEmpty()) break;

            List<Skill> activeSkills = enemy.getSkills().stream()
                .filter(s -> Boolean.FALSE.equals(s.getIsPassive()))
                .collect(Collectors.toList());

            if (activeSkills.isEmpty()) continue;

            Skill skill = activeSkills.get(random.nextInt(activeSkills.size()));
            
            Skill chosenSkill = game.getSkillByIdWithEffects(skill.getId());

            if (chosenSkill.getEffects() == null || chosenSkill.getEffects().isEmpty()) {
                Player fallback = alivePlayers.get(random.nextInt(alivePlayers.size()));
                results.add(new EnemyActionResult(
                    enemy.getCombatId(), chosenSkill.getName(),
                    (int) fallback.getId(), 0, false, false, null
                ));
                continue;
            }

            int[] myTargetsId;
            int[] bdTargetsId;

            if (chosenSkill.getIsAoe() != null && chosenSkill.getIsAoe()) {
                myTargetsId = alivePlayers.stream().mapToInt(p -> (int) p.getId()).toArray();
                bdTargetsId = alivePlayers.stream().mapToInt(p -> p.getCharacter().getId()).toArray();
            } else {
                Player target = alivePlayers.get(random.nextInt(alivePlayers.size()));
                myTargetsId = new int[]{ (int) target.getId() };
                bdTargetsId = new int[]{ target.getCharacter().getId() };
            }

            boolean anyCrit      = false;
            boolean anyHit       = false;
            String statisticName = "";
            String statusApplied = null;

            for (Effect effect : chosenSkill.getEffects()) {
                for (int i = 0; i < myTargetsId.length; i++) {
                    for (int j = 0; j < chosenSkill.getHits(); j++) {
                        int totalValue = 0;

                        boolean hit = random.nextFloat() < (chosenSkill.getAccuracy() * enemy.getAccuracyMultiplier());
                        if (!hit) continue;
                        anyHit = true;

                        boolean crit = random.nextFloat() < enemy.getCritChance();
                        if (crit) anyCrit = true;

                        if (random.nextFloat() < effect.getProbability()){
                            if (effect.getStatus() != null) {
                                statusApplied = effect.getStatus().getName();
                                for (Player player : memPlayers) {
                                    if (player.getId() == myTargetsId[i]) {
                                        if(player.getCharacter().hasStatus(effect.getStatus().getName())){
                                            int level = player.getCharacter().getStatusLevel(statusApplied);
                                            int turns = player.getCharacter().getDurationTurns(statusApplied);
                                            
                                            if(level < effect.getEffectLevel()){
                                                player.getCharacter().removeStatus(statusApplied);
                                                player.getCharacter().addStatusEffect(statusApplied, effect.getEffectLevel(), effect.getDurationTurns());
                                                System.out.println("Status level of new effect is bigger, applying new status effect");
                                            } else if(level == effect.getEffectLevel() && turns < effect.getDurationTurns()){
                                                player.getCharacter().removeStatus(statusApplied);
                                                player.getCharacter().addStatusEffect(statusApplied, effect.getEffectLevel(), effect.getDurationTurns());
                                                System.out.println("Turns of new effect is bigger, applying new status effect");
                                            }
                                        } else{
                                            player.getCharacter().addStatusEffect(statusApplied, effect.getEffectLevel(), effect.getDurationTurns());
                                        }
                                    }
                                }
                                for (Enemy en : enemies) {
                                    if (en.getCombatId() == myTargetsId[i]) {
                                        if(en.hasStatus(effect.getStatus().getName())){
                                            int level = en.getStatusLevel(statusApplied);
                                            int turns = en.getDurationTurns(statusApplied);
                                            
                                            if(level < effect.getEffectLevel()){
                                                en.removeStatus(statusApplied);
                                                en.addStatusEffect(statusApplied, effect.getEffectLevel(), effect.getDurationTurns());
                                                System.out.println("Status level of new effect is bigger, applying new status effect");
                                            } else if(level == effect.getEffectLevel() && turns < effect.getDurationTurns()){
                                                en.removeStatus(statusApplied);
                                                en.addStatusEffect(statusApplied, effect.getEffectLevel(), effect.getDurationTurns());
                                                System.out.println("Turns of new effect is bigger, applying new status effect");
                                            }
                                        } else{
                                            en.addStatusEffect(statusApplied, effect.getEffectLevel(), effect.getDurationTurns());
                                        }
                                    }
                                }
                                continue;
                            }
                        }

                        if (effect.getStatistic() == null) continue;
                        statisticName = effect.getStatistic().getName();

                        if ("attack".equals(statisticName)) {
                            if (effect.getMinFlatPower() != null && effect.getMaxFlatPower() != null) {
                                int flatComponent = randomBetween(effect.getMinFlatPower(), effect.getMaxFlatPower());
                                int rawDamage = flatComponent;

                                if (crit) rawDamage = (int)(rawDamage * enemy.getCritDamage());

                                int finalDamage = Math.max(1, rawDamage - getDefenseOf(bdTargetsId[i]));

                                statisticName = "hp";

                                for (Player player : memPlayers) {
                                    if(player.getId() == myTargetsId[i]){
                                        player.getCharacter().setHp(player.getCharacter().getHp()-finalDamage);
                                    }
                                }
                                for (Enemy en : enemies) {
                                    if(en.getCombatId() == myTargetsId[i]){
                                        en.setHp(en.getHp()-finalDamage);
                                    }
                                }

                                totalValue -= finalDamage;
                            } 
                            else if (effect.getStatMultiplier() != null) {
                                float currentAttack = getStatOf(bdTargetsId[i],"attack");

                                int attackChange = (int)(currentAttack * effect.getStatMultiplier());

                                for (Player player : memPlayers) {
                                    if(player.getId() == myTargetsId[i]){
                                        player.getCharacter().setAttack(player.getCharacter().getAttack()+attackChange);
                                    }
                                }
                                for (Enemy en : enemies) {
                                    if(en.getCombatId() == myTargetsId[i]){
                                        en.setAttack(en.getAttack()+attackChange);
                                    }
                                }

                                totalValue += attackChange;
                            }
                        } else if ("hp".equals(statisticName)) {
                            int tHpMax = getHpMaxOf(bdTargetsId[i]);
                            int value  = 0;

                            if (effect.getMinFlatPower() != null && effect.getMaxFlatPower() != null) {
                                int flatComponent = randomBetween(effect.getMinFlatPower(), effect.getMaxFlatPower());
                                value = flatComponent + (int) (tHpMax * 0.05f);
                            } else if (effect.getStatMultiplier() != null) {
                                value = (int) (tHpMax * effect.getStatMultiplier());
                            }

                            for (Player player : memPlayers) {
                                if(player.getId() == myTargetsId[i]){
                                    player.getCharacter().setHp(player.getCharacter().getHp()+value);
                                }
                            }
                            for (Enemy en : enemies) {
                                if(en.getCombatId() == myTargetsId[i]){
                                    en.setHp(en.getHp()+value);
                                }
                            }

                            totalValue += value;

                        } else if ("accuracy_multiplier".equals(statisticName)) {
                            float mult = effect.getStatMultiplier() != null ? effect.getStatMultiplier() : 0f;
                            float value = (mult * 100);

                            for (Player player : memPlayers) {
                                if(player.getId() == myTargetsId[i]){
                                    player.getCharacter().setAccuracyMultiplier(player.getCharacter().getAccuracyMultiplier()+value);
                                }
                            }
                            for (Enemy en : enemies) {
                                if(en.getCombatId() == myTargetsId[i]){
                                    en.setAccuracyMultiplier(en.getAccuracyMultiplier()+value);
                                }
                            } 

                            totalValue += value;
                        } else {
                            float statValue = getStatOf(bdTargetsId[i], statisticName);
                            float mult      = effect.getStatMultiplier() != null ? effect.getStatMultiplier() : 0f;
                            float value = (statValue * mult);

                            for (Player player : memPlayers) {
                                if(player.getId() == myTargetsId[i]){
                                    player.getCharacter().setStat(statisticName, value);
                                }
                            }
                            for (Enemy en : enemies) {
                                if(en.getCombatId() == myTargetsId[i]){
                                    en.setStat(statisticName, value);
                                }
                            }

                            totalValue += value;
                        }

                        results.add(new EnemyActionResult(
                            e.getCombatId(), chosenSkill.getName(), myTargetsId[i],
                            totalValue, anyCrit, anyHit, statusApplied
                        ));
                    }
                }
            }
        }

        return results;
    }

    private Status_Applied_OUT processStatusEffects() {
        Status_Applied_OUT statusApplied = new Status_Applied_OUT();
        statusApplied.updates = new ArrayList<>();

        // PLAYERS
        for (Player player : memPlayers) {

            BdPlayer c = player.getCharacter();

            if (c.getHp() <= 0) continue;

            List<StatusUpdate> tmpStatus = applyStatusEffects(c, (int)player.getId());

            if(tmpStatus != null && !tmpStatus.isEmpty()){
                for (StatusUpdate su : tmpStatus) {
                    statusApplied.updates.add(su);
                }
            }
        }

        // ENEMIES
        for (Enemy enemy : enemies) {

            if (enemy.getHp() <= 0) continue;

            List<StatusUpdate> tmpStatus = applyStatusEffects(enemy, enemy.getCombatId());

            if(tmpStatus != null && !tmpStatus.isEmpty()){
                for (StatusUpdate su : tmpStatus) {
                    statusApplied.updates.add(su);
                }
            }
        }

        return statusApplied;
    }

    private List<StatusUpdate> applyStatusEffects(Character c, int id) {

        List<StatusCharacter> statuses = c.getStatusEffects();

        List<StatusUpdate> statusesList = new ArrayList<>();

        if (statuses == null || statuses.isEmpty()) return null;

        // BLEEDING
        if (statuses.stream().anyMatch(se -> se.getName().equals("bleeding"))) {

            int level = statuses.stream().filter(se -> se.getName().equals("bleeding")).findFirst().get().getLevel();
            int durationTurns = statuses.stream().filter(se -> se.getName().equals("bleeding")).findFirst().get().getDurationTurns();
            
            float percent = switch (level) {
                case 1 -> 0.03f;
                case 2 -> 0.06f;
                case 3 -> 0.09f;
                default -> 0f;
            };

            int damage = (int)(c.getHpMax() * percent);

            c.setHp(Math.max(0, c.getHp() - damage));

            // debuff defensa
            int originalDefense = c.getDefense();

            float defReduction = originalDefense * 0.10f;

            c.setDefense((int)(originalDefense - defReduction));

            System.out.println(c.getName() + " suffers bleeding damage: -" + damage);

            statusesList.add(new StatusUpdate(id, "bleeding", -damage, "hp", durationTurns, level));
            statusesList.add(new StatusUpdate(id, "bleeding", -defReduction, "defense", durationTurns, level));
        }

        // POISONED
        if (statuses.stream().anyMatch(se -> se.getName().equals("poisoned"))) {

            int level = statuses.stream().filter(se -> se.getName().equals("poisoned")).findFirst().get().getLevel();
            int durationTurns = statuses.stream().filter(se -> se.getName().equals("poisoned")).findFirst().get().getDurationTurns();

            float percent = switch (level) {
                case 1 -> 0.05f;
                case 2 -> 0.10f;
                case 3 -> 0.15f;
                default -> 0f;
            };

            int damage = (int)(c.getHpMax() * percent);

            c.setHp(Math.max(0, c.getHp() - damage));

            System.out.println(c.getName() + " suffers poison damage: -" + damage);

            statusesList.add(new StatusUpdate(id, "poisoned", -damage, "hp", durationTurns, level));
        }

        // STRENGTHENED
        if (statuses.stream().anyMatch(se -> se.getName().equals("strengthened"))) {

            boolean alreadyApplied = false;

            if(!alreadyApplied){
                alreadyApplied = true;

                int level = statuses.stream().filter(se -> se.getName().equals("strengthened")).findFirst().get().getLevel();
                int durationTurns = statuses.stream().filter(se -> se.getName().equals("strengthened")).findFirst().get().getDurationTurns();

                float percent = switch (level) {
                    case 1 -> 0.10f;
                    case 2 -> 0.20f;
                    case 3 -> 0.30f;
                    default -> 0f;
                };

                int attackBonus = (int)(c.getAttack() * percent);

                c.setAttack(c.getAttack() + attackBonus);

                System.out.println(c.getName() + " gains strengthened buff: +" + attackBonus + " ATK");

                statusesList.add(new StatusUpdate(id, "strengthened", attackBonus, "attack", durationTurns, level));
            }
        }

        c.passStatusTurns();
        return statusesList;
    }

    private int randomBetween(Integer min, Integer max) {
        if (min == null || max == null) return 0;
        if (min.equals(max)) return min;
        return min + random.nextInt(max - min + 1);
    }

    private int getHpMaxOf(int entityId) {
        for (Enemy e : enemies) {
            if (e.getId() == entityId) return e.getHpMax();
        }
        for (BdPlayer bp : game.getPlayerCharacters().values()) {
            if (bp.getId() == entityId) return bp.getHpMax();
        }
        return 1;
    }

    private float getStatOf(int entityId, String statName) {
        Character c = getCharacterOf(entityId);
        if (c == null) return 0f;
        return switch (statName) {
            case "attack"              -> c.getAttack();
            case "defense"             -> c.getDefense();
            case "speed"               -> c.getSpeed();
            case "crit_chance"         -> c.getCritChance();
            case "crit_damage"         -> c.getCritDamage();
            case "accuracy_multiplier" -> c.getAccuracyMultiplier();
            default                    -> 0f;
        };
    }

    private Character getCharacterOf(int entityId) {
        for (Enemy e : enemies) {
            if (e.getId() == entityId) return e;
        }
        for (BdPlayer bp : game.getPlayerCharacters().values()) {
            if (bp.getId() == entityId) return bp;
        }
        return null;
    }

    private int getDefenseOf(int entityId) {
        for (Enemy e : enemies) {
            if (e.getId() == entityId) return e.getDefense();
        }
        for (BdPlayer bp : game.getPlayerCharacters().values()) {
            if (bp.getId() == entityId) return bp.getDefense();
        }
        return 0;
    }
}