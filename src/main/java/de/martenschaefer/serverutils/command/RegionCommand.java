package de.martenschaefer.serverutils.command;

import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import net.minecraft.command.argument.BlockPosArgumentType;
import net.minecraft.command.argument.DimensionArgumentType;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import de.martenschaefer.serverutils.ServerUtilsMod;
import de.martenschaefer.serverutils.region.Region;
import de.martenschaefer.serverutils.region.RegionPersistentState;
import de.martenschaefer.serverutils.region.shape.ProtectionShape;
import de.martenschaefer.serverutils.region.shape.RegionShapes;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import me.lucko.fabric.api.permissions.v0.Permissions;

public final class RegionCommand {
    private static final DynamicCommandExceptionType REGION_ALREADY_EXISTS = new DynamicCommandExceptionType(id ->
        new LiteralMessage("Region with the id '" + id + "' already exists!"));

    public static final String PERMISSION_ROOT = ".command.region.root";

    public static final String[] PERMISSIONS = new String[] {
        PERMISSION_ROOT,
        ".command.region.add",
        ".command.region.remove",
        ".command.region.list",
    };

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        // TODO Add remaining commands
        dispatcher.register(CommandManager.literal("region")
            .requires(Permissions.require(ServerUtilsMod.MODID + PERMISSION_ROOT, 3))
            .then(CommandManager.literal("add")
                .requires(Permissions.require(ServerUtilsMod.MODID + ".command.region.add", true))
                .then(CommandManager.argument("name", StringArgumentType.string())
                    .then(CommandManager.literal("with")
                        .then(CommandManager.literal("universe"))
                        .then(CommandManager.argument("dimension", DimensionArgumentType.dimension()))
                        .then(CommandManager.argument("min", BlockPosArgumentType.blockPos())
                            .then(CommandManager.argument("max", BlockPosArgumentType.blockPos())
                                .executes(RegionCommand::executeAddWithLocalBox))))))
            .then(CommandManager.literal("remove")
                .requires(Permissions.require(ServerUtilsMod.MODID + ".command.region.remove", true)))
            .then(CommandManager.literal("list")
                .requires(Permissions.require(ServerUtilsMod.MODID + ".command.region.list", true))
                .executes(RegionCommand::executeList)));
    }

    private static int executeAddWithLocalBox(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        RegistryKey<World> dimension = context.getSource().getWorld().getRegistryKey();
        BlockPos min = BlockPosArgumentType.getBlockPos(context, "min");
        BlockPos max = BlockPosArgumentType.getBlockPos(context, "max");
        return addRegion(context, region -> region.addShape(region.getKey(), ProtectionShape.box(dimension, min, max)));
    }

    private static int addRegion(CommandContext<ServerCommandSource> context, UnaryOperator<Region> operator) throws CommandSyntaxException {
        var key = StringArgumentType.getString(context, "authority");

        ServerCommandSource source = context.getSource();
        RegionPersistentState regionState = RegionPersistentState.get(source.getServer());
        Region region = operator.apply(Region.create(key));

        if (regionState.addRegion(region)) {
            RegionShapes shapes = region.getShapes();

            if (shapes.isEmpty()) {
                source.sendFeedback(Text.literal("Added empty region as '" + key + "'"), true);
            } else {
                source.sendFeedback(Text.literal("Added region as '" + key + "' with ").append(shapes.displayShort()), true);
            }

            source.sendFeedback(Text.literal("Run ")
                    .append(Text.literal("/region shape start").formatted(Formatting.GRAY))
                    .append(" to include additional shapes in this authority"),
                false
            );

            return Command.SINGLE_SUCCESS;
        } else {
            throw REGION_ALREADY_EXISTS.create(key);
        }
    }

    private static int executeRemove(CommandContext<ServerCommandSource> context) {
        return 0;
    }

    private static int executeList(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        RegionPersistentState regionState = RegionPersistentState.get(source.getServer());

        source.sendFeedback(Text.empty()
            .append(Text.literal("Regions:\n"))
            .append(Text.literal(regionState.getRegions().stream()
                .map(Region::getKey).map(key -> "- " + key)
                .collect(Collectors.joining("\n")))), false);

        return Command.SINGLE_SUCCESS;
    }
}
