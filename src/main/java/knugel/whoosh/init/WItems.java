package knugel.whoosh.init;

import cofh.core.util.core.IInitializer;
import knugel.whoosh.item.ItemTransporter;
import net.minecraft.item.crafting.IRecipe;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.ArrayList;

public class WItems {

    public static final WItems INSTANCE = new WItems();

    private WItems() {

    }

    public static void preInit() {

        itemTransporter = new ItemTransporter();

        initList.add(itemTransporter);

        for (IInitializer init : initList) {
            init.preInit();
        }
        MinecraftForge.EVENT_BUS.register(INSTANCE);
    }

    /* EVENT HANDLING */
    @SubscribeEvent
    public void registerRecipes(RegistryEvent.Register<IRecipe> event) {

        for (IInitializer init : initList) {
            init.initialize();
        }
    }

    private static ArrayList<IInitializer> initList = new ArrayList<>();

    /* REFERENCES */
    public static ItemTransporter itemTransporter;

}
