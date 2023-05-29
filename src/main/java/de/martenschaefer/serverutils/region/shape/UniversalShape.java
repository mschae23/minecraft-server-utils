package de.martenschaefer.serverutils.region.shape;

import com.mojang.serialization.Codec;
import net.minecraft.text.Text;
import net.minecraft.text.MutableText;
import net.minecraft.util.Formatting;

public final class UniversalShape implements ProtectionShape {
    public static final UniversalShape INSTANCE = new UniversalShape();

    public static Codec<UniversalShape> CODEC = Codec.unit(INSTANCE);

    private UniversalShape() {
    }

    @Override
    public ProtectionShapeType<?> getType() {
        return ProtectionShapeType.UNIVERSAL;
    }

    @Override
    public boolean test(ProtectionContext context) {
        return true;
    }

    @Override
    public MutableText display() {
        return Text.literal("Universe").formatted(Formatting.YELLOW);
    }

    @Override
    public MutableText displayShort() {
        return this.display();
    }
}
