package de.martenschaefer.serverutils.config.v5;

import de.martenschaefer.config.api.ModConfig;
import de.martenschaefer.serverutils.config.ChatConfig;
import de.martenschaefer.serverutils.config.ContainerLockConfig;
import de.martenschaefer.serverutils.config.MiscConfig;
import de.martenschaefer.serverutils.config.ServerUtilsConfigV6;
import de.martenschaefer.serverutils.config.VoteConfig;
import de.martenschaefer.serverutils.config.command.CommandConfig;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

@Deprecated
@SuppressWarnings("DeprecatedIsStillUsed")
public record ServerUtilsConfigV5(CommandConfig command,
                                  ChatConfig chat,
                                  RegionConfigV5 region,
                                  ContainerLockConfig lock,
                                  VoteConfig vote,
                                  MiscConfig misc) implements ModConfig<ServerUtilsConfigV6> {
    public static final Codec<ServerUtilsConfigV5> TYPE_CODEC = RecordCodecBuilder.create(instance -> instance.group(
        CommandConfig.CODEC.fieldOf("command").forGetter(ServerUtilsConfigV5::command),
        ChatConfig.CODEC.fieldOf("chat").forGetter(ServerUtilsConfigV5::chat),
        RegionConfigV5.CODEC.fieldOf("region").forGetter(ServerUtilsConfigV5::region),
        ContainerLockConfig.CODEC.fieldOf("container_lock").forGetter(ServerUtilsConfigV5::lock),
        VoteConfig.CODEC.fieldOf("vote").forGetter(ServerUtilsConfigV5::vote),
        MiscConfig.CODEC.fieldOf("misc").forGetter(ServerUtilsConfigV5::misc)
    ).apply(instance, instance.stable(ServerUtilsConfigV5::new)));

    public static final Type<ServerUtilsConfigV6, ServerUtilsConfigV5> TYPE = new Type<>(5, TYPE_CODEC);

    public static final ServerUtilsConfigV5 DEFAULT =
        new ServerUtilsConfigV5(CommandConfig.DEFAULT, ChatConfig.DEFAULT, RegionConfigV5.DEFAULT, ContainerLockConfig.DEFAULT, VoteConfig.DEFAULT, MiscConfig.DEFAULT);

    @Override
    public Type<ServerUtilsConfigV6, ?> type() {
        return TYPE;
    }

    @Override
    public ServerUtilsConfigV6 latest() {
        return new ServerUtilsConfigV6(this.command, this.chat, this.lock, this.vote, this.misc);
    }

    @Override
    public boolean shouldUpdate() {
        return true;
    }
}
