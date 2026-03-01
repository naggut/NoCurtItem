package com.nocurtitem.client;

import com.nocurtitem.config.BlockedItemsConfig;
import com.nocurtitem.NoCurtItemMod;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.gui.widget.list.AbstractList;
import net.minecraft.client.gui.widget.list.ExtendedList;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class NoPickupScreen extends Screen {

    private TextFieldWidget searchField;
    private BlockedListWidget listWidget;
    private List<ResourceLocation> currentList = new ArrayList<>();

    public NoPickupScreen() {
        super(new TranslationTextComponent("gui.nocurtitem.title"));
    }

    @Override
    protected void init() {
        super.init();
        int padding = 10;
        int fieldHeight = 20;
        int buttonWidth = 60;
        int listTop = 50;
        int listBottom = this.height - padding;

        searchField = new TextFieldWidget(this.font, padding, padding, this.width - padding * 2 - buttonWidth - 5, fieldHeight, new TranslationTextComponent("gui.nocurtitem.search"));
        searchField.setMaxLength(256);
        searchField.setValue("");
        addWidget(searchField);

        addButton(new Button(this.width - padding - buttonWidth, padding, buttonWidth, fieldHeight, new TranslationTextComponent("gui.nocurtitem.add"), b -> tryAddFromSearch()));

        listWidget = new BlockedListWidget(this.minecraft, this.width - padding * 2, listBottom - listTop, listTop, listBottom, 25);
        listWidget.setLeftPos(padding);
        refreshList();
        addWidget(listWidget);
    }

    private void tryAddFromSearch() {
        String text = searchField.getValue().trim();
        if (text.isEmpty()) return;
        ResourceLocation id = ResourceLocation.tryParse(text.contains(":") ? text : "minecraft:" + text);
        if (id != null && ForgeRegistries.ITEMS.containsKey(id)) {
            BlockedItemsConfig.add(id);
            searchField.setValue("");
            refreshList();
            NoCurtItemMod.LOGGER.debug("Added blocked item: {}", id);
        }
    }

    private void refreshList() {
        currentList = BlockedItemsConfig.getBlockedItems().stream().sorted((a, b) -> a.toString().compareTo(b.toString())).collect(Collectors.toList());
        listWidget.clearEntriesPublic();
        for (ResourceLocation id : currentList) {
            listWidget.addEntryPublic(listWidget.new Entry(id));
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 257 && searchField.isFocused()) { // Enter
            tryAddFromSearch();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(matrixStack);
        super.render(matrixStack, mouseX, mouseY, partialTicks);
        this.drawCenteredString(matrixStack, this.font, this.title.getString(), this.width / 2, 5, 0xFFFFFF);
        listWidget.render(matrixStack, mouseX, mouseY, partialTicks);
        searchField.render(matrixStack, mouseX, mouseY, partialTicks);
    }

    public void onRemoved(ResourceLocation id) {
        BlockedItemsConfig.remove(id);
        refreshList();
    }

    private class BlockedListWidget extends ExtendedList<BlockedListWidget.Entry> {

        public BlockedListWidget(net.minecraft.client.Minecraft mc, int width, int height, int top, int bottom, int itemHeight) {
            super(mc, width, height, top, bottom, itemHeight);
        }

        public void setLeftPos(int left) {
            this.x0 = left;
            this.x1 = left + this.width;
        }

        @Override
        public int getRowWidth() {
            return this.width - 20;
        }

        public void clearEntriesPublic() {
            clearEntries();
        }

        public void addEntryPublic(Entry e) {
            addEntry(e);
        }

        public class Entry extends AbstractList.AbstractListEntry<BlockedListWidget.Entry> {
            private final ResourceLocation itemId;

            public Entry(ResourceLocation itemId) {
                this.itemId = itemId;
            }

            @Override
            public void render(MatrixStack matrixStack, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean isMouseOver, float partialTicks) {
                Item item = ForgeRegistries.ITEMS.getValue(itemId);
                if (item == null || item == net.minecraft.item.Items.AIR) return;
                ItemStack stack = new ItemStack(item);
                NoPickupScreen.this.minecraft.getItemRenderer().renderAndDecorateItem(stack, left + 2, top + 2);
                String name = itemId.toString();
                NoPickupScreen.this.font.drawShadow(matrixStack, name, left + 22, top + 6, 0xFFFFFF);
                int btnWidth = 50;
                int btnX = left + width - btnWidth - 2;
                int btnY = top + 2;
                boolean removeHover = mouseX >= btnX && mouseX <= btnX + btnWidth && mouseY >= btnY && mouseY <= btnY + height - 4;
                AbstractGui.fill(matrixStack, btnX, btnY, btnX + btnWidth, btnY + height - 4, removeHover ? 0xFF6666 : 0x994444);
                NoPickupScreen.this.font.drawShadow(matrixStack, new TranslationTextComponent("gui.nocurtitem.remove").getString(), btnX + 4, btnY + 4, 0xFFFFFF);
            }

            @Override
            public boolean mouseClicked(double mouseX, double mouseY, int button) {
                if (button != 0) return false;
                BlockedListWidget list = BlockedListWidget.this;
                int left = list.x0;
                int width = list.getRowWidth();
                int btnWidth = 50;
                int btnX = left + width - btnWidth - 2;
                double relY = mouseY - list.y0;
                int rowHeight = list.itemHeight;
                int index = (int) (relY / rowHeight);
                if (index >= 0 && index < list.children().size()) {
                    BlockedListWidget.Entry e = list.children().get(index);
                    if (e == this) {
                        if (mouseX >= btnX && mouseX <= btnX + btnWidth) {
                            onRemoved(itemId);
                            return true;
                        }
                    }
                }
                return false;
            }
        }
    }
}
