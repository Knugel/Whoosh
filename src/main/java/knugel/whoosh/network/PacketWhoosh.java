package knugel.whoosh.network;

import cofh.api.core.ISecurable;
import cofh.core.network.PacketCoFHBase;
import cofh.core.network.PacketHandler;
import knugel.whoosh.Whoosh;
import knugel.whoosh.gui.ContainerTransporter;
import knugel.whoosh.item.ItemTransporter;
import knugel.whoosh.util.TeleportPosition;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;

public class PacketWhoosh extends PacketCoFHBase {

    public enum PacketTypes {
        SECURITY_UPDATE, ADD_POS, REMOVE_POS, SET_SELECTED
    }

    @Override
    public void handlePacket(EntityPlayer player, boolean isServer) {

        try {
            int type = getByte();
            switch (PacketTypes.values()[type]) {
                case SECURITY_UPDATE:
                    if (player.openContainer instanceof ISecurable) {
                        ((ISecurable) player.openContainer).setAccess(ISecurable.AccessMode.values()[getByte()]);
                    }
                    return;
                case ADD_POS:
                    if(player.openContainer instanceof ContainerTransporter) {
                        ItemStack stack = ((ContainerTransporter) player.openContainer).getContainerStack();
                        if(stack.getItem() instanceof ItemTransporter) {
                            String name = getString();
                            BlockPos pos = getCoords();
                            int dim = getInt();
                            ItemTransporter.appendPoint(stack, new TeleportPosition(pos, dim, name));
                        }
                    }
                    return;
                case REMOVE_POS:
                    if(player.openContainer instanceof ContainerTransporter) {
                        ItemStack stack = ((ContainerTransporter) player.openContainer).getContainerStack();
                        if (stack.getItem() instanceof ItemTransporter) {
                            ItemTransporter.removePoint(stack, getInt());
                        }
                    }
                    return;
                case SET_SELECTED:
                    if(player.openContainer instanceof ContainerTransporter) {
                        ItemStack stack = ((ContainerTransporter) player.openContainer).getContainerStack();
                        if (stack.getItem() instanceof ItemTransporter) {
                            ItemTransporter.setSelected(stack, getInt());
                        }
                    }
                    return;
                default:
                    Whoosh.LOG.error("Unknown Packet! Internal: WPH, ID: " + type);
            }

        } catch (Exception e) {
            Whoosh.LOG.error("Packet payload failure! Please check your config files!");
            e.printStackTrace();
        }
    }

    public static void sendSecurityPacketToServer(ISecurable securable) {

        PacketHandler.sendToServer(getPacket(PacketTypes.SECURITY_UPDATE).addByte(securable.getAccess().ordinal()));
    }

    public static void sendRemovePosPacketToServer(int index) {

        PacketHandler.sendToServer(getPacket(PacketTypes.REMOVE_POS).addInt(index));
    }

    public static void sendAddPosPacketToServer(TeleportPosition pos) {

        PacketHandler.sendToServer(getPacket(PacketTypes.ADD_POS)
                .addString(pos.name)
                .addCoords(pos.position.getX(), pos.position.getY(), pos.position.getZ())
                .addInt(pos.dimension));
    }

    public static void sendSetSelectedPacketToServer(int index) {

        PacketHandler.sendToServer(getPacket(PacketTypes.SET_SELECTED).addInt(index));
    }

    public static PacketCoFHBase getPacket(PacketTypes theType) {

        return new PacketWhoosh().addByte(theType.ordinal());
    }
}
