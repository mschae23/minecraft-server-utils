package de.martenschaefer.serverutils.config.v3;

import de.martenschaefer.config.api.ModConfig;
import de.martenschaefer.serverutils.config.BroadcastEntityDeathConfig;
import de.martenschaefer.serverutils.config.ChatConfig;
import de.martenschaefer.serverutils.config.ContainerLockConfig;
import de.martenschaefer.serverutils.config.DeathCoordsConfig;
import de.martenschaefer.serverutils.config.ServerUtilsConfigV6;
import de.martenschaefer.serverutils.config.VoteConfig;
import de.martenschaefer.serverutils.config.command.CommandConfig;
import de.martenschaefer.serverutils.config.v4.ServerUtilsConfigV4;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

@Deprecated
@SuppressWarnings("DeprecatedIsStillUsed")
public record ServerUtilsConfigV3(CommandConfig command,
                                  ChatConfig chat,
                                  DeathCoordsConfig deathCoords,
                                  BroadcastEntityDeathConfig broadcastEntityDeath,
                                  ContainerLockConfig lock,
                                  VoteConfig vote,
                                  MiscConfigV3 misc) implements ModConfig<ServerUtilsConfigV6> {
    public static final Codec<ServerUtilsConfigV3> TYPE_CODEC = RecordCodecBuilder.create(instance -> instance.group(
        CommandConfig.CODEC.fieldOf("command").forGetter(ServerUtilsConfigV3::command),
        ChatConfig.CODEC.fieldOf("chat").forGetter(ServerUtilsConfigV3::chat),
        DeathCoordsConfig.CODEC.fieldOf("death_coords").forGetter(ServerUtilsConfigV3::deathCoords),
        BroadcastEntityDeathConfig.CODEC.fieldOf("broadcast_entity_death").forGetter(ServerUtilsConfigV3::broadcastEntityDeath),
        ContainerLockConfig.CODEC.fieldOf("container_lock").forGetter(ServerUtilsConfigV3::lock),
        VoteConfig.CODEC.fieldOf("vote").forGetter(ServerUtilsConfigV3::vote),
        MiscConfigV3.CODEC.fieldOf("misc").forGetter(ServerUtilsConfigV3::misc)
    ).apply(instance, instance.stable(ServerUtilsConfigV3::new)));

    public static final Type<ServerUtilsConfigV6, ServerUtilsConfigV3> TYPE = new Type<>(3, TYPE_CODEC);

    public static final ServerUtilsConfigV3 DEFAULT =
        new ServerUtilsConfigV3(CommandConfig.DEFAULT, ChatConfig.DEFAULT, DeathCoordsConfig.DEFAULT, BroadcastEntityDeathConfig.DEFAULT, ContainerLockConfig.DEFAULT, VoteConfig.DEFAULT, MiscConfigV3.DEFAULT);

    @Override
    public Type<ServerUtilsConfigV6, ?> type() {
        return TYPE;
    }

    @Override
    public ServerUtilsConfigV6 latest() {
        return new ServerUtilsConfigV4(this.command, this.chat, this.deathCoords, this.broadcastEntityDeath, this.lock, this.vote, this.misc.latestV4()).latest();
    }

    @Override
    public boolean shouldUpdate() {
        return true;
    }
}
