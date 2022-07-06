package de.martenschaefer.serverutils.config;

import de.martenschaefer.serverutils.config.command.CommandConfig;
import de.martenschaefer.serverutils.config.impl.ModConfig;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record ServerUtilsConfigV2(CommandConfig command,
                                  ChatConfig chat,
                                  DeathCoordsConfig deathCoords,
                                  BroadcastEntityDeathConfig broadcastEntityDeath,
                                  ContainerLockConfig lock,
                                  MiscConfigV2 misc) implements ModConfig {
    public static final Codec<ServerUtilsConfigV2> TYPE_CODEC = RecordCodecBuilder.create(instance -> instance.group(
        CommandConfig.CODEC.fieldOf("command").forGetter(ServerUtilsConfigV2::command),
        ChatConfig.CODEC.fieldOf("chat").forGetter(ServerUtilsConfigV2::chat),
        DeathCoordsConfig.CODEC.fieldOf("death_coords").forGetter(ServerUtilsConfigV2::deathCoords),
        BroadcastEntityDeathConfig.CODEC.fieldOf("broadcast_entity_death").forGetter(ServerUtilsConfigV2::broadcastEntityDeath),
        ContainerLockConfig.CODEC.fieldOf("container_lock").forGetter(ServerUtilsConfigV2::lock),
        MiscConfigV2.CODEC.fieldOf("misc").forGetter(ServerUtilsConfigV2::misc)
    ).apply(instance, instance.stable(ServerUtilsConfigV2::new)));

    public static final ModConfig.Type<ServerUtilsConfigV2> TYPE = new ModConfig.Type<>(2, TYPE_CODEC);

    public static final ServerUtilsConfigV2 DEFAULT =
        new ServerUtilsConfigV2(CommandConfig.DEFAULT, ChatConfig.DEFAULT, DeathCoordsConfig.DEFAULT, BroadcastEntityDeathConfig.DEFAULT, ContainerLockConfig.DEFAULT, MiscConfigV2.DEFAULT);

    @Override
    public ModConfig.Type<?> type() {
        return TYPE;
    }

    @Override
    public ServerUtilsConfigV2 latest() {
        return this;
    }

    @Override
    public boolean shouldUpdate() {
        return true;
    }
}
