package com.covercorp.kaelaevent.util;

import com.covercorp.kaelaevent.util.simple.LoreDisplayArray;

import com.destroystokyo.paper.profile.PlayerProfile;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.profile.PlayerTextures;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.UUID;

public final class ItemBuilder {
    private final ItemStack ITEM_STACK;

    public ItemBuilder(final Material mat) {
        this.ITEM_STACK = new ItemStack(mat);
    }

    public ItemBuilder(final ItemStack item) {
        this.ITEM_STACK = item;
    }

    public ItemBuilder withAmount(final int amount) {
        this.ITEM_STACK.setAmount(amount);
        return this;
    }

    public ItemBuilder withName(final String name) {
        final ItemMeta meta = this.ITEM_STACK.getItemMeta();
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
        this.ITEM_STACK.setItemMeta(meta);
        return this;
    }

    public ItemBuilder withCustomModel(final int data) {
        final ItemMeta meta = this.ITEM_STACK.getItemMeta();

        meta.setCustomModelData(data);
        this.ITEM_STACK.setItemMeta(meta);

        return this;
    }

    public ItemBuilder withTexture(final String skinUrl) {
        final SkullMeta meta = (SkullMeta) ITEM_STACK.getItemMeta();

        final PlayerProfile playerProfile = Bukkit.createProfile(UUID.randomUUID());
        final PlayerTextures playerTextures = playerProfile.getTextures();

        try {
            playerTextures.setSkin(new URL(skinUrl));
        } catch (MalformedURLException ignored) {}

        playerProfile.setTextures(playerTextures);
        meta.setPlayerProfile(playerProfile);

        this.ITEM_STACK.setItemMeta(meta);

        return this;
    }

    public ItemBuilder withNBTTag(final String name, final String content) {
        NBTMetadataUtil.addString(ITEM_STACK, name, content);

        return this;
    }

    public ItemBuilder hideStats() {
        final ItemMeta meta = this.ITEM_STACK.getItemMeta();

        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        this.ITEM_STACK.setItemMeta(meta);

        return this;
    }

    public ItemBuilder withLore(final LoreDisplayArray<String> lore) {
        final ItemMeta meta = this.ITEM_STACK.getItemMeta();
        meta.setLore(lore);
        this.ITEM_STACK.setItemMeta(meta);
        return this;
    }

    public ItemBuilder hideEnchantments() {
        final ItemMeta meta = this.ITEM_STACK.getItemMeta();
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        meta.addItemFlags(ItemFlag.HIDE_STORED_ENCHANTS);
        this.ITEM_STACK.setItemMeta(meta);
        return this;
    }

    public ItemBuilder setGlint(boolean glint) {
        final ItemMeta meta = this.ITEM_STACK.getItemMeta();
        meta.setEnchantmentGlintOverride(glint);
        this.ITEM_STACK.setItemMeta(meta);
        return this;
    }

    public ItemBuilder withDurability(final int durability) {
        this.ITEM_STACK.setDurability((short)durability);
        return this;
    }

    public ItemBuilder withPotionEffect(final PotionEffect effect, final Color color) {
        final PotionMeta meta = (PotionMeta) this.ITEM_STACK.getItemMeta();
        meta.setColor(color);
        meta.addCustomEffect(effect, true);
        this.ITEM_STACK.setItemMeta(meta);

        return this;
    }

    public ItemBuilder withData(final int data) {
        this.ITEM_STACK.setDurability((short)data);
        return this;
    }

    public ItemBuilder withEnchantment(final Enchantment enchantment, final int level) {
        this.ITEM_STACK.addUnsafeEnchantment(enchantment, level);
        return this;
    }

    public ItemBuilder withEnchantment(final Enchantment enchantment) {
        this.ITEM_STACK.addUnsafeEnchantment(enchantment, 1);
        return this;
    }

    public ItemBuilder withType(final Material material) {
        this.ITEM_STACK.setType(material);
        return this;
    }

    public ItemBuilder setUnbreakable() {
        final ItemMeta meta = this.ITEM_STACK.getItemMeta();
        meta.setUnbreakable(true);
        this.ITEM_STACK.setItemMeta(meta);
        return this;
    }

    public ItemBuilder clearLore() {
        final ItemMeta meta = this.ITEM_STACK.getItemMeta();
        meta.setLore(new ArrayList<>());
        this.ITEM_STACK.setItemMeta(meta);
        return this;
    }

    public ItemBuilder clearEnchantments() {
        for (final Enchantment enchantment : this.ITEM_STACK.getEnchantments().keySet()) {
            this.ITEM_STACK.removeEnchantment(enchantment);
        }
        return this;
    }

    public ItemBuilder withColorArmour(final Color color) {
        final Material type = this.ITEM_STACK.getType();
        if (type == Material.LEATHER_BOOTS || type == Material.LEATHER_CHESTPLATE || type == Material.LEATHER_HELMET || type == Material.LEATHER_LEGGINGS) {
            final LeatherArmorMeta meta = (LeatherArmorMeta)this.ITEM_STACK.getItemMeta();
            meta.setColor(color);
            this.ITEM_STACK.setItemMeta((ItemMeta)meta);
            return this;
        }
        throw new IllegalArgumentException("withColor is only applicable for leather armor!");
    }

    public ItemStack build() {
        return this.ITEM_STACK;
    }
}