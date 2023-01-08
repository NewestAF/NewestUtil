package com.newestaf.gui;

import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Consumer;

import java.util.Arrays;

@SuppressWarnings("deprecation")
public class InventoryGUIButton {

    private String name;
    private String description;
    private ItemStack item;
    private boolean locked;
    private Consumer<InventoryClickEvent> onClick;

    public InventoryGUIButton(String name, String description, Material material) {
        ItemStack newItem = new ItemStack(material, 1);
        ItemMeta meta = newItem.getItemMeta();
        if (name != null) {
            this.name = name;
            meta.setDisplayName(name);
        }
        if (description != null) {
            this.description = description;
            String[] lines = description.split("\n");
            meta.setLore(Arrays.asList(lines));
        }
        if (material != Material.AIR) {
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            newItem.setItemMeta(meta);
        }
        this.item = newItem;
        this.locked = true;
        this.onClick = null;
    }

    public InventoryGUIButton(
            String name,
            String description,
            Material material,
            boolean locked
    ) {
        this(name, description, material);
        this.locked = locked;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
        ItemMeta meta = this.item.getItemMeta();
        meta.setDisplayName(name);
        this.item.setItemMeta(meta);
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
        ItemMeta meta = this.item.getItemMeta();
        String[] lines = description.split("\n");
        meta.setLore(Arrays.asList(lines));
        this.item.setItemMeta(meta);
    }

    public ItemStack getItem() {
        return this.item;
    }

    public void setItem(ItemStack item) {
        this.item = item.clone();
    }

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    public void onClick(InventoryClickEvent event) {
        if (onClick != null) {
            onClick.accept(event);
        }
    }

    public Consumer<InventoryClickEvent> getOnClick() {
        return this.onClick;
    }

    public void setOnClick(Consumer<InventoryClickEvent> onClick) {
        this.onClick = onClick;
    }

    public boolean equals(InventoryGUIButton compare) {
        return this.name.equals(compare.getName())
                && this.description.equals(compare.getDescription())
                && this.item.getType() == compare.item.getType();
    }
}
