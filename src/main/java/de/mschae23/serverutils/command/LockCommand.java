/*
 * Copyright (C) 2024  mschae23
 *
 * This file is part of Server utils.
 *
 * Server utils is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package de.mschae23.serverutils.command;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.LockableContainerBlockEntity;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.BlockPosArgumentType;
import net.minecraft.command.argument.NbtCompoundArgumentType;
import net.minecraft.inventory.ContainerLock;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtOps;
import net.minecraft.predicate.item.ItemPredicate;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import de.mschae23.serverutils.ModUtils;
import de.mschae23.serverutils.ServerUtilsMod;
import de.mschae23.serverutils.holder.LockPermissionHolder;
import me.lucko.fabric.api.permissions.v0.Permissions;

public final class LockCommand {
    public static final String PERMISSION_ROOT = ".command.lock.root";

    public static final String[] PERMISSIONS = new String[] {
        PERMISSION_ROOT,
        ".command.lock.item",
        ".command.lock.permission",
    };

    static final DynamicCommandExceptionType NO_CONTAINER_EXCEPTION = new DynamicCommandExceptionType(pos ->
        Text.empty().append("There is no lockable container at ").append(ModUtils.getCoordinateTextUnstyled((BlockPos) pos))
            .append("."));

    private static final DynamicCommandExceptionType INVALID_PREDICATE_EXCEPTION = new DynamicCommandExceptionType(message ->
        Text.empty().append("Failed to decode item predicate: ").append((String) message));

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess) {
        dispatcher.register(CommandManager.literal("lock")
            .requires(Permissions.require(ServerUtilsMod.MODID + PERMISSION_ROOT, 2))
            .then(CommandManager.argument("pos", BlockPosArgumentType.blockPos())
                .then(CommandManager.literal("item")
                    .requires(Permissions.require(ServerUtilsMod.MODID + ".command.lock.item", true))
                    .then(CommandManager.argument("predicate", NbtCompoundArgumentType.nbtCompound())
                        .executes(LockCommand::executeItemPredicate)))
                .then(CommandManager.literal("permission")
                    .requires(Permissions.require(ServerUtilsMod.MODID + ".command.lock.permission", true))
                    .then(CommandManager.argument("permission", StringArgumentType.word())
                        .executes(LockCommand::executePermission)))));
    }

    private static int executeItemPredicate(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        BlockPos pos = BlockPosArgumentType.getBlockPos(context, "pos");
        NbtCompound itemPredicateNbt = NbtCompoundArgumentType.getNbtCompound(context, "predicate");

        ServerWorld world = context.getSource().getWorld();
        BlockState state = world.getBlockState(pos);

        if (state.hasBlockEntity()) {
            BlockEntity entity = world.getBlockEntity(pos);

            if (entity instanceof LockableContainerBlockEntity locked) {
                DataResult<Pair<ItemPredicate, NbtElement>> predicateResult = ItemPredicate.CODEC.decode(world.getRegistryManager().getOps(NbtOps.INSTANCE), itemPredicateNbt);
                ItemPredicate predicate;

                if (predicateResult.isSuccess()) {
                    predicate = predicateResult.getOrThrow().getFirst();
                } else {
                    throw INVALID_PREDICATE_EXCEPTION.create(predicateResult.error().map(DataResult.Error::message).orElse("Unknown error"));
                }

                ContainerLock lock = locked.lock;
                locked.lock = new ContainerLock(predicate);
                ((LockPermissionHolder) locked.lock).setLockPermission(((LockPermissionHolder) lock).getLockPermission());
                locked.markDirty();

                context.getSource().sendFeedback(() -> Text.empty().append(Text.literal("Locked container at "))
                    .append(ModUtils.getCoordinateText(pos)).append("."), true);
                return Command.SINGLE_SUCCESS;
            }
        }

        throw NO_CONTAINER_EXCEPTION.create(pos);
    }

    private static int executePermission(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        BlockPos pos = BlockPosArgumentType.getBlockPos(context, "pos");
        String permission = StringArgumentType.getString(context, "permission");

        ServerWorld world = context.getSource().getWorld();
        BlockState state = world.getBlockState(pos);

        if (state.hasBlockEntity()) {
            BlockEntity entity = world.getBlockEntity(pos);

            if (entity instanceof LockableContainerBlockEntity locked) {
                ContainerLock lock = new ContainerLock(locked.lock.predicate());
                ((LockPermissionHolder) lock).setLockPermission(permission);
                locked.lock = lock;
                locked.markDirty();

                context.getSource().sendFeedback(() -> Text.empty().append(Text.literal("Locked container at "))
                    .append(ModUtils.getCoordinateText(pos)).append("."), true);
                return Command.SINGLE_SUCCESS;
            }
        }

        throw NO_CONTAINER_EXCEPTION.create(pos);
    }
}
