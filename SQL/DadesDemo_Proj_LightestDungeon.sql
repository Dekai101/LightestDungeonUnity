SET FOREIGN_KEY_CHECKS = 0;

DELETE FROM ItemEffect;
DELETE FROM SkillEffect;
DELETE FROM EntitySkill;
DELETE FROM LootEntry;
DELETE FROM LootTable;
DELETE FROM Enemy;
DELETE FROM Player;
DELETE FROM Item;
DELETE FROM Effect;
DELETE FROM Skill;
DELETE FROM Status;
DELETE FROM Statistic;
DELETE FROM Entity;

SET FOREIGN_KEY_CHECKS = 1;

-- =====================================================
-- 1️⃣ STATISTICS
-- =====================================================
INSERT INTO Statistic (name) VALUES
('hp'),
('energy'),
('defense'),
('attack'),
('speed'),
('crit_chance'),
('crit_damage'),
('accuracy_multiplier');

-- =====================================================
-- 2️⃣ STATUS
-- =====================================================
INSERT INTO Status (name, max_level, description, scaling_formula) VALUES
('Bleeding',     3, 'Loses HP each turn',                          'HP * 0.05 * level'),
('Poisoned',     3, 'Loses HP each turn - Apilable debuff',        'HP * 0.04 * level'),
('Strengthened', 3, 'Increases target attack - Temporary buff',    'Attack * 0.15 * level'),
('Stunned',      2, 'Chance of losing your turn - Control debuff', '0.40 + (0.20 * level)');

-- =====================================================
-- 3️⃣ SKILLS
-- =====================================================
INSERT INTO Skill
(name, description, energy_cost, accuracy, hits, target_type, is_aoe, is_passive, image_thumb)
VALUES
('Aquatic Blessing', 'Restores HP and Energy to an ally',                              40, 0.90, 1, 'ALLY',  false, false, 'https://art.pixilart.com/d16e02d1168d72a.png'),
('Iron Body',        'It increases the user defense',                                  30, 1.00, 1, 'SELF',  false, false, 'https://dinopixel.com/preload/0223/Iron-Armor.png'),
('Blood Rain',       'Area attack with potential bleeding',                             70, 0.95, 1, 'ENEMY', true,  false, 'https://i.postimg.cc/xdnvgH4F/Blood-Rain.png'),
('Shadow Strike',    'Fast strike that ignores part of the defense.',                  25, 0.95, 1, 'ENEMY', false, false, 'https://art.pixilart.com/57e7b9e3361ddb3.png'),
('Meteor Crash',     'Massive impact that damages all enemies.',                        80, 0.85, 1, 'ENEMY', true,  false, 'https://art.pixilart.com/02ffa52bad60aca.png'),
('Poison Dart',      'Shot that applies Poisoned to the target.',                      20, 0.90, 1, 'ENEMY', false, false, 'https://www.sprite-ai.art/sprite-img/a64613b4-5540-45f3-8784-a42be61f5f6f/a-beam-of-green-noxious-bubbles-16x16-pixel-art-6c50.png'),
('Twin Slash',       'Two consecutive fast strikes on the same target.',               35, 0.90, 2, 'ENEMY', false, false, 'https://i.postimg.cc/L8DK6crg/pixilart-sprite.png'),
('Soul Drain',       'Absorbs enemy HP and heals self for 50%.',                       50, 0.85, 1, 'ENEMY', false, false, 'https://riftwizard2.wiki.gg/images/256_drain_pulse.png'),
('Battle Cry',       'Applies Strengthened to all allies.',                            45, 1.00, 1, 'ALLY',  true,  false, 'https://static.wikia.nocookie.net/icewind-dale/images/8/8c/Strength_SPWI214C_Spell_icon_IWD.png'),
('Healing Wave',     'Moderately heals all allies.',                                   60, 1.00, 1, 'ALLY',  true,  false, 'https://art.pixilart.com/bcab0df4223d963.png'),
('Barrier Shield',   'Applies a defense shield to the target ally.',                   40, 1.00, 1, 'ALLY',  false, false, 'https://art.pixilart.com/sr2c3a62e1292aws3.png'),
('Revive',           'Revives a fallen ally with 30% HP.',                             90, 1.00, 1, 'ALLY',  false, false, 'https://thumb.ac-illust.com/46/46a7e5f601813270100cc2b018dbaa36_t.jpeg'),
('Stun Smash',       'Strong hit with a chance to stun.',                              45, 0.88, 1, 'ENEMY', false, false, 'https://cdna.artstation.com/p/marketplace/presentation_assets/000/134/976/large/file.png'),
('Frost Nova',       'Ice burst that stuns all enemies.',                              75, 0.80, 1, 'ENEMY', true,  false, 'https://i.pinimg.com/1200x/cf/3c/05/cf3c05494a54196a819c76de6debae36.jpg'),
('Evasion Mastery',  'Increases the base accuracy of the user.',                       0,  1.00, 0, 'SELF',  false, true,  'https://files.d20.io/marketplace/2960094/q6DzRX-MUTafsq8waiN9PA/med.png'),
('Berserker Rage',   'When HP drops below 30%, automatically increases attack.',       0,  1.00, 0, 'SELF',  false, true,  'https://ih1.redbubble.net/image.5516888541.0428/st,extra_large,507x507-pad,600x600,f8f8f8.jpg');

-- =====================================================
-- 4️⃣ EFECTOS
-- =====================================================

-- Capturar IDs de estadísticas
SET @HP       = (SELECT id FROM Statistic WHERE name = 'hp');
SET @Energy   = (SELECT id FROM Statistic WHERE name = 'energy');
SET @Defense  = (SELECT id FROM Statistic WHERE name = 'defense');
SET @Attack   = (SELECT id FROM Statistic WHERE name = 'attack');
SET @Speed    = (SELECT id FROM Statistic WHERE name = 'speed');

-- Capturar IDs de estados (nombres en inglés, igual que el INSERT de Status)
SET @Bleeding     = (SELECT id FROM Status WHERE name = 'Bleeding');
SET @Poisoned     = (SELECT id FROM Status WHERE name = 'Poisoned');
SET @Strengthened = (SELECT id FROM Status WHERE name = 'Strengthened');
SET @Stunned      = (SELECT id FROM Status WHERE name = 'Stunned');

-- Aquatic Blessing: curación porcentual de HP
INSERT INTO Effect (stat_id, stat_multiplier, status_id, min_flat_power, max_flat_power, effect_level, probability, duration_turns)
VALUES (@HP, 1.30, NULL, NULL, NULL, NULL, 1.0, 1);

-- Aquatic Blessing: restaura energía porcentual
INSERT INTO Effect (stat_id, stat_multiplier, status_id, min_flat_power, max_flat_power, effect_level, probability, duration_turns)
VALUES (@Energy, 1.10, NULL, NULL, NULL, NULL, 1.0, 1);

