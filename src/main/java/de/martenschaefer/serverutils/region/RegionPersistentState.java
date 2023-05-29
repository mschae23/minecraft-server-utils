package de.martenschaefer.serverutils.region;

import java.util.stream.Stream;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtOps;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateManager;
import net.minecraft.world.World;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.util.TriState;
import de.martenschaefer.serverutils.ServerUtilsMod;
import de.martenschaefer.serverutils.region.shape.ProtectionContext;
import com.mojang.datafixers.util.Pair;
import me.lucko.fabric.api.permissions.v0.Permissions;
import org.jetbrains.annotations.Nullable;

public class RegionPersistentState extends PersistentState {
    public static final String ID = ServerUtilsMod.MODID + "_region";
    private static final Text DENIED_TEXT = Text.literal("You cannot do that in this region!");

    private final IndexedRegionMap regions;

    private RegionPersistentState() {
        this.regions = new IndexedRegionMap();
    }

    @SuppressWarnings("UnusedReturnValue")
    public boolean addRegion(Region region) {
        return this.regions.add(region);
    }

    public boolean removeRegion(Region region) {
        return this.removeRegion(region.key()) != null;
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

    @Override
    public boolean isDirty() {
        return true;
    }

    public RegionMap getRegions() {
        return this.regions;
    }

    public Stream<Region> findRegion(ProtectionContext context) {
        return this.getRegions().findRegion(context);
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

        NbtList regions = root.getList("regions", NbtElement.COMPOUND_TYPE);

        for (NbtElement regionElement : regions) {
            Region.CODEC.decode(NbtOps.INSTANCE, regionElement)
                .map(Pair::getFirst)
                .result()
                .ifPresent(regionState::addRegion);
        }

        return regionState;
    }

    private static String getPermission(String key, String action) {
        return ServerUtilsMod.MODID + ".region." + key + "." + action;
    }

    private static boolean checkPermission(PlayerEntity player, Stream<Region> regions, String action) {
        return regions.map(Region::key).map(key -> getPermission(key, action))
            .map(permission -> Permissions.getPermissionValue(player, permission))
            .filter(state -> state != TriState.DEFAULT).findFirst().orElse(TriState.TRUE).get();
    }

    private static void onCheckedPermission(PlayerEntity player, boolean result) {
        if (!result && player instanceof ServerPlayerEntity serverPlayer) {
            serverPlayer.sendMessageToClient(DENIED_TEXT, true);
        }
    }

    public void onWorldLoad(ServerWorld world) {
        this.regions.addDimension(world.getRegistryKey());
    }

    public void onWorldUnload(ServerWorld world) {
        this.regions.removeDimension(world.getRegistryKey());
    }

    public ActionResult onAttackBlock(PlayerEntity player, ServerWorld world, Hand hand, BlockPos pos, Direction direction) {
        ProtectionContext protectionContext = new ProtectionContext(world.getRegistryKey(), Vec3d.ofCenter(pos));
        boolean result = checkPermission(player, this.findRegion(protectionContext), "block.break");

        onCheckedPermission(player, result);
        return result ? ActionResult.PASS : ActionResult.FAIL;
    }

    public static ActionResult onAttackBlockEvent(PlayerEntity player, World world, Hand hand, BlockPos pos, Direction direction) {
        if (!world.isClient && world instanceof ServerWorld serverWorld) {
            return get(world.getServer()).onAttackBlock(player, serverWorld, hand, pos, direction);
        }

        return ActionResult.PASS;
    }

    public ActionResult onPlaceBlock(ItemPlacementContext context, ServerWorld world) {
        ProtectionContext protectionContext = new ProtectionContext(world.getRegistryKey(), Vec3d.ofCenter(context.getBlockPos()));
        boolean result = checkPermission(context.getPlayer(), this.findRegion(protectionContext), "block.place");

        onCheckedPermission(context.getPlayer(), result);

        if (!result && context.getPlayer() != null) {
            // Sync the player's inventory, as it may have used the item already.
            context.getPlayer().getInventory().markDirty();
            context.getPlayer().playerScreenHandler.updateToClient();
        }

        return result ? ActionResult.PASS : ActionResult.FAIL;
    }

    public static ActionResult onPlaceBlockEvent(ItemPlacementContext context) {
        if (!context.getWorld().isClient && context.getWorld() instanceof ServerWorld world) {
            return get(world.getServer()).onPlaceBlock(context, world);
        }

        return ActionResult.PASS;
    }

    public ActionResult onUseBlock(ServerPlayerEntity player, ServerWorld world, Hand hand, BlockHitResult hitResult) {
        if (player.getStackInHand(hand).getItem() instanceof BlockItem) {
            return ActionResult.PASS;
        }

        ProtectionContext protectionContext = new ProtectionContext(world.getRegistryKey(), Vec3d.ofCenter(hitResult.getBlockPos()));
        boolean result = checkPermission(player, this.findRegion(protectionContext), "block.interact");

        onCheckedPermission(player, result);
        return result ? ActionResult.PASS : ActionResult.FAIL;
    }

    public static ActionResult onUseBlockEvent(ServerPlayerEntity player, World world, Hand hand, BlockHitResult hitResult) {
        if (!world.isClient && world instanceof ServerWorld serverWorld) {
            return get(world.getServer()).onUseBlock(player, serverWorld, hand, hitResult);
        }

        return ActionResult.PASS;
    }

    public static RegionPersistentState get(MinecraftServer server) {
        PersistentStateManager stateManager = server.getOverworld().getPersistentStateManager();
        return stateManager.getOrCreate(RegionPersistentState::readNbt, RegionPersistentState::new, ID);
    }

    public static void init() {
        ServerWorldEvents.LOAD.register((server, world) -> RegionPersistentState.get(server).onWorldLoad(world));
        ServerWorldEvents.UNLOAD.register((server, world) -> RegionPersistentState.get(server).onWorldUnload(world));

        AttackBlockCallback.EVENT.register(RegionPersistentState::onAttackBlockEvent);
    }
}
