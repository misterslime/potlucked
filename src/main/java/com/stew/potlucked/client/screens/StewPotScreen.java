package com.stew.potlucked.client.screens;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.stew.potlucked.Potlucked;
import com.stew.potlucked.client.registries.ShaderRegistry;
import com.stew.potlucked.content.StewPotBlock;
import com.stew.potlucked.gui.StewPotMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.event.ContainerScreenEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;

public class StewPotScreen extends AbstractContainerScreen<StewPotMenu> {
    private static final ResourceLocation GUI_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(Potlucked.MOD_ID, "textures/gui/containers/stew_pot.png");

    private static final ResourceLocation WATER_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(Potlucked.MOD_ID, "textures/gui/containers/stew_pot_water.png");

    private float time;

    public StewPotScreen(StewPotMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);

        this.time = 0.0F;
    }

    @Override
    protected void renderBg(@NotNull GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        this.time += partialTick / 20.0F;

        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, GUI_TEXTURE);

        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        guiGraphics.blit(GUI_TEXTURE, x, y, 0, 0, imageWidth, imageHeight);

        if (!menu.hasWater())
            return;

        int fluidHeight = menu.getScaledWaterProgress();

        BlockState state = menu.blockEntity.getBlockState();

        Vec3i color = new Vec3i(101, 84, 255);

        if (state.getValue(StewPotBlock.HEATED))
            color = new Vec3i(255, 255, 255);

        drawStewPotWater(guiGraphics, x + 62, y + 17, 0.67F);
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);


        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(0.0F, 0.0F, 400.0F);
        drawStewPotWater(guiGraphics, x + 62, y + 17, 0.25F);
        guiGraphics.pose().popPose();
    }

    void drawStewPotWater(@NotNull GuiGraphics guiGraphics, int x, int y, float progress) {
        int x2 = x + 52;
        int y2 = y + 52;
        float minU = (0.0F) / (float) 52;
        float maxU = 1.0F;
        float minV = (0.0F) / (float) 52;
        float maxV = 1.0F;

        RenderSystem.setShaderTexture(0, WATER_TEXTURE);
        RenderSystem.setShader(ShaderRegistry::getTankShader);
        RenderSystem.getShader().getUniform("FillProgress").set(progress);
        RenderSystem.getShader().getUniform("uTime").set(time);

        Matrix4f matrix4f = guiGraphics.pose().last().pose();
        BufferBuilder bufferbuilder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
        bufferbuilder.addVertex(matrix4f, (float)x, (float)y, (float) 0).setUv(minU, minV).setColor(255, 255, 255, 255);
        bufferbuilder.addVertex(matrix4f, (float)x, (float)y2, (float) 0).setUv(minU, maxV).setColor(255, 255, 255, 255);
        bufferbuilder.addVertex(matrix4f, (float)x2, (float)y2, (float) 0).setUv(maxU, maxV).setColor(255, 255, 255, 255);
        bufferbuilder.addVertex(matrix4f, (float)x2, (float)y, (float) 0).setUv(maxU, minV).setColor(255, 255, 255, 255);
        BufferUploader.drawWithShader(bufferbuilder.buildOrThrow());
    }

    public static void drawStewPotWaterOnScreen(@NotNull StewPotScreen screen, @NotNull GuiGraphics guiGraphics, float progress) {
        int x = (screen.width - screen.imageWidth) / 2;
        int y = (screen.height - screen.imageHeight) / 2;
        screen.drawStewPotWater(guiGraphics, x, y, progress);
    }
}
