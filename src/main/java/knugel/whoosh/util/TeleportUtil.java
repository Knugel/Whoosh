package knugel.whoosh.util;

import knugel.whoosh.Whoosh;
import knugel.whoosh.init.WProps;
import knugel.whoosh.item.ItemTransporter;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

public class TeleportUtil {

    private TeleportUtil() {

    }

    public static int getRFCost(World world, BlockPos pos, TeleportPosition target) {

        int cost = 0;
        if(world.provider.getDimension() != target.dimension)
            cost += ItemTransporter.teleportDimensionCost;
        cost += Math.sqrt(pos.distanceSq(target.position)) * ItemTransporter.teleportBlockCost;
        return cost;
    }

    public static int getFluidCost(World world, TeleportPosition target) {

        int cost = 0;
        if(world.provider.getDimension() != target.dimension)
            cost += ItemTransporter.teleportDimensionFluidCost;
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


    public static int getRFCostBlink(World world, EntityPlayer player, int distance) {

        Vec3d eye = new Vec3d(player.posX, player.posY + player.getEyeHeight(), + player.posZ);
        Vec3d look = player.getLookVec();
        Vec3d end = eye.add(new Vec3d(look.x * distance, look.y * distance, look.z * distance));

        RayTraceResult res = world.rayTraceBlocks(eye, end, false, true, false);
        if(res == null) {
            return distance * ItemTransporter.teleportBlockBlinkCost;
        }
        else {

            Vec3d n = end.subtract(res.hitVec).normalize();
            for(int d = distance; d > 0; d--) {
                Vec3d v = res.hitVec.add(n.scale(d));
                if(world.isAirBlock(new BlockPos(v)) && world.isAirBlock(new BlockPos(v.subtract(0, 1, 0)))) {
                    BlockPos pos = new BlockPos(v);
                    return (int)Math.sqrt(pos.distanceSq(new BlockPos(eye))) * ItemTransporter.teleportBlockBlinkCost;
                }
            }
        }

        return 0;
    }

    public static int getFluidCostBlink(World world, EntityPlayer player, int distance) {

        Vec3d eye = new Vec3d(player.posX, player.posY + player.getEyeHeight(), + player.posZ);
        Vec3d look = player.getLookVec();
        Vec3d end = eye.add(new Vec3d(look.x * distance, look.y * distance, look.z * distance));

        RayTraceResult res = world.rayTraceBlocks(eye, end, false, true, false);
        if(res == null) {
           return 0;
        }
        else {
            return ItemTransporter.teleportFluidBlinkCost;
        }
    }

    public static boolean performBlink(World world, EntityPlayer player, int distance) {

        Vec3d eye = new Vec3d(player.posX, player.posY + player.getEyeHeight(), + player.posZ);
        Vec3d look = player.getLookVec();
        Vec3d end = eye.add(new Vec3d(look.x * distance, look.y * distance, look.z * distance));

        RayTraceResult res = world.rayTraceBlocks(eye, end, false, true, false);
        if(res == null) {
            player.setPositionAndUpdate(end.x, end.y, end.z);
            return true;
        }
        else {

            Vec3d n = end.subtract(res.hitVec).normalize();
            for(int d = distance; d > 0; d--) {
                Vec3d v = res.hitVec.add(n.scale(d));
                if(world.isAirBlock(new BlockPos(v)) && world.isAirBlock(new BlockPos(v.subtract(0, 1, 0)))) {
                    BlockPos pos = new BlockPos(v);
                    player.setPositionAndUpdate(pos.getX() + 0.5, pos.getY() - 1, pos.getZ());
                    return true;
                }
            }
        }

        return false;
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
