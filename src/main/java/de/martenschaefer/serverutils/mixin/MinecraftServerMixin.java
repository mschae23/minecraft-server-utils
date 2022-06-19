package de.martenschaefer.serverutils.mixin;

import net.minecraft.network.message.MessageSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Util;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(MinecraftServer.class)
public class MinecraftServerMixin {
    @Redirect(method = "logChatMessage", at = @At(value = "INVOKE", target = "Lnet/minecraft/text/Text;translatable(Ljava/lang/String;[Ljava/lang/Object;)Lnet/minecraft/text/MutableText;"))
    private MutableText changeLoggedMessageText(@SuppressWarnings("unused") String translationKey, Object[] args, MessageSender sender, Text message) {
        if (sender.uuid().equals(Util.NIL_UUID)) { // For /say command used on the server console
            return Text.translatable("chat.type.announcement", args);
        }

        return Text.literal(Formatting.strip(message.copy().getString()));
    }
}
