package knugel.whoosh.gui;

import cofh.core.gui.GuiContainerCore;
import cofh.core.gui.element.ElementBase;
import cofh.core.init.CoreProps;
import cofh.core.util.helpers.FluidHelper;
import cofh.core.util.helpers.MathHelper;
import cofh.core.util.helpers.RenderHelper;
import cofh.core.util.helpers.StringHelper;
import knugel.whoosh.util.IFluidItem;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.IFluidTank;

import java.util.List;

public class ElementFluidItem extends ElementBase {

    public static final ResourceLocation LARGE_TEXTURE = new ResourceLocation(CoreProps.PATH_ELEMENTS + "fluid_tank_large.png");
    public static final ResourceLocation MEDIUM_TEXTURE = new ResourceLocation(CoreProps.PATH_ELEMENTS + "fluid_tank_medium.png");
    public static final ResourceLocation SMALL_TEXTURE = new ResourceLocation(CoreProps.PATH_ELEMENTS + "fluid_tank_small.png");

    protected IFluidItem tank;
    protected ItemStack stack;
    protected int gaugeType;
    protected boolean drawTank;
    protected boolean isInfinite;
    protected boolean isThin;
    protected float durationFactor = 1.0F;

    // If this is enabled, 1 pixel of fluid will always show in the tank as long as fluid is present.
    protected boolean alwaysShowMinimum = false;

    protected TextureAtlasSprite fluidTextureOverride;

    public ElementFluidItem(GuiContainerCore gui, int posX, int posY, ItemStack tank) {

        this(gui, posX, posY, tank, LARGE_TEXTURE);
    }

    public ElementFluidItem(GuiContainerCore gui, int posX, int posY, ItemStack tank, ResourceLocation texture) {

        super(gui, posX, posY);
        this.tank = (IFluidItem)tank.getItem();
        this.stack = tank;

        //this.texture = texture;
        this.texW = 64;
        this.texH = 64;

        this.sizeX = 8;
        this.sizeY = 60;
    }

    public ElementFluidItem setGauge(int gaugeType) {

        this.gaugeType = gaugeType;
        return this;
    }

    public ElementFluidItem setLarge() {

        this.texture = LARGE_TEXTURE;
        this.sizeX = 16;
        this.sizeY = 60;
        return this;
    }

    public ElementFluidItem setMedium() {

        this.texture = MEDIUM_TEXTURE;
        this.sizeY = 40;
        return this;
    }

    public ElementFluidItem setSmall() {

        //this.texture = SMALL_TEXTURE;
        this.sizeY = 54;
        return this;
    }

    public ElementFluidItem setFluidTextureOverride(TextureAtlasSprite fluidTextureOverride) {

        this.fluidTextureOverride = fluidTextureOverride;
        return this;
    }

    public ElementFluidItem drawTank(boolean drawTank) {

        this.drawTank = drawTank;
        return this;
    }

    public ElementFluidItem setAlwaysShow(boolean show) {

        alwaysShowMinimum = show;
        return this;
    }

    public ElementFluidItem setInfinite(boolean infinite) {

        isInfinite = infinite;
        return this;
    }

    public ElementFluidItem setThin(boolean thin) {

        this.isThin = thin;
        this.sizeX = 7;
        return this;
    }

    public ElementFluidItem setDurationFactor(float durationFactor) {

        this.durationFactor = durationFactor;
        return this;
    }

    @Override
    public void drawBackground(int mouseX, int mouseY, float gameTicks) {
        drawFluid();
    }

    @Override
    public void drawForeground(int mouseX, int mouseY) {

    }

    @Override
    public void addTooltip(List<String> list) {

        if (tank.getFluid(stack) != null && tank.getFluid(stack).amount > 0) {
            list.add(StringHelper.getFluidName(tank.getFluid(stack)));

            if (FluidHelper.isPotionFluid(tank.getFluid(stack))) {
                FluidHelper.addPotionTooltip(tank.getFluid(stack), list, durationFactor);
            }
        }
        if (isInfinite) {
            list.add("Infinite Fluid");
        } else {
            if(tank.getFluid(stack) != null) {
                list.add(StringHelper.formatNumber(tank.getFluid(stack).amount) + " / " + StringHelper.formatNumber(tank.getTankCapacity(stack)) + " mB");
            }
        }
    }

    protected int getScaled() {

        if (tank.getTankCapacity(stack) < 0) {
            return sizeY;
        }

        long fraction = 0;
        if(tank.getFluid(stack) != null) {
            fraction = (long) tank.getFluid(stack).amount * sizeY / tank.getTankCapacity(stack);
        }

        return alwaysShowMinimum && tank.getFluid(stack).amount > 0 ? Math.max(1, MathHelper.ceil(fraction)) : MathHelper.ceil(fraction);
    }

    protected void drawFluid() {

        int amount = getScaled();

        /*if (fluidTextureOverride != null) {
            RenderHelper.setBlockTextureSheet();
            gui.drawTiledTexture(posX, posY + sizeY - amount, fluidTextureOverride, sizeX, amount);
        } else {
        */
            gui.drawFluid(posX, posY + sizeY - amount, tank.getFluid(stack), sizeX, amount);
        //}
    }

}
