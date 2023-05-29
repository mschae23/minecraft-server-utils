package de.martenschaefer.serverutils.region;

import java.util.Iterator;
import java.util.Set;
import java.util.SortedSet;
import java.util.stream.Stream;
import de.martenschaefer.serverutils.region.shape.ProtectionContext;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMaps;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectRBTreeSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class SortedRegionHashMap implements RegionMap {
    private final SortedSet<Region> regions = new ObjectRBTreeSet<>();
    private final Object2ObjectMap<String, Region> byKey = new Object2ObjectOpenHashMap<>();

    @Override
    public void clear() {
        this.regions.clear();
        this.byKey.clear();
    }

    @Override
    public boolean add(Region region) {
        if (this.byKey.put(region.key(), region) == null) {
            this.regions.add(region);
            return true;
        }
        return false;
    }

    @Override
    public boolean replace(Region from, Region to) {
        if (from.key().equals(to.key()) && this.byKey.replace(from.key(), from, to)) {
            this.regions.remove(from);
            this.regions.add(to);
            return true;
        }
        return false;
    }

    @Override
    @Nullable
    public Region remove(String key) {
        var authority = this.byKey.remove(key);
        if (authority != null) {
            this.regions.remove(authority);
            return authority;
        }
        return null;
    }

    @Override
    @Nullable
    public Region byKey(String key) {
        return this.byKey.get(key);
    }

    @Override
    public boolean contains(String key) {
        return this.byKey.containsKey(key);
    }

    @Override
    public Set<String> keySet() {
        return this.byKey.keySet();
    }

    @Override
    public Iterable<Object2ObjectMap.Entry<String, Region>> entries() {
        return Object2ObjectMaps.fastIterable(this.byKey);
    }

    @Override
    public Stream<Region> findRegion(ProtectionContext context) {
        return this.regions.stream().filter(region -> region.shapes().test(context));
    }

    @Override
    public int size() {
        return this.regions.size();
    }

    @Override
    public boolean isEmpty() {
        return this.regions.isEmpty();
    }

    @Override
    @NotNull
    public Iterator<Region> iterator() {
        return this.regions.iterator();
    }

    @Override
    public Stream<Region> stream() {
        return this.regions.stream();
    }
}
