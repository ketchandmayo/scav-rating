package ru.toxin.scavrating;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.registries.BuiltInRegistries;

public class LeaderboardScreen extends Screen {
    private final Screen parent;
    private final String itemId;
    private final String modifierId;
    private final Runnable onStartRun;
    private JsonArray leaderboardData = null;
    
    private String filterItemId = "";
    private String filterModifierId = "";
    private List<String> availableItems = new ArrayList<>();
    private List<String> availableModifiers = new ArrayList<>();

    public LeaderboardScreen(Screen parent, String itemId, String modifierId, Runnable onStartRun) {
        super(Component.literal("Leaderboard"));
        this.parent = parent;
        this.itemId = itemId;
        this.modifierId = modifierId;
        this.onStartRun = onStartRun;
        this.filterItemId = itemId;
        this.filterModifierId = modifierId;
    }

    @Override
    protected void init() {
        refreshData();
        
        BackendClient.getFilters().thenAccept(filters -> {
            if (filters.has("items")) {
                availableItems.clear();
                filters.getAsJsonArray("items").forEach(e -> availableItems.add(e.getAsString()));
            }
            if (filters.has("modifiers")) {
                availableModifiers.clear();
                filters.getAsJsonArray("modifiers").forEach(e -> availableModifiers.add(e.getAsString()));
            }
        });

        int buttonY = this.height - 30;
        this.addRenderableWidget(Button.builder(Component.literal(onStartRun != null ? "Start Run" : "Back"), b -> {
            if (onStartRun != null) {
                onStartRun.run();
            } else {
                this.minecraft.setScreen(parent);
            }
        }).bounds(this.width / 2 - 100, buttonY, 200, 20).build());

        this.addRenderableWidget(Button.builder(Component.literal("Item: " + getShortName(filterItemId)), b -> {
            this.minecraft.setScreen(new FilterSelectionScreen(this, true, availableItems, filterItemId, selected -> {
                this.filterItemId = selected;
                this.refreshData();
            }));
        }).bounds(this.width / 2 - 155, 25, 150, 20).build());

        this.addRenderableWidget(Button.builder(Component.literal("Modifier: " + getShortName(filterModifierId)), b -> {
            this.minecraft.setScreen(new FilterSelectionScreen(this, false, availableModifiers, filterModifierId, selected -> {
                this.filterModifierId = selected;
                this.refreshData();
            }));
        }).bounds(this.width / 2 + 5, 25, 150, 20).build());
    }

    private String getShortName(String id) {
        if (id == null || id.isEmpty()) return "All";
        return id.contains(":") ? id.substring(id.indexOf(":") + 1) : id;
    }

    private void refreshData() {
        this.leaderboardData = null;
        BackendClient.getLeaderboard(filterItemId, filterModifierId).thenAccept(data -> {
            this.leaderboardData = data;
        });
    }

    private Item getItemSafe(String itemId) {
        try {
            ResourceLocation loc = ResourceLocation.parse(itemId);
            for (java.lang.reflect.Method m : BuiltInRegistries.ITEM.getClass().getMethods()) {
                if (m.getParameterCount() == 1 && m.getParameterTypes()[0] == ResourceLocation.class) {
                    Object res = m.invoke(BuiltInRegistries.ITEM, loc);
                    if (res instanceof Item) {
                        return (Item) res;
                    }
                }
            }
        } catch (Exception e) {}
        return net.minecraft.world.item.Items.STONE;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        guiGraphics.fill(0, 0, this.width, this.height, 0xAA000000);
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 10, 0xFFFFFFFF);

        if (!filterItemId.isEmpty()) {
            try {
                Item item = getItemSafe(filterItemId);
                ItemStack stack = new ItemStack(item);
                guiGraphics.renderItem(stack, this.width / 2 - 155 - 20, 27);
            } catch (Exception e) {}
        }

        if (leaderboardData == null) {
            drawCenteredStringSafe(guiGraphics, Component.literal("Loading..."), this.width / 2, 60, 0xFFAAAAAA);
        } else if (leaderboardData.size() == 0) {
            drawCenteredStringSafe(guiGraphics, Component.literal("No runs found for these filters."), this.width / 2, 60, 0xFFFF5555);
        } else {
            int cx = this.width / 2;
            // Draw table headers
            drawStringSafe(guiGraphics, Component.literal("Player"), cx - 235, 50, 0xFFFFFF55);
            drawStringSafe(guiGraphics, Component.literal("Time"), cx - 120, 50, 0xFFFFFF55);
            drawStringSafe(guiGraphics, Component.literal("Item"), cx - 55, 50, 0xFFFFFF55);
            drawStringSafe(guiGraphics, Component.literal("Modifier"), cx + 45, 50, 0xFFFFFF55);
            drawStringSafe(guiGraphics, Component.literal("Seed"), cx + 120, 50, 0xFFFFFF55);

            int y = 65;
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

                int rowColor = 0xFFFFFFFF;
                if (i == 0) rowColor = 0xFFFFD700; // Gold
                else if (i == 1) rowColor = 0xFFC0C0C0; // Silver
                else if (i == 2) rowColor = 0xFFCD7F32; // Bronze

                int seedColor = i < 3 ? rowColor : 0xFFAAAAAA;

                drawStringSafe(guiGraphics, Component.literal(String.format("%d. %s", i + 1, name)), cx - 235, y, rowColor);
                drawStringSafe(guiGraphics, Component.literal(timeStr), cx - 120, y, rowColor);
                drawStringSafe(guiGraphics, Component.literal(itemShort), cx - 55, y, rowColor);
                drawStringSafe(guiGraphics, Component.literal(modShort), cx + 45, y, rowColor);
                drawStringSafe(guiGraphics, Component.literal(seedStr), cx + 120, y, seedColor);
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
