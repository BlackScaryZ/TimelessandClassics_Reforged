package com.tac.guns.client.render.gun.model;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.tac.guns.Config;
import com.tac.guns.client.SpecialModels;
import com.tac.guns.client.handler.ShootingHandler;
import com.tac.guns.client.render.animation.HkMp5a5AnimationController;
import com.tac.guns.client.render.animation.module.GunAnimationController;
import com.tac.guns.client.render.animation.module.PlayerHandAnimation;
import com.tac.guns.client.render.gun.IOverrideModel;
import com.tac.guns.client.render.gun.ModelOverrides;
import com.tac.guns.client.util.RenderUtil;
import com.tac.guns.common.Gun;
import com.tac.guns.init.ModEnchantments;
import com.tac.guns.init.ModItems;
import com.tac.guns.item.GunItem;
import com.tac.guns.item.attachment.IAttachment;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.vector.Vector3f;
import com.tac.guns.util.GunModifierHelper;

/*
 * Because the revolver has a rotating chamber, we need to render it in a
 * different way than normal items. In this case we are overriding the model.
 */

/**
 * Author: Timeless Development, and associates.
 */
public class hk_mp5a5_animation implements IOverrideModel {

    @Override
    public void render(float v, ItemCameraTransforms.TransformType transformType, ItemStack stack, ItemStack parent, LivingEntity entity, MatrixStack matrices, IRenderTypeBuffer renderBuffer, int light, int overlay)
    {
        
        HkMp5a5AnimationController controller = HkMp5a5AnimationController.getInstance();

        matrices.push();
        {
            controller.applySpecialModelTransform(SpecialModels.HK_MP5A5_BODY.getModel(), HkMp5a5AnimationController.INDEX_BODY, transformType, matrices);
            if (Gun.getScope(stack) != null) {
                RenderUtil.renderModel(SpecialModels.HK_MP5A5_OPTICS_RAIL.getModel(), stack, matrices, renderBuffer, light, overlay);
            }

            if (Gun.getAttachment(IAttachment.Type.STOCK, stack).getItem() == ModItems.TACTICAL_STOCK.orElse(ItemStack.EMPTY.getItem())) {
                RenderUtil.renderModel(SpecialModels.HK_MP5A5_TACTICAL_STOCK.getModel(), stack, matrices, renderBuffer, light, overlay);
            } else if (Gun.getAttachment(IAttachment.Type.STOCK, stack).getItem() == ModItems.LIGHT_STOCK.orElse(ItemStack.EMPTY.getItem())) {
                RenderUtil.renderModel(SpecialModels.HK_MP5A5_LIGHT_STOCK.getModel(), stack, matrices, renderBuffer, light, overlay);
            } else if (Gun.getAttachment(IAttachment.Type.STOCK, stack).getItem() == ModItems.WEIGHTED_STOCK.orElse(ItemStack.EMPTY.getItem())) {
                RenderUtil.renderModel(SpecialModels.HK_MP5A5_HEAVY_STOCK.getModel(), stack, matrices, renderBuffer, light, overlay);
            } else {
                RenderUtil.renderModel(SpecialModels.HK_MP5A5_DEFAULT_STOCK.getModel(), stack, matrices, renderBuffer, light, overlay);
            }

            if (Gun.getAttachment(IAttachment.Type.UNDER_BARREL, stack) == ItemStack.EMPTY && Gun.getAttachment(IAttachment.Type.SIDE_RAIL, stack) == ItemStack.EMPTY) {
                RenderUtil.renderModel(SpecialModels.HK_MP5A5_STANDARD_HANDGUARD.getModel(), stack, matrices, renderBuffer, light, overlay);
            } else {
                RenderUtil.renderModel(SpecialModels.HK_MP5A5_EXTENDED_HANDGUARD.getModel(), stack, matrices, renderBuffer, light, overlay);
            }

            if (Gun.getAttachment(IAttachment.Type.UNDER_BARREL, stack).getItem() == ModItems.LIGHT_GRIP.orElse(ItemStack.EMPTY.getItem())) {
                RenderUtil.renderModel(SpecialModels.HK_MP5A5_LIGHT_GRIP.getModel(), stack, matrices, renderBuffer, light, overlay);
            } else if (Gun.getAttachment(IAttachment.Type.UNDER_BARREL, stack).getItem() == ModItems.SPECIALISED_GRIP.orElse(ItemStack.EMPTY.getItem())) {
                RenderUtil.renderModel(SpecialModels.HK_MP5A5_TACTICAL_GRIP.getModel(), stack, matrices, renderBuffer, light, overlay);
            }

            if (Gun.getAttachment(IAttachment.Type.SIDE_RAIL, stack).getItem() == ModItems.BASIC_LASER.orElse(ItemStack.EMPTY.getItem())) {
                RenderUtil.renderLaserModuleModel(SpecialModels.HK_MP5A5_B_LASER_DEVICE.getModel(), Gun.getAttachment(IAttachment.Type.SIDE_RAIL, stack), matrices, renderBuffer, light, overlay);
                RenderUtil.renderLaserModuleModel(SpecialModels.HK_MP5A5_B_LASER.getModel(), Gun.getAttachment(IAttachment.Type.SIDE_RAIL, stack), matrices, renderBuffer, 15728880, overlay); // 15728880 For fixed max light
            }

            if (Gun.getAttachment(IAttachment.Type.BARREL, stack).getItem() == ModItems.SILENCER.orElse(ItemStack.EMPTY.getItem())) {
                RenderUtil.renderModel(SpecialModels.HK_MP5A5_SUPPRESSOR.getModel(), stack, matrices, renderBuffer, light, overlay);
            } else if (Gun.getAttachment(IAttachment.Type.BARREL, stack).getItem() == ModItems.MUZZLE_COMPENSATOR.orElse(ItemStack.EMPTY.getItem())) {
                RenderUtil.renderModel(SpecialModels.HK_MP5A5_COMPENSATOR.getModel(), stack, matrices, renderBuffer, light, overlay);
            } else if (Gun.getAttachment(IAttachment.Type.BARREL, stack).getItem() == ModItems.MUZZLE_BRAKE.orElse(ItemStack.EMPTY.getItem())) {
                RenderUtil.renderModel(SpecialModels.HK_MP5A5_BRAKE.getModel(), stack, matrices, renderBuffer, light, overlay);
            }

            matrices.push();
            if(transformType.isFirstPerson()) {
                Gun gun = ((GunItem) stack.getItem()).getGun();
                float cooldownOg = ShootingHandler.get().getshootMsGap() / ShootingHandler.calcShootTickGap(gun.getGeneral().getRate()) < 0 ? 1 : ShootingHandler.get().getshootMsGap() / ShootingHandler.calcShootTickGap(gun.getGeneral().getRate());

                matrices.translate(0, 0, 0.085f * (-4.5 * Math.pow(cooldownOg - 0.5, 2) + 1.0));
            }
            RenderUtil.renderModel(SpecialModels.HK_MP5A5_BOLT.getModel(), stack, matrices, renderBuffer, light, overlay);
            matrices.pop();

            RenderUtil.renderModel(SpecialModels.HK_MP5A5_BODY.getModel(), stack, matrices, renderBuffer, light, overlay);
        }
        matrices.pop();

        matrices.push();
        {
            controller.applySpecialModelTransform(SpecialModels.HK_MP5A5_BODY.getModel(), HkMp5a5AnimationController.INDEX_MAGAZINE, transformType, matrices);
            if (GunModifierHelper.getAmmoCapacity(stack) > -1) {
                RenderUtil.renderModel(SpecialModels.HK_MP5A5_EXTENDED_MAG.getModel(), stack, matrices, renderBuffer, light, overlay);
            } else {
                RenderUtil.renderModel(SpecialModels.HK_MP5A5_STANDARD_MAG.getModel(), stack, matrices, renderBuffer, light, overlay);
            }
        }
        matrices.pop();

        matrices.push();
        {
            controller.applySpecialModelTransform(SpecialModels.HK_MP5A5_BODY.getModel(), HkMp5a5AnimationController.INDEX_BOLT,transformType,matrices);
            RenderUtil.renderModel(SpecialModels.HK_MP5A5_BOLT_HANDLE.getModel(), stack, matrices, renderBuffer, light, overlay);
        }
        matrices.pop();

        PlayerHandAnimation.render(controller,transformType,matrices,renderBuffer,light);
    }
}
