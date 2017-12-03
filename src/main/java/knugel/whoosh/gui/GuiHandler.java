package knugel.whoosh.gui;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.awt.*;

public class GuiHandler implements IGuiHandler {

    public static final int TRANSPORTER_ID = 0;

    @Override
    public Object getClientGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {

        switch (id) {
            case TRANSPORTER_ID:
                return new GuiTransporter(player, new ContainerTransporter(player.getHeldItemMainhand()));

            default:
                return null;
        }
    }

    @Override
    public Object getServerGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {

        switch (id) {
            case TRANSPORTER_ID:
                return new ContainerTransporter(player.getHeldItemMainhand());

            default:
                return null;
        }
    }
}
