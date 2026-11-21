package com.stew.potlucked.content;

import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public record StewPotRecipeInput(List<ItemStack> input) implements RecipeInput {
    @Override
    public @NotNull ItemStack getItem(int i) {
        return input.get(i);
    }

    @Override
    public int size() {
        return 1;
    }

    public StackedContents stackedContents() {
        StackedContents stackedContents = new StackedContents();

        for(ItemStack itemstack : input) {
            if (!itemstack.isEmpty()) {
                stackedContents.accountStack(itemstack, 1);
            }
        }

        return stackedContents;
    }
}
