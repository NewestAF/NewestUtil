package com.newestaf.newestutil.gui;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

@SuppressWarnings("deprecation")
public class InventoryGUI implements Listener, InventoryHolder {
    private final Plugin plugin;
    private final String title;
    private Inventory inventory;
    private final HashMap<Integer, InventoryGUIButton> buttons;
    private int slot;

    public InventoryGUI(Plugin plugin, String title, int rows) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
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

    public HashMap<Integer, InventoryGUIButton> getButtons() {
        return buttons;
    }

    public InventoryGUIButton addButton(InventoryGUIButton button) {
        if (this.slot <= this.inventory.getSize()) {
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

    public void setButtonName(int slot, String name) {
        InventoryGUIButton button = this.buttons.get(slot);
        button.setName(name);
        this.inventory.setItem(slot, button.getItem());
    }

    public void setButtonDescription(int slot, String description) {
        InventoryGUIButton button = this.buttons.get(slot);
        button.setDescription(description);
        this.inventory.setItem(slot, button.getItem());
    }

    public void setButtonItem(int slot, ItemStack itemStack) {
        InventoryGUIButton button = this.buttons.get(slot);
        button.setItem(itemStack);
        this.inventory.setItem(slot, button.getItem());
    }

    public int getSlot() {
        return slot;
    }
    public void showMenu(Player player) {
        player.openInventory(this.inventory);
    }

    public void removeAllClickEvents() {
        for (int i = 0; i < this.slot; i++)
            this.buttons.get(i).setOnClick(null);
    }

    @EventHandler
    public void onItemClick(InventoryClickEvent event) {
        if (!event.getView().getTitle().equals(this.title) || event.getCurrentItem() == null) {
            return;
        }
        InventoryGUIButton button = this.buttons.get(event.getRawSlot());
        if (button == null) {
            return;
        }
        event.setCancelled(true);
        if (button.isLocked()) {
            return;
        }
        if (button.getOnClick() != null) {
            button.onClick(event);
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!event.getView().getTitle().equals(this.title)) {
            this.removeAllClickEvents();
        }
    }



}
