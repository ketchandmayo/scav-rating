package ru.toxin.scavrating.mixin;

import meow.binary.scavenger.client.ClientScavengerData;
import meow.binary.scavenger.client.screen.VictoryScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.toxin.scavrating.LeaderboardScreen;

@Mixin(VictoryScreen.class)
public abstract class VictoryScreenMixin extends Screen {
    protected VictoryScreenMixin(Component title) {
        super(title);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void onInit(CallbackInfo ci) {
        String itemId = net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(ClientScavengerData.item).toString();
        String modId = ClientScavengerData.modifier.toString();
        
        this.addRenderableWidget(Button.builder(Component.translatable("scav_rating.gui.leaderboard"), b -> {
            Minecraft.getInstance().setScreen(new LeaderboardScreen(this, itemId, modId, null));
        }).bounds(this.width / 2 - 100, this.height - 30, 200, 20).build());
    }
}
