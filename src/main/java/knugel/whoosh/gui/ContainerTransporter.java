package knugel.whoosh.gui;

import cofh.api.core.ISecurable;
import cofh.core.gui.container.ContainerCore;
import cofh.core.network.PacketHandler;
import cofh.core.util.CoreUtils;
import cofh.core.util.helpers.SecurityHelper;
import com.mojang.authlib.GameProfile;
import knugel.whoosh.Whoosh;
import knugel.whoosh.item.ItemTransporter;
import knugel.whoosh.network.PacketWhoosh;
import knugel.whoosh.util.TeleportPosition;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.network.NetworkRegistry;

import java.util.List;

public class ContainerTransporter extends ContainerCore implements ISecurable {

    ItemStack stack;

    public ContainerTransporter(ItemStack stack) {

        this.stack = stack;
    }

    public ItemStack getContainerStack() {

        return stack;
    }

    public void appendPoint(TeleportPosition pos) {

        ItemTransporter.appendPoint(stack, pos);
        if (CoreUtils.isClient()) {
            PacketWhoosh.sendAddPosPacketToServer(pos);
        }
    }

    public void removePoint(int index) {

        ItemTransporter.removePoint(stack, index);
        if(CoreUtils.isClient()) {
            PacketWhoosh.sendRemovePosPacketToServer(index);
        }
    }

    public List<TeleportPosition> getPoints() {

        return ItemTransporter.getPositions(stack);
    }


    public int getSelected() {

        return ItemTransporter.getSelected(stack);
    }

    public void setSelected(int index) {

        ItemTransporter.setSelected(stack, index);
        if(CoreUtils.isClient()) {
            PacketWhoosh.sendSetSelectedPacketToServer(index);
        }
    }

    /* ContainerCore */
    @Override
    public boolean canInteractWith(EntityPlayer player) {

        return true;
    }

    @Override
    public boolean supportsShiftClick(int slotIndex) {

        return false;
    }

    @Override
    protected int getPlayerInventoryVerticalOffset() {

        return 0;
    }

    @Override
    protected int getSizeInventory() {

        return 0;
    }

    /* ISecurable */
    @Override
    public boolean setAccess(AccessMode access) {

        if (SecurityHelper.setAccess(getContainerStack(), access)) {
            if (CoreUtils.isClient()) {
                PacketWhoosh.sendSecurityPacketToServer(this);
            }
            return true;
        }
        return false;
    }

    @Override
    public AccessMode getAccess() {

        return SecurityHelper.getAccess(getContainerStack());
    }

    @Override
    public String getOwnerName() {

        return SecurityHelper.getOwnerName(getContainerStack());
    }

    @Override
    public GameProfile getOwner() {

        return SecurityHelper.getOwner(getContainerStack());
    }

    @Override
    public boolean canPlayerAccess(EntityPlayer player) {

        throw new UnsupportedOperationException();
    }

    @Override
    public boolean setOwnerName(String name) {

        throw new UnsupportedOperationException();
    }

    @Override
    public boolean setOwner(GameProfile name) {

        throw new UnsupportedOperationException();
    }
}
