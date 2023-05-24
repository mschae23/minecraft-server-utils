package de.martenschaefer.serverutils.mixin;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import de.martenschaefer.serverutils.ModUtils;
import de.martenschaefer.serverutils.ServerUtilsMod;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public class PlayerEntityMixin {
    @Unique
    private static LuckPerms serverutils_luckPerms = null;

    @Inject(method = "getDisplayName", at = @At("RETURN"), cancellable = true)
    private void onGetDisplayname(CallbackInfoReturnable<Text> cir) {
        if (!ServerUtilsMod.getConfig().chat().enabled() || ((PlayerEntity) (Object) this).getWorld().isClient) {
            return;
        }

        @SuppressWarnings("DataFlowIssue")
        User user = getLuckPerms().getPlayerAdapter(ServerPlayerEntity.class).getUser(((ServerPlayerEntity) (Object) this));
        String colorName = user.getCachedData().getMetaData().getMetaValue("username-color");
        Formatting usernameFormatting = ModUtils.getUsernameFormatting(colorName);

        if (usernameFormatting == Formatting.RESET) {
            return;
        }

        cir.setReturnValue(((MutableText) cir.getReturnValue()).formatted(usernameFormatting));
    }

    @Unique
    private static LuckPerms getLuckPerms() {
        if (serverutils_luckPerms == null) {
            serverutils_luckPerms = LuckPermsProvider.get();
        }

        return serverutils_luckPerms;
    }
}
