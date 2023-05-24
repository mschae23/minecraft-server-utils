package de.martenschaefer.serverutils.region;

import java.util.Iterator;
import java.util.Set;
import java.util.stream.Stream;
import net.minecraft.registry.RegistryKey;
import net.minecraft.world.World;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class IndexedRegionMap implements RegionMap {
    private final RegionMap main = new SortedRegionHashMap();
    // private final Reference2ObjectMap<RegistryKey<World>, DimensionMap> byDimension = new Reference2ObjectOpenHashMap<>();

    public void addDimension(RegistryKey<World> dimension) {
        // var source = EventSource.allOf(dimension);
        //
        // var dimensionMap = new DimensionMap(dimension);
        // for (var authority : this.main) {
        //     if (authority.getEventFilter().accepts(source)) {
        //         dimensionMap.add(authority);
        //     }
        // }
        //
        // this.byDimension.put(dimension, dimensionMap);
    }

    public void removeDimension(RegistryKey<World> dimension) {
        // this.byDimension.remove(dimension);
    }

    // public Iterable<Region> select(RegistryKey<World> dimension, StimulusEvent<?> event) {
    //     var dimensionMap = this.byDimension.get(dimension);
    //     if (dimensionMap != null) {
    //         var map = dimensionMap.byEvent.get(event);
    //         if (map != null) {
    //             return map;
    //         }
    //     }
    //     return Collections.emptyList();
    // }

    @Override
    public void clear() {
        this.main.clear();
        // this.byDimension.clear();
    }

    @Override
    public boolean add(Region authority) {
        if (this.main.add(authority)) {
            // this.addToDimension(authority);
            return true;
        }

        return false;
    }

    @Override
    public boolean replace(Region from, Region to) {
        if (this.main.replace(from, to)) {
            // this.replaceInDimension(from, to);
            return true;
        }

        return false;
    }

    @Override
    @Nullable
    public Region remove(String key) {
        var authority = this.main.remove(key);

        if (authority != null) {
            // this.removeFromDimension(key);
            return authority;
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

    // private void addToDimension(Region authority) {
    //     var filter = authority.getEventFilter();
    //     for (var entry : Reference2ObjectMaps.fastIterable(this.byDimension)) {
    //         var dimension = entry.getKey();
    //         if (filter.accepts(EventSource.allOf(dimension))) {
    //             var dimensionMap = entry.getValue();
    //             dimensionMap.add(authority);
    //         }
    //     }
    // }

    // private void replaceInDimension(Region from, Region to) {
    //     var fromFilter = from.getEventFilter();
    //     var toFilter = to.getEventFilter();
    //
    //     for (var dimensionMap : this.byDimension.values()) {
    //         boolean fromIncluded = fromFilter.accepts(dimensionMap.eventSource);
    //         boolean toIncluded = toFilter.accepts(dimensionMap.eventSource);
    //         if (fromIncluded && toIncluded) {
    //             dimensionMap.replace(from, to);
    //         } else if (fromIncluded) {
    //             dimensionMap.remove(from.getKey());
    //         } else if (toIncluded) {
    //             dimensionMap.add(to);
    //         }
    //     }
    // }

    // private void removeFromDimension(String key) {
    //     for (var authorities : this.byDimension.values()) {
    //         authorities.remove(key);
    //     }
    // }
}
