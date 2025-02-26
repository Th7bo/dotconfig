package dev.th7bo.meleesim.database.entities.weapons;

import dev.th7bo.meleesim.weapons.WeaponModel;
import dev.th7bo.meleesim.weapons.WeaponsRegistry;
import lombok.Data;

import java.util.*;
import java.util.stream.Collectors;


@Data
public class WeaponsPlayerData {
    private List<StoredWeapon> weapons = new ArrayList<>();
    private List<Integer> equippedWeapons;
    private static final int MAIN_HAND = 0;
    private static final int OFF_HAND = 1;

    public WeaponsPlayerData() {
        equippedWeapons = new ArrayList<>(Arrays.asList(null, null));
    }

    public void addWeapon(StoredWeapon weapon) {
        this.weapons.add(weapon);
        sortWeapons();
    }

    public List<Integer> getEquippedWeapons() {
        if (equippedWeapons == null) {
            equippedWeapons = new ArrayList<>(Arrays.asList(null, null));
        }
        while (equippedWeapons.size() < 2) {
            equippedWeapons.add(null);
        }
        return equippedWeapons;
    }

    public boolean isWeaponEquipped(int weaponIndex) {
        return getEquippedWeapons().contains(weaponIndex);
    }

    public StoredWeapon removeWeapon(int index) {
        if (index < 0 || index >= weapons.size()) return null;
        StoredWeapon removed = weapons.remove(index);
        handleWeaponRemoval(index);
        return removed;
    }

    public StoredWeapon getWeapon(int index) {
        if (index < 0 || index >= weapons.size()) return null;
        return weapons.get(index);
    }

    public StoredWeapon getEquippedWeapon(int slot) {
        if (slot != 0 && slot != 40) return null;

        int index = slot == 0 ? 0 : 1;
        Integer weaponIndex = equippedWeapons.get(index);
        return weaponIndex != null ? weapons.get(weaponIndex) : null;
    }

    public boolean equipWeapon(int slot, int weaponIndex) {
        if (!isValidSlot(slot) || !isValidWeaponIndex(weaponIndex)) {
            return false;
        }

        int index = convertSlotToIndex(slot);
        
        if (isWeaponEquipped(weaponIndex)) {
            return false;
        }

        equippedWeapons.set(index, weaponIndex);
        return true;
    }

    public StoredWeapon unequipWeapon(int slot) {
        if (!isValidSlot(slot)) return null;
        
        int index = convertSlotToIndex(slot);
        Integer weaponIndex = equippedWeapons.get(index);
        
        if (weaponIndex == null || !isValidWeaponIndex(weaponIndex)) {
            return null;
        }

        equippedWeapons.set(index, null);
        return weapons.get(weaponIndex);
    }

    public void handleWeaponRemoval(int removedIndex) {
        for (int i = 0; i < equippedWeapons.size(); i++) {
            Integer equippedIndex = equippedWeapons.get(i);
            if (equippedIndex != null) {
                if (equippedIndex == removedIndex) {
                    equippedWeapons.set(i, null);
                } else if (equippedIndex > removedIndex) {
                    equippedWeapons.set(i, equippedIndex - 1);
                }
            }
        }
    }

    public boolean equipWeapon(int slot, StoredWeapon storedWeapon) {
        return equipWeapon(slot, weapons.indexOf(storedWeapon));
    }

    public int getNextEquipSlot() {
        List<Integer> equipped = getEquippedWeapons();
        if (equipped.get(MAIN_HAND) == null) return 0;
        if (equipped.get(OFF_HAND) == null) return 40;
        return -1;
    }

    public boolean canEquipWeapon() {
        return getNextEquipSlot() != -1;
    }

    public boolean canStoreMore() {
        return weapons.size() < 100; // Arbitrary limit, adjust as needed
    }

    public List<StoredWeapon> getWeaponsByRarity(String rarity) {
        return weapons.stream()
                .filter(weapon -> {
                    WeaponModel model = WeaponsRegistry.getInstance().getWeapon(weapon.getWeaponKey());
                    return model != null && model.getRarity().name().equals(rarity);
                })
                .collect(Collectors.toList());
    }

    public void sortWeapons() {
        weapons.sort(Comparator.comparing((StoredWeapon w) -> {
            WeaponModel model = WeaponsRegistry.getInstance().getWeapon(w.getWeaponKey());
            return model != null ? w.getFinalPower(model) : 0;
        }).reversed());
    }

    private boolean isValidSlot(int slot) {
        return slot == 0 || slot == 40;
    }

    private boolean isValidWeaponIndex(int index) {
        return index >= 0 && index < weapons.size();
    }

    private int convertSlotToIndex(int slot) {
        return slot == 40 ? OFF_HAND : MAIN_HAND;
    }
}