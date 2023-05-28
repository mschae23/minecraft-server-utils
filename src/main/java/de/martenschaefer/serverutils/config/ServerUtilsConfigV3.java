package de.martenschaefer.serverutils.config;

import de.martenschaefer.config.api.ModConfig;
import de.martenschaefer.serverutils.config.command.CommandConfig;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record ServerUtilsConfigV3(CommandConfig command,
                                  ChatConfig chat,
                                  DeathCoordsConfig deathCoords,
                                  BroadcastEntityDeathConfig broadcastEntityDeath,
                                  ContainerLockConfig lock,
                                  VoteConfig vote,
                                  MiscConfigV2 misc) implements ModConfig<ServerUtilsConfigV3> {
    public static final Codec<ServerUtilsConfigV3> TYPE_CODEC = RecordCodecBuilder.create(instance -> instance.group(
        CommandConfig.CODEC.fieldOf("command").forGetter(ServerUtilsConfigV3::command),
        ChatConfig.CODEC.fieldOf("chat").forGetter(ServerUtilsConfigV3::chat),
        DeathCoordsConfig.CODEC.fieldOf("death_coords").forGetter(ServerUtilsConfigV3::deathCoords),
        BroadcastEntityDeathConfig.CODEC.fieldOf("broadcast_entity_death").forGetter(ServerUtilsConfigV3::broadcastEntityDeath),
        ContainerLockConfig.CODEC.fieldOf("container_lock").forGetter(ServerUtilsConfigV3::lock),
        VoteConfig.CODEC.fieldOf("vote").forGetter(ServerUtilsConfigV3::vote),
        MiscConfigV2.CODEC.fieldOf("misc").forGetter(ServerUtilsConfigV3::misc)
    ).apply(instance, instance.stable(ServerUtilsConfigV3::new)));

    public static final ModConfig.Type<ServerUtilsConfigV3, ServerUtilsConfigV3> TYPE = new ModConfig.Type<>(3, TYPE_CODEC);

    public static final ServerUtilsConfigV3 DEFAULT =
        new ServerUtilsConfigV3(CommandConfig.DEFAULT, ChatConfig.DEFAULT, DeathCoordsConfig.DEFAULT, BroadcastEntityDeathConfig.DEFAULT, ContainerLockConfig.DEFAULT, VoteConfig.DEFAULT, MiscConfigV2.DEFAULT);

    @Override
    public ModConfig.Type<ServerUtilsConfigV3, ?> type() {
        return TYPE;
    }

    @Override
    public ServerUtilsConfigV3 latest() {
        return this;
    }

    @Override
    public boolean shouldUpdate() {
        return true;
    }
}
