package de.martenschaefer.serverutils.config;

import de.martenschaefer.config.api.ModConfig;
import de.martenschaefer.serverutils.config.command.CommandConfig;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record ServerUtilsConfigV5(CommandConfig command,
                                  ChatConfig chat,
                                  RegionConfig region,
                                  DeathCoordsConfig deathCoords,
                                  BroadcastEntityDeathConfig broadcastEntityDeath,
                                  ContainerLockConfig lock,
                                  VoteConfig vote,
                                  MiscConfig misc) implements ModConfig<ServerUtilsConfigV5> {
    public static final Codec<ServerUtilsConfigV5> TYPE_CODEC = RecordCodecBuilder.create(instance -> instance.group(
        CommandConfig.CODEC.fieldOf("command").forGetter(ServerUtilsConfigV5::command),
        ChatConfig.CODEC.fieldOf("chat").forGetter(ServerUtilsConfigV5::chat),
        RegionConfig.CODEC.fieldOf("region").forGetter(ServerUtilsConfigV5::region),
        DeathCoordsConfig.CODEC.fieldOf("death_coords").forGetter(ServerUtilsConfigV5::deathCoords),
        BroadcastEntityDeathConfig.CODEC.fieldOf("broadcast_entity_death").forGetter(ServerUtilsConfigV5::broadcastEntityDeath),
        ContainerLockConfig.CODEC.fieldOf("container_lock").forGetter(ServerUtilsConfigV5::lock),
        VoteConfig.CODEC.fieldOf("vote").forGetter(ServerUtilsConfigV5::vote),
        MiscConfig.CODEC.fieldOf("misc").forGetter(ServerUtilsConfigV5::misc)
    ).apply(instance, instance.stable(ServerUtilsConfigV5::new)));

    public static final ModConfig.Type<ServerUtilsConfigV5, ServerUtilsConfigV5> TYPE = new ModConfig.Type<>(5, TYPE_CODEC);

    public static final ServerUtilsConfigV5 DEFAULT =
        new ServerUtilsConfigV5(CommandConfig.DEFAULT, ChatConfig.DEFAULT, RegionConfig.DEFAULT, DeathCoordsConfig.DEFAULT, BroadcastEntityDeathConfig.DEFAULT, ContainerLockConfig.DEFAULT, VoteConfig.DEFAULT, MiscConfig.DEFAULT);

    @Override
    public ModConfig.Type<ServerUtilsConfigV5, ?> type() {
        return TYPE;
    }

    @Override
    public ServerUtilsConfigV5 latest() {
        return this;
    }

    @Override
    public boolean shouldUpdate() {
        return true;
    }
}
