package com.example.demo.api.model.states;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.example.demo.api.model.Player;
import com.example.demo.api.model.bd.BdPlayer;
import com.example.demo.api.model.bd.Character;
import com.example.demo.api.model.bd.Effect;
import com.example.demo.api.model.bd.Enemy;
import com.example.demo.api.model.bd.Item;
import com.example.demo.api.model.bd.Skill;
import com.example.demo.api.model.messages.JSONMessage;
import com.example.demo.api.model.messages.in.players_turn.PlayerTurn;
import com.example.demo.api.model.messages.in.players_turn.PlayersTurn_IN;
import com.example.demo.api.model.messages.in.room_cleared.RoomCleared_IN;
import com.example.demo.api.model.messages.out.enemy_action.EnemyAction_OUT;
import com.example.demo.api.model.messages.out.enemy_action.EnemyActionResult;
import com.example.demo.api.model.messages.out.enemies.ShowEnemies_OUT;
import com.example.demo.api.model.messages.out.generic.ActionResult_OUT;
import com.example.demo.api.model.messages.out.player_action.PlayerActionResult;
import com.example.demo.api.model.messages.out.player_action.PlayerActionResult_OUT;
import com.example.demo.components.GameInstance;
import com.example.demo.components.GameMessage;

import tools.jackson.databind.ObjectMapper;

public class StateEnemyRoom extends State {

    private ObjectMapper mapper;
    private Random random = new Random();

    private List<Enemy> enemies;

    private Map<Player, PlayerTurn> pendingTurns = new HashMap<>();

    private Map<Integer, Integer> entityCurrentHp = new HashMap<>();

    private HashMap<Player, Boolean> playersCleared = new HashMap<>();

    private List<PlayerActionResult> results = new ArrayList<>();

    public StateEnemyRoom(GameInstance game) {
        super(game);
        mapper = new ObjectMapper();

        enemies = game.getEnemiesByLevel();

        for (Enemy e : enemies) {
            entityCurrentHp.put(e.getCombatId(), e.getHpMax());
        }

        for (Player p : game.getPlayers()) {
            entityCurrentHp.put((int) p.getId(), p.getCharacter().getHpMax());
        }

        for (Player p : game.getPlayers()) {
            playersCleared.put(p, false);
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
                        makeTurn(message.player(), gm);
                        break;
                    case RoomCleared_IN.TYPE:
                        waitAllPlayers(message.player(), gm);
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
            game.setState(new StateMap(game));
        }
    }

    private void makeTurn(Player p, JSONMessage jsonMsg) {
        PlayersTurn_IN message = mapper.treeToValue(jsonMsg.data, PlayersTurn_IN.class);

        if (message.players != null && !message.players.isEmpty()) {
            pendingTurns.put(p, message.players.get(0));
        }

        if (pendingTurns.size() < game.getPlayers().size()) {
            System.out.println("Esperant jugadors... (" + pendingTurns.size() + "/" + game.getPlayers().size() + ")");
            return;
        }

        System.out.println("Tots els jugadors han triat. Processant torn...");

        List<PlayerActionResult> playerResults = processPlayerTurns();
        List<EnemyActionResult> enemyResults   = processEnemyTurns();

        game.broadcast(new JSONMessage(game.getId(), new PlayerActionResult_OUT(playerResults)));
        game.broadcast(new JSONMessage(game.getId(), new EnemyAction_OUT(enemyResults)));

        pendingTurns.clear();

        boolean allEnemiesDead = enemies.stream().allMatch(e -> getHp(e.getCombatId()) <= 0);
        boolean allPlayersDead = game.getPlayers().stream().allMatch(p2 -> p2.getCharacter().getHp() <= 0);
        
        if (allEnemiesDead) {
            System.out.println("Tots els enemics han mort. Sala netejada!");
        } else if (allPlayersDead) {
            System.out.println("Tots els jugadors han mort. Game Over.");
            game.stop();
        }
    }