-- Iron Body: buff porcentual de defensa
INSERT INTO Effect (stat_id, stat_multiplier, status_id, min_flat_power, max_flat_power, effect_level, probability, duration_turns)
VALUES (@Defense, 1.20, NULL, NULL, NULL, NULL, 1.0, 3);

-- Blood Rain: DAÑO FIJO
INSERT INTO Effect (stat_id, stat_multiplier, status_id, min_flat_power, max_flat_power, effect_level, probability, duration_turns)
VALUES (@Attack, NULL, NULL, 18, 24, NULL, 1.0, 0);

-- Blood Rain: aplica Bleeding lv1
INSERT INTO Effect (stat_id, stat_multiplier, status_id, min_flat_power, max_flat_power, effect_level, probability, duration_turns)
VALUES (NULL, NULL, @Bleeding, NULL, NULL, 1, 0.15, 3);

-- Shadow Strike: DAÑO FIJO + multiplicador leve
INSERT INTO Effect (stat_id, stat_multiplier, status_id, min_flat_power, max_flat_power, effect_level, probability, duration_turns)
VALUES (@Attack, 1.15, NULL, 10, 14, NULL, 1.00, 0);

-- Meteor Crash: DAÑO FIJO alto AOE
INSERT INTO Effect (stat_id, stat_multiplier, status_id, min_flat_power, max_flat_power, effect_level, probability, duration_turns)
VALUES (@Attack, NULL, NULL, 22, 30, NULL, 1.00, 0);

-- Poison Dart: aplica Poisoned lv1
INSERT INTO Effect (stat_id, stat_multiplier, status_id, min_flat_power, max_flat_power, effect_level, probability, duration_turns)
VALUES (NULL, NULL, @Poisoned, NULL, NULL, 1, 1.00, 4);

-- Twin Slash: DAÑO FIJO por hit
INSERT INTO Effect (stat_id, stat_multiplier, status_id, min_flat_power, max_flat_power, effect_level, probability, duration_turns)
VALUES (@Attack, NULL, NULL, 8, 11, NULL, 1.00, 0);

-- Soul Drain: DAÑO FIJO
INSERT INTO Effect (stat_id, stat_multiplier, status_id, min_flat_power, max_flat_power, effect_level, probability, duration_turns)
VALUES (@Attack, NULL, NULL, 14, 19, NULL, 1.00, 0);

-- Soul Drain: curación porcentual del HP
INSERT INTO Effect (stat_id, stat_multiplier, status_id, min_flat_power, max_flat_power, effect_level, probability, duration_turns)
VALUES (@HP, 1.50, NULL, NULL, NULL, NULL, 1.00, 1);

-- Battle Cry: aplica Strengthened lv2
INSERT INTO Effect (stat_id, stat_multiplier, status_id, min_flat_power, max_flat_power, effect_level, probability, duration_turns)
VALUES (NULL, NULL, @Strengthened, NULL, NULL, 2, 1.00, 3);

-- Healing Wave: curación porcentual AOE
INSERT INTO Effect (stat_id, stat_multiplier, status_id, min_flat_power, max_flat_power, effect_level, probability, duration_turns)
VALUES (@HP, 1.25, NULL, NULL, NULL, NULL, 1.00, 1);

-- Barrier Shield: buff porcentual de defensa
INSERT INTO Effect (stat_id, stat_multiplier, status_id, min_flat_power, max_flat_power, effect_level, probability, duration_turns)
VALUES (@Defense, 1.30, NULL, NULL, NULL, NULL, 1.00, 2);

-- Revive: restaura HP porcentual
INSERT INTO Effect (stat_id, stat_multiplier, status_id, min_flat_power, max_flat_power, effect_level, probability, duration_turns)
VALUES (@HP, 1.30, NULL, NULL, NULL, NULL, 1.00, 1);

-- Stun Smash: DAÑO FIJO
INSERT INTO Effect (stat_id, stat_multiplier, status_id, min_flat_power, max_flat_power, effect_level, probability, duration_turns)
VALUES (@Attack, NULL, NULL, 16, 22, NULL, 1.00, 0);

-- Stun Smash: aplica Stunned lv1
INSERT INTO Effect (stat_id, stat_multiplier, status_id, min_flat_power, max_flat_power, effect_level, probability, duration_turns)
VALUES (NULL, NULL, @Stunned, NULL, NULL, 1, 0.30, 1);

-- Frost Nova: DAÑO FIJO menor AOE
INSERT INTO Effect (stat_id, stat_multiplier, status_id, min_flat_power, max_flat_power, effect_level, probability, duration_turns)
VALUES (@Attack, NULL, NULL, 10, 14, NULL, 1.00, 0);

-- Frost Nova: aplica Stunned lv1 50%
INSERT INTO Effect (stat_id, stat_multiplier, status_id, min_flat_power, max_flat_power, effect_level, probability, duration_turns)
VALUES (NULL, NULL, @Stunned, NULL, NULL, 1, 0.50, 1);

-- Evasion Mastery (pasiva): buff porcentual de velocidad
INSERT INTO Effect (stat_id, stat_multiplier, status_id, min_flat_power, max_flat_power, effect_level, probability, duration_turns)
VALUES (@Speed, 1.20, NULL, NULL, NULL, NULL, 1.00, 0);

-- Berserker Rage (pasiva): buff porcentual de ataque
INSERT INTO Effect (stat_id, stat_multiplier, status_id, min_flat_power, max_flat_power, effect_level, probability, duration_turns)
VALUES (@Attack, 1.40, NULL, NULL, NULL, NULL, 1.00, 0);

-- -------------------------------------------------------
-- EFECTOS DE ITEMS — HEAD
-- -------------------------------------------------------
INSERT INTO Effect (stat_id, stat_multiplier, status_id, min_flat_power, max_flat_power, effect_level, probability, duration_turns) VALUES
(@Defense, 1.10, NULL, NULL, NULL, 1, 1.0, 0),   -- Iron Helm         +10% def
(@Defense, 1.20, NULL, NULL, NULL, 1, 1.0, 0),   -- Knight Visor      +20% def
(@Speed,   1.15, NULL, NULL, NULL, 1, 1.0, 0),   -- Shadow Hood       +15% spd
(@Energy,  1.25, NULL, NULL, NULL, 1, 1.0, 0),   -- Arcane Crown      +25% energy
(@Speed,   1.10, NULL, NULL, NULL, 1, 1.0, 0),   -- Ranger Cap        +10% spd
(@HP,      1.15, NULL, NULL, NULL, 1, 1.0, 0),   -- Vampire Mask      +15% hp
(@Defense, 1.35, NULL, NULL, NULL, 1, 1.0, 0),   -- Golem Skull Plate +35% def
(NULL,     NULL, NULL, NULL, NULL, 1, 1.0, 0);   -- Blessed Tiara     (se actualiza abajo)

