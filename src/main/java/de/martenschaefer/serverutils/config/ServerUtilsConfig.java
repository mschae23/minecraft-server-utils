package de.martenschaefer.serverutils.config;

import de.martenschaefer.serverutils.config.command.CommandConfig;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record ServerUtilsConfig(CommandConfig command, ChatConfig chat, DeathCoordsConfig deathCoords, BroadcastEntityDeathConfig broadcastEntityDeath, ContainerLockConfig lock) {
    public static final Codec<ServerUtilsConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        CommandConfig.CODEC.fieldOf("command").forGetter(ServerUtilsConfig::command),
        ChatConfig.CODEC.fieldOf("chat").forGetter(ServerUtilsConfig::chat),
        DeathCoordsConfig.CODEC.fieldOf("death_coords").forGetter(ServerUtilsConfig::deathCoords),
        BroadcastEntityDeathConfig.CODEC.fieldOf("chat").forGetter(ServerUtilsConfig::broadcastEntityDeath),
        ContainerLockConfig.CODEC.fieldOf("container_lock").forGetter(ServerUtilsConfig::lock)
    ).apply(instance, instance.stable(ServerUtilsConfig::new)));

    public static final ServerUtilsConfig DEFAULT =
        new ServerUtilsConfig(CommandConfig.DEFAULT, ChatConfig.DEFAULT, DeathCoordsConfig.DEFAULT, BroadcastEntityDeathConfig.DEFAULT, ContainerLockConfig.DEFAULT);
}
