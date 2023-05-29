package de.martenschaefer.serverutils.config;

import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record ItemFrameConfig(boolean enabled, Item invisibilityItem) {
    public static final Codec<ItemFrameConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.BOOL.fieldOf("enabled").forGetter(ItemFrameConfig::enabled),
        Registries.ITEM.getCodec().fieldOf("invisibility_item").forGetter(ItemFrameConfig::invisibilityItem)
    ).apply(instance, instance.stable(ItemFrameConfig::new)));

    public static final ItemFrameConfig DEFAULT = new ItemFrameConfig(false, Items.GLASS_PANE);
}
