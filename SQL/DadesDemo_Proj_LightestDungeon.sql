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
('Berserker Rage',   'When HP drops below 30%, automatically increases attack.',       0,  1.00, 0, 'SELF',  false, true,  'https://ih1.redbubble.net/image.5516888541.0428/st,extra_large,507x507-pad,600x600,f8f8f8.jpg'),
('Destroy',   'Te revienta', 0,  1.00, 2, 'ENEMY',  true, false,  'https://cdn-icons-png.flaticon.com/512/5663/5663908.png');

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

-- Revive: restaura HP porcentual (duration_turns=0 → instantáneo, para distinguirlo del HP de Aquatic Blessing)
INSERT INTO Effect (stat_id, stat_multiplier, status_id, min_flat_power, max_flat_power, effect_level, probability, duration_turns)
VALUES (@HP, 1.30, NULL, NULL, NULL, NULL, 1.00, 0);

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

-- Destroy
INSERT INTO Effect (stat_id, stat_multiplier, status_id, min_flat_power, max_flat_power, effect_level, probability, duration_turns)
VALUES (@Attack, NULL, NULL, 100, 100, NULL, 1.00, 0);

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
(@Speed,  1.30, NULL,          NULL, NULL, 1, 1.0,  3);  -- Elixir of Speed    +30% spd temporal

-- =====================================================
-- 5️⃣ LINK SKILL ↔ EFFECT
-- =====================================================

-- Aquatic Blessing HP
INSERT INTO SkillEffect (skill_id, effect_id)
SELECT s.id, e.id FROM Skill s JOIN Effect e
  ON e.stat_id = @HP AND e.stat_multiplier = 1.30
     AND e.effect_level IS NULL AND e.duration_turns = 1
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
  ON e.stat_id = @HP AND e.stat_multiplier = 1.50
     AND e.duration_turns = 1 AND e.effect_level IS NULL
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
  ON e.stat_id = @HP AND e.stat_multiplier = 1.30
     AND e.effect_level IS NULL AND e.duration_turns = 0
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

-- Destroy
INSERT INTO SkillEffect (skill_id, effect_id)
SELECT s.id, e.id FROM Skill s JOIN Effect e
  ON e.stat_id = @Attack AND e.stat_multiplier IS NULL AND e.duration_turns = 0 AND e.min_flat_power = 100 AND e.max_flat_power = 100
WHERE s.name = 'Destroy';

-- =====================================================
-- 5️⃣b LINK ITEM ↔ EFFECT
-- =====================================================

-- Health Potion: +30% HP (instantáneo) — distinguible por effect_level=1
INSERT INTO ItemEffect (item_id, effect_id)
SELECT i.id, e.id FROM Item i JOIN Effect e
  ON e.stat_id = @HP AND e.stat_multiplier = 1.30
     AND e.effect_level = 1 AND e.duration_turns = 1
WHERE i.name = 'Health Potion';

-- Energy Elixir: +30% Energy
INSERT INTO ItemEffect (item_id, effect_id)
SELECT i.id, e.id FROM Item i JOIN Effect e
  ON e.stat_id = @Energy AND e.stat_multiplier = 1.30
     AND e.effect_level = 1 AND e.duration_turns = 1
WHERE i.name = 'Energy Elixir';

-- Antidote: limpia Poisoned (effect_level=0 = cleanse)
INSERT INTO ItemEffect (item_id, effect_id)
SELECT i.id, e.id FROM Item i JOIN Effect e
  ON e.status_id = @Poisoned AND e.effect_level = 0 AND e.duration_turns = 0
WHERE i.name = 'Antidote';

-- Rage Brew: Strengthened lv1
INSERT INTO ItemEffect (item_id, effect_id)
SELECT i.id, e.id FROM Item i JOIN Effect e
  ON e.status_id = @Strengthened AND e.effect_level = 1 AND e.duration_turns = 3
WHERE i.name = 'Rage Brew';

-- Smoke Bomb: Stunned 80% AOE
INSERT INTO ItemEffect (item_id, effect_id)
SELECT i.id, e.id FROM Item i JOIN Effect e
  ON e.status_id = @Stunned AND e.probability = 0.80 AND e.duration_turns = 1
WHERE i.name = 'Smoke Bomb';

-- Greater Health Pot: +60% HP
INSERT INTO ItemEffect (item_id, effect_id)
SELECT i.id, e.id FROM Item i JOIN Effect e
  ON e.stat_id = @HP AND e.stat_multiplier = 1.60
     AND e.effect_level = 1 AND e.duration_turns = 1
WHERE i.name = 'Greater Health Pot';

