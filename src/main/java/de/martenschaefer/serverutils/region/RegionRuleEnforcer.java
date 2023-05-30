package de.martenschaefer.serverutils.region;

import java.util.stream.Stream;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.util.TriState;
import de.martenschaefer.serverutils.ServerUtilsMod;
import de.martenschaefer.serverutils.region.shape.ProtectionContext;
import me.lucko.fabric.api.permissions.v0.Permissions;

public final class RegionRuleEnforcer {
    private static final Text DENIED_TEXT = Text.literal("You cannot do that in this region!");

    private RegionRuleEnforcer() {
    }

    public static ActionResult onBlockBreak(PlayerEntity player, BlockPos pos) {
        return onEvent(player, Vec3d.ofCenter(pos), "block.break");
    }

    public static ActionResult onBlockPlace(PlayerEntity player, BlockPos pos) {
        return onEvent(player, Vec3d.ofCenter(pos), "block.place");
    }

    public static ActionResult onBlockUse(PlayerEntity player, BlockPos pos) {
        return onEvent(player, Vec3d.ofCenter(pos), "block.use");
    }

    public static ActionResult onNetherPortalUse(PlayerEntity player, Vec3d pos) {
        return onEvent(player, pos, "portal.nether.use");
    }

    public static ActionResult onEndPortalUse(PlayerEntity player, Vec3d pos) {
        return onEvent(player, pos, "portal.end.use");
    }

    public static void init() {
        AttackBlockCallback.EVENT.register((player, world, hand, pos, direction) -> onBlockBreak(player, pos));
        // UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> onBlockUse(player, hitResult.getBlockPos()));
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

    private static String getPermission(String key, String action) {
        return ServerUtilsMod.MODID + ".region." + key + "." + action;
    }

    private static boolean checkPermission(PlayerEntity player, Stream<Region> regions, String action) {
        return regions.map(Region::key).map(key -> getPermission(key, action))
            .map(permission -> Permissions.getPermissionValue(player, permission))
            .filter(state -> state != TriState.DEFAULT).findFirst().orElse(TriState.TRUE).get();
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
