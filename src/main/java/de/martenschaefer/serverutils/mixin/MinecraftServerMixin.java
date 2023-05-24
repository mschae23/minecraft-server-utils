package de.martenschaefer.serverutils.mixin;

// @Mixin(MinecraftServer.class)
public class MinecraftServerMixin {
    /*
    @Redirect(method = "logChatMessage", at = @At(value = "INVOKE", target = "Lnet/minecraft/text/Text;translatable(Ljava/lang/String;[Ljava/lang/Object;)Lnet/minecraft/text/MutableText;"))
    private MutableText changeLoggedMessageText(String translationKey, Object[] args, MessageSender sender, Text message) {
        if (!ServerUtilsMod.getConfig().chat().enabled()) {
            return Text.translatable(translationKey, args);
        }

        if (sender.profileId().equals(Util.NIL_UUID)) { // For /say command used on the server console
            return Text.translatable("chat.type.announcement", args);
        }

        return Text.literal(Formatting.strip(message.copy().getString()));
    }
    */
}
