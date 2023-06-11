package de.martenschaefer.serverutils.region;

import java.util.Set;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import de.martenschaefer.serverutils.region.shape.ProtectionContext;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import org.jetbrains.annotations.Nullable;

public interface RegionMap extends Iterable<RegionV2> {
    void clear();

    boolean add(RegionV2 authority);

    boolean replace(RegionV2 from, RegionV2 to);

    @Nullable
    RegionV2 remove(String key);

    @Nullable
    RegionV2 byKey(String key);

    boolean contains(String key);

    Set<String> keySet();

    int size();

    default boolean isEmpty() {
        return this.size() == 0;
    }

    Iterable<Object2ObjectMap.Entry<String, RegionV2>> entries();

    Stream<RegionV2> findRegion(ProtectionContext context);

    default Stream<RegionV2> stream() {
        return StreamSupport.stream(this.spliterator(), false);
    }
}
