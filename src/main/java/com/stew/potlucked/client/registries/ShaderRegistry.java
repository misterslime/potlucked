package com.stew.potlucked.client.registries;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.stew.potlucked.Potlucked;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterShadersEvent;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

@EventBusSubscriber(modid = Potlucked.MOD_ID, value = Dist.CLIENT)
public class ShaderRegistry {

    @Nullable
    private static ShaderInstance tankShader;

    private static final ResourceLocation GUI_TANK_SHADER =
            ResourceLocation.fromNamespaceAndPath(Potlucked.MOD_ID, "stew_pot_tank_shader");

    @SubscribeEvent
    static void registerShaders(RegisterShadersEvent event) {
        try {
            event.registerShader(
                    new ShaderInstance(
                            event.getResourceProvider(),
                            GUI_TANK_SHADER,
                            DefaultVertexFormat.POSITION_TEX_COLOR
                    ),
                    (instance) -> tankShader = instance);
        }
        catch (IOException ioexception) {
            throw new RuntimeException("could not reload potlucked shaders", ioexception);
        }
    }

    @Nullable public static ShaderInstance getTankShader() { return tankShader; }
}
