package dev.saltupdate;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/** Модель ящерицы: туловище, голова, гребень, хвост из трёх сегментов и четыре лапы. */
public class LizardModel extends EntityModel<LizardEntity> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(SaltUpdate.MOD_ID, "lizard"), "main");

    private final ModelPart root, body, head, tail1, tail2, tail3, legFR, legFL, legBR, legBL;

    public LizardModel(ModelPart root) {
        this.root = root;
        this.body = root.getChild("body");
        this.head = body.getChild("head");
        this.tail1 = body.getChild("tail1");
        this.tail2 = tail1.getChild("tail2");
        this.tail3 = tail2.getChild("tail3");
        this.legFR = root.getChild("leg_fr");
        this.legFL = root.getChild("leg_fl");
        this.legBR = root.getChild("leg_br");
        this.legBL = root.getChild("leg_bl");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition r = mesh.getRoot();

        PartDefinition body = r.addOrReplaceChild("body",
                CubeListBuilder.create().texOffs(0, 0).addBox(-2.5F, -2F, -4.5F, 5, 4, 9), PartPose.offset(0, 19, 0));
        body.addOrReplaceChild("crest",
                CubeListBuilder.create().texOffs(0, 23).addBox(-0.5F, -1F, -4F, 1, 1, 8), PartPose.offset(0, -2, 0));
        body.addOrReplaceChild("head",
                CubeListBuilder.create().texOffs(0, 14).addBox(-2F, -1.5F, -5F, 4, 3, 5), PartPose.offset(0, -0.5F, -4.5F));
        PartDefinition t1 = body.addOrReplaceChild("tail1",
                CubeListBuilder.create().texOffs(30, 0).addBox(-1.5F, -1.5F, 0F, 3, 3, 5), PartPose.offset(0, 0, 4.5F));
        PartDefinition t2 = t1.addOrReplaceChild("tail2",
                CubeListBuilder.create().texOffs(30, 9).addBox(-1F, -1F, 0F, 2, 2, 5), PartPose.offset(0, 0, 5));
        t2.addOrReplaceChild("tail3",
                CubeListBuilder.create().texOffs(46, 0).addBox(-0.5F, -0.5F, 0F, 1, 1, 4), PartPose.offset(0, 0, 5));

        CubeListBuilder leg = CubeListBuilder.create().texOffs(46, 6).addBox(-1F, 0F, -1F, 2, 3, 2);
        r.addOrReplaceChild("leg_fr", leg, PartPose.offset(-3, 21, -3));
        r.addOrReplaceChild("leg_fl", leg, PartPose.offset(3, 21, -3));
        r.addOrReplaceChild("leg_br", leg, PartPose.offset(-3, 21, 3));
        r.addOrReplaceChild("leg_bl", leg, PartPose.offset(3, 21, 3));
        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public void setupAnim(LizardEntity e, float limbSwing, float limbSwingAmount, float age, float netHeadYaw, float headPitch) {
        float swing = limbSwing * 0.9F;
        float amt = Math.min(limbSwingAmount, 1.0F);

        // голова: следит за целью, лёгкое дыхание и покачивание при ходьбе
        head.yRot = netHeadYaw * Mth.DEG_TO_RAD * 0.6F;
        head.xRot = headPitch * Mth.DEG_TO_RAD * 0.6F + Mth.sin(age * 0.07F) * 0.04F + Mth.sin(swing * 2F) * 0.06F * amt;

        // лапы: диагональный шаг
        float a = Mth.cos(swing) * 1.1F * amt;
        legFR.xRot = a;
        legBL.xRot = a;
        legFL.xRot = -a;
        legBR.xRot = -a;

        // тело и хвост: змеиное извивание при ходьбе, плавное покачивание в покое
        body.yRot = Mth.cos(swing) * 0.2F * amt;
        body.y = 19F + Mth.sin(age * 0.1F) * 0.1F;
        tail1.yRot = -body.yRot * 1.2F + Mth.sin(age * 0.08F) * 0.05F;
        tail2.yRot = Mth.sin(swing - 0.8F) * 0.3F * amt + Mth.sin(age * 0.08F - 0.6F) * 0.08F;
        tail3.yRot = Mth.sin(swing - 1.6F) * 0.4F * amt + Mth.sin(age * 0.08F - 1.2F) * 0.12F;
    }

    @Override
    public void renderToBuffer(PoseStack pose, VertexConsumer buffer, int light, int overlay, int color) {
        root.render(pose, buffer, light, overlay, color);
    }
}
