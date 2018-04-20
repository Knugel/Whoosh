package knugel.whoosh.util;

import cofh.core.util.capabilities.FluidContainerItemWrapper;
import cofh.redstoneflux.util.EnergyContainerItemWrapper;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class CapabilityWrapper implements ICapabilityProvider {

    EnergyContainerItemWrapper energy;
    FluidContainerItemWrapper fluid;

    public CapabilityWrapper(EnergyContainerItemWrapper energy, FluidContainerItemWrapper fluid) {

        this.energy = energy;
        this.fluid = fluid;
    }

    @Override
    public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {

        return capability == CapabilityEnergy.ENERGY ||capability == CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY;
    }

    @Nullable
    @Override
    public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {

        if(capability == CapabilityEnergy.ENERGY) {
            return energy.getCapability(capability, facing);
        }
        else if(capability == CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY) {
            return fluid.getCapability(capability, facing);
        }

        return null;
    }
}
