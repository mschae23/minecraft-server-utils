package de.martenschaefer.serverutils.region;

import java.util.Iterator;
import java.util.Set;
import java.util.stream.Stream;
import net.minecraft.registry.RegistryKey;
import net.minecraft.world.World;
import de.martenschaefer.serverutils.region.shape.ProtectionContext;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectMaps;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class IndexedRegionMap implements RegionMap {
    private final RegionMap main = new SortedRegionHashMap();
    private final Reference2ObjectMap<RegistryKey<World>, SortedRegionHashMap> byDimension = new Reference2ObjectOpenHashMap<>();

    public void addDimension(RegistryKey<World> dimension) {
        SortedRegionHashMap map = new SortedRegionHashMap();

        for (Region region : this.main) {
            if (region.shapes().testDimension(dimension)) {
                map.add(region);
            }
        }

        this.byDimension.put(dimension, map);
    }

    public void removeDimension(RegistryKey<World> dimension) {
        this.byDimension.remove(dimension);
    }

    public Stream<Region> findRegion(ProtectionContext context) {
        SortedRegionHashMap map = this.byDimension.get(context.dimension());

        if (map != null) {
            return map.findRegion(context);
        }

        return Stream.empty();
    }

    @Override
    public void clear() {
        this.main.clear();
        this.byDimension.clear();
    }

    @Override
    public boolean add(Region authority) {
        if (this.main.add(authority)) {
            this.addToDimension(authority);
            return true;
        }

        return false;
    }

    @Override
    public boolean replace(Region from, Region to) {
        if (this.main.replace(from, to)) {
            this.replaceInDimension(from, to);
            return true;
        }

        return false;
    }

    @Override
    @Nullable
    public Region remove(String key) {
        var region = this.main.remove(key);

        if (region != null) {
            this.removeFromDimension(key);
            return region;
        }

        return null;
    }

    @Override
    @Nullable
    public Region byKey(String key) {
        return this.main.byKey(key);
    }

    @Override
    public boolean contains(String key) {
        return this.main.contains(key);
    }

    @Override
    public Set<String> keySet() {
        return this.main.keySet();
    }

    @Override
    public int size() {
        return this.main.size();
    }

    @Override
    public Iterable<Object2ObjectMap.Entry<String, Region>> entries() {
        return this.main.entries();
    }

    @NotNull
    @Override
    public Iterator<Region> iterator() {
        return this.main.iterator();
    }

    @Override
    public Stream<Region> stream() {
        return this.main.stream();
    }

    private void addToDimension(Region region) {
        for (Reference2ObjectMap.Entry<RegistryKey<World>, SortedRegionHashMap> entry : Reference2ObjectMaps.fastIterable(this.byDimension)) {
            RegistryKey<World> dimension = entry.getKey();

            if (region.shapes().testDimension(dimension)) {
                SortedRegionHashMap map = entry.getValue();
                map.add(region);
            }
        }
    }

    private void replaceInDimension(Region from, Region to) {
        for (Reference2ObjectMap.Entry<RegistryKey<World>, SortedRegionHashMap> entry : Reference2ObjectMaps.fastIterable(this.byDimension)) {
            boolean fromIncluded = from.shapes().testDimension(entry.getKey());
            boolean toIncluded = to.shapes().testDimension(entry.getKey());

            if (fromIncluded && toIncluded) {
                entry.getValue().replace(from, to);
            } else if (fromIncluded) {
                entry.getValue().remove(from.key());
            } else if (toIncluded) {
                entry.getValue().add(to);
            }
        }
    }

    private void removeFromDimension(String key) {
        for (SortedRegionHashMap map : this.byDimension.values()) {
            map.remove(key);
        }
    }
}
