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

    public StateEnemyRoom(GameInstance game) {
        super(game);
        mapper = new ObjectMapper();

        enemies = game.getEnemiesByLevel();

        for (Enemy e : enemies) {
            entityCurrentHp.put(e.getId(), e.getHpMax());
        }

        for (BdPlayer bp : game.getPlayerCharacters().values()) {
            entityCurrentHp.put(bp.getId(), bp.getHpMax());
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

                default:
                    break;
            }
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

        List<EnemyActionResult> enemyResults = processEnemyTurns();

        game.broadcast(new JSONMessage(game.getId(), new PlayerActionResult_OUT(playerResults)));
        game.broadcast(new JSONMessage(game.getId(), new EnemyAction_OUT(enemyResults)));

        pendingTurns.clear();

        boolean allEnemiesDead = enemies.stream().allMatch(e -> getHp(e.getId()) <= 0);
        boolean allPlayersDead = game.getPlayerCharacters().values().stream()
                .allMatch(bp -> getHp(bp.getId()) <= 0);

        if (allEnemiesDead) {
            System.out.println("Tots els enemics han mort. Sala netejada!");
        } else if (allPlayersDead) {
            System.out.println("Tots els jugadors han mort. Game Over.");
            game.stop();
        }
    }

    private List<PlayerActionResult> processPlayerTurns() {
        List<PlayerActionResult> results = new ArrayList<>();

        for (Map.Entry<Player, PlayerTurn> entry : pendingTurns.entrySet()) {
            PlayerTurn turn = entry.getValue();

            if (!turn.choiceMade || turn.player == null) {
                int pid = turn.player != null ? turn.player.getId() : -1;
                results.add(new PlayerActionResult(pid, "PASS", new int[-1], 0, "", false, false, null));
                continue;
            }

            if ("SKILL".equals(turn.choiceType) && turn.skillCasted != null) {
                results.add(processSkillAction(turn));
            } else if ("ITEM".equals(turn.choiceType) && turn.itemUsed != null) {
                // results.add(new PlayerActionResult(
                //     turn.player.getId(), "ITEM",
                //     turn.target != null ? turn.target.getId() : -1,
                //     0, false, true, null
                // ));
            } else {
                results.add(new PlayerActionResult(turn.player.getId(), "PASS", new int[-1], 0, "", false, false, null));
            }
        }

        return results;
    }

    private PlayerActionResult processSkillAction(PlayerTurn turn) {
        BdPlayer attacker = turn.player;
        Character target  = turn.target;
        Skill skill       = turn.skillCasted;

        int totalValue       = 0;
        boolean anyCrit      = false;
        boolean anyHit       = false;
        String statisticName = "";
        String statusApplied = null;

        // ── Construir llista d'objectius ──────────────────────────────────────
        int[] targetsId;

        if (skill.getIsAoe() && "ENEMY".equals(skill.getTargetType())) {
            targetsId = enemies.stream().mapToInt(e -> e.getId()).toArray();

        } else if (skill.getIsAoe() && ("ALLY".equals(skill.getTargetType()) || "SELF".equals(skill.getTargetType()))) {
            targetsId = game.getPlayers().stream()
                    .mapToInt(p -> p.getCharacter().getId())
                    .toArray();

        } else {
            targetsId = target != null ? new int[]{ target.getId() } : new int[0];
        }

        if (targetsId.length == 0) {
            return new PlayerActionResult(attacker.getId(), "SKILL", targetsId, 0, "", false, false, null);
        }

        Effect effect = skill.getEffect();

        if (effect != null) {

            boolean hit = random.nextFloat() < (skill.getAccuracy() * attacker.getAccuracyMultiplier());

            if (hit) {

                anyHit = true;

                boolean crit = random.nextFloat() < attacker.getCritChance();

                if (crit) {
                    anyCrit = true;
                }

                if (random.nextFloat() <= effect.getProbability()) {

                    if (effect.getStatus() != null) {

                        statusApplied = effect.getStatus().getName();

                    } else if (effect.getStatistic() != null) {
                        statisticName = effect.getStatistic().getName();

                        if ("hp".equals(statisticName)) {

                            int flatComponent = randomBetween(
                                effect.getMinFlatPower(),
                                effect.getMaxFlatPower()
                            );

                            if ("ENEMY".equals(skill.getTargetType())) {

                                float mult = effect.getStatMultiplier() != null
                                    ? Math.abs(effect.getStatMultiplier())
                                    : 1.0f;

                                int rawDamage = flatComponent + (int)(attacker.getAttack() * mult);

                                if (crit) {
                                    rawDamage = (int)(rawDamage * attacker.getCritDamage());
                                }

                                for (int tId : targetsId) {

                                    int finalDamage = Math.max(1, rawDamage - getDefenseOf(tId));

                                    setHp(tId, Math.max(0, getHp(tId) - finalDamage));

                                    totalValue -= finalDamage; // negatiu = dany
                                }

                            } else {

                                for (int tId : targetsId) {

                                    int tHpMax = getHpMaxOf(tId);

                                    float mult = effect.getStatMultiplier() != null
                                        ? effect.getStatMultiplier()
                                        : 0f;

                                    int value = flatComponent + (int)(tHpMax * mult);

                                    int newHp = Math.min(
                                        tHpMax,
                                        Math.max(0, getHp(tId) + value)
                                    );

                                    setHp(tId, newHp);

                                    totalValue += value;
                                }
                            }

                        } else if ("accuracy_multiplier".equals(statisticName)) {

                            float mult = effect.getStatMultiplier() != null
                                ? effect.getStatMultiplier()
                                : 0f;

                            totalValue += (int)(mult * 100);

                        } else {

                            for (int tId : targetsId) {

                                float statValue = getStatOf(tId, statisticName);

                                float mult = effect.getStatMultiplier() != null
                                    ? effect.getStatMultiplier()
                                    : 0f;

                                int value = (int)(statValue * mult);

                                totalValue += value;
                            }
                        }
                    }
                }
            }
        }

        return new PlayerActionResult(
            attacker.getId(),
            "SKILL",
            targetsId,
            totalValue,
            statisticName,
            anyCrit,
            anyHit,
            statusApplied
        );
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

    private List<EnemyActionResult> processEnemyTurns() {
        List<EnemyActionResult> results = new ArrayList<>();

        List<BdPlayer> alivePlayers = game.getPlayerCharacters().values().stream()
                .filter(bp -> getHp(bp.getId()) > 0)
                .collect(Collectors.toList());

        for (Enemy enemy : enemies) {

            if (getHp(enemy.getId()) <= 0) continue;
            if (alivePlayers.isEmpty()) break;

            List<Skill> activeSkills = enemy.getSkills().stream()
                    .filter(s -> Boolean.FALSE.equals(s.getIsPassive()))
                    .collect(Collectors.toList());

            if (activeSkills.isEmpty()) continue;

            Skill chosenSkill   = activeSkills.get(random.nextInt(activeSkills.size()));
            BdPlayer target     = alivePlayers.get(random.nextInt(alivePlayers.size()));

            int totalDamage      = 0;
            boolean anyCrit      = false;
            boolean anyHit       = false;
            String statusApplied = null;

            Effect effect = chosenSkill.getEffect();

            if (effect != null) {

                boolean hit = random.nextFloat() < (chosenSkill.getAccuracy() * enemy.getAccuracyMultiplier());

                if (hit) {

                    anyHit = true;

                    boolean crit = random.nextFloat() < enemy.getCritChance();

                    if (crit) {
                        anyCrit = true;
                    }

                    if (random.nextFloat() <= effect.getProbability()) {

                        if (effect.getStatus() != null) {

                            statusApplied = effect.getStatus().getName();

                        } else if (effect.getStatistic() != null) {

                            String statName = effect.getStatistic().getName();

                            if ("attack".equals(statName)) {
                                int damage = calculateValue(
                                    enemy.getAttack(),
                                    effect,
                                    crit,
                                    enemy.getCritDamage()
                                );

                                int finalDamage = Math.max(1, damage - target.getDefense());

                                setHp(target.getId(), Math.max(0, getHp(target.getId()) - finalDamage));

                                totalDamage += finalDamage;
                            }
                        }
                    }
                }
            }

            results.add(new EnemyActionResult(
                enemy.getId(),
                chosenSkill.getName(),
                target.getId(),
                totalDamage,
                anyCrit,
                anyHit,
                statusApplied
            ));
        }

        return results;
    }

    private int calculateValue(int attackerStat, Effect effect, boolean crit, float critDamage) {
        int value = 0;

        if (effect.getMinFlatPower() != null && effect.getMaxFlatPower() != null) {
            int min = effect.getMinFlatPower();
            int max = effect.getMaxFlatPower();
            value += min + random.nextInt(Math.max(1, max - min + 1));
        }

        if (effect.getStatMultiplier() != null && attackerStat > 0) {
            value += (int)(attackerStat * effect.getStatMultiplier());
        }

        if (value == 0) value = attackerStat;

        if (crit) value = (int)(value * critDamage);

        return value;
    }

    private int getHp(int entityId) {
        return entityCurrentHp.getOrDefault(entityId, 0);
    }

    private void setHp(int entityId, int hp) {
        entityCurrentHp.put(entityId, hp);
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