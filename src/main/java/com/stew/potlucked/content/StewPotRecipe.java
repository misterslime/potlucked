package com.stew.potlucked.content;

import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.stew.potlucked.Potlucked;
import com.stew.potlucked.registries.RecipeRegistry;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public record StewPotRecipe(NonNullList<Ingredient> ingredients, ItemStack output) implements Recipe<StewPotRecipeInput> {
    // we need to:
    // read in a json file

    @Override
    public @NotNull NonNullList<Ingredient> getIngredients() {
        return this.ingredients;
    }

    @Override
    public boolean matches(@NotNull StewPotRecipeInput input, Level level) {

        Potlucked.LOGGER.info("Checking if client side");

        if (level.isClientSide()) {
            return false;
        }


        Potlucked.LOGGER.info("Is not client side");

        if (input.input().size() != this.ingredients.size()) {
            return false;
        }

        Potlucked.LOGGER.info("Is same size");
        return input.size() == 1 && this.ingredients.size() == 1
                ? this.ingredients.getFirst().test(input.getItem(0))
                : input.stackedContents().canCraft(this, null);
    }

    @Override
    public @NotNull ItemStack assemble(@NotNull StewPotRecipeInput craftingInput, HolderLookup.@NotNull Provider provider) {
        return output.copy();
    }

    @Override
    public boolean canCraftInDimensions(int i, int i1) {
        return true;
    }

    @Override
    public @NotNull ItemStack getResultItem(HolderLookup.@NotNull Provider provider) {
        return output;
    }

    @Override
    public @NotNull RecipeSerializer<?> getSerializer() {
        return RecipeRegistry.STEW_POT_SERIALIZER.get();
    }

    @Override
    public @NotNull RecipeType<?> getType() {
        return RecipeRegistry.STEW_POT_TYPE.get();
    }

    public static class Serializer implements RecipeSerializer<StewPotRecipe> {

        // readed from json file
        public static final MapCodec<StewPotRecipe> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
                Ingredient.CODEC_NONEMPTY.listOf().fieldOf("ingredients")
                        .flatXmap(
                                p_301021_ -> {
                                    Ingredient[] aingredient = p_301021_.toArray(Ingredient[]::new); // Neo skip the empty check and immediately create the array.
                                    if (aingredient.length == 0) {
                                        return DataResult.error(() -> "No ingredients for shapeless recipe");
                                    } else {
                                        return aingredient.length > 3 * 3
                                                ? DataResult.error(() -> "Too many ingredients for shapeless recipe. The maximum is: %s".formatted(3 * 3))
                                                : DataResult.success(NonNullList.of(Ingredient.EMPTY, aingredient));
                                    }
                                },
                                DataResult::success
                        ).forGetter(StewPotRecipe::ingredients),
                ItemStack.CODEC.fieldOf("result").forGetter(StewPotRecipe::output)
        ).apply(inst, StewPotRecipe::new));

        // communicated between server and client
        public static final StreamCodec<RegistryFriendlyByteBuf, StewPotRecipe> STREAM_CODEC = StreamCodec.of(
                StewPotRecipe.Serializer::toNetwork, StewPotRecipe.Serializer::fromNetwork
        );


        @Override
        public @NotNull MapCodec<StewPotRecipe> codec() {
            return CODEC;
        }

        @Override
        public @NotNull StreamCodec<RegistryFriendlyByteBuf, StewPotRecipe> streamCodec() {
            return STREAM_CODEC;
        }

        private static StewPotRecipe fromNetwork(RegistryFriendlyByteBuf buffer) {
            int count = buffer.readVarInt();
            NonNullList<Ingredient> ingredients = NonNullList.withSize(count, Ingredient.EMPTY);
            ingredients.replaceAll(ingredient -> Ingredient.CONTENTS_STREAM_CODEC.decode(buffer));
            ItemStack output = ItemStack.STREAM_CODEC.decode(buffer);
            return new StewPotRecipe(ingredients, output);
        }

        private static void toNetwork(RegistryFriendlyByteBuf buffer, StewPotRecipe recipe) {
            buffer.writeVarInt(recipe.ingredients.size());

            for (Ingredient ingredient : recipe.ingredients) {
                Ingredient.CONTENTS_STREAM_CODEC.encode(buffer, ingredient);
            }

            ItemStack.STREAM_CODEC.encode(buffer, recipe.output);
        }
    }
}
