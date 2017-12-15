package knugel.whoosh.proxy;

import knugel.whoosh.item.ItemTransporter;
import knugel.whoosh.network.PacketWhoosh;
import knugel.whoosh.util.TeleportPosition;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.awt.*;
import java.util.List;

@SideOnly(Side.CLIENT)
public class EventHandlerClient {

    public static EventHandlerClient INSTANCE = new EventHandlerClient();

    @SubscribeEvent
    public void onMouseEvent(MouseEvent event) {
        EntityPlayerSP player = Minecraft.getMinecraft().player;
        if(event.getDwheel() != 0 && player != null && player.isSneaking()) {
            ItemStack stack = player.getHeldItemMainhand();
            Item item = stack.getItem();
            if(item instanceof ItemTransporter) {
                if(((ItemTransporter) item).getMode(stack) == 1) {

                    ItemTransporter.cycleSelected(stack, event.getDwheel());
                    PacketWhoosh.sendCycleSelectedPacketToServer(event.getDwheel());
                    event.setCanceled(true);
                }
            }
        }
    }

    @SubscribeEvent
    public void onRender(RenderGameOverlayEvent.Post event) {
        if(event.getType() != RenderGameOverlayEvent.ElementType.EXPERIENCE) {
            return;
        }

        Minecraft mc = Minecraft.getMinecraft();
        EntityPlayerSP player = mc.player;
        ItemStack stack = player.getHeldItemMainhand();
        if(stack == null || !(stack.getItem() instanceof ItemTransporter))
            return;

        if(!ItemTransporter.canPlayerAccess(stack, player))
            return;

        if(stack.getTagCompound().getInteger("Mode") == 0)
            return;

        int selected = ItemTransporter.getSelected(stack);
        List<TeleportPosition> positions = ItemTransporter.getPositions(stack);
        if(selected != -1 && positions.size() > 0) {
            int h = event.getResolution().getScaledHeight();
            int w = event.getResolution().getScaledWidth();
            String name = positions.get(selected).name;

            int width = mc.fontRenderer.getStringWidth(name);
            mc.fontRenderer.drawString(name, (w - width) / 2, h - 33, Color.WHITE.getRGB());
        }
    }
}
