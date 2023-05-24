package de.martenschaefer.serverutils.command;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.LockableContainerBlockEntity;
import net.minecraft.command.argument.BlockPosArgumentType;
import net.minecraft.inventory.ContainerLock;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import de.martenschaefer.serverutils.ModUtils;
import de.martenschaefer.serverutils.ServerUtilsMod;
import de.martenschaefer.serverutils.holder.LockPermissionHolder;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import me.lucko.fabric.api.permissions.v0.Permissions;

public final class UnlockCommand {
    public static final String PERMISSION_ROOT = ".command.unlock.root";

    public static final String[] PERMISSIONS = new String[] {
        PERMISSION_ROOT,
        ".command.unlock.all",
        ".command.unlock.item_name",
        ".command.unlock.permission",
    };

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("unlock")
            .requires(Permissions.require(ServerUtilsMod.MODID + PERMISSION_ROOT, 2))
            .then(CommandManager.argument("pos", BlockPosArgumentType.blockPos())
                .then(CommandManager.literal("all")
                    .requires(Permissions.require(ServerUtilsMod.MODID + ".command.unlock.all", true))
                    .executes(context -> execute(context, true, true)))
                .then(CommandManager.literal("item_name")
                    .requires(Permissions.require(ServerUtilsMod.MODID + ".command.unlock.item_name", true))
                    .executes(context -> execute(context, true, false)))
                .then(CommandManager.literal("permission")
                    .requires(Permissions.require(ServerUtilsMod.MODID + ".command.unlock.permission", true))
                    .executes(context -> execute(context, false, true)))
            ));
    }

    private static int execute(CommandContext<ServerCommandSource> context, boolean itemName, boolean permission) { // throws CommandSyntaxException {
        BlockPos pos = BlockPosArgumentType.getBlockPos(context, "pos");

        ServerWorld world = context.getSource().getWorld();
        BlockState state = world.getBlockState(pos);

        if (state.hasBlockEntity()) {
            BlockEntity entity = world.getBlockEntity(pos);

            if (entity instanceof LockableContainerBlockEntity locked) {
                ContainerLock lock;

                if (itemName) {
                    lock = new ContainerLock("");
                } else {
                    lock = new ContainerLock(locked.lock.key);
                }

                if (!permission) {
                    ((LockPermissionHolder) lock).setLockPermission(((LockPermissionHolder) locked.lock).getLockPermission());
                }

                locked.lock = lock;
                locked.markDirty();

                context.getSource().sendFeedback(Text.empty().append(Text.literal("Unlocked container at "))
                    .append(ModUtils.getCoordinateText(pos)).append("."), true);
                return Command.SINGLE_SUCCESS;
            }
        }

        throw LockCommand.NO_CONTAINER_EXCEPTION.create(pos);
    }
}
