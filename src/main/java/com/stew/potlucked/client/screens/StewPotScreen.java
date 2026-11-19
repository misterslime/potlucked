package com.stew.potlucked.client.screens;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.stew.potlucked.Potlucked;
import com.stew.potlucked.client.registries.ShaderRegistry;
import com.stew.potlucked.content.StewPotBlock;
import com.stew.potlucked.content.StewPotBlockEntity;
import com.stew.potlucked.gui.StewPotMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;

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
    protected void init() {
        super.init();

        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        this.addWidget(Button.builder(Component.translatable("block.potlucked.stew_pot.stir_button"), new Button.OnPress() {
            @Override
            public void onPress(@NotNull Button button) {
                BlockState blockState = menu.blockEntity.getBlockState();

                if (!blockState.getValue(StewPotBlock.HEATED) || !blockState.getValue(StewPotBlock.FILLED) ||
                        menu.blockEntity.toolsHandler.getStackInSlot(StewPotBlockEntity.SHOVEL_STICK_SLOT).isEmpty() ||
                        menu.blockEntity.toolsHandler.getStackInSlot(StewPotBlockEntity.BOWL_SLOT).isEmpty()) {
                    Potlucked.LOGGER.info("Stew pot conditions inadequate for cooking.");
                    return;
                }

                Potlucked.LOGGER.info("Crafting successful!");
            }
        }).bounds(x + 128, y + 52, 35, 18).build());
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

        float fluidHeight = menu.getWaterProgress();

        BlockState state = menu.blockEntity.getBlockState();

        Vector3f color = new Vector3f(0.2F, 0.208F, 0.929F);

        drawStewPotWater(
                guiGraphics, x + 59, y + 14, 0.67F, color, 1.0F,
                -0.05F, -2.0F, 3.14159F, -1.0F, -0.125F, -1.0F);
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);

        if (menu.hasWater())
        {
            int x = (width - imageWidth) / 2;
            int y = (height - imageHeight) / 2;

            Vector3f color = new Vector3f(0.396F, 0.329F, 1.0F);

            guiGraphics.pose().pushPose();
            guiGraphics.pose().translate(-1.0F, 0.0F, 400.0F);

            drawStewPotWater(
                    guiGraphics, x + 59, y + 14, 0.67F, color, 0.521F,
                    0.05F, 2.0F, 0.0F, 1.0F, 0.125F, -1.0F);

            guiGraphics.pose().popPose();
        }

        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    void drawStewPotWater(@NotNull GuiGraphics guiGraphics, int x, int y,
                          float progress, Vector3f fillColor, float alpha, float fillAmplitude, float waveAnimation,
                          float wavePhase, float fillMultiplier, float verticalBandStrength, float fresnelOffset) {
        int x2 = x + 58;
        int y2 = y + 58;
        float minU = (0.0F) / (float) 58;
        float maxU = 1.0F;
        float minV = (0.0F) / (float) 58;
        float maxV = 1.0F;


        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        RenderSystem.setShaderTexture(0, WATER_TEXTURE);
        RenderSystem.setShader(ShaderRegistry::getTankShader);
        RenderSystem.getShader().getUniform("uTime").set(time);
        RenderSystem.getShader().getUniform("FillProgress").set(progress);
        RenderSystem.getShader().getUniform("FillColour").set(fillColor.x, fillColor.y, fillColor.z);
        RenderSystem.getShader().getUniform("FillAnimationAmplitude").set(fillAmplitude);
        RenderSystem.getShader().getUniform("FillAnimationMultiplier").set(fillMultiplier);
        RenderSystem.getShader().getUniform("WavePhase").set(wavePhase);
        RenderSystem.getShader().getUniform("AnimatedVerticalBandStrength").set(verticalBandStrength);
        RenderSystem.getShader().getUniform("FresnelOffset").set(fresnelOffset);
        RenderSystem.getShader().getUniform("WaveAnimation").set(waveAnimation);
        RenderSystem.getShader().getUniform("DrawAlpha").set(alpha);

        Matrix4f matrix4f = guiGraphics.pose().last().pose();
        BufferBuilder bufferbuilder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
        bufferbuilder.addVertex(matrix4f, (float)x, (float)y, (float) 0).setUv(minU, minV).setColor(255, 255, 255, 255* alpha);
        bufferbuilder.addVertex(matrix4f, (float)x, (float)y2, (float) 0).setUv(minU, maxV).setColor(255, 255, 255, 255* alpha);
        bufferbuilder.addVertex(matrix4f, (float)x2, (float)y2, (float) 0).setUv(maxU, maxV).setColor(255, 255, 255, 255* alpha);
        bufferbuilder.addVertex(matrix4f, (float)x2, (float)y, (float) 0).setUv(maxU, minV).setColor(255, 255, 255, 255 * alpha);
        BufferUploader.drawWithShader(bufferbuilder.buildOrThrow());
    }
}