-- Elixir of Speed: +30% Speed durante 3 turnos
INSERT INTO ItemEffect (item_id, effect_id)
SELECT i.id, e.id FROM Item i JOIN Effect e
  ON e.stat_id = @Speed AND e.stat_multiplier = 1.30
     AND e.effect_level = 1 AND e.duration_turns = 3
WHERE i.name = 'Elixir of Speed';

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
('Dark Assassin',    4,  50,  50,  100, 100, 40, 15, 40, 0.15, 1.7, 1.0, 'https://www.sprite-ai.art/sprite-img/ea2516b3-4417-4a37-83e9-c8758669c3a9/medival-assain-16x16-piels-with-2-daggers-with-64x64-pixel-art-4fe1.png', 'https://www.sprite-ai.art/sprite-img/ea2516b3-4417-4a37-83e9-c8758669c3a9/medival-assain-16x16-piels-with-2-daggers-with-64x64-pixel-art-4fe1.png', 'Speedy sneaky assassin.'),
('Brainrot Kid',    6,  70,  70,  100, 100, 40, 15, 40, 0.15, 1.7, 1.0, 'https://www.sprite-ai.art/sprite-img/ea2516b3-4417-4a37-83e9-c8758669c3a9/medival-assain-16x16-piels-with-2-daggers-with-64x64-pixel-art-4fe1.png', 'https://www.sprite-ai.art/sprite-img/ea2516b3-4417-4a37-83e9-c8758669c3a9/medival-assain-16x16-piels-with-2-daggers-with-64x64-pixel-art-4fe1.png', 'Brainrot Kid.');

-- (El trigger trg_enemy_create_loot_table crea la LootTable automáticamente)
INSERT INTO Enemy (entity_id, passive_id)
SELECT id, 0 FROM Entity
WHERE name IN ('Goblin Berserker', 'Stone Golem', 'Vampire Lord', 'Dark Assassin', 'Brainrot Kid');

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
  AND s.name IN ('Meteor Crash', 'Frost Nova', 'Poison Dart', 'Destroy');

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

INSERT INTO EntitySkill (entity_id, skill_id)
SELECT e.id, s.id FROM Entity e, Skill s
WHERE e.name = 'Brainrot Kid'
  AND s.name IN ('Shadow Strike', 'Twin Slash', 'Poison Dart', 'Evasion Mastery');

-- =====================================================
-- 9️⃣ ITEMS
-- =====================================================

INSERT INTO Item (name, description, quality, consumable, max_uses, image_thumb, target_type, is_aoe) VALUES
('Health Potion',      'Restores 30% HP.',                             'COMMON',   true, 1, 'https://art.pixilart.com/cb38cacb9cd5e61.png', 'SELF',  false),
('Energy Elixir',      'Restores 30% energy.',                         'COMMON',   true, 1, 'https://dinopixel.com/preload/0223/Energy-Potion.png', 'SELF',  false),
('Antidote',           'Removes the Poisoned status.',                 'COMMON',   true, 1, 'https://art.pixilart.com/sr2f4a5a186faaws3.png', 'SELF',  false),
('Rage Brew',          'Applies Strengthened lv1 to user.',            'UNCOMMON', true, 1, 'https://i.pinimg.com/736x/cb/ca/5e/cbca5ed5aed24778866103608c0bc890.jpg', 'SELF',  false),
('Smoke Bomb',         'Stuns all enemies for 1 turn.',               'UNCOMMON', true, 1, 'https://img.magnific.com/premium-vector/8-bits-pixel-smoke-blast-vector-illustration_1310125-42.jpg',    'ENEMY', true),
('Greater Health Pot', 'Restores 60% HP.',                             'RARE',     true, 1, 'https://i.postimg.cc/K1PHMgJD/pixil-frame-0.png',    'SELF',  false),
('Elixir of Speed',    'Increases speed by 30% for 3 turns.',         'UNCOMMON', true, 1, 'https://art.pixilart.com/11a1cd892fd2a0b.png',  'SELF',  false);

-- =====================================================
-- ITEM EFFECTS
-- =====================================================

-- IDs necesarios
SET @HP       = (SELECT id FROM Statistic WHERE name = 'hp');
SET @Energy   = (SELECT id FROM Statistic WHERE name = 'energy');
SET @Speed    = (SELECT id FROM Statistic WHERE name = 'speed');

SET @Poisoned     = (SELECT id FROM Status WHERE name = 'Poisoned');
SET @Strengthened = (SELECT id FROM Status WHERE name = 'Strengthened');
SET @Stunned      = (SELECT id FROM Status WHERE name = 'Stunned');

