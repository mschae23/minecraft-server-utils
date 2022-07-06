package de.martenschaefer.serverutils.config.v1;

import de.martenschaefer.config.api.ModConfig;
import de.martenschaefer.serverutils.config.BroadcastEntityDeathConfig;
import de.martenschaefer.serverutils.config.ChatConfig;
import de.martenschaefer.serverutils.config.ContainerLockConfig;
import de.martenschaefer.serverutils.config.DeathCoordsConfig;
import de.martenschaefer.serverutils.config.MiscConfigV2;
import de.martenschaefer.serverutils.config.ServerUtilsConfigV2;
import de.martenschaefer.serverutils.config.command.CommandConfig;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

@SuppressWarnings("DeprecatedIsStillUsed")
@Deprecated
public record ServerUtilsConfigV1(CommandConfig command,
                                  ChatConfig chat,
                                  DeathCoordsConfig deathCoords,
                                  BroadcastEntityDeathConfig broadcastEntityDeath,
                                  ContainerLockConfig lock) implements ModConfig<ServerUtilsConfigV2> {
    public static final Codec<ServerUtilsConfigV1> TYPE_CODEC = RecordCodecBuilder.create(instance -> instance.group(
        CommandConfig.CODEC.fieldOf("command").forGetter(ServerUtilsConfigV1::command),
        ChatConfig.CODEC.fieldOf("chat").forGetter(ServerUtilsConfigV1::chat),
        DeathCoordsConfig.CODEC.fieldOf("death_coords").forGetter(ServerUtilsConfigV1::deathCoords),
        BroadcastEntityDeathConfig.CODEC.fieldOf("broadcast_entity_death").forGetter(ServerUtilsConfigV1::broadcastEntityDeath),
        ContainerLockConfig.CODEC.fieldOf("container_lock").forGetter(ServerUtilsConfigV1::lock)
    ).apply(instance, instance.stable(ServerUtilsConfigV1::new)));

    public static final ModConfig.Type<ServerUtilsConfigV2, ServerUtilsConfigV1> TYPE = new ModConfig.Type<>(1, TYPE_CODEC);

    public static final ServerUtilsConfigV1 DEFAULT =
        new ServerUtilsConfigV1(CommandConfig.DEFAULT, ChatConfig.DEFAULT, DeathCoordsConfig.DEFAULT, BroadcastEntityDeathConfig.DEFAULT, ContainerLockConfig.DEFAULT);

    @Override
    public ModConfig.Type<ServerUtilsConfigV2, ?> type() {
        return TYPE;
    }

    @Override
    public ServerUtilsConfigV2 latest() {
        return new ServerUtilsConfigV2(this.command, this.chat, this.deathCoords, this.broadcastEntityDeath, this.lock, MiscConfigV2.DEFAULT);
    }

    @Override
    public boolean shouldUpdate() {
        return true;
    }
}
