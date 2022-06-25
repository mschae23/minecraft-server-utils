package de.martenschaefer.serverutils.config;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record DeathCoordsConfig(boolean enabled, boolean inPublicChat) {
    public static final Codec<DeathCoordsConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.BOOL.fieldOf("enabled").forGetter(DeathCoordsConfig::enabled),
        Codec.BOOL.fieldOf("in_public_chat").forGetter(DeathCoordsConfig::inPublicChat)
    ).apply(instance, instance.stable(DeathCoordsConfig::new)));

    public static final DeathCoordsConfig DEFAULT = new DeathCoordsConfig(true, false);
}
