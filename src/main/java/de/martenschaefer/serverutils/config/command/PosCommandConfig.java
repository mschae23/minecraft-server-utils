package de.martenschaefer.serverutils.config.command;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record PosCommandConfig(boolean inPublicChat) {
    public static final Codec<PosCommandConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.BOOL.fieldOf("in_public_chat").forGetter(PosCommandConfig::inPublicChat)
    ).apply(instance, instance.stable(PosCommandConfig::new)));

    public static final PosCommandConfig DEFAULT = new PosCommandConfig(false);
}
