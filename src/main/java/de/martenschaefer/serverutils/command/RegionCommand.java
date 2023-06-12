package de.martenschaefer.serverutils.command;

import java.util.Arrays;
import java.util.List;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.BlockPosArgumentType;
import net.minecraft.command.argument.DimensionArgumentType;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.fabricmc.fabric.api.util.TriState;
import de.martenschaefer.serverutils.ModUtils;
import de.martenschaefer.serverutils.ServerUtilsMod;
import de.martenschaefer.serverutils.region.RegionMap;
import de.martenschaefer.serverutils.region.RegionPersistentState;
import de.martenschaefer.serverutils.region.RegionRuleEnforcer;
import de.martenschaefer.serverutils.region.RegionV2;
import de.martenschaefer.serverutils.region.rule.ProtectionRule;
import de.martenschaefer.serverutils.region.shape.ProtectionContext;
import de.martenschaefer.serverutils.region.shape.ProtectionShape;
import de.martenschaefer.serverutils.region.shape.RegionShapes;
import de.martenschaefer.serverutils.util.StringTriState;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.luckperms.api.cacheddata.CachedPermissionData;

public final class RegionCommand {
    private static final DynamicCommandExceptionType REGION_ALREADY_EXISTS_EXCEPTION = new DynamicCommandExceptionType(id ->
        new LiteralMessage("Region with the id '" + id + "' already exists"));
    private static final DynamicCommandExceptionType REGION_DOES_NOT_EXIST_EXCEPTION = new DynamicCommandExceptionType(id ->
        new LiteralMessage("Region with the id '" + id + "' does not exist"));
    private static final DynamicCommandExceptionType RULE_DOES_NOT_EXIST_EXCEPTION = new DynamicCommandExceptionType(name ->
        new LiteralMessage("Protection rule '" + name + "' does not exist"));
    private static final DynamicCommandExceptionType TRISTATE_DOES_NOT_EXIST_EXCEPTION = new DynamicCommandExceptionType(name ->
        new LiteralMessage("Protection rule value '" + name + "' does not exist"));

