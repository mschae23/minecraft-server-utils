package de.martenschaefer.serverutils.mixin.region;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.world.World;
import de.martenschaefer.serverutils.ServerUtilsMod;
import de.martenschaefer.serverutils.region.RegionRuleEnforcer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(CreeperEntity.class)
public class CreeperEntityMixin extends HostileEntity {
    protected CreeperEntityMixin(EntityType<? extends HostileEntity> entityType, World world) {
        super(entityType, world);
    }

    @Redirect(method = "interactMob", at = @At(value = "INVOKE", target = "net/minecraft/item/ItemStack.isIn(Lnet/minecraft/registry/tag/TagKey;)Z", ordinal = 0))
    private boolean redirectShouldIgnite(ItemStack handStack, TagKey<Item> tag, PlayerEntity player) {
        if (!handStack.isIn(tag)) {
            return false;
        } else if (ServerUtilsMod.getConfig().region().enabled() && player instanceof ServerPlayerEntity serverPlayer) {
            ActionResult result = RegionRuleEnforcer.onExplosionIgnite(serverPlayer, this.getPos());

            if (result == ActionResult.FAIL) {
                RegionRuleEnforcer.sendDeniedText(serverPlayer);
                return false;
            }
        }

        return true;
    }
}
