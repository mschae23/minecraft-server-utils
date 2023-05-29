package de.martenschaefer.serverutils.config;

import de.martenschaefer.config.api.ModConfig;
import de.martenschaefer.serverutils.config.command.CommandConfig;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record ServerUtilsConfigV4(CommandConfig command,
                                  ChatConfig chat,
                                  DeathCoordsConfig deathCoords,
                                  BroadcastEntityDeathConfig broadcastEntityDeath,
                                  ContainerLockConfig lock,
                                  VoteConfig vote,
                                  MiscConfig misc) implements ModConfig<ServerUtilsConfigV4> {
    public static final Codec<ServerUtilsConfigV4> TYPE_CODEC = RecordCodecBuilder.create(instance -> instance.group(
        CommandConfig.CODEC.fieldOf("command").forGetter(ServerUtilsConfigV4::command),
        ChatConfig.CODEC.fieldOf("chat").forGetter(ServerUtilsConfigV4::chat),
        DeathCoordsConfig.CODEC.fieldOf("death_coords").forGetter(ServerUtilsConfigV4::deathCoords),
        BroadcastEntityDeathConfig.CODEC.fieldOf("broadcast_entity_death").forGetter(ServerUtilsConfigV4::broadcastEntityDeath),
        ContainerLockConfig.CODEC.fieldOf("container_lock").forGetter(ServerUtilsConfigV4::lock),
        VoteConfig.CODEC.fieldOf("vote").forGetter(ServerUtilsConfigV4::vote),
        MiscConfig.CODEC.fieldOf("misc").forGetter(ServerUtilsConfigV4::misc)
    ).apply(instance, instance.stable(ServerUtilsConfigV4::new)));

    public static final ModConfig.Type<ServerUtilsConfigV4, ServerUtilsConfigV4> TYPE = new ModConfig.Type<>(4, TYPE_CODEC);

    public static final ServerUtilsConfigV4 DEFAULT =
        new ServerUtilsConfigV4(CommandConfig.DEFAULT, ChatConfig.DEFAULT, DeathCoordsConfig.DEFAULT, BroadcastEntityDeathConfig.DEFAULT, ContainerLockConfig.DEFAULT, VoteConfig.DEFAULT, MiscConfig.DEFAULT);

    @Override
    public ModConfig.Type<ServerUtilsConfigV4, ?> type() {
        return TYPE;
    }

    @Override
    public ServerUtilsConfigV4 latest() {
        return this;
    }

    @Override
    public boolean shouldUpdate() {
        return true;
    }
}