-- Blessed Tiara: +20% speed como proxy de accuracy
UPDATE Effect SET stat_id = @Speed, stat_multiplier = 1.20
WHERE id = LAST_INSERT_ID();

-- -------------------------------------------------------
-- EFECTOS DE ITEMS — CHEST
-- -------------------------------------------------------
INSERT INTO Effect (stat_id, stat_multiplier, status_id, min_flat_power, max_flat_power, effect_level, probability, duration_turns) VALUES
(@Defense, 1.15, NULL, NULL, NULL, 1, 1.0, 0),   -- Iron Chestplate   +15% def
(@Defense, 1.25, NULL, NULL, NULL, 1, 1.0, 0),   -- Battle Armor      +25% def
(@Speed,   1.12, NULL, NULL, NULL, 1, 1.0, 0),   -- Leather Vest      +12% spd
(@Energy,  1.30, NULL, NULL, NULL, 1, 1.0, 0),   -- Mage Robe         +30% energy
(@Speed,   1.18, NULL, NULL, NULL, 1, 1.0, 0),   -- Shadow Cloak      +18% spd
(@HP,      1.20, NULL, NULL, NULL, 1, 1.0, 0),   -- Blessed Vestment  +20% hp
(@Defense, 1.40, NULL, NULL, NULL, 1, 1.0, 0),   -- Golem Shell       +40% def
(@Energy,  1.25, NULL, NULL, NULL, 1, 1.0, 0);   -- Vampiric Coat     +25% energy

-- -------------------------------------------------------
-- EFECTOS DE ITEMS — LOWER
-- -------------------------------------------------------
INSERT INTO Effect (stat_id, stat_multiplier, status_id, min_flat_power, max_flat_power, effect_level, probability, duration_turns) VALUES
(@Defense, 1.08, NULL, NULL, NULL, 1, 1.0, 0),   -- Iron Greaves      +8%  def
(@Defense, 1.18, NULL, NULL, NULL, 1, 1.0, 0),   -- Knight Leggings   +18% def
(@Speed,   1.25, NULL, NULL, NULL, 1, 1.0, 0),   -- Swift Boots       +25% spd
(@Energy,  1.10, NULL, NULL, NULL, 1, 1.0, 0),   -- Mage Sandals      +10% energy
(@Speed,   1.20, NULL, NULL, NULL, 1, 1.0, 0),   -- Shadow Leggings   +20% spd
(@Speed,   1.15, NULL, NULL, NULL, 1, 1.0, 0),   -- Ranger Boots      +15% spd
(@Attack,  1.12, NULL, NULL, NULL, 1, 1.0, 0),   -- Golem Stompers    +12% atk
(@Defense, 1.15, NULL, NULL, NULL, 1, 1.0, 0);   -- Blessed Sandals   +15% def

-- -------------------------------------------------------
-- EFECTOS DE ITEMS — WEAPONS
-- -------------------------------------------------------
INSERT INTO Effect (stat_id, stat_multiplier, status_id, min_flat_power, max_flat_power, effect_level, probability, duration_turns) VALUES
(@Attack, NULL, NULL,  6,    9,    1, 1.0, 0),   -- Short Sword
(@Attack, NULL, NULL, 10,   14,    1, 1.0, 0),   -- Longsword
(@Attack, NULL, NULL,  7,   10,    1, 1.0, 0),   -- Shadow Dagger
(@Attack, 1.18, NULL, NULL, NULL,  1, 1.0, 0),   -- Poison Blade
(@Attack, NULL, NULL, 16,   22,    1, 1.0, 0),   -- War Hammer
(@Energy, 1.35, NULL, NULL, NULL,  1, 1.0, 0),   -- Arcane Staff
(@HP,     1.10, NULL, NULL, NULL,  1, 1.0, 0),   -- Holy Wand
(@Attack, NULL, NULL,  9,   13,    1, 1.0, 0),   -- Longbow
(@Attack, NULL, NULL, 12,   17,    1, 1.0, 0),   -- Crossbow
(@Attack, 1.30, NULL, NULL, NULL,  1, 1.0, 0),   -- Blood Scythe
(@Attack, NULL, NULL, 20,   28,    1, 1.0, 0),   -- Stone Fist Gauntlet
(@Attack, NULL, NULL,  7,   10,    1, 1.0, 0);   -- Twin Blades

-- Poison Blade: Poisoned on-hit 25%
INSERT INTO Effect (stat_id, stat_multiplier, status_id, min_flat_power, max_flat_power, effect_level, probability, duration_turns)
VALUES (NULL, NULL, @Poisoned, NULL, NULL, 1, 0.25, 3);

-- Blood Scythe: Bleeding on-hit 20%
INSERT INTO Effect (stat_id, stat_multiplier, status_id, min_flat_power, max_flat_power, effect_level, probability, duration_turns)
VALUES (NULL, NULL, @Bleeding, NULL, NULL, 1, 0.20, 3);

-- -------------------------------------------------------
-- EFECTOS DE ITEMS — NON-CONSUMABLES
-- -------------------------------------------------------

-- Vampire Ring: Bleeding lv2 on-hit 30%
INSERT INTO Effect (stat_id, stat_multiplier, status_id, min_flat_power, max_flat_power, effect_level, probability, duration_turns)
VALUES (NULL, NULL, @Bleeding, NULL, NULL, 2, 0.30, 3);

-- Poison Amulet: Poisoned lv2 on-hit 30%
INSERT INTO Effect (stat_id, stat_multiplier, status_id, min_flat_power, max_flat_power, effect_level, probability, duration_turns)
VALUES (NULL, NULL, @Poisoned, NULL, NULL, 2, 0.30, 4);

-- Battle Banner: Strengthened lv1 a aliados 2 turnos
INSERT INTO Effect (stat_id, stat_multiplier, status_id, min_flat_power, max_flat_power, effect_level, probability, duration_turns)
VALUES (NULL, NULL, @Strengthened, NULL, NULL, 1, 1.00, 2);

-- Lucky Charm, Stone Totem, Speed Anklet, Accuracy Lens, Crit Gem
INSERT INTO Effect (stat_id, stat_multiplier, status_id, min_flat_power, max_flat_power, effect_level, probability, duration_turns) VALUES
(@Speed,   1.15, NULL, NULL, NULL, 1, 1.0, 0),   -- Lucky Charm   +15% spd
(@Defense, 1.20, NULL, NULL, NULL, 1, 1.0, 0),   -- Stone Totem   +20% def
(@Speed,   1.20, NULL, NULL, NULL, 1, 1.0, 0),   -- Speed Anklet  +20% spd
(@Speed,   1.18, NULL, NULL, NULL, 1, 1.0, 0),   -- Accuracy Lens +18% spd
(@Attack,  1.10, NULL, NULL, NULL, 1, 1.0, 0);   -- Crit Gem      +10% atk

