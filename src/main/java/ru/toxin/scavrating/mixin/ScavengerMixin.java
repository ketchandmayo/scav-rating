package ru.toxin.scavrating.mixin;

import meow.binary.scavenger.Scavenger;
import meow.binary.scavenger.data.ScavengerSavedData;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.toxin.scavrating.BackendClient;

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

            BackendClient.submitRun(playerName, playerUuid, itemId, modifierId, timeTicks, seed);
        }
    }

    private static String getSeedSafe(ServerPlayer player) {
        try {
            for (java.lang.reflect.Method m : player.getClass().getMethods()) {
                String name = m.getName();
                if ((name.equals("method_37908") || name.equals("level") || name.equals("serverLevel") || name.equals("getWorld")) && m.getParameterCount() == 0) {
                    Object levelObj = m.invoke(player);
                    if (levelObj instanceof net.minecraft.server.level.ServerLevel) {
                        return String.valueOf(((net.minecraft.server.level.ServerLevel) levelObj).getSeed());
                    }
                }
            }
        } catch (Exception e) {}
        return "0";
    }
}
