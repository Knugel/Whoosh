package knugel.whoosh;

import cofh.CoFHCore;
import cofh.core.init.CoreProps;
import cofh.core.network.PacketHandler;
import cofh.core.util.ConfigHandler;
import cofh.thermalfoundation.ThermalFoundation;
import com.jcraft.jogg.Packet;
import knugel.whoosh.gui.GuiHandler;
import knugel.whoosh.init.WItems;
import knugel.whoosh.init.WProps;
import knugel.whoosh.network.PacketWhoosh;
import knugel.whoosh.proxy.Proxy;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLLoadCompleteEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;

@Mod(modid = Whoosh.MOD_ID, name = Whoosh.MOD_NAME, version = Whoosh.VERSION, dependencies = Whoosh.DEPENDENCIES)
public class Whoosh {

    public static final String MOD_ID = "whoosh";
    public static final String MOD_NAME = "Whoosh";

    public static final String VERSION = "0.1.5";
    public static final String VERSION_MAX = "1.0.0";
    public static final String VERSION_GROUP = "required-after:" + MOD_ID + "@[" + VERSION + "," + VERSION_MAX + ");";

    public static final String DEPENDENCIES = CoFHCore.VERSION_GROUP + ThermalFoundation.VERSION_GROUP;

    @Instance(MOD_ID)
    public static Whoosh instance;

    @SidedProxy(clientSide = "knugel.whoosh.proxy.ProxyClient", serverSide = "knugel.whoosh.proxy.Proxy")
    public static Proxy proxy;

    public static final Logger LOG = LogManager.getLogger(MOD_ID);
    public static final ConfigHandler CONFIG = new ConfigHandler(VERSION);
    public static final ConfigHandler CONFIG_CLIENT = new ConfigHandler(VERSION);

    public static CreativeTabs tabCommon;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {

        CONFIG.setConfiguration(new Configuration(new File(CoreProps.configDir, MOD_ID + "/common.cfg"), true));
        CONFIG_CLIENT.setConfiguration(new Configuration(new File(CoreProps.configDir, MOD_ID + "/client.cfg"), true));

        WProps.preInit();
        WItems.preInit();

        PacketHandler.INSTANCE.registerPacket(PacketWhoosh.class);
        NetworkRegistry.INSTANCE.registerGuiHandler(instance, new GuiHandler());

        proxy.preInit(event);
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {

        proxy.init(event);
    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent event) {

        proxy.postInit(event);
    }


    @EventHandler
    public void loadComplete(FMLLoadCompleteEvent event) {

        CONFIG.cleanUp(false, true);
        CONFIG_CLIENT.cleanUp(false, true);

        LOG.info(MOD_NAME + ": Load Complete.");
    }
}
