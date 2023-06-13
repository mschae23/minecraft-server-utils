package de.martenschaefer.serverutils.config.v4;

import de.martenschaefer.serverutils.config.BroadcastEntityDeathConfig;
import de.martenschaefer.serverutils.config.DeathCoordsConfig;
import de.martenschaefer.serverutils.config.EnableGamemodeSwitcherConfig;
import de.martenschaefer.serverutils.config.ItemFrameConfig;
import de.martenschaefer.serverutils.config.MiscConfig;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

@Deprecated
public record MiscConfigV4(EnableGamemodeSwitcherConfig enableGamemodeSwitcher, ItemFrameConfig itemFrame) {
    public static final Codec<MiscConfigV4> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        EnableGamemodeSwitcherConfig.CODEC.fieldOf("enable_gamemode_switcher").forGetter(MiscConfigV4::enableGamemodeSwitcher),
        ItemFrameConfig.CODEC.fieldOf("item_frame").forGetter(MiscConfigV4::itemFrame)
    ).apply(instance, instance.stable(MiscConfigV4::new)));

    public static final MiscConfigV4 DEFAULT = new MiscConfigV4(EnableGamemodeSwitcherConfig.DEFAULT, ItemFrameConfig.DEFAULT);

    public MiscConfig latest() {
        return new MiscConfig(DeathCoordsConfig.DEFAULT, BroadcastEntityDeathConfig.DEFAULT, EnableGamemodeSwitcherConfig.DEFAULT, ItemFrameConfig.DEFAULT);
    }
}
