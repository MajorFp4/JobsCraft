package com.majorfp4.jobscraft.client.jei;

import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.runtime.IIngredientManager;
import net.minecraft.world.item.ItemStack;
import java.util.ArrayList;
import java.util.List;

public class JobsCraftIngredientHider {

    public static void onIngredientsUpdated(IIngredientManager ingredientManager) {

        IIngredientType<ItemStack> itemType = ingredientManager.getIngredientType(ItemStack.class);
        IIngredientHelper<ItemStack> itemHelper = ingredientManager.getIngredientHelper(itemType);

        List<ItemStack> itemsToHide = new ArrayList<>();
        List<ItemStack> itemsToShow = new ArrayList<>();

        for (ItemStack stack : JobsCraftJEIPlugin.getMasterItemStackList()) {
            try {
                if (JobsCraftJEIPlugin.isItemVisible(stack)) {
                    itemsToShow.add(stack);
                } else {
                    itemsToHide.add(stack);
                }
            } catch (Exception e) {
                System.err.println("Erro ao checar visibilidade do item: " + stack.getDisplayName().getString());
                e.printStackTrace();
            }
        }

        if (!itemsToHide.isEmpty()) {
            ingredientManager.removeIngredientsAtRuntime(itemType, itemsToHide);
        }
        if (!itemsToShow.isEmpty()) {
            ingredientManager.addIngredientsAtRuntime(itemType, itemsToShow);
        }
    }
}