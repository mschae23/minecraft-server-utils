package de.martenschaefer.serverutils.config.v2;

import de.martenschaefer.config.api.ModConfig;
import de.martenschaefer.serverutils.config.BroadcastEntityDeathConfig;
import de.martenschaefer.serverutils.config.ChatConfig;
import de.martenschaefer.serverutils.config.ContainerLockConfig;
import de.martenschaefer.serverutils.config.DeathCoordsConfig;
import de.martenschaefer.serverutils.config.MiscConfig;
import de.martenschaefer.serverutils.config.ServerUtilsConfigV4;
import de.martenschaefer.serverutils.config.VoteConfig;
import de.martenschaefer.serverutils.config.command.CommandConfig;
import de.martenschaefer.serverutils.config.v3.MiscConfigV3;
import de.martenschaefer.serverutils.config.v3.ServerUtilsConfigV3;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

@Deprecated
@SuppressWarnings("DeprecatedIsStillUsed")
public record ServerUtilsConfigV2(CommandConfig command,
                                  ChatConfig chat,
                                  DeathCoordsConfig deathCoords,
                                  BroadcastEntityDeathConfig broadcastEntityDeath,
                                  ContainerLockConfig lock,
                                  MiscConfigV3 misc) implements ModConfig<ServerUtilsConfigV4> {
    public static final Codec<ServerUtilsConfigV2> TYPE_CODEC = RecordCodecBuilder.create(instance -> instance.group(
        CommandConfig.CODEC.fieldOf("command").forGetter(ServerUtilsConfigV2::command),
        ChatConfig.CODEC.fieldOf("chat").forGetter(ServerUtilsConfigV2::chat),
        DeathCoordsConfig.CODEC.fieldOf("death_coords").forGetter(ServerUtilsConfigV2::deathCoords),
        BroadcastEntityDeathConfig.CODEC.fieldOf("broadcast_entity_death").forGetter(ServerUtilsConfigV2::broadcastEntityDeath),
        ContainerLockConfig.CODEC.fieldOf("container_lock").forGetter(ServerUtilsConfigV2::lock),
        MiscConfigV3.CODEC.fieldOf("misc").forGetter(ServerUtilsConfigV2::misc)
    ).apply(instance, instance.stable(ServerUtilsConfigV2::new)));

    public static final Type<ServerUtilsConfigV4, ServerUtilsConfigV2> TYPE = new Type<>(2, TYPE_CODEC);

    public static final ServerUtilsConfigV2 DEFAULT =
        new ServerUtilsConfigV2(CommandConfig.DEFAULT, ChatConfig.DEFAULT, DeathCoordsConfig.DEFAULT, BroadcastEntityDeathConfig.DEFAULT, ContainerLockConfig.DEFAULT, MiscConfigV3.DEFAULT);

    @Override
    public Type<ServerUtilsConfigV4, ?> type() {
        return TYPE;
    }

    @Override
    public ServerUtilsConfigV4 latest() {
        return new ServerUtilsConfigV3(this.command, this.chat, this.deathCoords, this.broadcastEntityDeath, this.lock, VoteConfig.DEFAULT, this.misc).latest();
    }

    @Override
    public boolean shouldUpdate() {
        return true;
    }
}
