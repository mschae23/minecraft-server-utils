package de.martenschaefer.serverutils.config;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record ContainerLockConfig(boolean enabled, String permissionPrefix) {
    public static final Codec<ContainerLockConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.BOOL.fieldOf("enabled").forGetter(ContainerLockConfig::enabled),
        Codec.STRING.fieldOf("key_permission_prefix").forGetter(ContainerLockConfig::permissionPrefix)
    ).apply(instance, instance.stable(ContainerLockConfig::new)));

    public static final ContainerLockConfig DEFAULT = new ContainerLockConfig(true, "key");
}
