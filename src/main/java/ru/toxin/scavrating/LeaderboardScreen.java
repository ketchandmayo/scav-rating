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
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 20, 0xFFFFFFFF);

        if (leaderboardData == null) {
            drawCenteredStringSafe(guiGraphics, Component.literal("Loading..."), this.width / 2, 50, 0xFFAAAAAA);
        } else {
            int y = 50;
            for (int i = 0; i < leaderboardData.size() && i < 10; i++) {
                JsonObject entry = leaderboardData.get(i).getAsJsonObject();
                String name = entry.get("player_name").getAsString();
                long ticks = entry.get("time_ticks").getAsLong();
                String timeStr = formatTicks(ticks);
                drawStringSafe(guiGraphics, Component.literal((i + 1) + ". " + name + " - " + timeStr), this.width / 2 - 100, y, 0xFFFFFFFF);
                y += 15;
            }
        }

        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    private static java.lang.reflect.Method drawStringMethod;
    private static java.lang.reflect.Method drawCenteredStringMethod;

    private void drawStringSafe(GuiGraphics guiGraphics, Component component, int x, int y, int color) {
        if (drawStringMethod == null) {
            for (java.lang.reflect.Method m : guiGraphics.getClass().getMethods()) {
                Class<?>[] p = m.getParameterTypes();
                if (p.length == 5 && p[0] == net.minecraft.client.gui.Font.class && p[1] == net.minecraft.network.chat.Component.class) {
                    if (m.getName().equals("method_27535") || m.getName().equals("drawString")) {
                        drawStringMethod = m;
                        break;
                    }
                }
            }
        }
        if (drawStringMethod != null) {
            try {
                drawStringMethod.invoke(guiGraphics, this.font, component, x, y, color);
            } catch (Exception e) {}
        }
    }

    private void drawCenteredStringSafe(GuiGraphics guiGraphics, Component component, int x, int y, int color) {
        if (drawCenteredStringMethod == null) {
            for (java.lang.reflect.Method m : guiGraphics.getClass().getMethods()) {
                Class<?>[] p = m.getParameterTypes();
                if (p.length == 5 && p[0] == net.minecraft.client.gui.Font.class && p[1] == net.minecraft.network.chat.Component.class) {
                    if (m.getName().equals("method_27534") || m.getName().equals("drawCenteredString")) {
                        drawCenteredStringMethod = m;
                        break;
                    }
                }
            }
        }
        if (drawCenteredStringMethod != null) {
            try {
                drawCenteredStringMethod.invoke(guiGraphics, this.font, component, x, y, color);
            } catch (Exception e) {}
        }
    }

    private String formatTicks(long totalTicks) {
        long totalSeconds = totalTicks / 20;
        long hours = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;
        return String.format("%d:%02d:%02d", hours, minutes, seconds);
    }
}
