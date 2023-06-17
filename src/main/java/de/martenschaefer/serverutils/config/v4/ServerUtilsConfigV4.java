package de.martenschaefer.serverutils.config.v4;

import de.martenschaefer.config.api.ModConfig;
import de.martenschaefer.serverutils.config.BroadcastEntityDeathConfig;
import de.martenschaefer.serverutils.config.ChatConfig;
import de.martenschaefer.serverutils.config.ContainerLockConfig;
import de.martenschaefer.serverutils.config.DeathCoordsConfig;
import de.martenschaefer.serverutils.config.MiscConfig;
import de.martenschaefer.serverutils.config.v5.RegionConfigV5;
import de.martenschaefer.serverutils.config.ServerUtilsConfigV6;
import de.martenschaefer.serverutils.config.VoteConfig;
import de.martenschaefer.serverutils.config.command.CommandConfig;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

@Deprecated
@SuppressWarnings("DeprecatedIsStillUsed")
public record ServerUtilsConfigV4(CommandConfig command,
                                  ChatConfig chat,
                                  DeathCoordsConfig deathCoords,
                                  BroadcastEntityDeathConfig broadcastEntityDeath,
                                  ContainerLockConfig lock,
                                  VoteConfig vote,
                                  MiscConfigV4 misc) implements ModConfig<ServerUtilsConfigV6> {
    public static final Codec<ServerUtilsConfigV4> TYPE_CODEC = RecordCodecBuilder.create(instance -> instance.group(
        CommandConfig.CODEC.fieldOf("command").forGetter(ServerUtilsConfigV4::command),
        ChatConfig.CODEC.fieldOf("chat").forGetter(ServerUtilsConfigV4::chat),
        DeathCoordsConfig.CODEC.fieldOf("death_coords").forGetter(ServerUtilsConfigV4::deathCoords),
        BroadcastEntityDeathConfig.CODEC.fieldOf("broadcast_entity_death").forGetter(ServerUtilsConfigV4::broadcastEntityDeath),
        ContainerLockConfig.CODEC.fieldOf("container_lock").forGetter(ServerUtilsConfigV4::lock),
        VoteConfig.CODEC.fieldOf("vote").forGetter(ServerUtilsConfigV4::vote),
        MiscConfigV4.CODEC.fieldOf("misc").forGetter(ServerUtilsConfigV4::misc)
    ).apply(instance, instance.stable(ServerUtilsConfigV4::new)));

    public static final Type<ServerUtilsConfigV6, ServerUtilsConfigV4> TYPE = new Type<>(4, TYPE_CODEC);

    public static final ServerUtilsConfigV4 DEFAULT =
        new ServerUtilsConfigV4(CommandConfig.DEFAULT, ChatConfig.DEFAULT, DeathCoordsConfig.DEFAULT, BroadcastEntityDeathConfig.DEFAULT, ContainerLockConfig.DEFAULT, VoteConfig.DEFAULT, MiscConfigV4.DEFAULT);

    @Override
    public Type<ServerUtilsConfigV6, ?> type() {
        return TYPE;
    }

    @Override
    public ServerUtilsConfigV6 latest() {
        return new ServerUtilsConfigV6(this.command, this.chat, this.lock, this.vote,
            new MiscConfig(this.deathCoords, this.broadcastEntityDeath, this.misc.enableGamemodeSwitcher(), this.misc.itemFrame()));
    }

    @Override
    public boolean shouldUpdate() {
        return true;
    }
}
