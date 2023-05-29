package de.martenschaefer.serverutils.config;

import com.mojang.serialization.Codec;

public record EnableGamemodeSwitcherConfig(String permission) {
    public static final Codec<EnableGamemodeSwitcherConfig> CODEC = Codec.STRING.fieldOf("permission").xmap(EnableGamemodeSwitcherConfig::new, EnableGamemodeSwitcherConfig::permission).codec();

    public static final EnableGamemodeSwitcherConfig DEFAULT = new EnableGamemodeSwitcherConfig("minecraft.command.gamemode");
}