-- Health Potion (+30% HP)
INSERT INTO Effect
(stat_id, stat_multiplier, status_id, effect_level, probability, duration_turns)
VALUES
(@HP, 1.30, NULL, 1, 1.0, 1);

-- Energy Elixir (+30% Energy)
INSERT INTO Effect
(stat_id, stat_multiplier, status_id, effect_level, probability, duration_turns)
VALUES
(@Energy, 1.30, NULL, 1, 1.0, 1);

-- Antidote
INSERT INTO Effect
(stat_id, stat_multiplier, status_id, effect_level, probability, duration_turns)
VALUES
(NULL, NULL, @Poisoned, 0, 1.0, 0);

-- Rage Brew
INSERT INTO Effect
(stat_id, stat_multiplier, status_id, effect_level, probability, duration_turns)
VALUES
(NULL, NULL, @Strengthened, 1, 1.0, 3);

-- =====================================================
-- LINK ITEM ↔ EFFECT
-- =====================================================

-- Health Potion
INSERT INTO ItemEffect (item_id, effect_id)
SELECT i.id, e.id
FROM Item i, Effect e
WHERE i.name = 'Health Potion'
  AND e.stat_id = @HP
  AND e.stat_multiplier BETWEEN 1.299 AND 1.301
  AND e.effect_level = 1
  LIMIT 1;

-- Energy Elixir
INSERT INTO ItemEffect (item_id, effect_id)
SELECT i.id, e.id
FROM Item i, Effect e
WHERE i.name = 'Energy Elixir'
  AND e.stat_id = @Energy
  AND e.stat_multiplier BETWEEN 1.299 AND 1.301
  AND e.effect_level = 1
  LIMIT 1;

-- Antidote
INSERT INTO ItemEffect (item_id, effect_id)
SELECT i.id, e.id
FROM Item i, Effect e
WHERE i.name = 'Antidote'
  AND e.status_id = @Poisoned
  AND e.effect_level = 0
  LIMIT 1;

-- Rage Brew
INSERT INTO ItemEffect (item_id, effect_id)
SELECT i.id, e.id
FROM Item i, Effect e
WHERE i.name = 'Rage Brew'
  AND e.status_id = @Strengthened
  AND e.effect_level = 1
  LIMIT 1;

-- =====================================================
-- 1️⃣1️⃣ LOOT ENTRIES
-- =====================================================

-- Goblin Berserker
INSERT INTO LootEntry (loot_table_id, item_id, drop_chance, min_quality, max_quality)
SELECT lt.id, i.id, 0.80, 'COMMON', 'COMMON'
FROM LootTable lt
JOIN Enemy en ON lt.enemy_id = en.entity_id
JOIN Entity e ON en.entity_id = e.id
JOIN Item i ON i.name = 'Health Potion'
WHERE e.name = 'Goblin Berserker';

INSERT INTO LootEntry (loot_table_id, item_id, drop_chance, min_quality, max_quality)
SELECT lt.id, i.id, 0.60, 'COMMON', 'COMMON'
FROM LootTable lt
JOIN Enemy en ON lt.enemy_id = en.entity_id
JOIN Entity e ON en.entity_id = e.id
JOIN Item i ON i.name = 'Energy Elixir'
WHERE e.name = 'Goblin Berserker';

INSERT INTO LootEntry (loot_table_id, item_id, drop_chance, min_quality, max_quality)
SELECT lt.id, i.id, 0.40, 'COMMON', 'UNCOMMON'
FROM LootTable lt
JOIN Enemy en ON lt.enemy_id = en.entity_id
JOIN Entity e ON en.entity_id = e.id
JOIN Item i ON i.name = 'Rage Brew'
WHERE e.name = 'Goblin Berserker';


-- Stone Golem
INSERT INTO LootEntry (loot_table_id, item_id, drop_chance, min_quality, max_quality)
SELECT lt.id, i.id, 0.85, 'COMMON', 'UNCOMMON'
FROM LootTable lt
JOIN Enemy en ON lt.enemy_id = en.entity_id
JOIN Entity e ON en.entity_id = e.id
JOIN Item i ON i.name = 'Health Potion'
WHERE e.name = 'Stone Golem';

INSERT INTO LootEntry (loot_table_id, item_id, drop_chance, min_quality, max_quality)
SELECT lt.id, i.id, 0.70, 'UNCOMMON', 'UNCOMMON'
FROM LootTable lt
JOIN Enemy en ON lt.enemy_id = en.entity_id
JOIN Entity e ON en.entity_id = e.id
JOIN Item i ON i.name = 'Smoke Bomb'
WHERE e.name = 'Stone Golem';

