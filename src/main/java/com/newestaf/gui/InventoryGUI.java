package com.newestaf.gui;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

public class InventoryGUI implements Listener, InventoryHolder {

    private final Player player;
    private final String title;
    private Inventory inventory;
    private final HashMap<Integer, InventoryGUIButton> buttons;
    private int slot;
    private final int maxItems;
    private final boolean locked;
    private BukkitRunnable runnable;

    public InventoryGUI(Player player, String title, int rows, boolean locked) {
        this.player = player;
        this.title = title;
        if (rows < 1) {
            rows = 1;
        }
        if (rows > 6) {
            rows = 6;
        }
        //noinspection deprecation
        this.inventory = Bukkit.createInventory(null, 9 * rows, title);
        this.buttons = new HashMap<>();
        this.slot = 0;
        this.maxItems = (9 * rows) - 1;
        this.locked = locked;
        this.runnable = null;
//        EarthMapUtil.getInstance().getMenuManager().addMenu(player.getUniqueId(), this);
    }


    public Player getPlayer() {
        return player;
    }

    public String getTitle() {
        return title;
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }

    public void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }

    public boolean isLocked() {
        return locked;
    }

    public HashMap<Integer, InventoryGUIButton> getButtons() {
        return buttons;
    }

    public InventoryGUIButton addButton(InventoryGUIButton button) {
        if (this.slot <= this.maxItems) {
            this.inventory.setItem(slot, button.getItem());
            this.buttons.put(slot, button);
            slot++;
        }
        return button;
    }

    public void addButtons(InventoryGUIButton button, int amount) {
        for (int i = 0; i < amount; i++) {
            this.addButton(button);
        }
    }

    public InventoryGUIButton setButton(int slot, InventoryGUIButton button) {
        this.inventory.setItem(slot, button.getItem());
        this.buttons.put(slot, button);
        return button;
    }

    public int getSlot() {
        return slot;
    }

    public BukkitRunnable getRunnable() {
        return runnable;
    }

    public void setRunnable(Plugin plugin, BukkitRunnable runnable) {
        this.runnable = runnable;
        this.runnable.runTaskTimer(plugin, 2, 2);
    }

    public void stopRunnable() {
        this.runnable.cancel();
    }

    public void showMenu() {
        this.player.openInventory(this.inventory);
    }

    public void removeAllClickEvents() {
        for (int i = 0; i < this.slot; i++)
            this.buttons.get(i).setOnClick(null);
    }


}
