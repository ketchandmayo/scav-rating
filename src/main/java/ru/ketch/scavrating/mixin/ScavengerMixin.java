package ru.ketch.scavrating.mixin;

import meow.binary.scavenger.Scavenger;
import meow.binary.scavenger.data.ScavengerSavedData;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.ketch.scavrating.BackendClient;
import ru.ketch.scavrating.CheatTracker;

@Mixin(Scavenger.class)
public class ScavengerMixin {
    @Inject(method = "checkWinCondition", at = @At("TAIL"))
    private static void onCheckWinCondition(ServerPlayer player, ScavengerSavedData data, CallbackInfo ci) {
        if (data.hasWon()) {
            String playerName = player.getName().getString();
            String playerUuid = player.getUUID().toString();
            String itemId = net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(data.getItem()).toString();
            String modifierId = data.getModifierId().toString();
            long timeTicks = data.getWinTimestamp();
            String seed = getSeedSafe(player);

            if (!CheatTracker.hasCheated(player.getUUID())) {
                BackendClient.submitRun(playerName, playerUuid, itemId, modifierId, timeTicks, seed);
            }
            CheatTracker.clear(player.getUUID());
        }
    }

    private static String getSeedSafe(ServerPlayer player) {
        try {
            for (java.lang.reflect.Method m : player.getClass().getMethods()) {
                if (m.getParameterCount() == 0 && net.minecraft.world.level.Level.class.isAssignableFrom(m.getReturnType())) {
                    Object levelObj = m.invoke(player);
                    if (levelObj instanceof net.minecraft.server.level.ServerLevel) {
                        long seed = ((net.minecraft.server.level.ServerLevel) levelObj).getSeed();
                        if (seed != 0L) {
                            return String.valueOf(seed);
                        }
                    }
                }
            }
        } catch (Exception e) {}
        return "0";
    }
}
