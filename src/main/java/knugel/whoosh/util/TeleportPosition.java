package knugel.whoosh.util;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class TeleportPosition implements INBTSerializable<NBTTagCompound> {

    public String name;
    public BlockPos position;
    public int dimension;

    public TeleportPosition() {

    }

    public TeleportPosition(BlockPos position, int dimension, String name) {

        this.position = position;
        this.dimension = dimension;
        this.name = name;
    }

    @SideOnly(Side.CLIENT)
    public String getDimensionName() {

        // return DimensionManager.getWorld(dimension).getBiome(position).getBiomeName();
        return Integer.toString(dimension);
    }

    /* INBTSerializable */
    @Override
    public NBTTagCompound serializeNBT() {

        NBTTagCompound tag = new NBTTagCompound();
        tag.setInteger("x", position.getX());
        tag.setInteger("y", position.getY());
        tag.setInteger("z", position.getZ());
        tag.setInteger("dim", dimension);
        tag.setString("name", name);
        return tag;
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {

        position = new BlockPos(nbt.getInteger("x"), nbt.getInteger("y"), nbt.getInteger("z"));
        dimension = nbt.getInteger("dim");
        name = nbt.getString("name");
    }
}