package de.martenschaefer.serverutils.command;

import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.command.argument.BlockPosArgumentType;
import net.minecraft.command.argument.DimensionArgumentType;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import de.martenschaefer.serverutils.ServerUtilsMod;
import de.martenschaefer.serverutils.region.Region;
import de.martenschaefer.serverutils.region.RegionPersistentState;
import de.martenschaefer.serverutils.region.shape.ProtectionContext;
import de.martenschaefer.serverutils.region.shape.ProtectionShape;
import de.martenschaefer.serverutils.region.shape.RegionShapes;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import me.lucko.fabric.api.permissions.v0.Permissions;

public final class RegionCommand {
    private static final DynamicCommandExceptionType REGION_ALREADY_EXISTS_EXCEPTION = new DynamicCommandExceptionType(id ->
        new LiteralMessage("Region with the id '" + id + "' already exists"));
    private static final DynamicCommandExceptionType REGION_DOES_NOT_EXIST_EXCEPTION = new DynamicCommandExceptionType(id ->
        new LiteralMessage("Region with the id '" + id + "' does not exist"));

    public static final String PERMISSION_ROOT = ".command.region.root";

    public static final String[] PERMISSIONS = new String[] {
        PERMISSION_ROOT,
        ".command.region.add",
        ".command.region.remove",
        ".command.region.modify",
        ".command.region.info",
        ".command.region.test",
        ".command.region.list",
    };

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        // TODO Add remaining commands
        dispatcher.register(CommandManager.literal("region")
            .requires(Permissions.require(ServerUtilsMod.MODID + PERMISSION_ROOT, 3))
            .then(CommandManager.literal("add")
                .requires(Permissions.require(ServerUtilsMod.MODID + ".command.region.add", true))
                .then(CommandManager.argument("name", StringArgumentType.word())
                    .then(CommandManager.literal("with")
                        .then(CommandManager.literal("universe")
                            .executes(RegionCommand::executeAddWithUniverse))
                        .then(CommandManager.literal("dimension")
                            .then(CommandManager.argument("dimension", DimensionArgumentType.dimension())
                                .executes(RegionCommand::executeAddWithDimension)))
                        .then(CommandManager.literal("pos")
                            .then(CommandManager.argument("min", BlockPosArgumentType.blockPos())
                                .then(CommandManager.argument("max", BlockPosArgumentType.blockPos())
                                    .executes(RegionCommand::executeAddWithLocalBox)))))))
            .then(CommandManager.literal("remove")
                .requires(Permissions.require(ServerUtilsMod.MODID + ".command.region.remove", true))
                .then(CommandManager.argument("name", StringArgumentType.word())
                    .executes(RegionCommand::executeRemove)))
            .then(CommandManager.literal("modify")
                .requires(Permissions.require(ServerUtilsMod.MODID + ".command.region.modify", true))
                .then(CommandManager.argument("name", StringArgumentType.word())
                    .then(CommandManager.literal("shape")
                        .then(CommandManager.literal("replace")
                            .then(CommandManager.literal("with")
                                .then(CommandManager.literal("universe")
                                    .executes(RegionCommand::executeModifyReplaceShapeWithUniverse))
                                .then(CommandManager.literal("dimension")
                                    .then(CommandManager.argument("dimension", DimensionArgumentType.dimension())
                                        .executes(RegionCommand::executeModifyReplaceShapeWithDimension)))
                                .then(CommandManager.literal("pos")
                                    .then(CommandManager.argument("min", BlockPosArgumentType.blockPos())
                                        .then(CommandManager.argument("max", BlockPosArgumentType.blockPos())
                                            .executes(RegionCommand::executeModifyReplaceShapeWithLocalBox)))))))
                    .then(CommandManager.literal("level")
                        .then(CommandManager.argument("level", IntegerArgumentType.integer())
                            .executes(RegionCommand::executeModifyLevel)))))
            .then(CommandManager.literal("info")
                .requires(Permissions.require(ServerUtilsMod.MODID + ".command.region.info", true))
                .then(CommandManager.argument("name", StringArgumentType.word())
                    .executes(RegionCommand::executeInfo)))
            .then(CommandManager.literal("test")
                .requires(Permissions.require(ServerUtilsMod.MODID + ".command.region.test").and(ServerCommandSource::isExecutedByPlayer))
                .executes(RegionCommand::executeTest))
            .then(CommandManager.literal("list")
                .requires(Permissions.require(ServerUtilsMod.MODID + ".command.region.list", true))
                .executes(RegionCommand::executeList)));
    }

    private static int executeAddWithUniverse(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        return addRegion(context, region -> region.withAddedShape(region.key(), ProtectionShape.universe()));
    }

    private static int executeAddWithDimension(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        RegistryKey<World> dimension = DimensionArgumentType.getDimensionArgument(context, "dimension").getRegistryKey();
        return addRegion(context, region -> region.withAddedShape(region.key(), ProtectionShape.dimension(dimension)));
    }

    private static int executeAddWithLocalBox(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        RegistryKey<World> dimension = context.getSource().getWorld().getRegistryKey();
        BlockPos min = BlockPosArgumentType.getBlockPos(context, "min");
        BlockPos max = BlockPosArgumentType.getBlockPos(context, "max");
        return addRegion(context, region -> region.withAddedShape(region.key(), ProtectionShape.box(dimension, min, max)));
    }

    private static int addRegion(CommandContext<ServerCommandSource> context, UnaryOperator<Region> operator) throws CommandSyntaxException {
        String key = StringArgumentType.getString(context, "name");

        ServerCommandSource source = context.getSource();
        RegionPersistentState regionState = RegionPersistentState.get(source.getServer());
        Region region = operator.apply(Region.create(key));

        if (regionState.addRegion(region)) {
            RegionShapes shapes = region.shapes();

            if (shapes.isEmpty()) {
                source.sendFeedback(Text.literal("Added empty region as '" + key + "'"), true);
            } else {
                source.sendFeedback(Text.literal("Added region as '" + key + "' with ").append(shapes.displayShort()), true);
            }

            source.sendFeedback(Text.literal("Run ")
                    .append(Text.literal("/region shape start").formatted(Formatting.GRAY))
                    .append(" to include additional shapes in this region"),
                false
            );

            return Command.SINGLE_SUCCESS;
        } else {
            throw REGION_ALREADY_EXISTS_EXCEPTION.create(key);
        }
    }

    private static int executeRemove(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        String key = StringArgumentType.getString(context, "name");

        ServerCommandSource source = context.getSource();
        RegionPersistentState regionState = RegionPersistentState.get(source.getServer());
        Region region = regionState.removeRegion(key);

        if (region != null) {
            source.sendFeedback(Text.literal("Removed region '" + key + "'"), true);
            return Command.SINGLE_SUCCESS;
        } else {
            throw REGION_DOES_NOT_EXIST_EXCEPTION.create(key);
        }
    }

    private static int executeModifyReplaceShapeWithUniverse(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        return modifyRegion(context, region -> region.withReplacedShape(region.key(), ProtectionShape.universe()));
    }

    private static int executeModifyReplaceShapeWithDimension(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        RegistryKey<World> dimension = DimensionArgumentType.getDimensionArgument(context, "dimension").getRegistryKey();
        return modifyRegion(context, region -> region.withReplacedShape(region.key(), ProtectionShape.dimension(dimension)));
    }

    private static int executeModifyReplaceShapeWithLocalBox(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        RegistryKey<World> dimension = context.getSource().getWorld().getRegistryKey();
        BlockPos min = BlockPosArgumentType.getBlockPos(context, "min");
        BlockPos max = BlockPosArgumentType.getBlockPos(context, "max");
        return modifyRegion(context, region -> region.withReplacedShape(region.key(), ProtectionShape.box(dimension, min, max)));
    }

    private static int executeModifyLevel(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        int level = IntegerArgumentType.getInteger(context, "level");
        return modifyRegion(context, region -> region.withLevel(level));
    }

    private static int executeInfo(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        String key = StringArgumentType.getString(context, "name");

        ServerCommandSource source = context.getSource();
        RegionPersistentState regionState = RegionPersistentState.get(source.getServer());
        Region region = regionState.getRegionByKey(key);

        if (region != null) {
            source.sendFeedback(Text.literal("Region '" + key + "':")
                .append("\n Level: ").append(String.valueOf(region.level()))
                .append("\n Shape:\n").append(region.shapes().displayList()), false);
            return Command.SINGLE_SUCCESS;
        } else {
            throw REGION_DOES_NOT_EXIST_EXCEPTION.create(key);
        }
    }

    private static int modifyRegion(CommandContext<ServerCommandSource> context, UnaryOperator<Region> operator) throws CommandSyntaxException {
        String key = StringArgumentType.getString(context, "name");

        ServerCommandSource source = context.getSource();
        RegionPersistentState regionState = RegionPersistentState.get(source.getServer());
        Region region = regionState.getRegionByKey(key);

        if (region != null) {
            regionState.replaceRegion(region, operator.apply(region));

            source.sendFeedback(Text.literal("Modified region '" + key + "'"), true);
            return Command.SINGLE_SUCCESS;
        } else {
            throw REGION_DOES_NOT_EXIST_EXCEPTION.create(key);
        }
    }

    private static int executeList(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        RegionPersistentState regionState = RegionPersistentState.get(source.getServer());

        source.sendFeedback(Text.empty()
            .append(Text.literal("Regions:\n"))
            .append(Text.literal(regionState.getRegions().stream()
                .map(Region::key).map(key -> " - " + key)
                .collect(Collectors.joining("\n")))), false);

        return Command.SINGLE_SUCCESS;
    }

    private static int executeTest(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        RegionPersistentState regionState = RegionPersistentState.get(source.getServer());
        RegistryKey<World> dimension = context.getSource().getWorld().getRegistryKey();
        Vec3d pos = context.getSource().getPosition();
        ProtectionContext protectionContext = new ProtectionContext(dimension, pos);

        Stream<Region> possibleRegions = regionState.findRegion(protectionContext);

        source.sendFeedback(Text.empty()
            .append(Text.literal("Regions:\n"))
            .append(Text.literal(possibleRegions
                .map(Region::key).map(key -> " - " + key)
                .collect(Collectors.joining("\n")))), false);

        return Command.SINGLE_SUCCESS;
    }
}