    private List<PlayerActionResult> processPlayerTurns() {
        results = new ArrayList<>();

        for (Map.Entry<Player, PlayerTurn> entry : pendingTurns.entrySet()) {
            PlayerTurn turn = entry.getValue();

            if (!turn.choiceMade || turn.player == null) {
                int pid = turn.player != null ? turn.player.getId() : -1;
                results.add(new PlayerActionResult(pid, "PASS", -1, 0, "", false, false, null));
                continue;
            }

            if ("SKILL".equals(turn.choiceType) && turn.skillCasted != null) {
                processSkillAction(turn);
            } else if ("ITEM".equals(turn.choiceType) && turn.itemUsed != null) {
                processItemAction(turn);
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

        Player realPlayer = game.getPlayers().stream()
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
                myTargetsId = enemies.stream().mapToInt(Enemy::getCombatId).toArray();
                bdTargetsId = enemies.stream().mapToInt(Enemy::getId).toArray();
            } else {
                myTargetsId = game.getPlayers().stream().mapToInt(p -> (int) p.getId()).toArray();
                bdTargetsId = game.getPlayers().stream().mapToInt(p -> p.getCharacter().getId()).toArray();
            }
        } else {
            if ("ENEMY".equals(skill.getTargetType())) {
                int i = 0;
                for (Enemy en : enemies) {
                    if (en.getCombatId() == turn.target.getId()) {
                        myTargetsId[i] = en.getCombatId();
                        bdTargetsId[i] = en.getId();
                        i++;
                    }
                }
            } else {
                int i = 0;
                for (Player pl : game.getPlayers()) {
                    if (pl.getId() == turn.target.getId()) {
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

                    if (random.nextFloat() > effect.getProbability()) continue;

                    if (effect.getStatus() != null) {
                        statusApplied = effect.getStatus().getName();
                        continue;
                    }

                    if (effect.getStatistic() == null) continue;
                    statisticName = effect.getStatistic().getName();

                    if ("attack".equals(statisticName) && "ENEMY".equals(skill.getTargetType())) {

                        if (effect.getMinFlatPower() != null && effect.getMaxFlatPower() != null) {
                            int flatComponent = randomBetween(effect.getMinFlatPower(), effect.getMaxFlatPower());
                            int rawDamage = flatComponent;

                            if (crit) rawDamage = (int)(rawDamage * attacker.getCritDamage());

                            int finalDamage = Math.max(1, rawDamage - getDefenseOf(bdTargetsId[i]));

                            setHp(myTargetsId[i], Math.max(0, getHp(myTargetsId[i]) - finalDamage));

                            statisticName = "hp";
                            
                            totalValue -= finalDamage;
                        } 
                        else if (effect.getStatMultiplier() != null) {
                            float currentAttack = getStatOf(bdTargetsId[i],"attack");

                            int attackChange = (int)(currentAttack * effect.getStatMultiplier());

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

                        totalValue += value;

                    } else if ("accuracy_multiplier".equals(statisticName)) {

                        float mult = effect.getStatMultiplier() != null ? effect.getStatMultiplier() : 0f;
                        totalValue += (int) (mult * 100);

                    } else {

                        float statValue = getStatOf(bdTargetsId[i], statisticName);
                        float mult      = effect.getStatMultiplier() != null ? effect.getStatMultiplier() : 0f;
                        totalValue     += (int) (statValue * mult);
                    }

                    results.add(new PlayerActionResult(
                        attacker.getId(), "SKILL", myTargetsId[i],
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

        Player realPlayer = game.getPlayers().stream()
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

        int[] myTargetsId;
        int[] bdTargetsId;

        if (item.isAoe()) {
            if ("ENEMY".equals(item.getTargetType())) {
                myTargetsId = enemies.stream().mapToInt(Enemy::getCombatId).toArray();
                bdTargetsId = enemies.stream().mapToInt(Enemy::getId).toArray();
            } else {
                myTargetsId = game.getPlayers().stream().mapToInt(p -> (int) p.getId()).toArray();
                bdTargetsId = game.getPlayers().stream().mapToInt(p -> p.getCharacter().getId()).toArray();
            }
        } else {
            if ("ENEMY".equals(item.getTargetType())) {
                int idx = 0;
                myTargetsId = new int[1];
                bdTargetsId = new int[1];
                for (Enemy en : enemies) {
                    if (en.getCombatId() == turn.target.getId()) {
                        myTargetsId[idx] = en.getCombatId();
                        bdTargetsId[idx] = en.getId();
                        break;
                    }
                }
            } else {
                myTargetsId = new int[1];
                bdTargetsId = new int[1];
                for (Player pl : game.getPlayers()) {
                    if (pl.getId() == turn.target.getId()) {
                        myTargetsId[0] = (int) pl.getId();
                        bdTargetsId[0] = pl.getCharacter().getId();
                        break;
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

                if (random.nextFloat() > effect.getProbability()) continue;

                if (effect.getStatus() != null) {
                    statusApplied = effect.getStatus().getName();
                    continue;
                }

                if (effect.getStatistic() == null) continue;
                statisticName = effect.getStatistic().getName();

                if ("attack".equals(statisticName) && "ENEMY".equals(item.getTargetType())) {
                    if (effect.getMinFlatPower() != null && effect.getMaxFlatPower() != null) {
                        int flatComponent = randomBetween(effect.getMinFlatPower(), effect.getMaxFlatPower());
                        int rawDamage = flatComponent;

                        int finalDamage = Math.max(1, rawDamage - getDefenseOf(bdTargetsId[i]));

                        setHp(myTargetsId[i], Math.max(0, getHp(myTargetsId[i]) - finalDamage));

                        statisticName = "hp";

                        totalValue -= finalDamage;
                    } 
                    else if (effect.getStatMultiplier() != null) {
                        float currentAttack = getStatOf(bdTargetsId[i],"attack");

                        int attackChange = (int)(currentAttack * effect.getStatMultiplier());

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

                    int newHp = Math.min(tHpMax, getHp(myTargetsId[i]) + value);
                    setHp(myTargetsId[i], newHp);
                    totalValue += value;

                } else if ("accuracy_multiplier".equals(statisticName)) {

                    float mult = effect.getStatMultiplier() != null ? effect.getStatMultiplier() : 0f;
                    totalValue += (int) (mult * 100);

                } else {

                    float statValue = getStatOf(bdTargetsId[i], statisticName);
                    float mult      = effect.getStatMultiplier() != null ? effect.getStatMultiplier() : 0f;
                    totalValue     += (int) (statValue * mult);
                }

                results.add(new PlayerActionResult(
                    attacker.getId(), "ITEM", myTargetsId[i],
                    totalValue, statisticName, anyCrit, anyHit, statusApplied
                ));
            }
        }
    }

    private List<EnemyActionResult> processEnemyTurns() {
        List<EnemyActionResult> results = new ArrayList<>();

        List<Player> alivePlayers = game.getPlayers().stream()
            .filter(p -> getHp((int) p.getId()) > 0)
            .collect(Collectors.toList());

        for (Enemy enemy : enemies) {

            if (getHp(enemy.getCombatId()) <= 0) continue;
            if (alivePlayers.isEmpty()) break;

            List<Skill> activeSkills = enemy.getSkills().stream()
                .filter(s -> Boolean.FALSE.equals(s.getIsPassive()))
                .collect(Collectors.toList());

            if (activeSkills.isEmpty()) continue;

            Skill chosenSkill = activeSkills.get(random.nextInt(activeSkills.size()));

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

                        if (random.nextFloat() > effect.getProbability()) continue;

                        if (effect.getStatus() != null) {
                            statusApplied = effect.getStatus().getName();
                            continue;
                        }

                        if (effect.getStatistic() == null) continue;
                        statisticName = effect.getStatistic().getName();

                        if ("attack".equals(statisticName)) {
                            if (effect.getMinFlatPower() != null && effect.getMaxFlatPower() != null) {
                                int flatComponent = randomBetween(effect.getMinFlatPower(), effect.getMaxFlatPower());
                                int rawDamage = flatComponent;

                                if (crit) rawDamage = (int)(rawDamage * enemy.getCritDamage());

                                int finalDamage = Math.max(1, rawDamage - getDefenseOf(bdTargetsId[i]));

                                setHp(myTargetsId[i], Math.max(0, getHp(myTargetsId[i]) - finalDamage));

                                statisticName = "hp";

                                totalValue -= finalDamage;
                            } 
                            else if (effect.getStatMultiplier() != null) {
                                float currentAttack = getStatOf(bdTargetsId[i],"attack");

                                int attackChange = (int)(currentAttack * effect.getStatMultiplier());

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

                            totalValue += value;

                        } else if ("accuracy_multiplier".equals(statisticName)) {

                            float mult = effect.getStatMultiplier() != null ? effect.getStatMultiplier() : 0f;
                            totalValue += (int) (mult * 100);

                        } else {

                            float statValue = getStatOf(bdTargetsId[i], statisticName);
                            float mult      = effect.getStatMultiplier() != null ? effect.getStatMultiplier() : 0f;
                            totalValue     += (int) (statValue * mult);
                        }

                        results.add(new EnemyActionResult(
                            enemy.getCombatId(), chosenSkill.getName(), myTargetsId[i],
                            totalValue, anyCrit, anyHit, statusApplied
                        ));
                    }
                }
            }
        }

        return results;
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

    private int getHp(int id) {
        return entityCurrentHp.getOrDefault(id, 0);
    }

    private void setHp(int id, int hp) {
        entityCurrentHp.put(id, hp);
    }
}