package de.martenschaefer.serverutils;

import java.nio.file.Paths;
import java.util.Arrays;
import java.util.stream.Stream;
import net.minecraft.command.CommandSource;
import net.minecraft.network.message.MessageType;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryOps;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.GlobalPos;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.util.TriState;
import de.martenschaefer.config.api.ConfigIo;
import de.martenschaefer.config.api.ModConfig;
import de.martenschaefer.serverutils.command.LockCommand;
import de.martenschaefer.serverutils.command.PosCommand;
import de.martenschaefer.serverutils.command.RegionCommand;
import de.martenschaefer.serverutils.command.ServerUtilsCommand;
import de.martenschaefer.serverutils.command.UnlockCommand;
import de.martenschaefer.serverutils.command.VoteCommand;
import de.martenschaefer.serverutils.config.ServerUtilsConfigV4;
import de.martenschaefer.serverutils.config.v1.ServerUtilsConfigV1;
import de.martenschaefer.serverutils.config.v2.ServerUtilsConfigV2;
import de.martenschaefer.serverutils.config.v3.ServerUtilsConfigV3;
import de.martenschaefer.serverutils.event.AnnounceEntityDeathEvent;
import de.martenschaefer.serverutils.region.Region;
import de.martenschaefer.serverutils.region.RegionPersistentState;
import de.martenschaefer.serverutils.region.RegionRuleEnforcer;
import de.martenschaefer.serverutils.region.shape.ProtectionShapeType;
import de.martenschaefer.serverutils.registry.ServerUtilsRegistries;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.luckperms.api.context.ContextCalculator;
import net.luckperms.api.context.ContextConsumer;
import net.luckperms.api.context.ContextSet;
import net.luckperms.api.context.ImmutableContextSet;
import net.luckperms.api.event.user.UserDataRecalculateEvent;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServerUtilsMod implements ModInitializer {
    public static final String MODID = "serverutils";
    @SuppressWarnings("unused")
    public static final Logger LOGGER = LoggerFactory.getLogger("Server Utils");

    private static final ServerUtilsConfigV4 LATEST_CONFIG_DEFAULT = ServerUtilsConfigV4.DEFAULT;
    private static final int LATEST_CONFIG_VERSION = LATEST_CONFIG_DEFAULT.version();
    private static final Codec<ModConfig<ServerUtilsConfigV4>> CONFIG_CODEC = ModConfig.createCodec(LATEST_CONFIG_VERSION, ServerUtilsMod::getConfigType);

    private static ServerUtilsConfigV4 CONFIG = LATEST_CONFIG_DEFAULT;

    public static final RegistryKey<MessageType> UNDECORATED_CHAT = RegistryKey.of(RegistryKeys.MESSAGE_TYPE, new Identifier(MODID, "undecorated_chat"));

    @Override
    public void onInitialize() {
        ServerLifecycleEvents.SERVER_STARTING.register(server ->
            CONFIG = ConfigIo.initializeConfig(Paths.get(MODID + ".json"), LATEST_CONFIG_VERSION, LATEST_CONFIG_DEFAULT, CONFIG_CODEC,
                RegistryOps.of(JsonOps.INSTANCE, server.getRegistryManager()), LOGGER::info, LOGGER::error)
        );

        ServerUtilsRegistries.init();
        ProtectionShapeType.init();

        var config = getConfig();

        // if (config.chat().enabled()) {
        //     // Message Decorator for prefixes, suffixes and username colors
        //     ServerMessageDecoratorEvent.EVENT.register(ServerMessageDecoratorEvent.STYLING_PHASE, new LuckPermsMessageDecorator());
        // }

        if (config.deathCoords().enabled()) {
            // Death Coordinates
            ServerPlayerEvents.COPY_FROM.register((oldPlayer, newPlayer, alive) -> {
                if (alive || !Permissions.check(newPlayer, MODID + ".death.printcoords.enabled", true)) {
                    return;
                }

                boolean inPublicChat = Permissions.check(newPlayer, MODID + ".death.printcoords.public", config.deathCoords().inPublicChat());
                GlobalPos deathPos = newPlayer.getLastDeathPos().orElseGet(() -> GlobalPos.create(oldPlayer.getWorld().getRegistryKey(), oldPlayer.getBlockPos()));

                MutableText text = Text.empty().append(oldPlayer.getDisplayName().copy()).append(Text.literal(" died at "))
                    .append(ModUtils.getCoordinateText(deathPos.getPos()));

                if (newPlayer.getLastDeathPos().isPresent()) {
                    text.append(" in ").append(Text.literal(deathPos.getDimension().getValue().toString()).formatted(Formatting.YELLOW));
                }

                if (inPublicChat) {
                    // If in_public_chat is true, send message to everyone
                    newPlayer.getServerWorld().getServer().getPlayerManager().broadcast(text, false);
                }

                // Otherwise, send it to the player directly.
                // Because COPY_FROM is called in a state where neither the old nor new player are in the
                // player manager's "players" list, the message needs to be manually sent to the player that died
                // even if in_public_chat is true.
                newPlayer.sendMessage(text, false);
            });
        }

        AnnounceEntityDeathEvent.EVENT.register((world, entity, message) -> {
            world.getServer().getPlayerManager().broadcast(message, false);
            return TriState.DEFAULT;
        });

        // Command registration
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            ServerUtilsCommand.register(dispatcher);
            PosCommand.register(dispatcher);
            LockCommand.register(dispatcher);
            UnlockCommand.register(dispatcher);
            VoteCommand.register(dispatcher);
            RegionCommand.register(dispatcher);
        });

        // Check permissions on the server once, so that they are registered and can be auto-completed
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            CommandSource source = server.getCommandSource();

            Stream<String> commandPermissions = Arrays.stream(new String[][] {
                ServerUtilsCommand.PERMISSIONS,
                PosCommand.PERMISSIONS,
                LockCommand.PERMISSIONS,
                UnlockCommand.PERMISSIONS,
                VoteCommand.PERMISSIONS,
                RegionCommand.PERMISSIONS,
            }).flatMap(Arrays::stream);

            String[] permissions = new String[] {
                ".death.printcoords.enabled",
                ".death.printcoords.public",
            };

            Stream<String> regionPermissions = RegionPersistentState.get(server).getRegions().stream()
                .map(Region::key).flatMap(name -> Arrays.stream(RegionRuleEnforcer.RULES)
                    .map(rule -> RegionRuleEnforcer.getBasePermission(name, rule)));

            Stream.concat(Stream.concat(commandPermissions, Arrays.stream(permissions)), regionPermissions)
                .forEach(permission -> Permissions.check(source, MODID + permission));

            ModUtils.getLuckPerms().getContextManager().registerCalculator(new ContextCalculator<ServerPlayerEntity>() {
                @Override
                public void calculate(@NotNull ServerPlayerEntity target, @NotNull ContextConsumer contextConsumer) {
                }

                @Override
                public @NotNull ContextSet estimatePotentialContexts() {
                    ImmutableContextSet.Builder builder = ImmutableContextSet.builder();
                    builder.add("message_type", "chat");
                    builder.add("message_type", "say_command");
                    builder.add("message_type", "msg_command_incoming");
                    builder.add("message_type", "msg_command_outgoing");
                    builder.add("message_type", "team_msg_command_incoming");
                    builder.add("message_type", "team_msg_command_outgoing");
                    builder.add("message_type", "emote_command");
                    builder.add("message_type", "default");
                    builder.add("message_team", "true");
                    builder.add("message_team", "false");
                    return builder.build();
                }
            });
        });

        RegionPersistentState.init();

        ServerLifecycleEvents.SERVER_STARTED.register(server ->
            ModUtils.getLuckPerms().getEventBus().subscribe(UserDataRecalculateEvent.class, event -> onUserDataRecalculate(server, event)));
    }

    private static void onUserDataRecalculate(MinecraftServer server, UserDataRecalculateEvent event) {
        ServerPlayerEntity player = server.getPlayerManager().getPlayer(event.getUser().getUniqueId());

        if (player != null) {
            ModUtils.updateUsernameFormatting(server, player, event.getData().getMetaData());
        }
    }

    @SuppressWarnings("deprecation")
    private static ModConfig.Type<ServerUtilsConfigV4, ?> getConfigType(int version) {
        return new ModConfig.Type<>(version, switch (version) {
            case 1 -> ServerUtilsConfigV1.TYPE_CODEC;
            case 2 -> ServerUtilsConfigV2.TYPE_CODEC;
            case 3 -> ServerUtilsConfigV3.TYPE_CODEC;
            default -> ServerUtilsConfigV4.TYPE_CODEC;
        });
    }

    public static ServerUtilsConfigV4 getConfig() {
        return CONFIG;
    }

    public static Identifier id(String path) {
        return new Identifier(MODID, path);
    }
}
