package de.martenschaefer.serverutils.region.shape;

import java.util.Arrays;
import java.util.List;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public class RegionShapes {
    public static final Codec<RegionShapes> CODEC = Entry.CODEC.listOf().xmap(RegionShapes::new, shapes -> Arrays.asList(shapes.entries));

    public final Entry[] entries;
    private final UnionShape combinedShape;

    public RegionShapes(Entry... entries) {
        this.entries = entries;

        ProtectionShape[] shapes = new ProtectionShape[entries.length];

        for (int i = 0; i < shapes.length; i++) {
            shapes[i] = entries[i].shape;
        }

        this.combinedShape = new UnionShape(shapes);
    }

    private RegionShapes(List<Entry> entries) {
        this(entries.toArray(new Entry[0]));
    }

    public boolean test(ProtectionContext context) {
        return this.combinedShape.test(context);
    }

    public RegionShapes withShape(String name, ProtectionShape shape) {
        var newShapes = Arrays.copyOf(this.entries, this.entries.length + 1);
        newShapes[newShapes.length - 1] = new Entry(name, shape);
        return new RegionShapes(newShapes);
    }

    public RegionShapes removeShape(String name) {
        int index = this.findIndex(name);
        if (index == -1) {
            return this;
        }

        int writer = 0;

        var newEntries = new Entry[this.entries.length - 1];
        for (int i = 0; i < this.entries.length; i++) {
            if (i != index) {
                newEntries[writer++] = this.entries[i];
            }
        }

        return new RegionShapes(newEntries);
    }

    private int findIndex(String name) {
        int index = -1;

        for (int i = 0; i < this.entries.length; i++) {
            if (this.entries[i].name.equals(name)) {
                index = i;
                break;
            }
        }

        return index;
    }

    public Text displayList() {
        if (this.entries.length == 0) {
            return Text.literal("Empty\n").formatted(Formatting.YELLOW);
        }

        MutableText text = Text.literal("");
        for (var entry : this.entries) {
            text = text.append(Text.literal("  " + entry.name).formatted(Formatting.AQUA))
                .append(": ")
                .append(entry.shape.displayShort())
                .append("\n");
        }

        return text;
    }

    public Text displayShort() {
        return this.combinedShape.displayShort();
    }

    public boolean isEmpty() {
        return this.entries.length == 0;
    }

    public record Entry(String name, ProtectionShape shape) {
        public static final Codec<Entry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("name").forGetter(entry -> entry.name),
            ProtectionShape.CODEC.fieldOf("shape").forGetter(entry -> entry.shape)
        ).apply(instance, Entry::new));
    }
}
