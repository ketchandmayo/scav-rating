package ru.ketch.scavrating.mixin;

import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.ketch.scavrating.CheatTracker;

@Mixin(ServerPlayerGameMode.class)
public class ServerPlayerGameModeMixin {
    @Shadow protected ServerPlayer player;

    @Inject(method = "changeGameModeForPlayer", at = @At("HEAD"))
    private void onChangeGameMode(GameType gameType, CallbackInfoReturnable<Boolean> cir) {
        if (gameType == GameType.CREATIVE || gameType == GameType.SPECTATOR) {
            CheatTracker.markCheated(this.player);
        }
    }
}
