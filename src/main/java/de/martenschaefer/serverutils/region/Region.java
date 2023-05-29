package de.martenschaefer.serverutils.region;

import de.martenschaefer.serverutils.region.shape.ProtectionShape;
import de.martenschaefer.serverutils.region.shape.RegionShapes;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.jetbrains.annotations.NotNull;

public record Region(String key, int level, RegionShapes shapes) implements Comparable<Region> {
    public static final Codec<Region> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.STRING.fieldOf("key").forGetter(region -> region.key),
        Codec.INT.fieldOf("level").forGetter(region -> region.level),
        RegionShapes.CODEC.fieldOf("shapes").forGetter(region -> region.shapes)
    ).apply(instance, Region::new));

    public Region withAddedShape(String name, ProtectionShape shape) {
        var newShapes = this.shapes.withShape(name, shape);
        return new Region(this.key, this.level, newShapes);
    }

    public Region withReplacedShape(String name, ProtectionShape shape) {
        RegionShapes shapes = this.shapes.withShapeReplaced(name, shape);
        return new Region(this.key, this.level, shapes);
    }

    public Region withRemovedShape(String name) {
        var newShapes = this.shapes.removeShape(name);

        if (this.shapes == newShapes) {
            return this;
        }

        return new Region(this.key, this.level, newShapes);
    }

    public Region withLevel(int level) {
        return new Region(this.key, level, this.shapes);
    }

    @Override
    public int compareTo(@NotNull Region o) {
        int levelCompare = Integer.compare(o.level, this.level);

        if (levelCompare != 0) {
            return levelCompare;
        } else {
            return this.key.compareTo(o.key);
        }
    }

    public static Region create(String key) {
        return new Region(key, 0, new RegionShapes());
    }
}
