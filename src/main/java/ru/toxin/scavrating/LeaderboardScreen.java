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
    private boolean filterAllItems = false;
    private boolean filterAllModifiers = false;

    public LeaderboardScreen(Screen parent, String itemId, String modifierId, Runnable onStartRun) {
        super(Component.literal("Leaderboard"));
        this.parent = parent;
        this.itemId = itemId;
        this.modifierId = modifierId;
        this.onStartRun = onStartRun;
    }

    @Override
    protected void init() {
        refreshData();

        int buttonY = this.height - 30;
        this.addRenderableWidget(Button.builder(Component.literal(onStartRun != null ? "Start Run" : "Back"), b -> {
            if (onStartRun != null) {
                onStartRun.run();
            } else {
                this.minecraft.setScreen(parent);
            }
        }).bounds(this.width / 2 - 100, buttonY, 200, 20).build());

        this.addRenderableWidget(Button.builder(Component.literal("Item: Current"), b -> {
            filterAllItems = !filterAllItems;
            b.setMessage(Component.literal("Item: " + (filterAllItems ? "All" : "Current")));
            refreshData();
        }).bounds(this.width / 2 - 155, 25, 150, 20).build());

        this.addRenderableWidget(Button.builder(Component.literal("Modifier: Current"), b -> {
            filterAllModifiers = !filterAllModifiers;
            b.setMessage(Component.literal("Modifier: " + (filterAllModifiers ? "All" : "Current")));
            refreshData();
        }).bounds(this.width / 2 + 5, 25, 150, 20).build());
    }

    private void refreshData() {
        this.leaderboardData = null;
        String queryItem = filterAllItems ? "" : this.itemId;
        String queryModifier = filterAllModifiers ? "" : this.modifierId;
        BackendClient.getLeaderboard(queryItem, queryModifier).thenAccept(data -> {
            this.leaderboardData = data;
        });
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        guiGraphics.fill(0, 0, this.width, this.height, 0xAA000000);
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 10, 0xFFFFFFFF);

        if (leaderboardData == null) {
            drawCenteredStringSafe(guiGraphics, Component.literal("Loading..."), this.width / 2, 60, 0xFFAAAAAA);
        } else if (leaderboardData.size() == 0) {
            drawCenteredStringSafe(guiGraphics, Component.literal("No runs found for these filters."), this.width / 2, 60, 0xFFFF5555);
        } else {
            int y = 60;
            for (int i = 0; i < leaderboardData.size() && i < 10; i++) {
                JsonObject entry = leaderboardData.get(i).getAsJsonObject();
                String name = entry.get("player_name").getAsString();
                long ticks = entry.get("time_ticks").getAsLong();
                String itemStr = entry.get("item_id").getAsString();
                String modStr = entry.get("modifier_id").getAsString();
                String seedStr = entry.get("seed").getAsString();

                String timeStr = formatTicks(ticks);
                String itemShort = itemStr.contains(":") ? itemStr.substring(itemStr.indexOf(":") + 1) : itemStr;
                String modShort = modStr.contains(":") ? modStr.substring(modStr.indexOf(":") + 1) : modStr;

                String rowText = String.format("%d. %s | %s | %s | %s | Seed: %s", i + 1, name, timeStr, itemShort, modShort, seedStr);
                drawCenteredStringSafe(guiGraphics, Component.literal(rowText), this.width / 2, y, 0xFFFFFFFF);
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
        long totalMs = totalTicks * 50;
        long hours = totalMs / 3600000;
        long minutes = (totalMs % 3600000) / 60000;
        long seconds = (totalMs % 60000) / 1000;
        long ms = totalMs % 1000;
        if (hours > 0) {
            return String.format("%d:%02d:%02d.%03d", hours, minutes, seconds, ms);
        } else {
            return String.format("%02d:%02d.%03d", minutes, seconds, ms);
        }
    }
}
