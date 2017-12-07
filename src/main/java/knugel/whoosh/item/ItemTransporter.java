package knugel.whoosh.item;

import cofh.api.core.ISecurable;
import cofh.api.fluid.IFluidContainerItem;
import cofh.api.item.IMultiModeItem;
import cofh.api.item.INBTCopyIngredient;
import cofh.core.init.CoreEnchantments;
import cofh.core.init.CoreProps;
import cofh.core.item.IEnchantableItem;
import cofh.core.item.ItemMulti;
import cofh.core.key.KeyBindingItemMultiMode;
import cofh.core.util.CoreUtils;
import cofh.core.util.RegistrySocial;
import cofh.core.util.capabilities.FluidContainerItemWrapper;
import cofh.core.util.core.IInitializer;
import cofh.core.util.helpers.*;
import cofh.redstoneflux.api.IEnergyContainerItem;
import cofh.thermalfoundation.init.TFFluids;
import cofh.thermalfoundation.init.TFItems;
import cofh.thermalfoundation.item.ItemMaterial;
import com.mojang.authlib.GameProfile;
import gnu.trove.map.hash.TIntObjectHashMap;
import knugel.whoosh.Whoosh;
import knugel.whoosh.gui.GuiHandler;
import knugel.whoosh.util.TeleportPosition;
import knugel.whoosh.util.TeleportUtil;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.WorldServer;
import net.minecraft.world.storage.WorldInfo;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static cofh.core.util.helpers.RecipeHelper.addShapedOreRecipe;
import static cofh.core.util.helpers.RecipeHelper.addShapedRecipe;

public class ItemTransporter extends ItemMulti implements IInitializer, IMultiModeItem, IEnergyContainerItem, IFluidContainerItem, IEnchantableItem, INBTCopyIngredient {

    public ItemTransporter() {

        super("whoosh");

        setMaxStackSize(1);
        setUnlocalizedName("transporter");
        setCreativeTab(Whoosh.tabCommon);
    }

    public int getBaseCapacity(int metadata) {

        if (!typeMap.containsKey(metadata)) {
            return 0;
        }
        return typeMap.get(metadata).capacity;
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {

        if (StringHelper.displayShiftForDetail && !StringHelper.isShiftKeyDown()) {
            tooltip.add(StringHelper.shiftForDetails());
        }
        if (!StringHelper.isShiftKeyDown()) {
            return;
        }

        SecurityHelper.addAccessInformation(stack, tooltip);

        tooltip.add(StringHelper.getInfoText("info.whoosh.transporter.a." + getMode(stack)));
        tooltip.add(StringHelper.localizeFormat("info.whoosh.transporter.b.0", StringHelper.getKeyName(KeyBindingItemMultiMode.INSTANCE.getKey())));

        if (!typeMap.get(ItemHelper.getItemDamage(stack)).dimension) {
            tooltip.add(StringHelper.localizeFormat("info.whoosh.transporter.c.0"));
        }

        if(getMode(stack) == 0) {
            tooltip.add(StringHelper.localizeFormat("info.whoosh.transporter.c.2", typeMap.get(ItemHelper.getItemDamage(stack)).range));
        }
        else if(getSelected(stack) != -1) {
            TeleportPosition pos = getPositions(stack).get(getSelected(stack));
            tooltip.add(StringHelper.localizeFormat("info.whoosh.transporter.c.1", pos.name));
        }

        if (ItemHelper.getItemDamage(stack) == CREATIVE) {
            tooltip.add(StringHelper.localize("info.cofh.charge") + ": 1.21G RF");
        } else {
            tooltip.add(StringHelper.localize("info.cofh.charge") + ": " + StringHelper.getScaledNumber(getEnergyStored(stack)) + " / " + StringHelper.getScaledNumber(getMaxEnergyStored(stack)) + " RF");
        }

        if (ItemHelper.getItemDamage(stack) == CREATIVE) {
            tooltip.add(StringHelper.localize("info.cofh.infiniteSource"));
        } else {
            int amount = 0;
            FluidStack fluid = getFluid(stack);
            if(fluid != null) {
                amount = fluid.amount;
            }

            tooltip.add(StringHelper.localize("info.cofh.level") + ": " + amount + " / " + StringHelper.formatNumber(getCapacity(stack)) + " mB");
        }
    }

    @Override
    public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items) {

        if (isInCreativeTab(tab)) {
            for (int metadata : itemList) {
                if (metadata != CREATIVE) {
                    items.add(EnergyHelper.setDefaultEnergyTag(new ItemStack(this, 1, metadata), 0));
                    items.add(EnergyHelper.setDefaultEnergyTag(FluidHelper.setDefaultFluidTag(new ItemStack(this, 1, metadata),
                                    new FluidStack(TFFluids.fluidEnder, getTankBaseCapacity(metadata))), getBaseCapacity(metadata)));
                } else {
                    items.add(EnergyHelper.setDefaultEnergyTag(FluidHelper.setDefaultFluidTag(new ItemStack(this, 1, metadata),
                                    new FluidStack(TFFluids.fluidEnder, getTankBaseCapacity(metadata))), getBaseCapacity(metadata)));
                }
            }
        }
    }

