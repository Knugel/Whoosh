package knugel.whoosh.item;

import cofh.api.core.ISecurable;
import cofh.api.item.IMultiModeItem;
import cofh.api.item.INBTCopyIngredient;
import cofh.core.init.CoreEnchantments;
import cofh.core.init.CoreProps;
import cofh.core.item.IEnchantableItem;
import cofh.core.item.ItemMulti;
import cofh.core.key.KeyBindingItemMultiMode;
import cofh.core.util.CoreUtils;
import cofh.core.util.RegistrySocial;
import cofh.core.util.core.IInitializer;
import cofh.core.util.helpers.*;
import cofh.redstoneflux.api.IEnergyContainerItem;
import com.mojang.authlib.GameProfile;
import gnu.trove.map.hash.TIntObjectHashMap;
import knugel.whoosh.Whoosh;
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
import net.minecraft.util.*;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

public class ItemTransporter extends ItemMulti implements IInitializer, IMultiModeItem, IEnergyContainerItem, IEnchantableItem, INBTCopyIngredient {

    public ItemTransporter() {

        super("whoosh");

        setMaxStackSize(1);
        setUnlocalizedName("transporter");
        setCreativeTab(Whoosh.tabCommon);
    }

    public int getCapacity(ItemStack stack) {

        if (!typeMap.containsKey(ItemHelper.getItemDamage(stack))) {
            return 0;
        }
        int capacity = typeMap.get(ItemHelper.getItemDamage(stack)).capacity;
        int enchant = EnchantmentHelper.getEnchantmentLevel(CoreEnchantments.holding, stack);

        return capacity + capacity * enchant / 2;
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

        if (ItemHelper.getItemDamage(stack) == CREATIVE) {
            tooltip.add(StringHelper.localize("info.cofh.charge") + ": 1.21G RF");
        } else {
            tooltip.add(StringHelper.localize("info.cofh.charge") + ": " + StringHelper.getScaledNumber(getEnergyStored(stack)) + " / " + StringHelper.getScaledNumber(getMaxEnergyStored(stack)) + " RF");
        }
    }

    @Override
    public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items) {

        if (isInCreativeTab(tab)) {
            for (int metadata : itemList) {
                if (metadata != CREATIVE) {
                    items.add(EnergyHelper.setDefaultEnergyTag(new ItemStack(this, 1, metadata), 0));
                    items.add(EnergyHelper.setDefaultEnergyTag(new ItemStack(this, 1, metadata), getBaseCapacity(metadata)));
                } else {
                    items.add(EnergyHelper.setDefaultEnergyTag(new ItemStack(this, 1, metadata), getBaseCapacity(metadata)));
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
    public int getMaxEnergyStored(ItemStack container) {

        return getCapacity(container);
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

        transporterBasic = addEntryItem(0, "standard0", CAPACITY[0], EnumRarity.COMMON);
        transporterHardened = addEntryItem(1, "standard1", CAPACITY[1], EnumRarity.COMMON);
        transporterReinforced = addEntryItem(2, "standard2", CAPACITY[2], EnumRarity.UNCOMMON);
        transporterSignalum = addEntryItem(3, "standard3", CAPACITY[3], EnumRarity.UNCOMMON);
        transporterResonant = addEntryItem(4, "standard4", CAPACITY[4], EnumRarity.RARE);

        transporterCreative = addEntryItem(CREATIVE, "creative", CAPACITY[4], EnumRarity.EPIC, false);

        Whoosh.proxy.addIModelRegister(this);

        return true;
    }

    @Override
    public boolean register() {

        return true;
    }

    private static void config() {

        String category = "Item.Transporter";

        int capacity = CAPACITY_BASE;
        String comment = "Adjust this value to change the amount of Energy (in RF) stored by a Basic Transporter. This base value will scale with item level.";
        capacity = Whoosh.CONFIG.getConfiguration().getInt("BaseCapacity", category, capacity, capacity / 5, capacity * 5, comment);

        for (int i = 0; i < CAPACITY.length; i++) {
            CAPACITY[i] *= capacity;
        }
    }

    /* ENTRY */
    public class TypeEntry {

        public final String name;
        public final int capacity;
        public final boolean enchantable;

        TypeEntry(String name, int capacity, boolean enchantable) {

            this.name = name;
            this.capacity = capacity;
            this.enchantable = enchantable;
        }
    }

    private void addEntry(int metadata, String name, int capacity, boolean enchantable) {

        typeMap.put(metadata, new TypeEntry(name, capacity, enchantable));
    }

    private ItemStack addEntryItem(int metadata, String name, int capacity, EnumRarity rarity) {

        addEntry(metadata, name, capacity, true);
        return addItem(metadata, name, rarity);
    }

    private ItemStack addEntryItem(int metadata, String name, int capacity, EnumRarity rarity, boolean enchantable) {

        addEntry(metadata, name, capacity, enchantable);
        return addItem(metadata, name, rarity);
    }

    private static TIntObjectHashMap<TypeEntry> typeMap = new TIntObjectHashMap<>();

    public static final int CAPACITY_BASE = 1000000;
    public static final int CREATIVE = 32000;
    public static final int[] CAPACITY = { 1, 4, 9, 16, 25 };

    /* REFERENCES */

    public static ItemStack transporterBasic;
    public static ItemStack transporterHardened;
    public static ItemStack transporterReinforced;
    public static ItemStack transporterSignalum;
    public static ItemStack transporterResonant;

    public static ItemStack transporterCreative;
}