-- -------------------------------------------------------
-- EFECTOS DE ITEMS — CONSUMABLES
-- -------------------------------------------------------
INSERT INTO Effect (stat_id, stat_multiplier, status_id, min_flat_power, max_flat_power, effect_level, probability, duration_turns) VALUES
(@HP,     1.30, NULL,          NULL, NULL, 1, 1.0,  1),  -- Health Potion      +30% HP max
(@Energy, 1.30, NULL,          NULL, NULL, 1, 1.0,  1),  -- Energy Elixir      +30% Energy max
(NULL,    NULL, @Poisoned,     NULL, NULL, 0, 1.0,  0),  -- Antidote           cleanse veneno
(NULL,    NULL, @Strengthened, NULL, NULL, 1, 1.0,  3),  -- Rage Brew          Strengthened lv1
(NULL,    NULL, @Stunned,      NULL, NULL, 1, 0.80, 1),  -- Smoke Bomb         Stunned 80% AOE
(@HP,     1.60, NULL,          NULL, NULL, 1, 1.0,  1),  -- Greater Health Pot +60% HP max
(@Speed,  1.30, NULL,          NULL, NULL, 1, 1.0,  3),  -- Elixir of Speed    +30% spd temporal
(@HP,     1.50, NULL,          NULL, NULL, 1, 1.0,  1);  -- Phoenix Feather    +50% HP al revivir

-- =====================================================
-- 5️⃣ LINK SKILL ↔ EFFECT
-- =====================================================

-- Aquatic Blessing HP
INSERT INTO SkillEffect (skill_id, effect_id)
SELECT s.id, e.id FROM Skill s JOIN Effect e
  ON e.stat_id = @HP AND e.stat_multiplier = 1.30
WHERE s.name = 'Aquatic Blessing';

-- Aquatic Blessing Energy
INSERT INTO SkillEffect (skill_id, effect_id)
SELECT s.id, e.id FROM Skill s JOIN Effect e
  ON e.stat_id = @Energy AND e.stat_multiplier = 1.10
WHERE s.name = 'Aquatic Blessing';

-- Iron Body
INSERT INTO SkillEffect (skill_id, effect_id)
SELECT s.id, e.id FROM Skill s JOIN Effect e
  ON e.stat_id = @Defense AND e.stat_multiplier = 1.20
WHERE s.name = 'Iron Body';

-- Blood Rain daño (fijo: stat_multiplier IS NULL, min=18)
INSERT INTO SkillEffect (skill_id, effect_id)
SELECT s.id, e.id FROM Skill s JOIN Effect e
  ON e.stat_id = @Attack AND e.stat_multiplier IS NULL AND e.min_flat_power = 18 AND e.duration_turns = 0
WHERE s.name = 'Blood Rain';

-- Blood Rain Bleeding
INSERT INTO SkillEffect (skill_id, effect_id)
SELECT s.id, e.id FROM Skill s JOIN Effect e
  ON e.status_id = @Bleeding AND e.probability = 0.15
WHERE s.name = 'Blood Rain';

-- Shadow Strike (fijo + multiplicador: min=10)
INSERT INTO SkillEffect (skill_id, effect_id)
SELECT s.id, e.id FROM Skill s JOIN Effect e
  ON e.stat_id = @Attack AND e.stat_multiplier = 1.15 AND e.min_flat_power = 10 AND e.duration_turns = 0
WHERE s.name = 'Shadow Strike';

-- Meteor Crash (fijo: min=22)
INSERT INTO SkillEffect (skill_id, effect_id)
SELECT s.id, e.id FROM Skill s JOIN Effect e
  ON e.stat_id = @Attack AND e.stat_multiplier IS NULL AND e.min_flat_power = 22 AND e.duration_turns = 0
WHERE s.name = 'Meteor Crash';

-- Poison Dart
INSERT INTO SkillEffect (skill_id, effect_id)
SELECT s.id, e.id FROM Skill s JOIN Effect e
  ON e.status_id = @Poisoned AND e.probability = 1.00 AND e.duration_turns = 4
WHERE s.name = 'Poison Dart';

-- Twin Slash (fijo por hit: min=8)
INSERT INTO SkillEffect (skill_id, effect_id)
SELECT s.id, e.id FROM Skill s JOIN Effect e
  ON e.stat_id = @Attack AND e.stat_multiplier IS NULL AND e.min_flat_power = 8 AND e.duration_turns = 0
WHERE s.name = 'Twin Slash';

-- Soul Drain daño (fijo: min=14)
INSERT INTO SkillEffect (skill_id, effect_id)
SELECT s.id, e.id FROM Skill s JOIN Effect e
  ON e.stat_id = @Attack AND e.stat_multiplier IS NULL AND e.min_flat_power = 14 AND e.duration_turns = 0
WHERE s.name = 'Soul Drain';

-- Soul Drain curación
INSERT INTO SkillEffect (skill_id, effect_id)
SELECT s.id, e.id FROM Skill s JOIN Effect e
  ON e.stat_id = @HP AND e.stat_multiplier = 1.50 AND e.duration_turns = 1
WHERE s.name = 'Soul Drain';

-- Battle Cry
INSERT INTO SkillEffect (skill_id, effect_id)
SELECT s.id, e.id FROM Skill s JOIN Effect e
  ON e.status_id = @Strengthened AND e.effect_level = 2 AND e.duration_turns = 3
WHERE s.name = 'Battle Cry';

-- Healing Wave
INSERT INTO SkillEffect (skill_id, effect_id)
SELECT s.id, e.id FROM Skill s JOIN Effect e
  ON e.stat_id = @HP AND e.stat_multiplier = 1.25 AND e.duration_turns = 1
WHERE s.name = 'Healing Wave';

-- Barrier Shield
INSERT INTO SkillEffect (skill_id, effect_id)
SELECT s.id, e.id FROM Skill s JOIN Effect e
  ON e.stat_id = @Defense AND e.stat_multiplier = 1.30 AND e.duration_turns = 2
WHERE s.name = 'Barrier Shield';

-- Revive
INSERT INTO SkillEffect (skill_id, effect_id)
SELECT s.id, e.id FROM Skill s JOIN Effect e
  ON e.stat_id = @HP AND e.stat_multiplier = 1.30 AND e.duration_turns = 1
WHERE s.name = 'Revive';

-- Stun Smash daño (fijo: min=16)
INSERT INTO SkillEffect (skill_id, effect_id)
SELECT s.id, e.id FROM Skill s JOIN Effect e
  ON e.stat_id = @Attack AND e.stat_multiplier IS NULL AND e.min_flat_power = 16 AND e.duration_turns = 0
WHERE s.name = 'Stun Smash';

-- Stun Smash Stunned
INSERT INTO SkillEffect (skill_id, effect_id)
SELECT s.id, e.id FROM Skill s JOIN Effect e
  ON e.status_id = @Stunned AND e.probability = 0.30
