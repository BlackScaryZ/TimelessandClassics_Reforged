package com.tac.guns.item.TransitionalTypes;


import com.tac.guns.Config;
import com.tac.guns.GunMod;
import com.tac.guns.client.InputHandler;
import com.tac.guns.common.Gun;
import com.tac.guns.common.network.ServerPlayHandler;
import com.tac.guns.interfaces.IGunModifier;
import com.tac.guns.item.GunItem;
import com.tac.guns.item.attachment.IAttachment;
import com.tac.guns.util.GunEnchantmentHelper;
import com.tac.guns.util.GunModifierHelper;
import com.tac.guns.util.Process;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.KeybindTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import com.tac.guns.util.GunModifierHelper;
import org.lwjgl.glfw.GLFW;


public class TimelessGunItem extends GunItem {
    private final IGunModifier[] modifiers;
    private Boolean integratedOptic = false;
    public TimelessGunItem(Process<Item.Properties> properties, IGunModifier... modifiers) {
        super(properties.process(new Item.Properties().maxStackSize(1).group(GunMod.GROUP)));
        this.modifiers = modifiers;
    }

    public TimelessGunItem(Process<Item.Properties> properties, Boolean integratedOptic, IGunModifier... modifiers) {
        super(properties.process(new Item.Properties().maxStackSize(1).group(GunMod.GROUP)));
        this.modifiers = modifiers;
        this.integratedOptic = integratedOptic;
    }

    public TimelessGunItem() {
        this(properties -> properties);
    }

