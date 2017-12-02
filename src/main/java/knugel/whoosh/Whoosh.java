package knugel.whoosh;

import cofh.CoFHCore;
import cofh.thermalfoundation.ThermalFoundation;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

@Mod(modid = Whoosh.MOD_ID, name = Whoosh.MOD_NAME, version = Whoosh.VERSION, dependencies = Whoosh.DEPENDENCIES)
public class Whoosh {

    public static final String MOD_ID = "whoosh";
    public static final String MOD_NAME = "Whoosh";

    public static final String VERSION = "0.1.0";
    public static final String VERSION_MAX = "1.0.0";
    public static final String VERSION_GROUP = "required-after:" + MOD_ID + "@[" + VERSION + "," + VERSION_MAX + ");";

    public static final String DEPENDENCIES = CoFHCore.VERSION_GROUP + ThermalFoundation.VERSION_GROUP;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {

    }

    @EventHandler
    public void init(FMLInitializationEvent event) {

    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent event) {

    }
}
