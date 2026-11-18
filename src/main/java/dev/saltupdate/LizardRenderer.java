package dev.saltupdate;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class LizardRenderer extends MobRenderer<LizardEntity, LizardModel> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(SaltUpdate.MOD_ID, "textures/entity/lizard.png");

    public LizardRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, new LizardModel(ctx.bakeLayer(LizardModel.LAYER)), 0.3F);
    }

    @Override
    public ResourceLocation getTextureLocation(LizardEntity entity) {
        return TEXTURE;
    }

    @Override
    protected void scale(LizardEntity entity, PoseStack pose, float partialTick) {
        float s = entity.isBaby() ? 0.5F : 1.0F;
        pose.scale(s, s, s);
    }
}
