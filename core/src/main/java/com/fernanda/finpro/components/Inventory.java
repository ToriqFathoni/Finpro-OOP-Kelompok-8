package com.fernanda.finpro.components;
import java.util.HashMap;
import java.util.Map;

public class Inventory {
    private final HashMap<ItemType, Integer> items;

    public Inventory() {
        this.items = new HashMap<>();
    }

    public void addItem(ItemType type, int amount) {
        items.put(type, items.getOrDefault(type, 0) + amount);
        System.out.println("[INVENTORY] Picked up: " + type.getDisplayName() + " x" + amount);
    }

    public int getItemCount(ItemType type) {
        return items.getOrDefault(type, 0);
    }

    public boolean removeItem(ItemType type, int amount) {
        int current = getItemCount(type);
        if (current >= amount) {
            items.put(type, current - amount);
            if (items.get(type) <= 0) items.remove(type);
            return true;
        }
        return false;
    }

    public boolean hasItem(ItemType type, int amount) {
        return getItemCount(type) >= amount;
    }

    public String getDisplayString() {
        if (items.isEmpty()) {
            return "[ EMPTY ]";
        }

        StringBuilder sb = new StringBuilder();
        for (Map.Entry<ItemType, Integer> entry : items. entrySet()) {
            sb.append(entry.getKey().getIcon()).append(" ");
            sb.append(entry. getKey().name()).append(": x");
            sb.append(entry. getValue()).append("\n");
        }
        return sb.toString().trim();
    }

    public void clear() {
        items.clear();
    }

    public HashMap<ItemType, Integer> getItems() {
        return items;
    }
}
