package de.martenschaefer.serverutils.config;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record BroadcastEntityDeathConfig(boolean enabled) {
    public static final Codec<BroadcastEntityDeathConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.BOOL.fieldOf("enabled").forGetter(BroadcastEntityDeathConfig::enabled)
    ).apply(instance, instance.stable(BroadcastEntityDeathConfig::new)));

    public static final BroadcastEntityDeathConfig DEFAULT = new BroadcastEntityDeathConfig(true);
}
