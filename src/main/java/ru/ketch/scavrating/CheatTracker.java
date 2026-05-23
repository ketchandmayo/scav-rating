package ru.ketch.scavrating;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class CheatTracker {
    private static final Set<UUID> cheatedPlayers = new HashSet<>();
    private static final Map<UUID, Long> warningTimers = new HashMap<>();

    public static void markCheated(ServerPlayer player) {
        if (!cheatedPlayers.contains(player.getUUID())) {
            cheatedPlayers.add(player.getUUID());
            warningTimers.put(player.getUUID(), System.currentTimeMillis() + 3000); // 3 seconds delay
        }
    }

    public static void tick(ServerPlayer player) {
        Long time = warningTimers.get(player.getUUID());
        if (time != null && System.currentTimeMillis() >= time) {
            warningTimers.remove(player.getUUID());
            try {
                ((net.minecraft.world.entity.player.Player) player).displayClientMessage(Component.literal("§c[Scavenger Rating] Cheats detected! Your current run will not be recorded on the leaderboard."), false);
            } catch (Throwable t) {
                // Ignore if method not found in some mappings
            }
        }
    }

    public static boolean hasCheated(UUID uuid) {
        return cheatedPlayers.contains(uuid);
    }

    public static void clear(UUID uuid) {
        cheatedPlayers.remove(uuid);
        warningTimers.remove(uuid);
    }
}
