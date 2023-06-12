package de.martenschaefer.serverutils.mixin;

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

@Mixin(ServerPlayerEntity.class)
public class ServerPlayerEntityMixin {
    @Inject(method = "getPlayerListName", at = @At("RETURN"), cancellable = true)
    private void onGetPlayerListName(CallbackInfoReturnable<Text> cir) {
        if (!ServerUtilsMod.getConfig().chat().enabled()) {
            return;
        }

        Text original = cir.getReturnValue();
        MutableText name;

        ServerPlayerEntity player = (ServerPlayerEntity) (Object) (this);

        if (original == null) {
            name = player.getName().copy();
        } else {
            name = original.copy();
        }

        User user = ModUtils.getLuckPerms().getPlayerAdapter(ServerPlayerEntity.class).getUser(player);
        String colorName = user.getCachedData().getMetaData().getMetaValue("username-color");
        Formatting usernameFormatting = ModUtils.getUsernameFormatting(colorName);

        cir.setReturnValue(usernameFormatting == Formatting.RESET ? name : name.formatted(usernameFormatting));
    }
}
