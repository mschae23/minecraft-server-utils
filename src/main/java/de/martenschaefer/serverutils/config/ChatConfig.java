package de.martenschaefer.serverutils.config;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record ChatConfig(boolean enabled) {
    public static final Codec<ChatConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.BOOL.fieldOf("enabled").forGetter(ChatConfig::enabled)
    ).apply(instance, instance.stable(ChatConfig::new)));

    public static final ChatConfig DEFAULT = new ChatConfig(true);
}
