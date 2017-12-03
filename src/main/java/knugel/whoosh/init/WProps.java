package knugel.whoosh.init;

import cofh.core.gui.CreativeTabCore;
import knugel.whoosh.Whoosh;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class WProps {

    public static int teleportDimensionCost;
    public static int teleportDimensionFluidCost;
    public static int teleportBlockCost;

    public static final int BASE_DIMENSION_COST = 250000;
    public static final int BASE_DIMENSION_FLUID_COST = 100;
    public static final int BASE_BLOCK_COST = 25;

    private WProps() {

    }

    public static void preInit() {

        configCommon();
        configClient();
    }

    /* HELPERS */
    private static void configCommon() {

        String category;
        String comment;

        category = "Item.Transporter";

        comment = "Adjust this value to change the amount of Energy (in RF) required to teleport across dimensions.";
        teleportDimensionCost = BASE_DIMENSION_COST;
        teleportDimensionCost = Whoosh.CONFIG.getConfiguration().getInt("DimensionCost", category, teleportDimensionCost, 0, Integer.MAX_VALUE, comment);

        comment = "Adjust this value to change the amount of Energy (in RF) required to teleport a distance of 1 block.";
        teleportBlockCost = BASE_BLOCK_COST;
        teleportBlockCost = Whoosh.CONFIG.getConfiguration().getInt("BlockCost", category, teleportBlockCost, 0, Integer.MAX_VALUE, comment);

        comment = "Adjust this value to change the amount of Fluid (in mb) required to teleport across dimensions.";
        teleportDimensionFluidCost = BASE_DIMENSION_FLUID_COST;
        teleportDimensionFluidCost = Whoosh.CONFIG.getConfiguration().getInt("DimensionFluidCost", category, teleportDimensionFluidCost, 0, Integer.MAX_VALUE, comment);
    }

    private static void configClient() {

		/* CREATIVE TABS */
        Whoosh.tabCommon = new CreativeTabCore("whoosh") {

            @Override
            @SideOnly(Side.CLIENT)
            public ItemStack getIconItemStack() {

                ItemStack iconStack = new ItemStack(WItems.itemTransporter, 1, 1);
                iconStack.setTagCompound(new NBTTagCompound());
                iconStack.getTagCompound().setBoolean("CreativeTab", true);

                return iconStack;
            }

        };
    }

}