    public Boolean isIntegratedOptic() {
        return integratedOptic;
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flag) {
        Gun modifiedGun = this.getModifiedGun(stack);
        Item ammo = ForgeRegistries.ITEMS.getValue(modifiedGun.getProjectile().getItem());
        if (ammo != null) {
            tooltip.add((new TranslationTextComponent("info.tac.ammo_type", new TranslationTextComponent(ammo.getTranslationKey()).mergeStyle(TextFormatting.GOLD)).mergeStyle(TextFormatting.DARK_GRAY)));
        }

        String additionalDamageText = "";
        CompoundNBT tagCompound = stack.getTag();
        float additionalDamage;
        if (tagCompound != null && tagCompound.contains("AdditionalDamage", 99)) {
            additionalDamage = tagCompound.getFloat("AdditionalDamage");
            additionalDamage += GunModifierHelper.getAdditionalDamage(stack);
            if (additionalDamage > 0.0F) {
                additionalDamageText = TextFormatting.GREEN + " +" + ItemStack.DECIMALFORMAT.format(additionalDamage);
            } else if (additionalDamage < 0.0F) {
                additionalDamageText = TextFormatting.RED + " " + ItemStack.DECIMALFORMAT.format(additionalDamage);
            }
        }
        additionalDamage = modifiedGun.getProjectile().getDamage();
        additionalDamage = GunModifierHelper.getModifiedProjectileDamage(stack, additionalDamage);
        additionalDamage = GunEnchantmentHelper.getAcceleratorDamage(stack, additionalDamage);
        tooltip.add((new TranslationTextComponent("info.tac.damage", TextFormatting.GOLD + ItemStack.DECIMALFORMAT.format(additionalDamage) + additionalDamageText)).mergeStyle(TextFormatting.DARK_GRAY));

        if (tagCompound != null) {
            if (tagCompound.getBoolean("IgnoreAmmo")) {
                tooltip.add((new TranslationTextComponent("info.tac.ignore_ammo")).mergeStyle(TextFormatting.AQUA));
            } else {
                int ammoCount = tagCompound.getInt("AmmoCount");
                tooltip.add((new TranslationTextComponent("info.tac.ammo", TextFormatting.GOLD.toString() + ammoCount + "/" + GunModifierHelper.getAmmoCapacity(stack, modifiedGun))).mergeStyle(TextFormatting.DARK_GRAY));
            }
        }

        boolean isShift = InputHandler.MORE_INFO_HOLD.down;
        if(!isShift) {
            //String text = "SHIFT";
            //if(!InputHandler.MORE_INFO_HOLD.keyCode().equals(GLFW.GLFW_KEY_LEFT_SHIFT))
            String text = (new KeybindTextComponent("key.tac.moreInfoHold")).getString().toUpperCase(Locale.ENGLISH);
            tooltip.add((new TranslationTextComponent("info.tac.more_info_gunitem", text)).mergeStyle(TextFormatting.YELLOW));
        }
        if(isShift) {
            GunItem gun = (GunItem) stack.getItem();
            if (tagCompound != null) {
                double armorPen = (1-(1 - Config.COMMON.gameplay.percentDamageIgnoresStandardArmor.get() * gun.getGun().getProjectile().getGunArmorIgnore()))*100;
                tooltip.add((new TranslationTextComponent("info.tac.armorPen", new TranslationTextComponent(String.format("%.1f", armorPen) + "%").mergeStyle(TextFormatting.RED)).mergeStyle(TextFormatting.DARK_AQUA)));

                int headDamgeModifier = (int)(Config.COMMON.gameplay.headShotDamageMultiplier.get() * gun.getGun().getProjectile().getGunHeadDamage())*100;
                tooltip.add((new TranslationTextComponent("info.tac.headDamageModifier", new TranslationTextComponent(String.format("%d", headDamgeModifier) + "%").mergeStyle(TextFormatting.RED)).mergeStyle(TextFormatting.DARK_AQUA)));

                float speed = ServerPlayHandler.calceldGunWeightSpeed(gun.getGun(), stack);
                speed = Math.max(Math.min(speed, 0.1F), 0.075F);
                if (speed > 0.094f)
                    tooltip.add((new TranslationTextComponent("info.tac.lightWeightGun", new TranslationTextComponent(-((int) ((0.1 - speed) * 1000)) + "%").mergeStyle(TextFormatting.RED)).mergeStyle(TextFormatting.DARK_AQUA)));
                else if (speed < 0.095 && speed > 0.0875)
                    tooltip.add((new TranslationTextComponent("info.tac.standardWeightGun", new TranslationTextComponent(-((int) ((0.1 - speed) * 1000)) + "%").mergeStyle(TextFormatting.RED)).mergeStyle(TextFormatting.DARK_GREEN)));
                else
                    tooltip.add((new TranslationTextComponent("info.tac.heavyWeightGun", new TranslationTextComponent(-((int) ((0.1 - speed) * 1000)) + "%").mergeStyle(TextFormatting.RED)).mergeStyle(TextFormatting.DARK_RED)));

                float percentageToNextLevel = (tagCompound.getFloat("levelDmg") * 100) / (modifiedGun.getGeneral().getLevelReq() * (((tagCompound.getInt("level")) * 3.0f)));
                tooltip.add((new TranslationTextComponent("info.tac.current_level").append(new TranslationTextComponent(" " + tagCompound.getInt("level") + " : " + String.format("%.2f", percentageToNextLevel) + "%")))
                        .mergeStyle(TextFormatting.GRAY).mergeStyle(TextFormatting.BOLD));
            }
            tooltip.add((new TranslationTextComponent("info.tac.attachment_help", (new KeybindTextComponent("key.tac.attachments")).getString().toUpperCase(Locale.ENGLISH))).mergeStyle(TextFormatting.YELLOW));
            if (gun.getGun().canAttachType(IAttachment.Type.PISTOL_SCOPE))
                tooltip.add((new TranslationTextComponent("info.tac.pistolScope", new TranslationTextComponent("MiniScope").mergeStyle(TextFormatting.BOLD)).mergeStyle(TextFormatting.LIGHT_PURPLE)));
            if (gun.getGun().canAttachType(IAttachment.Type.IR_DEVICE))
                tooltip.add((new TranslationTextComponent("info.tac.irLaserEquip", new TranslationTextComponent("IrLaser").mergeStyle(TextFormatting.BOLD)).mergeStyle(TextFormatting.AQUA)));
        }
    }

    public IGunModifier[] getModifiers() {
        return this.modifiers;
    }

    @Override
    public int getRGBDurabilityForDisplay(ItemStack stack) {
        return Objects.requireNonNull(TextFormatting.GOLD.getColor());
    }

    @Override
    public boolean showDurabilityBar(ItemStack stack) {
        if (Config.CLIENT.display.weaponAmmoBar.get()) {
            CompoundNBT tagCompound = stack.getOrCreateTag();
            Gun modifiedGun = this.getModifiedGun(stack);
            return !tagCompound.getBoolean("IgnoreAmmo") && tagCompound.getInt("AmmoCount") != GunModifierHelper.getAmmoCapacity(stack, modifiedGun);
        } else
            return false;
    }

    @Override
    public boolean hasEffect(ItemStack gunItem) {
        return false;
    }
}