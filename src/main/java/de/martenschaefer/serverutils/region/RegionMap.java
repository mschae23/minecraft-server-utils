package de.martenschaefer.serverutils.region;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import de.martenschaefer.serverutils.region.shape.ProtectionContext;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import org.jetbrains.annotations.Nullable;

public interface RegionMap extends Iterable<Region> {
    void clear();

    boolean add(Region authority);

    boolean replace(Region from, Region to);

    @Nullable
    Region remove(String key);

    @Nullable
    Region byKey(String key);

    boolean contains(String key);

    Set<String> keySet();

    int size();

    default boolean isEmpty() {
        return this.size() == 0;
    }

    Iterable<Object2ObjectMap.Entry<String, Region>> entries();

    Stream<Region> findRegion(ProtectionContext context);

    default Stream<Region> stream() {
        return StreamSupport.stream(this.spliterator(), false);
    }
}