WHERE s.name = 'Stun Smash';

-- Frost Nova daño (fijo: min=10)
INSERT INTO SkillEffect (skill_id, effect_id)
SELECT s.id, e.id FROM Skill s JOIN Effect e
  ON e.stat_id = @Attack AND e.stat_multiplier IS NULL AND e.min_flat_power = 10 AND e.duration_turns = 0
WHERE s.name = 'Frost Nova';

-- Frost Nova Stunned
INSERT INTO SkillEffect (skill_id, effect_id)
SELECT s.id, e.id FROM Skill s JOIN Effect e
  ON e.status_id = @Stunned AND e.probability = 0.50
WHERE s.name = 'Frost Nova';

-- Evasion Mastery
INSERT INTO SkillEffect (skill_id, effect_id)
SELECT s.id, e.id FROM Skill s JOIN Effect e
  ON e.stat_id = @Speed AND e.stat_multiplier = 1.20 AND e.duration_turns = 0
WHERE s.name = 'Evasion Mastery';

-- Berserker Rage
INSERT INTO SkillEffect (skill_id, effect_id)
SELECT s.id, e.id FROM Skill s JOIN Effect e
  ON e.stat_id = @Attack AND e.stat_multiplier = 1.40 AND e.duration_turns = 0
WHERE s.name = 'Berserker Rage';

-- =====================================================
-- 6️⃣ PLAYERS
-- =====================================================
INSERT INTO Entity
(name, level, hp, hp_max, energy, energy_max, attack, defense, speed,
 crit_chance, crit_damage, accuracy_multiplier,
 image_thumb, image_full, description)
VALUES
('Hero Knight',     1, 90,  90,  100, 100, 25, 40, 20, 0.05, 1.5, 1.0, 'https://www.sprite-ai.art/sprite-img/950983fc-6115-417d-b306-c75e4f8ebb1b/retro-16-bit-pixel-hero-sprite-courageous-knight-clad-128x128-pixel-art-fbb5.png',   'https://www.sprite-ai.art/sprite-img/950983fc-6115-417d-b306-c75e4f8ebb1b/retro-16-bit-pixel-hero-sprite-courageous-knight-clad-128x128-pixel-art-fbb5.png',   'Sturdy knight.'),
('Ocean Priestess', 1, 70,  70,  100, 100, 20, 25, 35, 0.05, 1.5, 1.0, 'https://static.tildacdn.com/tild3635-6434-4965-b664-386236613036/Witch_water_tr.png', 'https://static.tildacdn.com/tild3635-6434-4965-b664-386236613036/Witch_water_tr.png', 'Support priestess.'),
('Arcane Mage',     1, 40,  40,  100, 100, 40, 20, 30, 0.05, 1.5, 1.0, 'https://www.sprite-ai.art/sprite-img/be2d0a60-2eda-484c-b3aa-438390725ca3/pixel-art-mage-avec-sceptre-cape-bleu-16x16-64x64-pixel-art-b52e.png', 'https://www.sprite-ai.art/sprite-img/be2d0a60-2eda-484c-b3aa-438390725ca3/pixel-art-mage-avec-sceptre-cape-bleu-16x16-64x64-pixel-art-b52e.png',   'Versatile mage.'),
('Forest Ranger',   1, 60,  60,  100, 100, 35, 20, 40, 0.05, 1.5, 1.0, 'https://www.sprite-ai.art/sprite-img/437e4b8c-80d2-4cd9-a5d5-709ce23db090/a-skilled-bowman-wearing-a-green-tunic-aiming-32x32-pixel-art-a2e9.png', 'https://www.sprite-ai.art/sprite-img/437e4b8c-80d2-4cd9-a5d5-709ce23db090/a-skilled-bowman-wearing-a-green-tunic-aiming-32x32-pixel-art-a2e9.png', 'Forest dweller.');

INSERT INTO Player (entity_id, xp_points, skill_points)
SELECT id, 0, 3 FROM Entity
WHERE name IN ('Hero Knight', 'Ocean Priestess', 'Arcane Mage', 'Forest Ranger');

-- =====================================================
-- 7️⃣ ENEMIES
-- =====================================================
INSERT INTO Entity
(name, level, hp, hp_max, energy, energy_max, attack, defense, speed,
 crit_chance, crit_damage, accuracy_multiplier,
 image_thumb, image_full, description)
VALUES
('Goblin Berserker', 2,  40,  40,  100, 100, 35, 15, 25, 0.05, 1.5, 1.0, 'https://www.sprite-ai.art/sprite-img/c9d7071f-a616-46f2-9638-bf3099096ea5/pixel-art-for-a-tcg-in-a-jrpg-124x124-pixel-art-9879.png',   'https://www.sprite-ai.art/sprite-img/c9d7071f-a616-46f2-9638-bf3099096ea5/pixel-art-for-a-tcg-in-a-jrpg-124x124-pixel-art-9879.png',   'Strong but fragile.'),
('Stone Golem',      3, 100, 100,  100, 100, 15, 30, 15, 0.05, 1.5, 1.0, 'https://art.pixilart.com/5b83fa17af18340.png',    'https://art.pixilart.com/5b83fa17af18340.png',    'Walking Fortress, almost no offensive capabilities.'),
('Vampire Lord',     5,  70,  70,  100, 100, 35, 25, 25, 0.05, 1.5, 1.0, 'https://www.sprite-ai.art/sprite-img/e463b247-96f9-4015-82b6-17bb10d065aa/vampire-with-cloak-visible-fangs-claws-64x64-pixel-art-e27d.png',  'https://www.sprite-ai.art/sprite-img/e463b247-96f9-4015-82b6-17bb10d065aa/vampire-with-cloak-visible-fangs-claws-64x64-pixel-art-e27d.png',  'Expert in blood manipulation, draining his opponents blood heals him.'),
('Dark Assassin',    4,  50,  50,  100, 100, 40, 15, 40, 0.15, 1.7, 1.0, 'https://www.sprite-ai.art/sprite-img/ea2516b3-4417-4a37-83e9-c8758669c3a9/medival-assain-16x16-piels-with-2-daggers-with-64x64-pixel-art-4fe1.png', 'https://www.sprite-ai.art/sprite-img/ea2516b3-4417-4a37-83e9-c8758669c3a9/medival-assain-16x16-piels-with-2-daggers-with-64x64-pixel-art-4fe1.png', 'Speedy sneaky assassin.');

-- (El trigger trg_enemy_create_loot_table crea la LootTable automáticamente)
INSERT INTO Enemy (entity_id, passive_id)
SELECT id, 0 FROM Entity
WHERE name IN ('Goblin Berserker', 'Stone Golem', 'Vampire Lord', 'Dark Assassin');

-- =====================================================
-- 8️⃣ ASIGNAR SKILLS A PERSONAJES
-- =====================================================