INSERT INTO LootEntry (loot_table_id, item_id, drop_chance, min_quality, max_quality)
SELECT lt.id, i.id, 0.45, 'UNCOMMON', 'RARE'
FROM LootTable lt
JOIN Enemy en ON lt.enemy_id = en.entity_id
JOIN Entity e ON en.entity_id = e.id
JOIN Item i ON i.name = 'Greater Health Pot'
WHERE e.name = 'Stone Golem';


-- Vampire Lord
INSERT INTO LootEntry (loot_table_id, item_id, drop_chance, min_quality, max_quality)
SELECT lt.id, i.id, 0.90, 'UNCOMMON', 'RARE'
FROM LootTable lt
JOIN Enemy en ON lt.enemy_id = en.entity_id
JOIN Entity e ON en.entity_id = e.id
JOIN Item i ON i.name = 'Greater Health Pot'
WHERE e.name = 'Vampire Lord';

INSERT INTO LootEntry (loot_table_id, item_id, drop_chance, min_quality, max_quality)
SELECT lt.id, i.id, 0.75, 'UNCOMMON', 'RARE'
FROM LootTable lt
JOIN Enemy en ON lt.enemy_id = en.entity_id
JOIN Entity e ON en.entity_id = e.id
JOIN Item i ON i.name = 'Energy Elixir'
WHERE e.name = 'Vampire Lord';

INSERT INTO LootEntry (loot_table_id, item_id, drop_chance, min_quality, max_quality)
SELECT lt.id, i.id, 0.60, 'UNCOMMON', 'RARE'
FROM LootTable lt
JOIN Enemy en ON lt.enemy_id = en.entity_id
JOIN Entity e ON en.entity_id = e.id
JOIN Item i ON i.name = 'Elixir of Speed'
WHERE e.name = 'Vampire Lord';

INSERT INTO LootEntry (loot_table_id, item_id, drop_chance, min_quality, max_quality)
SELECT lt.id, i.id, 0.40, 'RARE', 'RARE'
FROM LootTable lt
JOIN Enemy en ON lt.enemy_id = en.entity_id
JOIN Entity e ON en.entity_id = e.id
JOIN Item i ON i.name = 'Smoke Bomb'
WHERE e.name = 'Vampire Lord';


-- Dark Assassin
INSERT INTO LootEntry (loot_table_id, item_id, drop_chance, min_quality, max_quality)
SELECT lt.id, i.id, 0.80, 'UNCOMMON', 'RARE'
FROM LootTable lt
JOIN Enemy en ON lt.enemy_id = en.entity_id
JOIN Entity e ON en.entity_id = e.id
JOIN Item i ON i.name = 'Elixir of Speed'
WHERE e.name = 'Dark Assassin';

INSERT INTO LootEntry (loot_table_id, item_id, drop_chance, min_quality, max_quality)
SELECT lt.id, i.id, 0.65, 'UNCOMMON', 'RARE'
FROM LootTable lt
JOIN Enemy en ON lt.enemy_id = en.entity_id
JOIN Entity e ON en.entity_id = e.id
JOIN Item i ON i.name = 'Smoke Bomb'
WHERE e.name = 'Dark Assassin';

INSERT INTO LootEntry (loot_table_id, item_id, drop_chance, min_quality, max_quality)
SELECT lt.id, i.id, 0.55, 'COMMON', 'UNCOMMON'
FROM LootTable lt
JOIN Enemy en ON lt.enemy_id = en.entity_id
JOIN Entity e ON en.entity_id = e.id
JOIN Item i ON i.name = 'Antidote'
WHERE e.name = 'Dark Assassin';

INSERT INTO LootEntry (loot_table_id, item_id, drop_chance, min_quality, max_quality)
SELECT lt.id, i.id, 0.35, 'UNCOMMON', 'RARE'
FROM LootTable lt
JOIN Enemy en ON lt.enemy_id = en.entity_id
JOIN Entity e ON en.entity_id = e.id
JOIN Item i ON i.name = 'Rage Brew'
WHERE e.name = 'Dark Assassin';

INSERT INTO LootEntry (loot_table_id, item_id, drop_chance, min_quality, max_quality)
SELECT lt.id, i.id, 0.35, 'UNCOMMON', 'RARE'
FROM LootTable lt
JOIN Enemy en ON lt.enemy_id = en.entity_id
JOIN Entity e ON en.entity_id = e.id
JOIN Item i ON i.name = 'Rage Brew'
WHERE e.name = 'Brainrot Kid';