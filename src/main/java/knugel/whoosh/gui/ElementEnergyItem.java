package knugel.whoosh.gui;

import cofh.core.gui.GuiContainerCore;
import cofh.core.gui.element.ElementBase;
import cofh.core.init.CoreProps;
import cofh.core.util.helpers.MathHelper;
import cofh.core.util.helpers.RenderHelper;
import cofh.core.util.helpers.StringHelper;
import cofh.redstoneflux.api.IEnergyContainerItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import java.util.List;

public class ElementEnergyItem extends ElementBase {

    public static final ResourceLocation DEFAULT_TEXTURE = new ResourceLocation("whoosh:textures/gui/energy.png");
    public static final int DEFAULT_SCALE = 56;

    protected IEnergyContainerItem storage;
    protected ItemStack stack;
    protected boolean isInfinite;

    // If this is enabled, 1 pixel of energy will always show in the bar as long as it is non-zero.
    protected boolean alwaysShowMinimum = false;

    public ElementEnergyItem(GuiContainerCore gui, int posX, int posY, ItemStack stack) {

        super(gui, posX, posY);
        this.storage = (IEnergyContainerItem)stack.getItem();
        this.stack = stack;

        this.texture = DEFAULT_TEXTURE;
        this.sizeX = 9;
        this.sizeY = DEFAULT_SCALE;

        this.texW = 32;
        this.texH = 64;
    }

    public ElementEnergyItem setAlwaysShow(boolean show) {

        alwaysShowMinimum = show;
        return this;
    }

    public ElementEnergyItem setInfinite(boolean infinite) {

        isInfinite = infinite;
        return this;
    }

    @Override
    public void drawBackground(int mouseX, int mouseY, float gameTicks) {

        int amount = getScaled();
        RenderHelper.bindTexture(texture);
        drawTexturedModalRect(posX, posY, 0, 0, sizeX, sizeY);
        drawTexturedModalRect(posX, posY + DEFAULT_SCALE - amount, 17, DEFAULT_SCALE - amount, sizeX, amount);
    }

    @Override
    public void drawForeground(int mouseX, int mouseY) {

    }

    @Override
    public void addTooltip(List<String> list) {

        if (isInfinite) {
            list.add("Infinite RF");
        } else {
            list.add(StringHelper.formatNumber(storage.getEnergyStored(stack)) + " / " + StringHelper.formatNumber(storage.getMaxEnergyStored(stack)) + " RF");
        }
    }

    protected int getScaled() {

        if (storage.getMaxEnergyStored(stack) <= 0) {
            return sizeY;
        }
        long fraction = (long) storage.getEnergyStored(stack) * sizeY / storage.getMaxEnergyStored(stack);

        return alwaysShowMinimum && storage.getEnergyStored(stack) > 0 ? Math.max(1, MathHelper.round(fraction)) : MathHelper.round(fraction);
    }

}