INSERT INTO EntitySkill (entity_id, skill_id)
SELECT e.id, s.id FROM Entity e, Skill s
WHERE e.name = 'Hero Knight'
  AND s.name IN ('Iron Body', 'Stun Smash', 'Battle Cry', 'Berserker Rage');

INSERT INTO EntitySkill (entity_id, skill_id)
SELECT e.id, s.id FROM Entity e, Skill s
WHERE e.name = 'Ocean Priestess'
  AND s.name IN ('Aquatic Blessing', 'Healing Wave', 'Barrier Shield', 'Revive');

INSERT INTO EntitySkill (entity_id, skill_id)
SELECT e.id, s.id FROM Entity e, Skill s
WHERE e.name = 'Arcane Mage'
  AND s.name IN ('Meteor Crash', 'Frost Nova', 'Poison Dart', 'Evasion Mastery');

INSERT INTO EntitySkill (entity_id, skill_id)
SELECT e.id, s.id FROM Entity e, Skill s
WHERE e.name = 'Forest Ranger'
  AND s.name IN ('Twin Slash', 'Shadow Strike', 'Evasion Mastery');

INSERT INTO EntitySkill (entity_id, skill_id)
SELECT e.id, s.id FROM Entity e, Skill s
WHERE e.name = 'Goblin Berserker'
  AND s.name IN ('Twin Slash', 'Berserker Rage');

INSERT INTO EntitySkill (entity_id, skill_id)
SELECT e.id, s.id FROM Entity e, Skill s
WHERE e.name = 'Stone Golem'
  AND s.name IN ('Iron Body', 'Stun Smash', 'Barrier Shield');

INSERT INTO EntitySkill (entity_id, skill_id)
SELECT e.id, s.id FROM Entity e, Skill s
WHERE e.name = 'Vampire Lord'
  AND s.name IN ('Blood Rain', 'Soul Drain', 'Battle Cry');

INSERT INTO EntitySkill (entity_id, skill_id)
SELECT e.id, s.id FROM Entity e, Skill s
WHERE e.name = 'Dark Assassin'
  AND s.name IN ('Shadow Strike', 'Twin Slash', 'Poison Dart', 'Evasion Mastery');

-- =====================================================
-- 9️⃣ ITEMS
-- =====================================================

-- ---- HEAD (8) ----
INSERT INTO Item (name, description, quality, consumable, max_uses, image_thumb, target_type, is_aoe) VALUES
('Iron Helm',         'Basic iron helmet.',                         'COMMON',   false, NULL, '/img/iron_helm.png',    'SELF', false),
('Knight Visor',      'Knight visor with high defense.',            'UNCOMMON', false, NULL, '/img/knight_visor.png', 'SELF', false),
('Shadow Hood',       'Dark hood that increases speed.',            'UNCOMMON', false, NULL, '/img/shadow_hood.png',  'SELF', false),
('Arcane Crown',      'Magical crown that boosts energy.',          'RARE',     false, NULL, '/img/arcane_crown.png', 'SELF', false),
('Ranger Cap',        'Light cap for archers.',                      'COMMON',   false, NULL, '/img/ranger_cap.png',   'SELF', false),
('Vampire Mask',      'Mask that grants dark regeneration.',         'RARE',     false, NULL, '/img/vamp_mask.png',    'SELF', false),
('Golem Skull Plate', 'Stone plate from a defeated golem.',         'EPIC',     false, NULL, '/img/golem_skull.png',  'SELF', false),
('Blessed Tiara',     'Sacred tiara that increases accuracy.',      'RARE',     false, NULL, '/img/tiara.png',        'SELF', false);

-- ---- CHEST (8) ----
INSERT INTO Item (name, description, quality, consumable, max_uses, image_thumb, target_type, is_aoe) VALUES
('Iron Chestplate',  'Standard iron chestplate.',                  'COMMON',   false, NULL, '/img/iron_chest.png',   'SELF', false),
('Battle Armor',     'Reinforced battle armor.',                   'UNCOMMON', false, NULL, '/img/battle_armor.png', 'SELF', false),
('Leather Vest',     'Light vest for agility.',                     'COMMON',   false, NULL, '/img/leather_vest.png', 'SELF', false),
('Mage Robe',        'Magical robe that amplifies energy.',        'UNCOMMON', false, NULL, '/img/mage_robe.png',    'SELF', false),
('Shadow Cloak',     'Dark cloak that improves evasion.',          'RARE',     false, NULL, '/img/shadow_cloak.png', 'SELF', false),
('Blessed Vestment', 'Sacred garment with healing aura.',           'RARE',     false, NULL, '/img/vestment.png',     'SELF', false),
('Golem Shell',      'Magical stone cuirass.',                      'EPIC',     false, NULL, '/img/golem_shell.png',  'SELF', false),
('Vampiric Coat',    'Dark coat that drains energy.',               'EPIC',     false, NULL, '/img/vamp_coat.png',    'SELF', false);

-- ---- LOWER (8) ----
INSERT INTO Item (name, description, quality, consumable, max_uses, image_thumb, target_type, is_aoe) VALUES
('Iron Greaves',    'Basic iron greaves.',                          'COMMON',   false, NULL, '/img/iron_greaves.png',    'SELF', false),
('Knight Leggings', 'Sturdy knight leggings.',                       'UNCOMMON', false, NULL, '/img/knight_legs.png',     'SELF', false),
('Swift Boots',     'Light boots for high speed.',                  'UNCOMMON', false, NULL, '/img/swift_boots.png',     'SELF', false),
('Mage Sandals',    'Sandals that channel magic.',                  'COMMON',   false, NULL, '/img/mage_sandals.png',    'SELF', false),
('Shadow Leggings', 'Stealth leggings.',                             'RARE',     false, NULL, '/img/shadow_legs.png',     'SELF', false),
('Ranger Boots',    'Explorer boots for rough terrain.',            'UNCOMMON', false, NULL, '/img/ranger_boots.png',    'SELF', false),
('Golem Stompers',  'Stone hooves with seismic impact.',            'EPIC',     false, NULL, '/img/golem_stomp.png',     'SELF', false),
('Blessed Sandals', 'Sacred sandals that improve critical chance.', 'RARE',     false, NULL, '/img/blessed_sandals.png', 'SELF', false);

