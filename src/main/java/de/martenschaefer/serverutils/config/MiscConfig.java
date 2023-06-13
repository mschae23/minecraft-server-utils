package de.martenschaefer.serverutils.config;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record MiscConfig(DeathCoordsConfig deathCoords, BroadcastEntityDeathConfig broadcastEntityDeath, EnableGamemodeSwitcherConfig enableGamemodeSwitcher, ItemFrameConfig itemFrame) {
    public static final Codec<MiscConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        DeathCoordsConfig.CODEC.fieldOf("death_coords").forGetter(MiscConfig::deathCoords),
        BroadcastEntityDeathConfig.CODEC.fieldOf("broadcast_entity_death").forGetter(MiscConfig::broadcastEntityDeath),
        EnableGamemodeSwitcherConfig.CODEC.fieldOf("enable_gamemode_switcher").forGetter(MiscConfig::enableGamemodeSwitcher),
        ItemFrameConfig.CODEC.fieldOf("item_frame").forGetter(MiscConfig::itemFrame)
    ).apply(instance, instance.stable(MiscConfig::new)));

    public static final MiscConfig DEFAULT = new MiscConfig(DeathCoordsConfig.DEFAULT, BroadcastEntityDeathConfig.DEFAULT, EnableGamemodeSwitcherConfig.DEFAULT, ItemFrameConfig.DEFAULT);
}
