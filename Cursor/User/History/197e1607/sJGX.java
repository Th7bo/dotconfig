package dev.th7bo.meleesim.inventory;

import dev.th7bo.meleesim.Weapons;
import dev.th7bo.meleesim.database.entities.weapons.StoredWeapon;
import dev.th7bo.meleesim.database.entities.weapons.WeaponsPlayerData;
import dev.th7bo.meleesim.database.entities.weapons.WeaponModel;
import dev.th7bo.meleesim.database.playerdata.PlayerData;
import dev.th7bo.meleesim.util.ItemBuilder;
import dev.th7bo.meleesim.weapons.WeaponRarity;
import dev.th7bo.meleesim.weapons.WeaponsManager;
import fr.mrmicky.fastinv.FastInv;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class WeaponDeleteGUI extends FastInv {
    private final Weapons plugin;
    private final WeaponsManager weaponManager;
    private final Player player;

    public WeaponDeleteGUI(Weapons plugin, WeaponsManager weaponManager, Player player) {
        super(27, "§8Mass Delete Weapons");
        this.plugin = plugin;
        this.weaponManager = weaponManager;
        this.player = player;

        initialize();
    }

    private void initialize() {
        // Fill background
        for (int i = 0; i < 27; i++) {
            setItem(i, new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE).setName(" ").toItemStack());
        }

        // Get player data
        PlayerData playerData = plugin.getPlayerDataManager().getPlayerData(player.getUniqueId());
        WeaponsPlayerData weaponsData = playerData.getWeaponsPlayerData();

        // Add rarity delete buttons
        addRarityDeleteButton(10, Material.GRAY_DYE, WeaponRarity.GENERIC, weaponsData);
        addRarityDeleteButton(11, Material.LIME_DYE, WeaponRarity.DIVINE, weaponsData);
        addRarityDeleteButton(12, Material.BLUE_DYE, WeaponRarity.ULTIMATE, weaponsData);
        addRarityDeleteButton(14, Material.LIGHT_BLUE_DYE, WeaponRarity.GODLY, weaponsData);
        addRarityDeleteButton(15, Material.RED_DYE, WeaponRarity.EXOTIC, weaponsData);
        addRarityDeleteButton(16, Material.YELLOW_DYE, WeaponRarity.ANCIENT, weaponsData);

        // Back button
        setItem(22, new ItemBuilder(Material.BARRIER)
                .setName("§cGo Back")
                .setLore(
                        "§8BUTTON",
                        "§f",
                        "§cINFORMATION",
                        "§c§l| §fThis will take you back",
                        "§c§l| §fto your §aWeapons Menu§f.",
                        "§f",
                        "§c§lCLICK HERE TO",
                        "§cGo to the Weapons Menu!"
                )
                .toItemStack(), e -> {
            new WeaponStorageGUI(plugin, weaponManager, player, 1).open(player);
            player.playSound(player.getLocation(), Sound.ITEM_BUCKET_FILL_POWDER_SNOW, 0.5f, 2.0f);
        });
    }

    private void addRarityDeleteButton(int slot, Material material, WeaponRarity rarity, WeaponsPlayerData weaponsData) {
        List<StoredWeapon> weapons = new ArrayList<>();
        List<Integer> indices = new ArrayList<>();
        
        for (int i = 0; i < weaponsData.getWeapons().size(); i++) {
            StoredWeapon weapon = weaponsData.getWeapons().get(i);
            WeaponModel model = plugin.getWeaponsRegistry().getWeapon(weapon.getWeaponKey());
            
            if (model != null && model.getRarity() == rarity && !weaponsData.isWeaponEquipped(i)) {
                weapons.add(weapon);
                indices.add(i);
            }
        }
        
        int count = weapons.size();
        String displayName = rarity.getDisplayName();
        String color = rarity.getColor();
        
        setItem(slot, new ItemBuilder(material)
                .setName("§4Mass Delete Weapons")
                .setLore(
                        "§8UTILITY BUTTON",
                        "§f",
                        " " + color + "INFORMATION",
                        " " + color + "§l| §f" + displayName + " Weapons: " + color + count + "x Weapon(s)",
                        "§f",
                        count > 0 ? "§4§lCLICK HERE TO" : "§c§lNO WEAPONS TO DELETE",
                        count > 0 ? "§cDelete all " + displayName + " Weapons!" : "§cof this rarity!"
                )
                .toItemStack(), e -> handleRarityDelete(weapons, indices, weaponsData, count, displayName, color));
    }

    private void handleRarityDelete(List<StoredWeapon> weapons, List<Integer> indices, 
                                  WeaponsPlayerData weaponsData, int count, String displayName, String color) {
        if (count > 0) {
            indices.sort(Comparator.reverseOrder());
            for (int index : indices) {
                weaponsData.removeWeapon(index);
            }
            
            player.sendMessage(" §a§lYou have mass deleted §f" + color + count + "x §a§l" + 
                    displayName + " Weapon(s)§f.");
            player.playSound(player.getLocation(), Sound.ENTITY_GENERIC_BURN, 0.5f, 1.0f);
            refresh();
        } else {
            player.sendMessage("§cNo " + displayName + " weapons to delete!");
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 0.5f, 1.0f);
        }
    }
}