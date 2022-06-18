package de.martenschaefer.serverutils.mixin;

import net.minecraft.server.MinecraftServer;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(MinecraftServer.class)
public class MinecraftServerMixin {
    @Redirect(method = "logChatMessage", at = @At(value = "INVOKE", target = "Lnet/minecraft/text/Text;translatable(Ljava/lang/String;[Ljava/lang/Object;)Lnet/minecraft/text/MutableText;"))
    private MutableText changeLoggedMessageText(String translationKey, Object[] args) {
        if ("chat.type.announcement".equals(translationKey)) { // For /say command
            return Text.translatable(translationKey, args);
        }

        Text message = (Text) args[1]; // Varargs

        return Text.literal(message.copy().getString().replaceAll("[&§][\\da-f]", ""));
    }
}
