package de.martenschaefer.serverutils.config;

import com.mojang.serialization.Codec;

public record EnableGamemodeSwitcherConfigV2(String permission) {
    public static final Codec<EnableGamemodeSwitcherConfigV2> CODEC = Codec.STRING.fieldOf("permission").xmap(EnableGamemodeSwitcherConfigV2::new, EnableGamemodeSwitcherConfigV2::permission).codec();

    public static final EnableGamemodeSwitcherConfigV2 DEFAULT = new EnableGamemodeSwitcherConfigV2("minecraft.command.gamemode");
}
