package de.martenschaefer.serverutils.config.v4;

import de.martenschaefer.config.api.ModConfig;
import de.martenschaefer.serverutils.config.BroadcastEntityDeathConfig;
import de.martenschaefer.serverutils.config.ChatConfig;
import de.martenschaefer.serverutils.config.ContainerLockConfig;
import de.martenschaefer.serverutils.config.DeathCoordsConfig;
import de.martenschaefer.serverutils.config.MiscConfig;
import de.martenschaefer.serverutils.config.RegionConfig;
import de.martenschaefer.serverutils.config.ServerUtilsConfigV5;
import de.martenschaefer.serverutils.config.VoteConfig;
import de.martenschaefer.serverutils.config.command.CommandConfig;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

@Deprecated
public record ServerUtilsConfigV4(CommandConfig command,
                                  ChatConfig chat,
                                  DeathCoordsConfig deathCoords,
                                  BroadcastEntityDeathConfig broadcastEntityDeath,
                                  ContainerLockConfig lock,
                                  VoteConfig vote,
                                  MiscConfig misc) implements ModConfig<ServerUtilsConfigV5> {
    public static final Codec<ServerUtilsConfigV4> TYPE_CODEC = RecordCodecBuilder.create(instance -> instance.group(
        CommandConfig.CODEC.fieldOf("command").forGetter(ServerUtilsConfigV4::command),
        ChatConfig.CODEC.fieldOf("chat").forGetter(ServerUtilsConfigV4::chat),
        DeathCoordsConfig.CODEC.fieldOf("death_coords").forGetter(ServerUtilsConfigV4::deathCoords),
        BroadcastEntityDeathConfig.CODEC.fieldOf("broadcast_entity_death").forGetter(ServerUtilsConfigV4::broadcastEntityDeath),
        ContainerLockConfig.CODEC.fieldOf("container_lock").forGetter(ServerUtilsConfigV4::lock),
        VoteConfig.CODEC.fieldOf("vote").forGetter(ServerUtilsConfigV4::vote),
        MiscConfig.CODEC.fieldOf("misc").forGetter(ServerUtilsConfigV4::misc)
    ).apply(instance, instance.stable(ServerUtilsConfigV4::new)));

    public static final Type<ServerUtilsConfigV5, ServerUtilsConfigV4> TYPE = new Type<>(4, TYPE_CODEC);

    public static final ServerUtilsConfigV4 DEFAULT =
        new ServerUtilsConfigV4(CommandConfig.DEFAULT, ChatConfig.DEFAULT, DeathCoordsConfig.DEFAULT, BroadcastEntityDeathConfig.DEFAULT, ContainerLockConfig.DEFAULT, VoteConfig.DEFAULT, MiscConfig.DEFAULT);

    @Override
    public Type<ServerUtilsConfigV5, ?> type() {
        return TYPE;
    }

    @Override
    public ServerUtilsConfigV5 latest() {
        return new ServerUtilsConfigV5(this.command, this.chat, RegionConfig.DEFAULT, this.deathCoords, this.broadcastEntityDeath, this.lock, this.vote, this.misc);
    }

    @Override
    public boolean shouldUpdate() {
        return true;
    }
}
