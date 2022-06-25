package de.martenschaefer.serverutils.config.command;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record CommandConfig(PosCommandConfig pos) {
    public static final Codec<CommandConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        PosCommandConfig.CODEC.fieldOf("pos").forGetter(CommandConfig::pos)
    ).apply(instance, instance.stable(CommandConfig::new)));

    public static final CommandConfig DEFAULT = new CommandConfig(PosCommandConfig.DEFAULT);
}
