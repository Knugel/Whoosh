package knugel.whoosh.util;

import knugel.whoosh.init.WProps;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

public class TeleportUtil {

    private TeleportUtil() {

    }

    public static int getRFCost(World world, BlockPos pos, TeleportPosition target) {

        int cost = 0;
        if(world.provider.getDimension() != target.dimension)
            cost += WProps.teleportDimensionCost;
        cost += Math.sqrt(pos.distanceSq(target.position)) * WProps.teleportBlockCost;
        return cost;
    }

    public static int getFluidCost(World world, BlockPos pos, TeleportPosition target) {

        int cost = 0;
        if(world.provider.getDimension() != target.dimension)
            cost += WProps.teleportDimensionFluidCost;
        return cost;
    }

    public static boolean performTeleport(World world, EntityPlayer player, TeleportPosition target) {

        BlockPos pos = target.position;
        if(world.provider.getDimension() != target.dimension) {
            teleportToDimension(player, target.dimension, pos.getX(), pos.getY(), pos.getZ());
        }
        else {
            player.setPositionAndUpdate(pos.getX()+0.5, pos.getY()+1, pos.getZ()+0.5);
        }

        return true;
    }


    // Copied from RFTools
    // https://github.com/McJty/RFTools/blob/73e3b57e83929c88ff0f3cdc841739ee76328f8f/src/main/java/mcjty/rftools/blocks/teleporter/TeleportationTools.java#L360
    public static void teleportToDimension(EntityPlayer player, int dimension, double x, double y, double z) {
        int oldDimension = player.getEntityWorld().provider.getDimension();
        EntityPlayerMP entityPlayerMP = (EntityPlayerMP) player;
        MinecraftServer server = player.getEntityWorld().getMinecraftServer();
        WorldServer worldServer = server.getWorld(dimension);
        player.addExperienceLevel(0);


        worldServer.getMinecraftServer().getPlayerList().transferPlayerToDimension(entityPlayerMP, dimension, new WhooshTeleporter(worldServer, x, y, z));
        player.setPositionAndUpdate(x, y, z);
        if (oldDimension == 1) {
            // For some reason teleporting out of the end does weird things.
            player.setPositionAndUpdate(x, y, z);
            worldServer.spawnEntity(player);
            worldServer.updateEntityWithOptionalForce(player, false);
        }
    }
}
