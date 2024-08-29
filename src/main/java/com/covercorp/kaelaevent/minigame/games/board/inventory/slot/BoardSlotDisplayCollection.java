package com.covercorp.kaelaevent.minigame.games.board.inventory.slot;

import com.covercorp.kaelaevent.util.ItemBuilder;
import com.covercorp.kaelaevent.util.simple.LoreDisplayArray;
import com.covercorp.kaelaevent.util.simple.StringUtils;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public final class BoardSlotDisplayCollection {
    private final static LoreDisplayArray<String> NO_GAME_INFO = new LoreDisplayArray<>(StringUtils.translate("&7No game in this slot."));

    private final static LoreDisplayArray<String> SLOT_3_LORE = new LoreDisplayArray<>(
            StringUtils.translate("&bSpecial Challenge Slot"),
            StringUtils.translate(" "),
            StringUtils.translate("&7Game: &e&lBASKETBALL"),
            StringUtils.translate("&7Talents per Team: &e&l1"),
            StringUtils.translate(" "),
            StringUtils.translate("&a> Click here to move ALL PLAYERS to this game.")
    );

    private final static LoreDisplayArray<String> SLOT_5_LORE = new LoreDisplayArray<>(
            StringUtils.translate("&bSpecial Challenge Slot"),
            StringUtils.translate(" "),
            StringUtils.translate("&7Game: &e&lPLATFORM RUSH"),
            StringUtils.translate("&7Talents per Team: &e&l1"),
            StringUtils.translate(" "),
            StringUtils.translate("&a> Click here to move ALL PLAYERS to this game.")
    );

    private final static LoreDisplayArray<String> SLOT_6_LORE = new LoreDisplayArray<>(
            StringUtils.translate("&6Event Chance"),
            StringUtils.translate(" "),
            StringUtils.translate("&7Who got this slot, must &bROLL &7 the"),
            StringUtils.translate("&6GOLDEN DICE&7 in their inventory.")
    );

    private final static LoreDisplayArray<String> SLOT_9_LORE = new LoreDisplayArray<>(
            StringUtils.translate("&bSpecial Challenge Slot"),
            StringUtils.translate(" "),
            StringUtils.translate("&7Game: &e&lTUG OF WAR"),
            StringUtils.translate("&7Talents per Team: &e&l1"),
            StringUtils.translate(" "),
            StringUtils.translate("&a> Click here to move ALL PLAYERS to this game.")
    );

    private final static LoreDisplayArray<String> SLOT_10_LORE = new LoreDisplayArray<>(
            StringUtils.translate("&bSpecial Challenge Slot"),
            StringUtils.translate(" "),
            StringUtils.translate("&7Game: &e&lCOLOR GACHA"),
            StringUtils.translate("&7Talents per Team: &e&l1"),
            StringUtils.translate(" "),
            StringUtils.translate("&a> Click here to move ALL PLAYERS to this game.")
    );

    private final static LoreDisplayArray<String> SLOT_11_LORE = new LoreDisplayArray<>(
            StringUtils.translate("&cTrap Slot"),
            StringUtils.translate(" "),
            StringUtils.translate("&7Who got this slot, MISSES 1 Turn")
    );

    private final static LoreDisplayArray<String> SLOT_12_LORE = new LoreDisplayArray<>(
            StringUtils.translate("&bSpecial Challenge Slot"),
            StringUtils.translate(" "),
            StringUtils.translate("&7Game: &e&lTHE ROOF IS LAVA"),
            StringUtils.translate("&7Talents per Team: &e&l1"),
            StringUtils.translate(" "),
            StringUtils.translate("&a> Click here to move ALL PLAYERS to this game.")
    );

    private final static LoreDisplayArray<String> SLOT_15_LORE = new LoreDisplayArray<>(
            StringUtils.translate("&3All Member Event Slot"),
            StringUtils.translate(" "),
            StringUtils.translate("&7Game: &e&lMOVING TARGET SHOOTING"),
            StringUtils.translate("&7Talents per Team: &e&l5"),
            StringUtils.translate(" "),
            StringUtils.translate("&a> Click here to move ALL PLAYERS to this game.")
    );

    private final static LoreDisplayArray<String> SLOT_17_LORE = new LoreDisplayArray<>(
            StringUtils.translate("&6Event Chance"),
            StringUtils.translate(" "),
            StringUtils.translate("&7Who got this slot, must &bROLL &7 the"),
            StringUtils.translate("&6GOLDEN DICE&7 in their inventory.")
    );

    private final static LoreDisplayArray<String> SLOT_19_LORE = new LoreDisplayArray<>(
            StringUtils.translate("&bSpecial Challenge Slot"),
            StringUtils.translate(" "),
            StringUtils.translate("&7Game: &e&lGLASS BRIDGE CROSSING"),
            StringUtils.translate("&7Talents per Team: &e&l3 Talents (1 Team Only)"),
            StringUtils.translate(" "),
            StringUtils.translate("&a> Click here to move ALL PLAYERS to this game.")
    );

    private final static LoreDisplayArray<String> SLOT_21_LORE = new LoreDisplayArray<>(
            StringUtils.translate("&6Event Chance"),
            StringUtils.translate(" "),
            StringUtils.translate("&7Who got this slot, must &bROLL &7 the"),
            StringUtils.translate("&6GOLDEN DICE&7 in their inventory.")
    );

    private final static LoreDisplayArray<String> SLOT_22_LORE = new LoreDisplayArray<>(
            StringUtils.translate("&3All Member Event Slot"),
            StringUtils.translate(" "),
            StringUtils.translate("&7Game: &e&lGREEN LIGHT, RED LIGHT"),
            StringUtils.translate("&7Talents per Team: &e&l5"),
            StringUtils.translate(" "),
            StringUtils.translate("&a> Click here to move ALL PLAYERS to this game.")
    );

    private final static LoreDisplayArray<String> SLOT_23_LORE = new LoreDisplayArray<>(
            StringUtils.translate("&bSpecial Challenge Slot"),
            StringUtils.translate(" "),
            StringUtils.translate("&7Game: &e&lTRIDENT RACE"),
            StringUtils.translate("&7Talents per Team: &e&l1"),
            StringUtils.translate(" "),
            StringUtils.translate("&a> Click here to move ALL PLAYERS to this game.")
    );

    private final static LoreDisplayArray<String> SLOT_25_LORE = new LoreDisplayArray<>(
            StringUtils.translate("&bSpecial Challenge Slot"),
            StringUtils.translate(" "),
            StringUtils.translate("&7Game: &e&lZOMBIE SHOOTING"),
            StringUtils.translate("&7Talents per Team: &e&l1"),
            StringUtils.translate(" "),
            StringUtils.translate("&a> Click here to move ALL PLAYERS to this game.")
    );

    private final static LoreDisplayArray<String> SLOT_SPECIAL_1_LORE = new LoreDisplayArray<>(
            StringUtils.translate("&5Special Global Game (1vs1)"),
            StringUtils.translate(" "),
            StringUtils.translate("&7Game: &e&lREFLEX GAME"),
            StringUtils.translate("&7Talents per Team: &e&l1"),
            StringUtils.translate(" "),
            StringUtils.translate("&a> Click here to move ALL PLAYERS to this game.")
    );

    private final static LoreDisplayArray<String> SLOT_SPECIAL_2_LORE = new LoreDisplayArray<>(
            StringUtils.translate("&5Special Global Game (1vs1)"),
            StringUtils.translate(" "),
            StringUtils.translate("&7Game: &e&lSNOWBALL RACE"),
            StringUtils.translate("&7Talents per Team: &e&l1"),
            StringUtils.translate(" "),
            StringUtils.translate("&a> Click here to move ALL PLAYERS to this game.")
    );

    private final static LoreDisplayArray<String> SLOT_SPECIAL_3_LORE = new LoreDisplayArray<>(
            StringUtils.translate("&5Special Global Game (1vs1)"),
            StringUtils.translate(" "),
            StringUtils.translate("&7Game: &e&lTHE TOWER OF LUCK"),
            StringUtils.translate("&7Talents per Team: &e&l1"),
            StringUtils.translate(" "),
            StringUtils.translate("&a> Click here to move ALL PLAYERS to this game.")
    );

    public static ItemStack START_SLOT = new ItemBuilder(Material.POPPED_CHORUS_FRUIT)
            .withCustomModel(2000)
            .withName("&7Slot: &aStart")
            .withLore(NO_GAME_INFO)
            .build();
    public static ItemStack SLOT_1 = new ItemBuilder(Material.POPPED_CHORUS_FRUIT)
            .withCustomModel(2001)
            .withName("&7Slot: &a1")
            .withLore(NO_GAME_INFO)
            .build();
    public static ItemStack SLOT_2 = new ItemBuilder(Material.POPPED_CHORUS_FRUIT)
            .withCustomModel(2002)
            .withName("&7Slot: &a2")
            .withLore(NO_GAME_INFO)
            .build();
    public static ItemStack SLOT_3 = new ItemBuilder(Material.POPPED_CHORUS_FRUIT)
            .withCustomModel(2003)
            .withName("&7Slot: &a3")
            .withLore(SLOT_3_LORE)
            .build();
    public static ItemStack SLOT_4 = new ItemBuilder(Material.POPPED_CHORUS_FRUIT)
            .withCustomModel(2004)
            .withName("&7Slot: &a4")
            .withLore(NO_GAME_INFO)
            .build();
    public static ItemStack SLOT_5 = new ItemBuilder(Material.POPPED_CHORUS_FRUIT)
            .withCustomModel(2005)
            .withName("&7Slot: &a5")
            .withLore(SLOT_5_LORE)
            .build();
    public static ItemStack SLOT_6 = new ItemBuilder(Material.POPPED_CHORUS_FRUIT)
            .withCustomModel(2006)
            .withName("&7Slot: &a6")
            .withLore(SLOT_6_LORE)
            .build();
    public static ItemStack SLOT_7 = new ItemBuilder(Material.POPPED_CHORUS_FRUIT)
            .withCustomModel(2007)
            .withName("&7Slot: &a7")
            .withLore(NO_GAME_INFO)
            .build();
    public static ItemStack SLOT_8 = new ItemBuilder(Material.POPPED_CHORUS_FRUIT)
            .withCustomModel(2008)
            .withName("&7Slot: &a8")
            .withLore(NO_GAME_INFO)
            .build();
    public static ItemStack SLOT_9 = new ItemBuilder(Material.POPPED_CHORUS_FRUIT)
            .withCustomModel(2009)
            .withName("&7Slot: &a9")
            .withLore(SLOT_9_LORE)
            .build();
    public static ItemStack SLOT_10 = new ItemBuilder(Material.POPPED_CHORUS_FRUIT)
            .withCustomModel(2010)
            .withName("&7Slot: &a10")
            .withLore(SLOT_10_LORE)
            .build();
    public static ItemStack SLOT_11 = new ItemBuilder(Material.POPPED_CHORUS_FRUIT)
            .withCustomModel(2011)
            .withName("&7Slot: &a11")
            .withLore(SLOT_11_LORE)
            .build();
    public static ItemStack SLOT_12 = new ItemBuilder(Material.POPPED_CHORUS_FRUIT)
            .withCustomModel(2012)
            .withName("&7Slot: &a12")
            .withLore(SLOT_12_LORE)
            .build();
    public static ItemStack SLOT_13 = new ItemBuilder(Material.POPPED_CHORUS_FRUIT)
            .withCustomModel(2013)
            .withName("&7Slot: &a13")
            .withLore(NO_GAME_INFO)
            .build();
    public static ItemStack SLOT_14 = new ItemBuilder(Material.POPPED_CHORUS_FRUIT)
            .withCustomModel(2014)
            .withName("&7Slot: &a14")
            .withLore(NO_GAME_INFO)
            .build();
    public static ItemStack SLOT_15 = new ItemBuilder(Material.POPPED_CHORUS_FRUIT)
            .withCustomModel(2015)
            .withName("&7Slot: &a15")
            .withLore(SLOT_15_LORE)
            .build();
    public static ItemStack SLOT_16 = new ItemBuilder(Material.POPPED_CHORUS_FRUIT)
            .withCustomModel(2016)
            .withName("&7Slot: &a16")
            .withLore(NO_GAME_INFO)
            .build();
    public static ItemStack SLOT_17 = new ItemBuilder(Material.POPPED_CHORUS_FRUIT)
            .withCustomModel(2017)
            .withName("&7Slot: &a17")
            .withLore(SLOT_17_LORE)
            .build();
    public static ItemStack SLOT_18 = new ItemBuilder(Material.POPPED_CHORUS_FRUIT)
            .withCustomModel(2018)
            .withName("&7Slot: &a18")
            .withLore(NO_GAME_INFO)
            .build();
    public static ItemStack SLOT_19 = new ItemBuilder(Material.POPPED_CHORUS_FRUIT)
            .withCustomModel(2019)
            .withName("&7Slot: &a19")
            .withLore(SLOT_19_LORE)
            .build();
    public static ItemStack SLOT_20 = new ItemBuilder(Material.POPPED_CHORUS_FRUIT)
            .withCustomModel(2020)
            .withName("&7Slot: &a20")
            .withLore(NO_GAME_INFO)
            .build();
    public static ItemStack SLOT_21 = new ItemBuilder(Material.POPPED_CHORUS_FRUIT)
            .withCustomModel(2021)
            .withName("&7Slot: &a21")
            .withLore(SLOT_21_LORE)
            .build();
    public static ItemStack SLOT_22 = new ItemBuilder(Material.POPPED_CHORUS_FRUIT)
            .withCustomModel(2022)
            .withName("&7Slot: &a22")
            .withLore(SLOT_22_LORE)
            .build();
    public static ItemStack SLOT_23 = new ItemBuilder(Material.POPPED_CHORUS_FRUIT)
            .withCustomModel(2023)
            .withName("&7Slot: &a23")
            .withLore(SLOT_23_LORE)
            .build();
    public static ItemStack SLOT_24 = new ItemBuilder(Material.POPPED_CHORUS_FRUIT)
            .withCustomModel(2024)
            .withName("&7Slot: &a24")
            .withLore(NO_GAME_INFO)
            .build();
    public static ItemStack SLOT_25 = new ItemBuilder(Material.POPPED_CHORUS_FRUIT)
            .withCustomModel(2025)
            .withName("&7Slot: &a25")
            .withLore(SLOT_25_LORE)
            .build();
    public static ItemStack SLOT_SPECIAL_1 = new ItemBuilder(Material.POPPED_CHORUS_FRUIT)
            .withCustomModel(2027)
            .withName("&7Slot: &aSpecial Slot 1")
            .withLore(SLOT_SPECIAL_1_LORE)
            .build();
    public static ItemStack SLOT_SPECIAL_2 = new ItemBuilder(Material.POPPED_CHORUS_FRUIT)
            .withCustomModel(2028)
            .withName("&7Slot: &aSpecial Slot 2")
            .withLore(SLOT_SPECIAL_2_LORE)
            .build();
    public static ItemStack SLOT_SPECIAL_3 = new ItemBuilder(Material.POPPED_CHORUS_FRUIT)
            .withCustomModel(2029)
            .withName("&7Slot: &aSpecial Slot 3")
            .withLore(SLOT_SPECIAL_3_LORE)
            .build();
    public static ItemStack END_SLOT = new ItemBuilder(Material.POPPED_CHORUS_FRUIT)
            .withCustomModel(2026)
            .withName("&7Slot: &aFinish")
            .withLore(NO_GAME_INFO)
            .build();
}
