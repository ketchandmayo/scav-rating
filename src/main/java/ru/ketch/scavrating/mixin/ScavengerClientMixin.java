package ru.ketch.scavrating.mixin;

import meow.binary.scavenger.client.screen.ScavengerWorldCreateScreen;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraft.client.Minecraft;
import ru.ketch.scavrating.LeaderboardScreen;

@Mixin(ScavengerWorldCreateScreen.class)
public class ScavengerClientMixin {
    private boolean isStartConfirmed = false;

    @Inject(method = "createWorld(Lnet/minecraft/world/item/Item;Lnet/minecraft/resources/ResourceLocation;)V", at = @At("HEAD"), cancellable = true)
    private void onCreateWorld(Item item, ResourceLocation modifier, CallbackInfo ci) {
        if (!isStartConfirmed) {
            ci.cancel();
            ScavengerWorldCreateScreen screen = (ScavengerWorldCreateScreen) (Object) this;
            String itemId = net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(item).toString();
            String modId = modifier.toString();
            
            Minecraft.getInstance().setScreen(new LeaderboardScreen(screen, itemId, modId, () -> {
                isStartConfirmed = true;
                Minecraft.getInstance().setScreen(screen);
                screen.createWorld(item, modifier);
            }));
        }
    }
}