-- ---- WEAPONS (12) ----
INSERT INTO Item (name, description, quality, consumable, max_uses, image_thumb, target_type, is_aoe) VALUES
('Short Sword',         'Starter short sword.',                       'COMMON',   false, NULL, '/img/short_sword.png',  'SELF', false),
('Longsword',           'Knights long sword.',                        'UNCOMMON', false, NULL, '/img/longsword.png',    'SELF', false),
('Shadow Dagger',       'Assassins fast dagger.',                     'UNCOMMON', false, NULL, '/img/shadow_dagger.png','SELF', false),
('Poison Blade',        'Dagger coated with poison.',                  'RARE',     false, NULL, '/img/poison_blade.png', 'SELF', false),
('War Hammer',          'Heavy war hammer.',                            'UNCOMMON', false, NULL, '/img/war_hammer.png',   'SELF', false),
('Arcane Staff',        'Staff that amplifies spells.',                'RARE',     false, NULL, '/img/arcane_staff.png', 'SELF', false),
('Holy Wand',           'Holy wand for healers.',                       'RARE',     false, NULL, '/img/holy_wand.png',    'SELF', false),
('Longbow',             'High-precision longbow.',                      'UNCOMMON', false, NULL, '/img/longbow.png',      'SELF', false),
('Crossbow',            'Compact and powerful crossbow.',               'RARE',     false, NULL, '/img/crossbow.png',     'SELF', false),
('Blood Scythe',        'Scythe that absorbs enemy life.',              'EPIC',     false, NULL, '/img/blood_scythe.png', 'SELF', false),
('Stone Fist Gauntlet', 'Stone gauntlet with tremendous impact.',       'EPIC',     false, NULL, '/img/stone_fist.png',   'SELF', false),
('Twin Blades',         'Pair of daggers for double attacks.',         'RARE',     false, NULL, '/img/twin_blades.png',  'SELF', false);

-- ---- CONSUMABLES (8) ----
INSERT INTO Item (name, description, quality, consumable, max_uses, image_thumb, target_type, is_aoe) VALUES
('Health Potion',      'Restores 30% HP.',                             'COMMON',   true, 1, 'https://art.pixilart.com/cb38cacb9cd5e61.png',    'SELF',  false),
('Energy Elixir',      'Restores 30% energy.',                         'COMMON',   true, 1, 'https://dinopixel.com/preload/0223/Energy-Potion.png', 'SELF',  false),
('Antidote',           'Removes the Poisoned status.',                 'COMMON',   true, 1, '/img/antidote.png',      'SELF',  false),
('Rage Brew',          'Applies Strengthened lv1 to user.',            'UNCOMMON', true, 1, '/img/rage_brew.png',     'SELF',  false),
('Smoke Bomb',         'Stuns all enemies for 1 turn.',               'UNCOMMON', true, 1, '/img/smoke_bomb.png',    'ENEMY', true),
('Greater Health Pot', 'Restores 60% HP.',                             'RARE',     true, 1, '/img/greater_hp.png',    'SELF',  false),
('Elixir of Speed',    'Increases speed by 30% for 3 turns.',         'UNCOMMON', true, 1, '/img/speed_elixir.png',  'SELF',  false),
('Phoenix Feather',    'Revives user with 50% HP.',                   'EPIC',     true, 1, '/img/phoenix.png',       'SELF',  false);

-- ---- NON-CONSUMABLES (8) ----
INSERT INTO Item (name, description, quality, consumable, max_uses, image_thumb, target_type, is_aoe) VALUES
('Lucky Charm',   'Amulet that increases crit chance.',          'UNCOMMON', false, NULL, '/img/lucky_charm.png',   'SELF',  false),
('Stone Totem',   'Totem that passively improves defense.',      'UNCOMMON', false, NULL, '/img/stone_totem.png',   'SELF',  false),
('Vampire Ring',  'Ring that applies Bleeding on attack.',      'RARE',     false, NULL, '/img/vamp_ring.png',     'ENEMY', false),
('Poison Amulet', 'Amulet that applies Poisoned on attack.',     'RARE',     false, NULL, '/img/poison_amulet.png', 'ENEMY', false),
('Battle Banner', 'Banner that grants Strengthened to allies.', 'RARE',     false, NULL, '/img/banner.png',        'ALLY',  true),
('Speed Anklet',  'Anklet that increases speed.',               'UNCOMMON', false, NULL, '/img/anklet.png',        'SELF',  false),
('Accuracy Lens', 'Lens that improves accuracy.',               'UNCOMMON', false, NULL, '/img/lens.png',          'SELF',  false),
('Crit Gem',      'Gem that improves critical damage.',         'RARE',     false, NULL, '/img/crit_gem.png',      'SELF',  false);

-- =====================================================
-- 1️⃣1️⃣ LOOT ENTRIES
-- =====================================================

-- Goblin Berserker
INSERT INTO LootEntry (loot_table_id, item_id, drop_chance, min_quality, max_quality)
SELECT lt.id, i.id, 0.80, 'COMMON', 'COMMON' FROM LootTable lt JOIN Enemy en ON lt.enemy_id = en.entity_id JOIN Entity e ON en.entity_id = e.id JOIN Item i ON i.name = 'Health Potion' WHERE e.name = 'Goblin Berserker';
INSERT INTO LootEntry (loot_table_id, item_id, drop_chance, min_quality, max_quality)
SELECT lt.id, i.id, 0.60, 'COMMON', 'COMMON' FROM LootTable lt JOIN Enemy en ON lt.enemy_id = en.entity_id JOIN Entity e ON en.entity_id = e.id JOIN Item i ON i.name = 'Short Sword' WHERE e.name = 'Goblin Berserker';
INSERT INTO LootEntry (loot_table_id, item_id, drop_chance, min_quality, max_quality)
SELECT lt.id, i.id, 0.40, 'COMMON', 'UNCOMMON' FROM LootTable lt JOIN Enemy en ON lt.enemy_id = en.entity_id JOIN Entity e ON en.entity_id = e.id JOIN Item i ON i.name = 'Shadow Dagger' WHERE e.name = 'Goblin Berserker';
INSERT INTO LootEntry (loot_table_id, item_id, drop_chance, min_quality, max_quality)
SELECT lt.id, i.id, 0.30, 'COMMON', 'UNCOMMON' FROM LootTable lt JOIN Enemy en ON lt.enemy_id = en.entity_id JOIN Entity e ON en.entity_id = e.id JOIN Item i ON i.name = 'Iron Helm' WHERE e.name = 'Goblin Berserker';
INSERT INTO LootEntry (loot_table_id, item_id, drop_chance, min_quality, max_quality)
SELECT lt.id, i.id, 0.20, 'UNCOMMON', 'UNCOMMON' FROM LootTable lt JOIN Enemy en ON lt.enemy_id = en.entity_id JOIN Entity e ON en.entity_id = e.id JOIN Item i ON i.name = 'Rage Brew' WHERE e.name = 'Goblin Berserker';

