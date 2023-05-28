package de.martenschaefer.serverutils.config;

import de.martenschaefer.serverutils.ServerUtilsMod;
import com.mojang.serialization.Codec;

public record VoteConfig(String permissionPrefix) {
    public static final Codec<VoteConfig> CODEC = Codec.STRING.fieldOf("permission_prefix").codec().xmap(VoteConfig::new, VoteConfig::permissionPrefix);

    public static final VoteConfig DEFAULT = new VoteConfig(ServerUtilsMod.MODID + ".vote");
}
