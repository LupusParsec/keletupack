package keletu.keletupack.items.armor;

import com.google.common.collect.Multimap;
import keletu.keletupack.clinet.ModelWings;
import keletu.keletupack.event.LivingEvent;
import keletu.keletupack.init.ModBlocks;
import keletu.keletupack.init.ModItems;
import keletu.keletupack.keletupack;
import keletu.keletupack.util.IHasModel;
import keletu.keletupack.util.Reference;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IProjectile;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.item.EntityExpBottle;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.entity.projectile.EntityPotion;
import net.minecraft.entity.projectile.EntitySnowball;
import net.minecraft.entity.projectile.ProjectileHelper;
import net.minecraft.init.Blocks;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.EnumHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import thaumcraft.api.items.IGoggles;
import thaumcraft.api.items.IVisDiscountGear;
import thaumcraft.codechicken.lib.vec.Vector3;
import thaumcraft.common.lib.events.PlayerEvents;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class KamiArmor extends ItemArmor implements IVisDiscountGear, IGoggles, IHasModel {
    public static final ArmorMaterial ICHORADV = EnumHelper.addArmorMaterial("ICHORADV", "ichoradv", 0, new int[]{
            3,
            8,
            6,
            3
    }, 40, SoundEvents.ITEM_ARMOR_EQUIP_LEATHER, 3F);
    private final int[] discounts = new int[]{
            0,
            0,
            4,
            4,
            4,
            4
    };

    public KamiArmor(String name, ArmorMaterial materialIn, int renderIndexIn, EntityEquipmentSlot equipmentSlotIn, CreativeTabs tab) {
        super(materialIn, renderIndexIn, equipmentSlotIn);
        setUnlocalizedName(name);
        setRegistryName(name);
        setCreativeTab(tab);

        ModItems.ITEMS.add(this);
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return true;
    }

    @Override
    public void onArmorTick(World world, EntityPlayer mp, ItemStack itemStack) {
        ItemStack tmp;
        boolean headWorn = !(tmp = mp.getItemStackFromSlot(EntityEquipmentSlot.HEAD)).isEmpty() && tmp.getItem() instanceof KamiArmor;
        boolean bodyWorn = !(tmp = mp.getItemStackFromSlot(EntityEquipmentSlot.CHEST)).isEmpty() && tmp.getItem() instanceof KamiArmor;
        boolean beltWorn = !(tmp = mp.getItemStackFromSlot(EntityEquipmentSlot.LEGS)).isEmpty() && tmp.getItem() instanceof KamiArmor;
        boolean bootsWorn = !(tmp = mp.getItemStackFromSlot(EntityEquipmentSlot.FEET)).isEmpty() && tmp.getItem() instanceof KamiArmor;

        switch (armorType) {
            case HEAD: {
                if (mp.getEntityWorld().getBlockState(mp.getPosition().up()).getBlock() == Blocks.WATER || mp.getEntityWorld().getBlockState(mp.getPosition().up()).getBlock() == Blocks.FLOWING_WATER) {
                    mp.addPotionEffect(new PotionEffect(MobEffects.WATER_BREATHING, 31, 0, true, false));
                    mp.addPotionEffect(new PotionEffect(MobEffects.NIGHT_VISION, 400, 0, true, false));
                }
                if ((mp.getEntityWorld().getBlockState(mp.getPosition().up()).getBlock() == Blocks.LAVA || mp.getEntityWorld().getBlockState(mp.getPosition().up()).getBlock() == Blocks.FLOWING_LAVA) && mp.ticksExisted % 10 == 0)
                    mp.addPotionEffect(new PotionEffect(MobEffects.BLINDNESS, 31, 0, true, false));
                int food = mp.getFoodStats().getFoodLevel();
                if (food > 0 && food < 18 && mp.shouldHeal()
                        && mp.ticksExisted % 80 == 0)
                    mp.heal(1F);
            }
            break;

            case CHEST: {
                mp.getEntityData().setBoolean("can_fly", true);
                doProjectileEffect(mp);
            }
            break;
            case LEGS: {
                if (mp.getActivePotionEffect(MobEffects.FIRE_RESISTANCE) == null || mp.getActivePotionEffect(MobEffects.FIRE_RESISTANCE).getDuration() <= 1) {
                    mp.addPotionEffect(new PotionEffect(MobEffects.FIRE_RESISTANCE, 1, 0, false, false));
                    if (mp.isBurning()) {
                        mp.addPotionEffect(new PotionEffect(MobEffects.REGENERATION, 20, 1, true, false));
                        mp.extinguish();
                    }
                }
            }
            break;

            case FEET: {
                {
                    if (!mp.capabilities.isFlying && mp.moveForward > 0.0F) {
                        if (mp.world.isRemote && !mp.isSneaking()) {
                            if (!PlayerEvents.prevStep.containsKey(Integer.valueOf(mp.getEntityId())))
                                PlayerEvents.prevStep.put(Integer.valueOf(mp.getEntityId()), Float.valueOf(mp.stepHeight));
                            mp.stepHeight = 1.0F;
                        }
                        if (mp.onGround) {
                            float bonus = 0.1F;
                            if (mp.isInWater())
                                bonus /= 4.0F;
                            mp.moveRelative(0.0F, 0.0F, bonus, 1.0F);
                        } else {
                            if (mp.isInWater())
                                mp.moveRelative(0.0F, 0.0F, 0.15F, 1.0F);
                            mp.jumpMovementFactor = 0.05F;
                        }
                    }
                    if (mp.getActivePotionEffect(MobEffects.HASTE) == null || mp.getActivePotionEffect(MobEffects.HASTE).getDuration() <= 1) {
                        mp.addPotionEffect(new PotionEffect(MobEffects.HASTE, 200, 1, false, false));
                    }
                }
                BlockPos posBelow = mp.getPosition().down();
                IBlockState blockStateBelow = mp.world.getBlockState(posBelow);
                if (blockStateBelow.getBlock() == Blocks.DIRT) {
                    mp.getEntityWorld().setBlockState(mp.getPosition().down(), Blocks.GRASS.getDefaultState(), 0);
                }
            }
            break;

        }
    }

    @Override
    public EnumRarity getRarity(ItemStack itemstack) {
        return EnumRarity.EPIC;
    }

    @Override
    public Multimap<String, AttributeModifier> getAttributeModifiers(EntityEquipmentSlot slot, ItemStack stack) {
        Multimap<String, AttributeModifier> map = super.getAttributeModifiers(slot, stack);
        if (slot == armorType) {
            if (slot == EntityEquipmentSlot.LEGS) {
            } else if (slot == EntityEquipmentSlot.FEET) {

            }
        }
        return map;
    }

    @Override
    public KamiArmor setUnlocalizedName(String key) {
        return (KamiArmor) super.setUnlocalizedName(key);
    }

    @Override
    public String getArmorTexture(ItemStack stack, Entity entity, EntityEquipmentSlot slot, String type) {
        return Reference.MOD_ID + ":textures/models/armor/kami_layer_" + (slot == EntityEquipmentSlot.LEGS ? "2" : "1") + ".png";
    }

    @Override
    @SideOnly(Side.CLIENT)
    public ModelBiped getArmorModel(EntityLivingBase entityLiving, ItemStack itemStack, EntityEquipmentSlot
            armorSlot, ModelBiped _default) {
        if (armorType == EntityEquipmentSlot.CHEST) {
            return new ModelWings();
        }
        return null;
    }


    @Override
    public int getVisDiscount(ItemStack stack, EntityPlayer mp) {
        return discounts[armorType.ordinal()];
    }

    @Override
    public void registerModels() {
        keletupack.proxy.registerItemRenderer(this, 0, "inventory");
    }

    @Override
    public boolean showIngamePopups(ItemStack stack, EntityLivingBase owner) {
        return armorType == EntityEquipmentSlot.HEAD;
    }

    private void doProjectileEffect(EntityPlayer mp) {
        if (!mp.isSneaking()) {
            List<Entity> projectiles = mp.world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(mp.posX - 2, mp.posY - 2, mp.posZ - 2, mp.posX + 2, mp.posY + 2, mp.posZ + 2), e -> e instanceof IProjectile);
            for (Entity potion : projectiles) {
                Vector3 motionVec = new Vector3(potion.motionX, potion.motionY, potion.motionZ).normalize().multiply(Math.sqrt((potion.posX - mp.posX) * (potion.posX - mp.posX) + (potion.posY - mp.posY) * (potion.posY - mp.posY) + (potion.posZ - mp.posZ) * (potion.posZ - mp.posZ)) * 2);

                for (int i = 0; i < 6; i++)
                    keletupack.proxy.sparkle((float) potion.posX, (float) potion.posY, (float) potion.posZ, 6);

                potion.posX += motionVec.x;
                potion.posY += motionVec.y;
                potion.posZ += motionVec.z;
            }
        }
    }

    public void performEffect(EntityLivingBase entity) {
        if (!entity.getEntityWorld().isRemote && entity.onGround && entity.getEntityWorld().isAirBlock(entity.getPosition()) && entity.getEntityWorld().getBlockState(entity.getPosition().down()).isNormalCube()) {
            entity.getEntityWorld().setBlockState(entity.getPosition(), Blocks.DIRT.getDefaultState(), 0);
        }
    }

    public void setBlock(BlockPos pos, World world, EntityPlayer player) {
        if ((world.isAirBlock(pos) || world.getBlockState(pos).equals(ModBlocks.NITOR_VAPOR)) && !world.isRemote && player.getItemStackFromSlot(EntityEquipmentSlot.LEGS).getItem() instanceof KamiArmor) {
            world.setBlockState(pos, ModBlocks.NITOR_VAPOR.getDefaultState());
        }
    }

    @Override
    public void onUpdate(ItemStack par1ItemStack, World world, Entity entity, int par4, boolean par5) {
        if (entity instanceof EntityPlayer) {
            BlockPos pos = new BlockPos(entity.posX, entity.posY, entity.posZ).up();
            setBlock(pos, world, (EntityPlayer) entity);
        }
    }
}
