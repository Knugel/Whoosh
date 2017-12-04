package knugel.whoosh.gui;

import cofh.api.fluid.IFluidContainerItem;
import cofh.core.gui.GuiContainerCore;
import cofh.core.gui.element.ElementBase;
import cofh.core.init.CoreProps;
import cofh.core.util.helpers.FluidHelper;
import cofh.core.util.helpers.MathHelper;
import cofh.core.util.helpers.StringHelper;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import java.util.List;

public class ElementFluidItem extends ElementBase {

    protected IFluidContainerItem tank;
    protected ItemStack stack;
    protected boolean isInfinite;

    // If this is enabled, 1 pixel of fluid will always show in the tank as long as fluid is present.
    protected boolean alwaysShowMinimum = false;

    public ElementFluidItem(GuiContainerCore gui, int posX, int posY, ItemStack tank) {

        super(gui, posX, posY);
        this.tank = (IFluidContainerItem) tank.getItem();
        this.stack = tank;

        this.texW = 64;
        this.texH = 64;

        this.sizeX = 8;
        this.sizeY = 54;
    }

    @Override
    public void drawBackground(int mouseX, int mouseY, float gameTicks) {

        int amount = getScaled();
        gui.drawFluid(posX, posY + sizeY - amount, tank.getFluid(stack), sizeX, amount);
    }

    @Override
    public void drawForeground(int mouseX, int mouseY) {

    }

    @Override
    public void addTooltip(List<String> list) {

        if (tank.getFluid(stack) != null && tank.getFluid(stack).amount > 0) {
            list.add(StringHelper.getFluidName(tank.getFluid(stack)));
        }
        if (isInfinite) {
            list.add("Infinite Fluid");
        } else {
            if(tank.getFluid(stack) != null) {
                list.add(StringHelper.formatNumber(tank.getFluid(stack).amount) + " / " + StringHelper.formatNumber(tank.getCapacity(stack)) + " mB");
            }
        }
    }

    protected int getScaled() {

        if (tank.getCapacity(stack) < 0) {
            return sizeY;
        }

        long fraction = 0;
        if(tank.getFluid(stack) != null) {
            fraction = (long) tank.getFluid(stack).amount * sizeY / tank.getCapacity(stack);
        }

        return alwaysShowMinimum && tank.getFluid(stack).amount > 0 ? Math.max(1, MathHelper.ceil(fraction)) : MathHelper.ceil(fraction);
    }
}
