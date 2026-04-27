package com.example.demo.api.model.bd;

import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class LootService {

    private final ItemRepository itemRepository;
    private final Random random = new Random();

    public LootService(ItemRepository itemRepository) {
        this.itemRepository = itemRepository;
    }

    public List<Item> generateChestLoot(int roomLevel) {

        if (roomLevel >= 5) {
            return Collections.emptyList();
        }

        List<String> qualities = getQualitiesForLevel(roomLevel);

        List<Item> possibleItems = itemRepository.findByQualityIn(qualities);

        List<Item> loot = new ArrayList<>();

        int amount = 1 + random.nextInt(3);

        for (int i = 0; i < amount; i++) {
            if (!possibleItems.isEmpty()) {
                Item item = possibleItems.get(random.nextInt(possibleItems.size()));
                loot.add(item);
            }
        }

        return loot;
    }

    private List<String> getQualitiesForLevel(int level) {

        switch (level) {
            case 1:
                return List.of("COMMON", "UNCOMMON");
            case 2:
                return List.of("UNCOMMON", "RARE");
            case 3:
                return List.of("RARE", "EPIC");
            case 4:
                return List.of("EPIC", "LEGENDARY");
            default:
                return List.of("COMMON");
        }
    }
}