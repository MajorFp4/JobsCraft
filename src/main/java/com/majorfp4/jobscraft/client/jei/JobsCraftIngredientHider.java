package com.majorfp4.jobscraft.client.jei;

import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.runtime.IIngredientManager;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * Esta classe é o "filtro" que diz ao JEI quais ingredientes esconder.
 */
public class JobsCraftIngredientHider {

    /**
     * Este é o nosso novo método "refresh".
     * Ele pega TODOS os itens e decide quais mostrar e quais esconder.
     */
    public static void onIngredientsUpdated(IIngredientManager ingredientManager) {

        IIngredientType<ItemStack> itemType = ingredientManager.getIngredientType(ItemStack.class);
        IIngredientHelper<ItemStack> itemHelper = ingredientManager.getIngredientHelper(itemType);

        List<ItemStack> itemsToHide = new ArrayList<>();
        List<ItemStack> itemsToShow = new ArrayList<>(); // <-- Precisamos disso

        // Pega TODOS os ingredientes (escondidos ou não)
        for (ItemStack stack : ingredientManager.getAllIngredients(itemType)) {
            try {
                // Pergunta ao nosso plugin se o item deve estar visível
                if (JobsCraftJEIPlugin.isItemVisible(stack)) {
                    itemsToShow.add(stack); // Adiciona à lista "mostrar"
                } else {
                    itemsToHide.add(stack); // Adiciona à lista "esconder"
                }
            } catch (Exception e) {
                // Proteção contra bugs
                System.err.println("Erro ao checar visibilidade do item: " + stack.getDisplayName().getString());
                e.printStackTrace();
            }
        }

        ingredientManager.removeIngredientsAtRuntime(itemType, itemsToHide);
        ingredientManager.addIngredientsAtRuntime(itemType, itemsToShow);
    }
}