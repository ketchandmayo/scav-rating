package ru.toxin.scavrating;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

public class LeaderboardScreen extends Screen {
    private final Screen parent;
    private final String itemId;
    private final String modifierId;
    private final Runnable onStartRun;
    private JsonArray leaderboardData = null;

    public LeaderboardScreen(Screen parent, String itemId, String modifierId, Runnable onStartRun) {
        super(Component.literal("Leaderboard"));
        this.parent = parent;
        this.itemId = itemId;
        this.modifierId = modifierId;
        this.onStartRun = onStartRun;
    }

    @Override
    protected void init() {
        BackendClient.getLeaderboard(itemId, modifierId).thenAccept(data -> {
            this.leaderboardData = data;
        });

        int buttonY = this.height - 30;
        this.addRenderableWidget(Button.builder(Component.literal(onStartRun != null ? "Start Run" : "Back"), b -> {
            if (onStartRun != null) {
                onStartRun.run();
            } else {
                this.minecraft.setScreen(parent);
            }
        }).bounds(this.width / 2 - 100, buttonY, 200, 20).build());
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        guiGraphics.fill(0, 0, this.width, this.height, 0xAA000000);
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 20, 0xFFFFFF);

        if (leaderboardData == null) {
            guiGraphics.drawCenteredString(this.font, Component.literal("Loading..."), this.width / 2, 50, 0xAAAAAA);
        } else {
            int y = 50;
            for (int i = 0; i < leaderboardData.size() && i < 10; i++) {
                JsonObject entry = leaderboardData.get(i).getAsJsonObject();
                String name = entry.get("player_name").getAsString();
                long ticks = entry.get("time_ticks").getAsLong();
                String timeStr = formatTicks(ticks);
                guiGraphics.drawString(this.font, Component.literal((i + 1) + ". " + name + " - " + timeStr), this.width / 2 - 100, y, 0xFFFFFF);
                y += 15;
            }
        }

        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    private String formatTicks(long totalTicks) {
        long totalSeconds = totalTicks / 20;
        long hours = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;
        return String.format("%d:%02d:%02d", hours, minutes, seconds);
    }
}
