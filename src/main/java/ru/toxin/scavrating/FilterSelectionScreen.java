package ru.toxin.scavrating;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item;
import net.minecraft.core.registries.BuiltInRegistries;

import java.util.List;
import java.util.function.Consumer;

public class FilterSelectionScreen extends Screen {
    private final Screen parent;
    private final boolean isItemFilter;
    private final List<String> options;
    private final String currentSelection;
    private final Consumer<String> onSelected;
    private int currentPage = 0;
    private final int itemsPerPage = 20;

    public FilterSelectionScreen(Screen parent, boolean isItemFilter, List<String> options, String currentSelection, Consumer<String> onSelected) {
        super(Component.literal(isItemFilter ? "Select Item Filter" : "Select Modifier Filter"));
        this.parent = parent;
        this.isItemFilter = isItemFilter;
        this.options = options;
        this.currentSelection = currentSelection;
        this.onSelected = onSelected;
    }

    @Override
    protected void init() {
        this.clearWidgets();
        
        this.addRenderableWidget(Button.builder(Component.literal("Back"), b -> {
            this.minecraft.setScreen(parent);
        }).bounds(this.width / 2 - 100, this.height - 30, 200, 20).build());

        this.addRenderableWidget(Button.builder(Component.literal("All"), b -> {
            onSelected.accept("");
            this.minecraft.setScreen(parent);
        }).bounds(this.width / 2 - 100, 30, 200, 20).build());

        int totalPages = (int) Math.ceil((double) options.size() / itemsPerPage);
        if (currentPage > 0) {
            this.addRenderableWidget(Button.builder(Component.literal("< Prev"), b -> {
                currentPage--;
                this.init();
            }).bounds(this.width / 2 - 160, 30, 50, 20).build());
        }
        if (currentPage < totalPages - 1) {
            this.addRenderableWidget(Button.builder(Component.literal("Next >"), b -> {
                currentPage++;
                this.init();
            }).bounds(this.width / 2 + 110, 30, 50, 20).build());
        }

        int startIdx = currentPage * itemsPerPage;
        int endIdx = Math.min(startIdx + itemsPerPage, options.size());

        int cols = 5;
        int buttonWidth = isItemFilter ? 24 : 100;
        int buttonHeight = isItemFilter ? 24 : 20;
        int spacingX = isItemFilter ? 30 : 110;
        int spacingY = isItemFilter ? 30 : 25;

        int startX = this.width / 2 - (cols * spacingX) / 2 + (spacingX - buttonWidth) / 2;
        int startY = 60;

        for (int i = startIdx; i < endIdx; i++) {
            String optionId = options.get(i);
            int relIdx = i - startIdx;
            int col = relIdx % cols;
            int row = relIdx / cols;

            int bx = startX + col * spacingX;
            int by = startY + row * spacingY;

            String shortName = optionId.contains(":") ? optionId.substring(optionId.indexOf(":") + 1) : optionId;

            if (isItemFilter) {
                this.addRenderableWidget(Button.builder(Component.empty(), b -> {
                    onSelected.accept(optionId);
                    this.minecraft.setScreen(parent);
                }).bounds(bx, by, buttonWidth, buttonHeight).build());
            } else {
                this.addRenderableWidget(Button.builder(Component.literal(shortName), b -> {
                    onSelected.accept(optionId);
                    this.minecraft.setScreen(parent);
                }).bounds(bx, by, buttonWidth, buttonHeight).build());
            }
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        guiGraphics.fill(0, 0, this.width, this.height, 0xAA000000);
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 10, 0xFFFFFFFF);
        
        // Draw highlight behind the selected option
        int startIdx = currentPage * itemsPerPage;
        int endIdx = Math.min(startIdx + itemsPerPage, options.size());
        int cols = 5;
        int buttonWidth = isItemFilter ? 24 : 100;
        int buttonHeight = isItemFilter ? 24 : 20;
        int spacingX = isItemFilter ? 30 : 110;
        int spacingY = isItemFilter ? 30 : 25;
        int startX = this.width / 2 - (cols * spacingX) / 2 + (spacingX - buttonWidth) / 2;
        int startY = 60;

        for (int i = startIdx; i < endIdx; i++) {
            String optionId = options.get(i);
            if (optionId.equals(currentSelection)) {
                int relIdx = i - startIdx;
                int col = relIdx % cols;
                int row = relIdx / cols;
                int bx = startX + col * spacingX;
                int by = startY + row * spacingY;
                
                // Draw a gold border around the selected button
                guiGraphics.fill(bx - 2, by - 2, bx + buttonWidth + 2, by + buttonHeight + 2, 0xFFFFAA00);
            }
        }

        super.render(guiGraphics, mouseX, mouseY, partialTick);

        if (isItemFilter) {
            for (int i = startIdx; i < endIdx; i++) {
                String optionId = options.get(i);
                int relIdx = i - startIdx;
                int col = relIdx % cols;
                int row = relIdx / cols;

                int bx = startX + col * spacingX;
                int by = startY + row * spacingY;

                try {
                    Item item = getItemSafe(optionId);
                    ItemStack stack = new ItemStack(item);
                    guiGraphics.renderItem(stack, bx + 4, by + 4);
                    
                    if (mouseX >= bx && mouseX < bx + buttonWidth && mouseY >= by && mouseY < by + buttonHeight) {
                        guiGraphics.renderTooltip(this.font, Component.literal(optionId), mouseX, mouseY);
                    }
                } catch (Exception e) {}
            }
        }
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
}
