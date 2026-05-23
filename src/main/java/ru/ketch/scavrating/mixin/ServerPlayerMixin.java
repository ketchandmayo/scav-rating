package ru.ketch.scavrating.mixin;

import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.ketch.scavrating.CheatTracker;

@Mixin(ServerPlayer.class)
public class ServerPlayerMixin {
    @Inject(method = "tick", at = @At("HEAD"))
    private void onTick(CallbackInfo ci) {
        try {
            net.minecraft.server.level.ServerPlayer player = (net.minecraft.server.level.ServerPlayer) (Object) this;
            CheatTracker.tick(player);
            
            net.minecraft.world.level.GameType mode = player.gameMode.getGameModeForPlayer();
            if (mode == net.minecraft.world.level.GameType.CREATIVE || mode == net.minecraft.world.level.GameType.SPECTATOR) {
                CheatTracker.markCheated(player);
            }
        } catch (Throwable t) {}
    }
}
