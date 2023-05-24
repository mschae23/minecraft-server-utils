package de.martenschaefer.serverutils.region;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtOps;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateManager;
import de.martenschaefer.serverutils.ServerUtilsMod;
import com.mojang.datafixers.util.Pair;
import org.jetbrains.annotations.Nullable;

public class RegionPersistentState extends PersistentState {
    public static final String ID = ServerUtilsMod.MODID + "_region";

    private final IndexedRegionMap regions;

    private RegionPersistentState() {
        this.regions = new IndexedRegionMap();
    }

    @SuppressWarnings("UnusedReturnValue")
    public boolean addRegion(Region region) {
        return this.regions.add(region);
    }

    public boolean removeRegion(Region authority) {
        return this.removeRegion(authority.getKey()) != null;
    }

    public Region removeRegion(String key) {
        return this.regions.remove(key);
    }

    public void replaceRegion(Region from, Region to) {
        this.regions.replace(from, to);
    }

    @Nullable
    public Region getRegionByKey(String key) {
        return this.regions.byKey(key);
    }

    // Iterable<Region> selectRegions(RegistryKey<World> dimension, StimulusEvent<?> event) {
    //     return this.regions.select(dimension, event);
    // }

    @Override
    public boolean isDirty() {
        return true;
    }

    public RegionMap getRegions() {
        return this.regions;
    }

    @Override
    public NbtCompound writeNbt(NbtCompound root) {
        NbtList regions = new NbtList();

        for (Region region : this.regions) {
            var result = Region.CODEC.encodeStart(NbtOps.INSTANCE, region);
            result.result().ifPresent(regions::add);
        }

        root.put("regions", regions);
        return root;
    }

    private static RegionPersistentState readNbt(NbtCompound root) {
        RegionPersistentState regionState = new RegionPersistentState();

        NbtList regions = root.getList("authorities", NbtElement.COMPOUND_TYPE);

        for (NbtElement regionElement : regions) {
            Region.CODEC.decode(NbtOps.INSTANCE, regionElement)
                .map(Pair::getFirst)
                .result()
                .ifPresent(regionState::addRegion);
        }

        return regionState;
    }

    public static RegionPersistentState get(MinecraftServer server) {
        PersistentStateManager stateManager = server.getOverworld().getPersistentStateManager();
        return stateManager.getOrCreate(RegionPersistentState::readNbt, RegionPersistentState::new, ID);
    }
}