    @Override
    public boolean isFull3D() {

        return true;
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {

        return typeMap.get(ItemHelper.getItemDamage(stack)).enchantable;
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {

        return super.shouldCauseReequipAnimation(oldStack, newStack, slotChanged) && (slotChanged || !ItemHelper.areItemStacksEqualIgnoreTags(oldStack, newStack, "Energy"));
    }

    @Override
    public boolean showDurabilityBar(ItemStack stack) {

        if (!stack.hasTagCompound()) {
            return ItemHelper.getItemDamage(stack) != CREATIVE;
        }
        else {
            return ItemHelper.getItemDamage(stack) != CREATIVE && !stack.getTagCompound().getBoolean("CreativeTab");
        }
    }

    @Override
    public int getItemEnchantability() {

        return 10;
    }

    @Override
    public int getRGBDurabilityForDisplay(ItemStack stack) {

        return CoreProps.RGB_DURABILITY_FLUX;
    }

    @Override
    public double getDurabilityForDisplay(ItemStack stack) {

        if (stack.getTagCompound() == null) {
            EnergyHelper.setDefaultEnergyTag(stack, 0);
        }
        return 1.0D - ((double) stack.getTagCompound().getInteger("Energy") / (double) getMaxEnergyStored(stack));
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {

        ItemStack stack = player.getHeldItem(hand);
        if (CoreUtils.isFakePlayer(player) || hand != EnumHand.MAIN_HAND) {
            return new ActionResult<>(EnumActionResult.FAIL, stack);
        }

        if (ServerHelper.isServerWorld(world)) {
            if (SecurityHelper.isSecure(stack) && SecurityHelper.isDefaultUUID(SecurityHelper.getOwner(stack).getId())) {
                SecurityHelper.setOwner(stack, player.getGameProfile());
                ChatHelper.sendIndexedChatMessageToPlayer(player, new TextComponentTranslation("chat.cofh.secure.item.success"));
                return new ActionResult<>(EnumActionResult.SUCCESS, stack);
            }
            if (canPlayerAccess(stack, player)) {

                long lastUsed = getLastUsed(stack);
                if(lastUsed != 0 && world.getTotalWorldTime() - lastUsed < cooldownUsage)
                    return new ActionResult<>(EnumActionResult.FAIL, stack);

                if(getMode(stack) == 1) {
                    if(player.isSneaking()) {
                        int index = getSelected(stack);
                        if(index != -1) {
                            TeleportPosition target = getPositions(stack).get(index);
                            int rfCost = TeleportUtil.getRFCost(world, new BlockPos(player.posX, player.posY, player.posZ), target);
                            int fluidCost = TeleportUtil.getFluidCost(world, target);
                            TypeEntry type = typeMap.get(ItemHelper.getItemDamage(stack));

                            if(target.dimension != world.provider.getDimension() && !type.dimension) {
                                ChatHelper.sendIndexedChatMessageToPlayer(player, new TextComponentTranslation("chat.transporter.dimension.warning"));
                                return new ActionResult<>(EnumActionResult.FAIL, stack);
                            }

                            if(rfCost > getEnergyStored(stack)) {
                                ChatHelper.sendIndexedChatMessageToPlayer(player, new TextComponentTranslation("chat.transporter.rf.warning", rfCost - getEnergyStored(stack)));
                                return new ActionResult<>(EnumActionResult.FAIL, stack);
                            }

                            if(fluidCost != 0 && (getFluid(stack) == null || fluidCost > getFluid(stack).amount)) {
                                ChatHelper.sendIndexedChatMessageToPlayer(player, new TextComponentTranslation("chat.transporter.fluid.warning"));
                                return new ActionResult<>(EnumActionResult.FAIL, stack);
                            }

                            if(TeleportUtil.performTeleport(world, player, target)) {
                                extractEnergy(stack, rfCost, false);
                                drain(stack, fluidCost, true);
                                world.playSound(null, player.posX, player.posY, player.posZ, SoundEvents.ENTITY_ENDERMEN_TELEPORT, SoundCategory.NEUTRAL, 0.5f, 1f);
                                setLastUsed(stack, world.getTotalWorldTime());
                            }
                        }
                    }
                    else {
                        player.openGui(Whoosh.instance, GuiHandler.TRANSPORTER_ID, world, 0, 0, 0);
                    }
                } else {
                    if(player.isSneaking()) {

                        int range = typeMap.get(ItemHelper.getItemDamage(stack)).range;
                        int rfCost = TeleportUtil.getRFCostBlink(world, player, range);
                        int fluidCost = TeleportUtil.getFluidCostBlink(world, player, range);

                        if(rfCost > getEnergyStored(stack)) {
                            ChatHelper.sendIndexedChatMessageToPlayer(player, new TextComponentTranslation("chat.transporter.rf.warning", rfCost - getEnergyStored(stack)));
                            return new ActionResult<>(EnumActionResult.FAIL, stack);
                        }

                        FluidStack fluid = getFluid(stack);
                        if(fluidCost != 0 && (fluid == null || fluidCost > fluid.amount)) {
                            ChatHelper.sendIndexedChatMessageToPlayer(player, new TextComponentTranslation("chat.transporter.fluid.warning", fluidCost - (fluid == null ? 0 : fluid.amount)));
                            return new ActionResult<>(EnumActionResult.FAIL, stack);
                        }

                        if(TeleportUtil.performBlink(world, player, range)) {
                            extractEnergy(stack, rfCost, false);
                            drain(stack, fluidCost, true);
                            world.playSound(null, player.posX, player.posY, player.posZ, SoundEvents.ENTITY_ENDERMEN_TELEPORT, SoundCategory.NEUTRAL, 0.5f, 1f);
                            setLastUsed(stack, world.getTotalWorldTime());
                        }
                    }
                    else {
                        return new ActionResult<>(EnumActionResult.FAIL, stack);
                    }
                }
            } else if (SecurityHelper.isSecure(stack)) {
                ChatHelper.sendIndexedChatMessageToPlayer(player, new TextComponentTranslation("chat.cofh.secure.warning", SecurityHelper.getOwnerName(stack)));
                return new ActionResult<>(EnumActionResult.FAIL, stack);
            }
        }
        return new ActionResult<>(EnumActionResult.SUCCESS, stack);
    }

    /* HELPERS */
    public static boolean canPlayerAccess(ItemStack stack, EntityPlayer player) {

        if (!SecurityHelper.isSecure(stack)) {
            return true;
        }
        String name = player.getName();
        ISecurable.AccessMode access = SecurityHelper.getAccess(stack);
        if (access.isPublic() || (CoreProps.enableOpSecureAccess && CoreUtils.isOp(name))) {
            return true;
        }
        GameProfile profile = SecurityHelper.getOwner(stack);
        UUID ownerID = profile.getId();
        if (SecurityHelper.isDefaultUUID(ownerID)) {
            return true;
        }
        UUID otherID = SecurityHelper.getID(player);
        return ownerID.equals(otherID) || access.isFriendsOnly() && RegistrySocial.playerHasAccess(name, profile);
    }

    private static void createDefaultTag(ItemStack stack) {

        if(!stack.hasTagCompound())
            stack.setTagCompound(new NBTTagCompound());

        NBTTagCompound tag = stack.getTagCompound();
        if(!tag.hasKey("Positions")) {
            NBTTagList list = new NBTTagList();
            tag.setTag("Positions", list);
        }
    }

    public static void appendPoint(ItemStack stack, TeleportPosition pos) {

        createDefaultTag(stack);
        NBTTagCompound tag = stack.getTagCompound();
        NBTTagList list = tag.getTagList("Positions", Constants.NBT.TAG_COMPOUND);
        list.appendTag(pos.serializeNBT());
    }

    public static boolean removePoint(ItemStack stack, int index) {

        createDefaultTag(stack);
        NBTTagCompound tag = stack.getTagCompound();
        NBTTagList list = tag.getTagList("Positions", Constants.NBT.TAG_COMPOUND);

        if(index > list.tagCount() || index < 0)
            return false;

        list.removeTag(index);
        return true;
    }

    public static List<TeleportPosition> getPositions(ItemStack stack) {

        createDefaultTag(stack);
        List<TeleportPosition> points = new ArrayList<>();
        NBTTagCompound tag = stack.getTagCompound();
        NBTTagList list = tag.getTagList("Positions", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < list.tagCount(); i++) {
            TeleportPosition p = new TeleportPosition();
            p.deserializeNBT(list.getCompoundTagAt(i));
            points.add(p);
        }

        return points;
    }

    public static int getSelected(ItemStack stack) {

        if(!stack.hasTagCompound())
            return -1;

        NBTTagCompound tag = stack.getTagCompound();
        if(!tag.hasKey("Selected"))
            return -1;

        return tag.getInteger("Selected");
    }

    public static void setSelected(ItemStack stack, int index) {

        if(!stack.hasTagCompound())
            return;

        NBTTagCompound tag = stack.getTagCompound();
        tag.setInteger("Selected", index);
    }

    public static void setLastUsed(ItemStack stack, long time) {

        if(!stack.hasTagCompound())
            return;

        NBTTagCompound tag = stack.getTagCompound();
        tag.setLong("LastUsed", time);
    }

    public static long getLastUsed(ItemStack stack) {

        if(!stack.hasTagCompound())
            return 0;

        NBTTagCompound tag = stack.getTagCompound();
        if(!tag.hasKey("LastUsed"))
            return 0;
        return tag.getLong("LastUsed");
    }

    /* IFluidContainerItem */
    @Override
    public FluidStack drain(ItemStack container, int maxDrain, boolean doDrain) {

        if (container.getTagCompound() == null) {
            container.setTagCompound(new NBTTagCompound());
        }
        if (!container.getTagCompound().hasKey("Fluid") || maxDrain == 0) {
            return null;
        }
        FluidStack stack = FluidStack.loadFluidStackFromNBT(container.getTagCompound().getCompoundTag("Fluid"));

        if (stack == null) {
            return null;
        }
        int drained = Math.min(stack.amount, maxDrain);

        if (doDrain && ItemHelper.getItemDamage(container) != CREATIVE) {
            if (maxDrain >= stack.amount) {
                container.getTagCompound().removeTag("Fluid");
                return stack;
            }
            NBTTagCompound fluidTag = container.getTagCompound().getCompoundTag("Fluid");
            fluidTag.setInteger("Amount", fluidTag.getInteger("Amount") - drained);
            container.getTagCompound().setTag("Fluid", fluidTag);
        }
        stack.amount = drained;
        return stack;
    }

    @Override
    public int fill(ItemStack container, FluidStack resource, boolean doFill) {

        if (container.getTagCompound() == null) {
            container.setTagCompound(new NBTTagCompound());
        }
        if (resource == null) {
            return 0;
        }
        if(resource.getFluid() != TFFluids.fluidEnder) {
            return 0;
        }

        int capacity = getCapacity(container);

        if (ItemHelper.getItemDamage(container) == CREATIVE) {
            if (doFill) {
                NBTTagCompound fluidTag = resource.writeToNBT(new NBTTagCompound());
                fluidTag.setInteger("Amount", capacity - Fluid.BUCKET_VOLUME);
                container.getTagCompound().setTag("Fluid", fluidTag);
            }
            return resource.amount;
        }
        if (!doFill) {
            if (!container.getTagCompound().hasKey("Fluid")) {
                return Math.min(capacity, resource.amount);
            }
            FluidStack stack = FluidStack.loadFluidStackFromNBT(container.getTagCompound().getCompoundTag("Fluid"));

            if (stack == null) {
                return Math.min(capacity, resource.amount);
            }
            if (!stack.isFluidEqual(resource)) {
                return 0;
            }
            return Math.min(capacity - stack.amount, resource.amount);
        }
        if (!container.getTagCompound().hasKey("Fluid")) {
            NBTTagCompound fluidTag = resource.writeToNBT(new NBTTagCompound());

            if (capacity < resource.amount) {
                fluidTag.setInteger("Amount", capacity);
                container.getTagCompound().setTag("Fluid", fluidTag);
                return capacity;
            }
            fluidTag.setInteger("Amount", resource.amount);
            container.getTagCompound().setTag("Fluid", fluidTag);
            return resource.amount;
        }
        NBTTagCompound fluidTag = container.getTagCompound().getCompoundTag("Fluid");
        FluidStack stack = FluidStack.loadFluidStackFromNBT(fluidTag);

        if (!stack.isFluidEqual(resource)) {
            return 0;
        }
        int filled = capacity - stack.amount;

        if (resource.amount < filled) {
            stack.amount += resource.amount;
            filled = resource.amount;
        } else {
            stack.amount = capacity;
        }
        container.getTagCompound().setTag("Fluid", stack.writeToNBT(fluidTag));
        return filled;
    }

    @Override
    public FluidStack getFluid(ItemStack container) {

        if (container.getTagCompound() == null) {
            container.setTagCompound(new NBTTagCompound());
        }
        if (!container.getTagCompound().hasKey("Fluid")) {
            return null;
        }
        return FluidStack.loadFluidStackFromNBT(container.getTagCompound().getCompoundTag("Fluid"));
    }

    @Override
    public int getCapacity(ItemStack stack) {

        if (!typeMap.containsKey(ItemHelper.getItemDamage(stack))) {
            return 0;
        }
        int capacity = typeMap.get(ItemHelper.getItemDamage(stack)).tank;
        int enchant = EnchantmentHelper.getEnchantmentLevel(CoreEnchantments.holding, stack);

        return capacity + capacity * enchant / 2;
    }

    public int getTankBaseCapacity(int metadata) {

        if (!typeMap.containsKey(metadata)) {
            return 0;
        }

        return typeMap.get(metadata).tank;
    }

    /* CAPABILITIES */
    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, NBTTagCompound nbt) {

        return new FluidContainerItemWrapper(stack, this);
    }

    /* IEnergyContainerItem */
    @Override
    public int receiveEnergy(ItemStack container, int maxReceive, boolean simulate) {

        if (container.getTagCompound() == null) {
            EnergyHelper.setDefaultEnergyTag(container, 0);
        }
        int stored = container.getTagCompound().getInteger("Energy");
        int receive = Math.min(maxReceive, getMaxEnergyStored(container) - stored);

        if (!simulate && ItemHelper.getItemDamage(container) != CREATIVE) {
            stored += receive;
            container.getTagCompound().setInteger("Energy", stored);
        }
        return receive;
    }

    @Override
    public int extractEnergy(ItemStack container, int maxExtract, boolean simulate) {

        if (container.getTagCompound() == null) {
            EnergyHelper.setDefaultEnergyTag(container, 0);
        }
        int stored = container.getTagCompound().getInteger("Energy");
        int extract = Math.min(maxExtract, stored);

        if (!simulate && ItemHelper.getItemDamage(container) != CREATIVE) {
            stored -= extract;
            container.getTagCompound().setInteger("Energy", stored);
        }
        return extract;
    }

    @Override
    public int getEnergyStored(ItemStack container) {

        if (container.getTagCompound() == null) {
            EnergyHelper.setDefaultEnergyTag(container, 0);
        }
        return container.getTagCompound().getInteger("Energy");
    }

    @Override
    public int getMaxEnergyStored(ItemStack stack) {

        if (!typeMap.containsKey(ItemHelper.getItemDamage(stack))) {
            return 0;
        }
        int capacity = typeMap.get(ItemHelper.getItemDamage(stack)).capacity;
        int enchant = EnchantmentHelper.getEnchantmentLevel(CoreEnchantments.holding, stack);

        return capacity + capacity * enchant / 2;
    }

    /* IEnchantableItem */
    @Override
    public boolean canEnchant(ItemStack stack, Enchantment enchantment) {

        return typeMap.containsKey(ItemHelper.getItemDamage(stack)) && typeMap.get(ItemHelper.getItemDamage(stack)).enchantable && enchantment == CoreEnchantments.holding;
    }

    /* IMultiModeItem */
    @Override
    public int getMode(ItemStack stack) {

        return !stack.hasTagCompound() ? 0 : stack.getTagCompound().getInteger("Mode");
    }

    @Override
    public boolean setMode(ItemStack stack, int mode) {

        if (!stack.hasTagCompound()) {
            stack.setTagCompound(new NBTTagCompound());
        }
        stack.getTagCompound().setInteger("Mode", mode);
        return false;
    }

    @Override
    public boolean incrMode(ItemStack stack) {

        if (!stack.hasTagCompound()) {
            stack.setTagCompound(new NBTTagCompound());
        }
        int curMode = getMode(stack);
        curMode++;
        if (curMode >= getNumModes(stack)) {
            curMode = 0;
        }
        stack.getTagCompound().setInteger("Mode", curMode);
        return true;
    }

    @Override
    public boolean decrMode(ItemStack stack) {

        if (!stack.hasTagCompound()) {
            stack.setTagCompound(new NBTTagCompound());
        }
        int curMode = getMode(stack);
        curMode--;
        if (curMode <= 0) {
            curMode = getNumModes(stack) - 1;
        }
        stack.getTagCompound().setInteger("Mode", curMode);
        return true;
    }

    @Override
    public int getNumModes(ItemStack stack) {

        return 2;
    }

    @Override
    public void onModeChange(EntityPlayer player, ItemStack stack) {

        player.world.playSound(null, player.getPosition(), SoundEvents.BLOCK_LEVER_CLICK, SoundCategory.PLAYERS, 0.4F, (0.5F) + 0.1F * getMode(stack));
        ChatHelper.sendIndexedChatMessageToPlayer(player, new TextComponentTranslation("info.whoosh.transporter.a." + getMode(stack)));
    }

    /* IInitializer */
    @Override
    public boolean initialize() {

        config();

        transporterBasic = addEntryItem(0, "standard0", CAPACITY[0], TANK[0], RANGE[0], DIMENSION[0], EnumRarity.COMMON);
        transporterHardened = addEntryItem(1, "standard1", CAPACITY[1], TANK[1], RANGE[1], DIMENSION[1], EnumRarity.COMMON);
        transporterReinforced = addEntryItem(2, "standard2", CAPACITY[2], TANK[2], RANGE[2], DIMENSION[2], EnumRarity.UNCOMMON);
        transporterSignalum = addEntryItem(3, "standard3", CAPACITY[3], TANK[3], RANGE[3], DIMENSION[3], EnumRarity.UNCOMMON);
        transporterResonant = addEntryItem(4, "standard4", CAPACITY[4], TANK[4], RANGE[4], DIMENSION[4], EnumRarity.RARE);

        transporterCreative = addEntryItem(CREATIVE, "creative", CAPACITY[4], TANK[4], RANGE[4], DIMENSION[4], EnumRarity.EPIC, false);

        Whoosh.proxy.addIModelRegister(this);

        return true;
    }

    @Override
    public boolean register() {

        addShapedRecipe(transporterBasic,
                " G ",
                "IXI",
                "RYR",
                'G', "paneGlass",
                'I', "plateLead",
                'R', "enderpearl",
                'Y', "ingotCopper",
                'X', ItemMaterial.powerCoilGold
        );

        return true;
    }

    private static void config() {

        String category = "Item.Transporter";

        int capacity = CAPACITY_BASE;
        String comment = "Adjust this value to change the amount of Energy (in RF) stored by a Basic Transporter. This base value will scale with item level.";
        capacity = Whoosh.CONFIG.getConfiguration().getInt("BaseCapacity", category, capacity, capacity / 5, capacity * 5, comment);

        comment = "Adjust this value to change the amount of Energy (in RF) required to teleport across dimensions.";
        teleportDimensionCost = BASE_DIMENSION_COST;
        teleportDimensionCost = Whoosh.CONFIG.getConfiguration().getInt("DimensionCost", category, teleportDimensionCost, 0, Integer.MAX_VALUE, comment);

        comment = "Adjust this value to change the amount of Energy (in RF) required to teleport a distance of 1 block.";
        teleportBlockCost = BASE_BLOCK_COST;
        teleportBlockCost = Whoosh.CONFIG.getConfiguration().getInt("BlockCost", category, teleportBlockCost, 0, Integer.MAX_VALUE, comment);

        comment = "Adjust this value to change the amount of Fluid (in mb) required to teleport across dimensions.";
        teleportDimensionFluidCost = BASE_DIMENSION_FLUID_COST;
        teleportDimensionFluidCost = Whoosh.CONFIG.getConfiguration().getInt("DimensionFluidCost", category, teleportDimensionFluidCost, 0, Integer.MAX_VALUE, comment);

        comment = "Adjust this value to change the amount of Fluid (in mb) required to blink through blocks.";
        teleportFluidBlinkCost = BASE_FLUID_BLINK_COST;
        teleportFluidBlinkCost = Whoosh.CONFIG.getConfiguration().getInt("BlinkFluidCost", category, teleportFluidBlinkCost, 0, Integer.MAX_VALUE, comment);

        comment = "Adjust this value to change the amount of Energy (in RF) required to blink a distance of 1 block.";
        teleportBlockBlinkCost = BASE_BLOCK_BLINK_COST;
        teleportBlockBlinkCost = Whoosh.CONFIG.getConfiguration().getInt("BlinkCost", category, teleportBlockBlinkCost, 0, Integer.MAX_VALUE, comment);

        comment = "Adjust this value to change the cooldown (in ticks) between usages.";
        cooldownUsage = BASE_COOLDOWN;
        cooldownUsage = Whoosh.CONFIG.getConfiguration().getInt("Cooldown", category, cooldownUsage, 0, Integer.MAX_VALUE, comment);

        for (int i = 0; i < CAPACITY.length; i++) {
            CAPACITY[i] *= capacity;
        }
    }

    /* ENTRY */
    public class TypeEntry {

        public final String name;
        public final int capacity;
        public final int tank;
        public final int range;
        public final boolean dimension;
        public final boolean enchantable;

        TypeEntry(String name, int capacity, int tank, int range, boolean dimension, boolean enchantable) {

            this.name = name;
            this.capacity = capacity;
            this.dimension = dimension;
            this.range = range;
            this.enchantable = enchantable;
            this.tank = tank;
        }
    }

    private void addEntry(int metadata, String name, int capacity, int tank, int range, boolean dimension, boolean enchantable) {

        typeMap.put(metadata, new TypeEntry(name, capacity, tank, range, dimension, enchantable));
    }

    private ItemStack addEntryItem(int metadata, String name, int capacity, int tank, int range, boolean dimension, EnumRarity rarity) {

        addEntry(metadata, name, capacity, tank, range, dimension, true);
        return addItem(metadata, name, rarity);
    }

    private ItemStack addEntryItem(int metadata, String name, int capacity, int tank, int range, boolean dimension, EnumRarity rarity, boolean enchantable) {

        addEntry(metadata, name, capacity, tank, range, dimension, enchantable);
        return addItem(metadata, name, rarity);
    }

    private static TIntObjectHashMap<TypeEntry> typeMap = new TIntObjectHashMap<>();

    public static final int CAPACITY_BASE = 50000;
    public static final int BASE_DIMENSION_COST = 50000;
    public static final int BASE_DIMENSION_FLUID_COST = 250;
    public static final int BASE_BLOCK_COST = 50;
    public static final int BASE_BLOCK_BLINK_COST = 150;
    public static final int BASE_FLUID_BLINK_COST = 50;
    public static final int BASE_COOLDOWN = 5;
    public static final int CREATIVE = 32000;
    public static final int[] CAPACITY = { 1, 2, 5, 10, 15 };
    public static final int[] TANK = { 1000, 2000, 5000, 10000, 15000 };
    public static final int[] RANGE = { 4, 6, 8, 10, 12 };
    public static final boolean[] DIMENSION = { false, false, true, true, true };

    public static int teleportDimensionCost;
    public static int teleportDimensionFluidCost;
    public static int teleportBlockCost;
    public static int teleportBlockBlinkCost;
    public static int teleportFluidBlinkCost;
    public static int cooldownUsage;

    /* REFERENCES */

    public static ItemStack transporterBasic;
    public static ItemStack transporterHardened;
    public static ItemStack transporterReinforced;
    public static ItemStack transporterSignalum;
    public static ItemStack transporterResonant;

    public static ItemStack transporterCreative;
}