    private static final SuggestionProvider<ServerCommandSource> REGION_NAME_SUGGESTION_PROVIDER = (context, builder) -> {
        RegionMap regions = RegionPersistentState.get(context.getSource().getServer()).getRegions();

        CommandSource.suggestMatching(regions.stream().map(RegionV2::key), builder);
        return builder.buildFuture();
    };

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
                .then(CommandManager.argument("name", StringArgumentType.word()).suggests(REGION_NAME_SUGGESTION_PROVIDER)
                    .executes(RegionCommand::executeRemove)))
            .then(CommandManager.literal("modify")
                .requires(Permissions.require(ServerUtilsMod.MODID + ".command.region.modify", true))
                .then(CommandManager.argument("name", StringArgumentType.word()).suggests(REGION_NAME_SUGGESTION_PROVIDER)
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
                            .executes(RegionCommand::executeModifyLevel)))
                    .then(CommandManager.literal("rule")
                        .then(CommandManager.literal("set")
                            .then(CommandManager.argument("rule_name", StringArgumentType.word()).suggests((context, builder) -> {
                                ProtectionRule[] rules = ProtectionRule.values();

                                CommandSource.suggestMatching(Arrays.stream(rules).map(ProtectionRule::asString), builder);
                                return builder.buildFuture();
                            })
                                .then(CommandManager.argument("rule_value", StringArgumentType.word()).suggests((context, builder) -> {
                                    StringTriState[] states = StringTriState.values();

                                    CommandSource.suggestMatching(Arrays.stream(states).map(StringTriState::asString), builder);
                                    return builder.buildFuture();
                                }).executes(RegionCommand::executeModifySetRule)))))))
            .then(CommandManager.literal("info")
                .requires(Permissions.require(ServerUtilsMod.MODID + ".command.region.info", true))
                .then(CommandManager.argument("name", StringArgumentType.word()).suggests(REGION_NAME_SUGGESTION_PROVIDER)
                    .executes(RegionCommand::executeInfo)))
            .then(CommandManager.literal("test")
                .requires(Permissions.require(ServerUtilsMod.MODID + ".command.region.test").and(ServerCommandSource::isExecutedByPlayer))
                .executes(RegionCommand::executeTest)
                .then(CommandManager.literal("player")
                    .executes(RegionCommand::executeTest))
                .then(CommandManager.literal("generic")
                    .executes(RegionCommand::executeTestGeneric)))
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

    private static int addRegion(CommandContext<ServerCommandSource> context, UnaryOperator<RegionV2> operator) throws CommandSyntaxException {
        String key = StringArgumentType.getString(context, "name");

        ServerCommandSource source = context.getSource();
        RegionPersistentState regionState = RegionPersistentState.get(source.getServer());
        RegionV2 region = operator.apply(RegionV2.create(key));

        if (regionState.addRegion(region)) {
            // Check permissions for every rule once, for LuckPerms command auto-completion
            Arrays.stream(RegionRuleEnforcer.RULES).map(rule -> RegionRuleEnforcer.getBasePermission(region.key(), rule))
                .forEach(permission -> Permissions.check(source, ServerUtilsMod.MODID + permission));

            RegionShapes shapes = region.shapes();

            if (shapes.isEmpty()) {
                source.sendFeedback(() -> Text.literal("Added empty region as '" + key + "'"), true);
            } else {
                source.sendFeedback(() -> Text.literal("Added region as '" + key + "' with ").append(shapes.displayShort()), true);
            }

            source.sendFeedback(() -> Text.literal("Run ")
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
        RegionV2 region = regionState.removeRegion(key);

        if (region != null) {
            source.sendFeedback(() -> Text.literal("Removed region '" + key + "'"), true);
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

    private static int executeModifySetRule(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        String ruleString = StringArgumentType.getString(context, "rule_name");
        String stateString = StringArgumentType.getString(context, "rule_value");
        ProtectionRule rule = ProtectionRule.byName(ruleString);
        TriState value = StringTriState.byName(stateString);

        if (rule == null) {
            throw RULE_DOES_NOT_EXIST_EXCEPTION.create(ruleString);
        } else if (value == null) {
            throw TRISTATE_DOES_NOT_EXIST_EXCEPTION.create(stateString);
        }

        return modifyRegion(context, region -> region.withRule(rule, value));
    }

    private static int executeInfo(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        String key = StringArgumentType.getString(context, "name");

        ServerCommandSource source = context.getSource();
        RegionPersistentState regionState = RegionPersistentState.get(source.getServer());
        RegionV2 region = regionState.getRegionByKey(key);

        if (region != null) {
            source.sendFeedback(() -> Text.literal("Region '" + key + "':")
                .append("\n Level: ").append(String.valueOf(region.level()))
                .append("\n Shape:\n").append(region.shapes().displayList())
                .append("\n Rules:\n").append(region.rulesForDisplay()), false);
            return Command.SINGLE_SUCCESS;
        } else {
            throw REGION_DOES_NOT_EXIST_EXCEPTION.create(key);
        }
    }

    private static int modifyRegion(CommandContext<ServerCommandSource> context, UnaryOperator<RegionV2> operator) throws CommandSyntaxException {
        String key = StringArgumentType.getString(context, "name");

        ServerCommandSource source = context.getSource();
        RegionPersistentState regionState = RegionPersistentState.get(source.getServer());
        RegionV2 region = regionState.getRegionByKey(key);

        if (region != null) {
            regionState.replaceRegion(region, operator.apply(region));

            source.sendFeedback(() -> Text.literal("Modified region '" + key + "'"), true);
            return Command.SINGLE_SUCCESS;
        } else {
            throw REGION_DOES_NOT_EXIST_EXCEPTION.create(key);
        }
    }

    private static int executeList(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        RegionPersistentState regionState = RegionPersistentState.get(source.getServer());

        source.sendFeedback(() -> Text.empty()
            .append(Text.literal("Regions:\n"))
            .append(Text.literal(regionState.getRegions().stream()
                .map(RegionV2::key).map(key -> " - " + key)
                .collect(Collectors.joining("\n")))), false);

        return Command.SINGLE_SUCCESS;
    }

    private static int executeTest(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        return executeTestInternal(context, true);
    }

    private static int executeTestGeneric(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        return executeTestInternal(context, false);
    }

    private static int executeTestInternal(CommandContext<ServerCommandSource> context, boolean player) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        ServerPlayerEntity playerEntity = source.getPlayerOrThrow();
        CachedPermissionData permissionData = ModUtils.getLuckPerms().getPlayerAdapter(ServerPlayerEntity.class).getPermissionData(playerEntity);
        RegionPersistentState regionState = RegionPersistentState.get(source.getServer());
        RegistryKey<World> dimension = context.getSource().getWorld().getRegistryKey();
        Vec3d pos = context.getSource().getPosition();
        ProtectionContext protectionContext = new ProtectionContext(dimension, pos);

        List<RegionV2> possibleRegions = regionState.findRegion(protectionContext).toList();

        MutableText rulesText = Text.empty();

        for (ProtectionRule rule : ProtectionRule.values()) {
            TriState state = TriState.DEFAULT;
            RegionV2 responsibleRegion = null;

            for (RegionV2 region : possibleRegions) {
                state = player ? ModUtils.toFabricTriState(permissionData.checkPermission(
                    RegionRuleEnforcer.getPermission(region.key(), rule.getName()))) : TriState.DEFAULT;
                state = !player || state == TriState.DEFAULT ? region.getRule(rule) : state;

                if (state != TriState.DEFAULT) {
                    responsibleRegion = region;
                    break;
                }
            }

            if (state != TriState.DEFAULT) {
                StringTriState stringState = StringTriState.from(state);

                rulesText.append("\n ").append(Text.literal(rule.getName()).formatted(Formatting.AQUA))
                    .append(": ").append(Text.literal(stringState.asString()).formatted(stringState.getFormatting()))
                    .append(" (").append(responsibleRegion.key()).append(")");
            }
        }

        source.sendFeedback(() -> Text.empty()
            .append("Regions:\n")
            .append(Text.literal(possibleRegions.stream()
                .map(RegionV2::key).map(key -> " - " + key)
                .collect(Collectors.joining("\n"))))
            .append("\n\nRules:").append(rulesText), false);

        return Command.SINGLE_SUCCESS;
    }
}
