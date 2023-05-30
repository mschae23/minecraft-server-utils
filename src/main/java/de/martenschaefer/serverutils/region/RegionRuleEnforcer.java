package de.martenschaefer.serverutils.region;

import java.util.stream.Stream;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.network.ServerPlayerEntity;
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
import de.martenschaefer.serverutils.ModUtils;
import de.martenschaefer.serverutils.ServerUtilsMod;
import de.martenschaefer.serverutils.region.shape.ProtectionContext;
import net.luckperms.api.util.Tristate;

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
    };

    private RegionRuleEnforcer() {
    }

    public static ActionResult onBlockBreak(PlayerEntity player, BlockPos pos) {
        return onEvent(player, Vec3d.ofCenter(pos), "block.break");
    }

    public static ActionResult onBlockPlace(PlayerEntity player, BlockPos pos) {
        return onEvent(player, Vec3d.ofCenter(pos), "block.place");
    }

    public static ActionResult onBlockUse(PlayerEntity player, BlockPos pos) {
        return onEvent(player, Vec3d.ofCenter(pos), "block.use", true);
    }

    public static TypedActionResult<ItemStack> onItemUse(PlayerEntity player, Hand hand, Vec3d pos) {
        return new TypedActionResult<>(onEvent(player, pos, "item.use", true), player.getStackInHand(hand));
    }

    public static ActionResult onWorldModify(PlayerEntity player, BlockPos pos) {
        return onEvent(player, Vec3d.ofCenter(pos), "world.modify", true);
    }

    public static ActionResult onNetherPortalUse(PlayerEntity player, Vec3d pos) {
        return onEvent(player, pos, "portal.nether.use");
    }

    public static ActionResult onEndPortalUse(PlayerEntity player, Vec3d pos) {
        return onEvent(player, pos, "portal.end.use");
    }

    public static ActionResult onEvent(PlayerEntity player, Vec3d pos, String action) {
        if (player instanceof ServerPlayerEntity serverPlayer) {
            RegistryKey<World> dimension = serverPlayer.getWorld().getRegistryKey();
            ProtectionContext protectionContext = new ProtectionContext(dimension, pos);
            EnforcementContext context = new EnforcementContext(RegionPersistentState.get(serverPlayer.getWorld().getServer()), serverPlayer, protectionContext);

            boolean result = checkPermission(serverPlayer, context.regionState().findRegion(context.context()), action);
            onCheckedPermission(serverPlayer, result);

            return result ? ActionResult.PASS : ActionResult.FAIL;
        }

        return ActionResult.PASS;
    }

    public static ActionResult onEvent(PlayerEntity player, Vec3d pos, String action, boolean syncInventory) {
        ActionResult result = onEvent(player, pos, action);

        if (syncInventory && result == ActionResult.FAIL) {
            // Sync the player's inventory, as it may have used the item already.
            player.getInventory().markDirty();
            player.playerScreenHandler.updateToClient();
        }

        return result;
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

    private static boolean checkPermission(ServerPlayerEntity player, Stream<Region> regions, String action) {
        return regions.map(Region::key).map(key -> getPermission(key, action))
            .map(permission -> ModUtils.getLuckPerms().getPlayerAdapter(ServerPlayerEntity.class).getPermissionData(player).checkPermission(permission))
            .filter(state -> state != Tristate.UNDEFINED).findFirst().orElse(Tristate.TRUE).asBoolean();
    }

    private static void onCheckedPermission(ServerPlayerEntity player, boolean result) {
        if (!result) {
            player.sendMessageToClient(DENIED_TEXT, true);
        }
    }

    private record EnforcementContext(RegionPersistentState regionState,
                                      ServerPlayerEntity player,
                                      ProtectionContext context) {
    }
}
