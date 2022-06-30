package de.martenschaefer.serverutils;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.command.CommandSource;
import net.minecraft.network.message.MessageType;
import net.minecraft.text.Decoration;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.BuiltinRegistries;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.message.v1.ServerMessageDecoratorEvent;
import net.fabricmc.loader.api.FabricLoader;
import de.martenschaefer.serverutils.chat.LuckPermsMessageDecorator;
import de.martenschaefer.serverutils.command.PosCommand;
import de.martenschaefer.serverutils.config.ConfigUtils;
import de.martenschaefer.serverutils.config.ServerUtilsConfig;
import me.lucko.fabric.api.permissions.v0.Permissions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServerUtilsMod implements ModInitializer {
    public static final String MODID = "serverutils";
    @SuppressWarnings("unused")
    public static final Logger LOGGER = LoggerFactory.getLogger("Server Utils");

    private static ServerUtilsConfig CONFIG = ServerUtilsConfig.DEFAULT;

    public static final RegistryKey<MessageType> UNDECORATED_CHAT = RegistryKey.of(Registry.MESSAGE_TYPE_KEY, new Identifier(MODID, "undecorated_chat"));

    @Override
    public void onInitialize() {
        initializeConfig();

        BuiltinRegistries.add(BuiltinRegistries.MESSAGE_TYPE, ServerUtilsMod.UNDECORATED_CHAT,
            new MessageType(Decoration.ofChat("%s"), Decoration.ofChat("chat.type.text.narrate")));

        ServerUtilsConfig config = getConfig();

        if (config.chat().enabled()) {
            // Message Decorator for prefixes, suffixes and username colors
            ServerMessageDecoratorEvent.EVENT.register(ServerMessageDecoratorEvent.STYLING_PHASE, new LuckPermsMessageDecorator());
        }

        if (config.deathCoords().enabled()) {
            // Death Coordinates
            ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> {
                if (!Permissions.check(newPlayer, MODID + ".death.printcoords.enabled")) {
                    return;
                }

                boolean inPublicChat = Permissions.check(newPlayer, MODID + ".death.printcoords.public", config.deathCoords().inPublicChat());

                MutableText text = Text.empty().append(oldPlayer.getDisplayName().copy()).append(Text.literal(" died at "))
                    .append(ModUtils.getCoordinateText(oldPlayer.getBlockPos())).append(".");

                ModUtils.sendMessage(newPlayer, text, inPublicChat);
            });
        }

        // Command registration
        //noinspection CodeBlock2Expr
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            PosCommand.register(dispatcher);
        });

        // Check permissions on the server once, so that they are registered and can be auto-completed
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            CommandSource source = server.getCommandSource();

            Stream<String> commandPermissions = Arrays.stream(new String[][] {
                PosCommand.PERMISSIONS,
            }).flatMap(Arrays::stream);

            String[] permissions = new String[] {
                ".death.printcoords.enabled",
                ".death.printcoords.public",
            };

            Stream.concat(commandPermissions, Arrays.stream(permissions))
                .forEach(permission -> Permissions.check(source, MODID + permission));
        });
    }

    private static void initializeConfig() {
        Path configPath = FabricLoader.getInstance().getConfigDir().resolve(ConfigUtils.CONFIG_PATH);

        if (Files.exists(configPath) && Files.isRegularFile(configPath)) {
            try (InputStream input = Files.newInputStream(configPath)) {
                LOGGER.info("Reading config.");

                CONFIG = ConfigUtils.decodeConfig(input);
            } catch (IOException e) {
                LOGGER.error("IO exception while trying to read config: " + e.getLocalizedMessage());
            } catch (RuntimeException e) {
                LOGGER.error(e.getLocalizedMessage());
            }
        } else {
            try (OutputStream output = Files.newOutputStream(configPath, StandardOpenOption.WRITE, StandardOpenOption.CREATE_NEW);
                 OutputStreamWriter writer = new OutputStreamWriter(new BufferedOutputStream(output))) {
                LOGGER.info("Writing default config.");

                ConfigUtils.encodeConfig(writer);
            } catch (IOException e) {
                LOGGER.error("IO exception while trying to write config: " + e.getLocalizedMessage());
            } catch (RuntimeException e) {
                LOGGER.error(e.getLocalizedMessage());
            }
        }
    }

    public static ServerUtilsConfig getConfig() {
        return CONFIG;
    }

    public static Identifier id(String path) {
        return new Identifier(MODID, path);
    }
}
