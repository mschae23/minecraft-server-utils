package de.martenschaefer.serverutils.region;

import net.fabricmc.fabric.api.util.TriState;
import de.martenschaefer.serverutils.region.shape.ProtectionShape;
import de.martenschaefer.serverutils.region.shape.RegionShapes;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.jetbrains.annotations.NotNull;

public class Region implements Comparable<Region> {
    public static final Codec<Region> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.STRING.fieldOf("key").forGetter(region -> region.key),
        Codec.INT.fieldOf("level").forGetter(region -> region.level),
        RegionShapes.CODEC.fieldOf("shapes").forGetter(region -> region.shapes)
    ).apply(instance, Region::new));

    private final String key;
    private final int level;
    private final RegionShapes shapes;
    private final TriState defaultRuleResult;

    public Region(String key, int level, RegionShapes shapes) {
        this.key = key;
        this.level = level;
        this.shapes = shapes;
        this.defaultRuleResult = TriState.DEFAULT;
    }

    public String getKey() {
        return this.key;
    }

    public int getLevel() {
        return this.level;
    }

    public RegionShapes getShapes() {
        return this.shapes;
    }

    public TriState getDefaultRuleResult() {
        return this.defaultRuleResult;
    }

    public Region addShape(String name, ProtectionShape shape) {
        var newShapes = this.shapes.withShape(name, shape);
        return new Region(this.key, this.level, newShapes);
    }

    public Region removeShape(String name) {
        var newShapes = this.shapes.removeShape(name);

        if (this.shapes == newShapes) {
            return this;
        }

        return new Region(this.key, this.level, newShapes);
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
