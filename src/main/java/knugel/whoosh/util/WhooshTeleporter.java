package knugel.whoosh.util;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Teleporter;
import net.minecraft.world.WorldServer;

// Copied from RFTools
// https://github.com/McJty/RFTools/blob/73e3b57e83929c88ff0f3cdc841739ee76328f8f/src/main/java/mcjty/rftools/blocks/teleporter/RfToolsTeleporter.java#L1
public class WhooshTeleporter extends Teleporter {
    private final WorldServer worldServerInstance;

    private double x;
    private double y;
    private double z;


    public WhooshTeleporter(WorldServer world, double x, double y, double z) {
        super(world);
        this.worldServerInstance = world;
        this.x = x;
        this.y = y;
        this.z = z;

    }

    @Override
    public void placeInPortal(Entity pEntity, float rotationYaw) {
        this.worldServerInstance.getBlockState(new BlockPos((int) this.x, (int) this.y, (int) this.z));   //dummy load to maybe gen chunk

        pEntity.setPosition(this.x, this.y, this.z);
        pEntity.motionX = 0.0f;
        pEntity.motionY = 0.0f;
        pEntity.motionZ = 0.0f;
    }

}