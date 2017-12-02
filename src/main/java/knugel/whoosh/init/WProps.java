package knugel.whoosh.init;

import cofh.core.gui.CreativeTabCore;
import knugel.whoosh.Whoosh;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class WProps {

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
