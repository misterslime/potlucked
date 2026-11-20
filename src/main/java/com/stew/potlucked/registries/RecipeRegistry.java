package com.stew.potlucked.registries;

import com.stew.potlucked.Potlucked;
import com.stew.potlucked.content.StewPotRecipe;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class RecipeRegistry {
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS =
            DeferredRegister.create(Registries.RECIPE_SERIALIZER, Potlucked.MOD_ID);

    public static final DeferredRegister<RecipeType<?>> RECIPE_TYPES =
            DeferredRegister.create(Registries.RECIPE_TYPE, Potlucked.MOD_ID);

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<StewPotRecipe>> STEW_POT_SERIALIZER =
            RECIPE_SERIALIZERS.register("stew_pot", StewPotRecipe.Serializer::new);

    public static final DeferredHolder<RecipeType<?>, RecipeType<StewPotRecipe>> STEW_POT_TYPE =
            RECIPE_TYPES.register("stew_pot", () -> new RecipeType<StewPotRecipe>() {
                @Override
                public String toString() {
                    return "stew_pot";
                }
            });

}