-- Stone Golem
INSERT INTO LootEntry (loot_table_id, item_id, drop_chance, min_quality, max_quality)
SELECT lt.id, i.id, 0.85, 'COMMON', 'UNCOMMON' FROM LootTable lt JOIN Enemy en ON lt.enemy_id = en.entity_id JOIN Entity e ON en.entity_id = e.id JOIN Item i ON i.name = 'Iron Chestplate' WHERE e.name = 'Stone Golem';
INSERT INTO LootEntry (loot_table_id, item_id, drop_chance, min_quality, max_quality)
SELECT lt.id, i.id, 0.70, 'UNCOMMON', 'RARE' FROM LootTable lt JOIN Enemy en ON lt.enemy_id = en.entity_id JOIN Entity e ON en.entity_id = e.id JOIN Item i ON i.name = 'Stone Totem' WHERE e.name = 'Stone Golem';
INSERT INTO LootEntry (loot_table_id, item_id, drop_chance, min_quality, max_quality)
SELECT lt.id, i.id, 0.55, 'UNCOMMON', 'RARE' FROM LootTable lt JOIN Enemy en ON lt.enemy_id = en.entity_id JOIN Entity e ON en.entity_id = e.id JOIN Item i ON i.name = 'War Hammer' WHERE e.name = 'Stone Golem';
INSERT INTO LootEntry (loot_table_id, item_id, drop_chance, min_quality, max_quality)
SELECT lt.id, i.id, 0.40, 'RARE', 'EPIC' FROM LootTable lt JOIN Enemy en ON lt.enemy_id = en.entity_id JOIN Entity e ON en.entity_id = e.id JOIN Item i ON i.name = 'Golem Shell' WHERE e.name = 'Stone Golem';
INSERT INTO LootEntry (loot_table_id, item_id, drop_chance, min_quality, max_quality)
SELECT lt.id, i.id, 0.15, 'EPIC', 'EPIC' FROM LootTable lt JOIN Enemy en ON lt.enemy_id = en.entity_id JOIN Entity e ON en.entity_id = e.id JOIN Item i ON i.name = 'Stone Fist Gauntlet' WHERE e.name = 'Stone Golem';

-- Vampire Lord
INSERT INTO LootEntry (loot_table_id, item_id, drop_chance, min_quality, max_quality)
SELECT lt.id, i.id, 0.90, 'UNCOMMON', 'RARE' FROM LootTable lt JOIN Enemy en ON lt.enemy_id = en.entity_id JOIN Entity e ON en.entity_id = e.id JOIN Item i ON i.name = 'Vampire Ring' WHERE e.name = 'Vampire Lord';
INSERT INTO LootEntry (loot_table_id, item_id, drop_chance, min_quality, max_quality)
SELECT lt.id, i.id, 0.70, 'RARE', 'EPIC' FROM LootTable lt JOIN Enemy en ON lt.enemy_id = en.entity_id JOIN Entity e ON en.entity_id = e.id JOIN Item i ON i.name = 'Blood Scythe' WHERE e.name = 'Vampire Lord';
INSERT INTO LootEntry (loot_table_id, item_id, drop_chance, min_quality, max_quality)
SELECT lt.id, i.id, 0.50, 'RARE', 'EPIC' FROM LootTable lt JOIN Enemy en ON lt.enemy_id = en.entity_id JOIN Entity e ON en.entity_id = e.id JOIN Item i ON i.name = 'Vampiric Coat' WHERE e.name = 'Vampire Lord';
INSERT INTO LootEntry (loot_table_id, item_id, drop_chance, min_quality, max_quality)
SELECT lt.id, i.id, 0.35, 'RARE', 'EPIC' FROM LootTable lt JOIN Enemy en ON lt.enemy_id = en.entity_id JOIN Entity e ON en.entity_id = e.id JOIN Item i ON i.name = 'Vampire Mask' WHERE e.name = 'Vampire Lord';
INSERT INTO LootEntry (loot_table_id, item_id, drop_chance, min_quality, max_quality)
SELECT lt.id, i.id, 0.20, 'EPIC', 'EPIC' FROM LootTable lt JOIN Enemy en ON lt.enemy_id = en.entity_id JOIN Entity e ON en.entity_id = e.id JOIN Item i ON i.name = 'Phoenix Feather' WHERE e.name = 'Vampire Lord';
INSERT INTO LootEntry (loot_table_id, item_id, drop_chance, min_quality, max_quality)
SELECT lt.id, i.id, 0.60, 'COMMON', 'UNCOMMON' FROM LootTable lt JOIN Enemy en ON lt.enemy_id = en.entity_id JOIN Entity e ON en.entity_id = e.id JOIN Item i ON i.name = 'Greater Health Pot' WHERE e.name = 'Vampire Lord';

-- Dark Assassin
INSERT INTO LootEntry (loot_table_id, item_id, drop_chance, min_quality, max_quality)
SELECT lt.id, i.id, 0.80, 'UNCOMMON', 'RARE' FROM LootTable lt JOIN Enemy en ON lt.enemy_id = en.entity_id JOIN Entity e ON en.entity_id = e.id JOIN Item i ON i.name = 'Poison Blade' WHERE e.name = 'Dark Assassin';
INSERT INTO LootEntry (loot_table_id, item_id, drop_chance, min_quality, max_quality)
SELECT lt.id, i.id, 0.65, 'UNCOMMON', 'RARE' FROM LootTable lt JOIN Enemy en ON lt.enemy_id = en.entity_id JOIN Entity e ON en.entity_id = e.id JOIN Item i ON i.name = 'Shadow Cloak' WHERE e.name = 'Dark Assassin';
INSERT INTO LootEntry (loot_table_id, item_id, drop_chance, min_quality, max_quality)
SELECT lt.id, i.id, 0.50, 'UNCOMMON', 'RARE' FROM LootTable lt JOIN Enemy en ON lt.enemy_id = en.entity_id JOIN Entity e ON en.entity_id = e.id JOIN Item i ON i.name = 'Twin Blades' WHERE e.name = 'Dark Assassin';
INSERT INTO LootEntry (loot_table_id, item_id, drop_chance, min_quality, max_quality)
SELECT lt.id, i.id, 0.40, 'UNCOMMON', 'RARE' FROM LootTable lt JOIN Enemy en ON lt.enemy_id = en.entity_id JOIN Entity e ON en.entity_id = e.id JOIN Item i ON i.name = 'Shadow Hood' WHERE e.name = 'Dark Assassin';
INSERT INTO LootEntry (loot_table_id, item_id, drop_chance, min_quality, max_quality)
SELECT lt.id, i.id, 0.30, 'RARE', 'RARE' FROM LootTable lt JOIN Enemy en ON lt.enemy_id = en.entity_id JOIN Entity e ON en.entity_id = e.id JOIN Item i ON i.name = 'Poison Amulet' WHERE e.name = 'Dark Assassin';
INSERT INTO LootEntry (loot_table_id, item_id, drop_chance, min_quality, max_quality)
SELECT lt.id, i.id, 0.55, 'COMMON', 'UNCOMMON' FROM LootTable lt JOIN Enemy en ON lt.enemy_id = en.entity_id JOIN Entity e ON en.entity_id = e.id JOIN Item i ON i.name = 'Antidote' WHERE e.name = 'Dark Assassin';