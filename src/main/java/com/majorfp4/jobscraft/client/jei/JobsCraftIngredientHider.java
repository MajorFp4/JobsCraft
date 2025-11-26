package com.majorfp4.jobscraft.client.jei;

import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.subtypes.UidContext;
import mezz.jei.api.runtime.IIngredientManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.*;

public class JobsCraftIngredientHider {

    // Cache to store ALL ingredients (visible + hidden)
    private static final Map<IIngredientType<?>, Collection<?>> INGREDIENT_CACHE = new HashMap<>();

    public static void onIngredientsUpdated(IIngredientManager ingredientManager) {
        // Iterate over ALL registered ingredient types
        for (IIngredientType<?> type : ingredientManager.getRegisteredIngredientTypes()) {
            hideIngredientsOfType(ingredientManager, type);
        }
    }

    private static <T> void hideIngredientsOfType(IIngredientManager ingredientManager, IIngredientType<T> type) {
        IIngredientHelper<T> helper = ingredientManager.getIngredientHelper(type);

        // Initialize cache if not present
        if (!INGREDIENT_CACHE.containsKey(type)) {
            Collection<T> allIngredients = ingredientManager.getAllIngredients(type);
            // Create a copy to avoid modification issues if JEI returns a mutable view
            INGREDIENT_CACHE.put(type, new ArrayList<>(allIngredients));
            System.out.println("[JobsCraft] Cached " + allIngredients.size() + " ingredients for type: "
                    + type.getIngredientClass().getSimpleName());
        }

        @SuppressWarnings("unchecked")
        Collection<T> allCachedIngredients = (Collection<T>) INGREDIENT_CACHE.get(type);

        // Get currently visible ingredients from JEI to know what to add/remove
        Collection<T> currentlyVisible = ingredientManager.getAllIngredients(type);
        // Optimize lookup
        Set<String> currentlyVisibleUids = new HashSet<>();
        for (T ing : currentlyVisible) {
            currentlyVisibleUids.add(helper.getUniqueId(ing, UidContext.Ingredient));
        }

        List<T> ingredientsToHide = new ArrayList<>();
        List<T> ingredientsToShow = new ArrayList<>();

        for (T ingredient : allCachedIngredients) {
            try {
                boolean shouldBeVisible = false;

                if (ingredient instanceof ItemStack stack) {
                    shouldBeVisible = JobsCraftJEIPlugin.isItemVisible(stack);
                } else {
                    ResourceLocation rl = helper.getResourceLocation(ingredient);
                    shouldBeVisible = JobsCraftJEIPlugin.isResourceVisible(rl);
                }

                String uid = helper.getUniqueId(ingredient, UidContext.Ingredient);
                boolean isVisible = currentlyVisibleUids.contains(uid);

                if (shouldBeVisible && !isVisible) {
                    ingredientsToShow.add(ingredient);
                } else if (!shouldBeVisible && isVisible) {
                    ingredientsToHide.add(ingredient);
                }

            } catch (Exception e) {
                // Ignore errors
            }
        }

        if (!ingredientsToHide.isEmpty()) {
            System.out.println("[JobsCraft] Hiding " + ingredientsToHide.size() + " ingredients of type "
                    + type.getIngredientClass().getSimpleName());
            ingredientManager.removeIngredientsAtRuntime(type, ingredientsToHide);
        }
        if (!ingredientsToShow.isEmpty()) {
            System.out.println("[JobsCraft] Showing " + ingredientsToShow.size() + " ingredients of type "
                    + type.getIngredientClass().getSimpleName());
            ingredientManager.addIngredientsAtRuntime(type, ingredientsToShow);
        }
    }
}