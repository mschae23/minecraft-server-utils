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
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import me.lucko.fabric.api.permissions.v0.Permissions;

public class LockCommand {
    public static final String PERMISSION_ROOT = ".command.lock.root";

    public static final String[] PERMISSIONS = new String[] {
        PERMISSION_ROOT,
        ".command.lock.item_name",
        ".command.lock.permission",
    };

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("lock")
            .requires(Permissions.require(ServerUtilsMod.MODID + PERMISSION_ROOT, 2))
            .then(CommandManager.argument("pos", BlockPosArgumentType.blockPos())
                .then(CommandManager.literal("item_name")
                    .requires(Permissions.require(ServerUtilsMod.MODID + ".command.lock.item_name", true))
                    .then(CommandManager.argument("item_name", StringArgumentType.string())
                        .executes(LockCommand::executeItemName)))
                .then(CommandManager.literal("permission")
                    .requires(Permissions.require(ServerUtilsMod.MODID + ".command.lock.permission", true))
                    .then(CommandManager.argument("permission", StringArgumentType.word())
                        .executes(LockCommand::executePermission)))));
    }

    private static int executeItemName(CommandContext<ServerCommandSource> context) { // throws CommandSyntaxException {
        BlockPos pos = BlockPosArgumentType.getBlockPos(context, "pos");
        String itemName = StringArgumentType.getString(context, "item_name");

        ServerWorld world = context.getSource().getWorld();
        BlockState state = world.getBlockState(pos);

        if (state.hasBlockEntity()) {
            BlockEntity entity = world.getBlockEntity(pos);

            if (entity instanceof LockableContainerBlockEntity locked) {
                ContainerLock lock = locked.lock;
                locked.lock = new ContainerLock(itemName);
                ((LockPermissionHolder) locked.lock).setLockPermission(((LockPermissionHolder) lock).getLockPermission());
                locked.markDirty();

                context.getSource().sendFeedback(Text.empty().append(Text.literal("Locked container at "))
                    .append(ModUtils.getCoordinateText(pos)).append("."), true);
                return 15;
            }
        }

        context.getSource().sendFeedback(Text.empty().append("There is no container at ").append(ModUtils.getCoordinateTextUnstyled(pos))
            .append(".").formatted(Formatting.RED), false);
        return 0;
    }

    private static int executePermission(CommandContext<ServerCommandSource> context) { // throws CommandSyntaxException {
        BlockPos pos = BlockPosArgumentType.getBlockPos(context, "pos");
        String permission = StringArgumentType.getString(context, "permission");

        ServerWorld world = context.getSource().getWorld();
        BlockState state = world.getBlockState(pos);

        if (state.hasBlockEntity()) {
            BlockEntity entity = world.getBlockEntity(pos);

            if (entity instanceof LockableContainerBlockEntity locked) {
                ContainerLock lock = new ContainerLock(locked.lock.key);
                ((LockPermissionHolder) lock).setLockPermission(permission);
                locked.lock = lock;
                locked.markDirty();

                context.getSource().sendFeedback(Text.empty().append(Text.literal("Locked container at "))
                    .append(ModUtils.getCoordinateText(pos)).append("."), true);
                return 15;
            }
        }

        context.getSource().sendFeedback(Text.empty().append("There is no container at ").append(ModUtils.getCoordinateTextUnstyled(pos))
            .append(".").formatted(Formatting.RED), false);
        return 0;
    }
}
