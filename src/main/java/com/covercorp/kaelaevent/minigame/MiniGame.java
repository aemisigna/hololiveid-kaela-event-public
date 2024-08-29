package com.covercorp.kaelaevent.minigame;

import com.covercorp.kaelaevent.KaelaEvent;

import com.covercorp.kaelaevent.entity.TimedEntityHelper;
import com.covercorp.kaelaevent.entity.TimedEntityHelperImpl;
import com.covercorp.kaelaevent.minigame.type.MiniGameType;

import lombok.AccessLevel;
import lombok.Getter;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.minimessage.tag.standard.StandardTags;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

@Getter(AccessLevel.PUBLIC)
public abstract class MiniGame {
    private final KaelaEvent kaelaEvent;
    private final MiniMessage miniMessage;

    private final FileConfiguration gameConfiguration;

    private final TimedEntityHelper timedEntityHelper;

    public MiniGame(final KaelaEvent kaelaEvent, final MiniGameType miniGameType) {
        this.kaelaEvent = kaelaEvent;

        final String configFileNaming = miniGameType.name().toLowerCase() + ".yml";
        final File customConfigFile = new File(kaelaEvent.getDataFolder(), configFileNaming);

        if (!customConfigFile.exists()) {
            customConfigFile.getParentFile().mkdirs();
            kaelaEvent.saveResource(configFileNaming, false);
        }

        gameConfiguration = YamlConfiguration.loadConfiguration(customConfigFile);

        kaelaEvent.getLogger().info("Selected minigame is " + kaelaEvent.getMiniGameType() + ". The config file for this minigame is " + configFileNaming + ". Loading...");

        miniMessage = MiniMessage.builder()
                .tags(
                        TagResolver.builder()
                                .resolver(StandardTags.font())
                                .resolver(StandardTags.color())
                                .resolver(StandardTags.clickEvent())
                                .resolver(StandardTags.hoverEvent())
                                .resolver(StandardTags.newline())
                                .resolver(StandardTags.decorations())
                                .build()
                )
                .build();

        timedEntityHelper = new TimedEntityHelperImpl(kaelaEvent);
    }

    public abstract void onGameLoad();

    public abstract void onGameUnload();
}
