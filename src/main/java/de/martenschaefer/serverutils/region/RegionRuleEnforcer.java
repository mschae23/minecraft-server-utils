package de.martenschaefer.serverutils.region;

import java.util.stream.Stream;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.fabricmc.fabric.api.util.TriState;
import de.martenschaefer.serverutils.ModUtils;
import de.martenschaefer.serverutils.ServerUtilsMod;
import de.martenschaefer.serverutils.region.rule.ProtectionRule;
import de.martenschaefer.serverutils.region.shape.ProtectionContext;
import de.martenschaefer.serverutils.util.ServerUtilsUtils;

public final class RegionRuleEnforcer {
    private static final Text DENIED_TEXT = Text.literal("You cannot do that in this region!");

    public static final String[] RULES = new String[] {
        "block.break",
        "block.place",
        "block.use",
        "item.use",
        "world.modify",
        "portal.nether.use",
        "portal.end.use",
        "villager.work",
        "villager.home",
    };

    private RegionRuleEnforcer() {
    }

    public static ActionResult onBlockBreak(PlayerEntity player, BlockPos pos) {
        return onEvent(player, Vec3d.ofCenter(pos), ProtectionRule.BlockBreak);
    }

    public static ActionResult onBlockPlace(PlayerEntity player, BlockPos pos) {
        return onEvent(player, Vec3d.ofCenter(pos), ProtectionRule.BlockPlace);
    }

    public static ActionResult onBlockUse(PlayerEntity player, BlockPos pos) {
        return onEvent(player, Vec3d.ofCenter(pos), ProtectionRule.BlockUse, true);
    }

    public static TypedActionResult<ItemStack> onItemUse(PlayerEntity player, Hand hand, Vec3d pos) {
        return new TypedActionResult<>(onEvent(player, pos, ProtectionRule.ItemUse, true), player.getStackInHand(hand));
    }

    public static ActionResult onWorldModify(PlayerEntity player, BlockPos pos) {
        return onEvent(player, Vec3d.ofCenter(pos), ProtectionRule.WorldModify, true);
    }

    public static ActionResult onNetherPortalUse(PlayerEntity player, Vec3d pos) {
        return onEvent(player, pos, ProtectionRule.PortalNetherUse);
    }

    public static ActionResult onEndPortalUse(PlayerEntity player, Vec3d pos) {
        return onEvent(player, pos, ProtectionRule.PortalEndUse);
    }

    public static ActionResult onVillagerWork(ServerWorld world, BlockPos pos) {
        return onEventGeneric(world, Vec3d.ofCenter(pos), ProtectionRule.VillagerWork);
    }

    public static ActionResult onVillagerHome(ServerWorld world, BlockPos pos) {
        return onEventGeneric(world, Vec3d.ofCenter(pos), ProtectionRule.VillagerHome);
    }

    public static ActionResult onEvent(PlayerEntity player, Vec3d pos, ProtectionRule rule) {
        if (player instanceof ServerPlayerEntity serverPlayer) {
            RegistryKey<World> dimension = serverPlayer.getServerWorld().getRegistryKey();
            ProtectionContext protectionContext = new ProtectionContext(dimension, pos);
            EnforcementContext context = new EnforcementContext(RegionPersistentState.get(serverPlayer.getServerWorld().getServer()), serverPlayer, protectionContext);

            boolean result = checkPermission(serverPlayer, context.regionState().findRegion(context.context()), rule);
            onCheckedPermission(serverPlayer, result);

            return result ? ActionResult.PASS : ActionResult.FAIL;
        }

        return ActionResult.PASS;
    }

    public static ActionResult onEvent(PlayerEntity player, Vec3d pos, ProtectionRule rule, boolean syncInventory) {
        ActionResult result = onEvent(player, pos, rule);

        if (syncInventory && result == ActionResult.FAIL) {
            // Sync the player's inventory, as it may have used the item already.
            player.getInventory().markDirty();
            player.playerScreenHandler.updateToClient();
        }

        return result;
    }

    public static ActionResult onEventGeneric(ServerWorld world, Vec3d pos, ProtectionRule rule) {
        RegistryKey<World> dimension = world.getRegistryKey();
        ProtectionContext protectionContext = new ProtectionContext(dimension, pos);
        RegionPersistentState regionState = RegionPersistentState.get(world.getServer());

        boolean result = checkPermissionGeneric(regionState.findRegion(protectionContext), rule);
        onCheckedPermissionGeneric(world, protectionContext, result);

        return result ? ActionResult.PASS : ActionResult.FAIL;
    }

    public static void init() {
        AttackBlockCallback.EVENT.register((player, world, hand, pos, direction) -> onBlockBreak(player, pos));
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> onBlockUse(player, hitResult.getBlockPos()));
        UseItemCallback.EVENT.register((player, world, hand) -> onItemUse(player, hand, player.getPos()));
    }

    public static String getBasePermission(String key, String action) {
        return ".region." + key + "." + action;
    }

    private static String getPermission(String key, String action) {
        return ServerUtilsMod.MODID + ".region." + key + "." + action;
    }

    private static boolean checkPermission(ServerPlayerEntity player, Stream<RegionV2> regions, ProtectionRule rule) {
        return regions
            .map(region -> ServerUtilsUtils.orTriState(ServerUtilsUtils.toFabricTriState(ModUtils.getLuckPerms().getPlayerAdapter(ServerPlayerEntity.class).getPermissionData(player)
                .checkPermission(getPermission(region.key(), rule.getName()))), region.getDefault(rule)))
            .filter(state -> state != TriState.DEFAULT).findFirst().orElse(TriState.TRUE).get();
    }

    private static boolean checkPermissionGeneric(Stream<RegionV2> regions, ProtectionRule rule) {
        return regions.map(region -> region.getDefault(rule))
            .filter(state -> state != TriState.DEFAULT).findFirst().orElse(TriState.TRUE).get();
    }

    private static void onCheckedPermission(ServerPlayerEntity player, boolean result) {
        if (!result) {
            player.sendMessageToClient(DENIED_TEXT, true);
        }
    }

    @SuppressWarnings("unused")
    private static void onCheckedPermissionGeneric(ServerWorld world, ProtectionContext context, boolean result) {
    }

    private record EnforcementContext(RegionPersistentState regionState,
                                      ServerPlayerEntity player,
                                      ProtectionContext context) {
    }
}
