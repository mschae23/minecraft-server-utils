package de.martenschaefer.serverutils;

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
import de.martenschaefer.serverutils.command.PosCommand;
import me.lucko.fabric.api.permissions.v0.Permissions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServerUtilsMod implements ModInitializer {
    public static final String MODID = "serverutils";
    @SuppressWarnings("unused")
    public static final Logger LOGGER = LoggerFactory.getLogger("Server Utils");

    public static final RegistryKey<MessageType> UNDECORATED_CHAT = RegistryKey.of(Registry.MESSAGE_TYPE_KEY, new Identifier(MODID, "undecorated_chat"));

    @Override
    public void onInitialize() {
        BuiltinRegistries.add(BuiltinRegistries.MESSAGE_TYPE, ServerUtilsMod.UNDECORATED_CHAT,
            new MessageType(Optional.of(MessageType.DisplayRule.of()), Optional.empty(),
                Optional.of(MessageType.NarrationRule.of(Decoration.ofChat("chat.type.text.narrate"),
                    MessageType.NarrationRule.Kind.CHAT))));

        // Message Decorator for prefixes, suffixes and username colors
        ServerMessageDecoratorEvent.EVENT.register(ServerMessageDecoratorEvent.STYLING_PHASE, new LuckPermsMessageDecorator());

        // Death Coordinates
        ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> {
            if (!Permissions.check(newPlayer, MODID + ".death.printcoords.enabled")) {
                return;
            }

            boolean inPublicChat = Permissions.check(newPlayer, MODID + ".death.printcoords.public");

            MutableText text = Text.empty().append(oldPlayer.getDisplayName().copy()).append(Text.literal(" died at "))
                .append(ModUtils.getCoordinateText(oldPlayer.getBlockPos())).append(".");

            ModUtils.sendMessage(newPlayer, text, inPublicChat);
        });

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
}
