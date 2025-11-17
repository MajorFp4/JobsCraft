package com.majorfp4.jobscraft.util;

import com.majorfp4.jobscraft.capability.PlayerProgress;
import com.majorfp4.jobscraft.config.JobsConfig;
import com.majorfp4.jobscraft.config.Profession;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraftforge.registries.ForgeRegistries;
import java.util.ArrayList;
import java.util.List;

public class RecipeHelper {

    private static boolean isItemInList(ItemStack stack, List<String> list) {
        for (String entry : list) {
            if (isItemInList(stack, entry)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isItemInList(ItemStack stack, String listEntry) {
        if (listEntry.startsWith("#")) {
            String tagName = listEntry.substring(1);
            TagKey<Item> tagKey = ItemTags.create(new ResourceLocation(tagName));
            return stack.is(tagKey);
        } else {
            ResourceLocation itemRL = ForgeRegistries.ITEMS.getKey(stack.getItem());
            return itemRL != null && itemRL.toString().equals(listEntry);
        }
    }

    private static boolean isBaseItem(Profession prof, ItemStack stack) {
        for (String baseItemEntry : prof.getBaseItems()) {
            if (isItemInList(stack, baseItemEntry)) {
                return true;
            }
        }
        return false;
    }

    public static List<Recipe<?>> getAllowedRecipes(ServerPlayer player, Profession profession, PlayerProgress progress) {
        List<Recipe<?>> allRecipes = new ArrayList<>(player.server.getRecipeManager().getRecipes());
        List<Recipe<?>> allowedRecipes = new ArrayList<>();

        String techModId = profession.getTechnicalMod();

        for (Recipe<?> recipe : allRecipes) {
            ItemStack resultItem = recipe.getResultItem();
            if (resultItem.isEmpty()) continue;

            ResourceLocation itemRL = ForgeRegistries.ITEMS.getKey(resultItem.getItem());
            if (itemRL == null) continue;

            String itemModId = itemRL.getNamespace();

            if (!itemModId.equals(techModId)) {
                allowedRecipes.add(recipe);
                continue;
            }

            if (techModId.equals("none")) {
                continue;
            }

            if (isRecipeAllowedByIngredients(profession, progress, recipe)) {
                allowedRecipes.add(recipe);
            }
        }
        return allowedRecipes;
    }
    private static boolean isRecipeAllowedByIngredients(Profession prof, PlayerProgress progress, Recipe<?> recipe) {
        String techModId = prof.getTechnicalMod();
        List<String> baseItems = prof.getBaseItems();

        for (Ingredient ingredient : recipe.getIngredients()) {
            if (ingredient.isEmpty()) continue;

            ItemStack repStack = ingredient.getItems().length > 0 ? ingredient.getItems()[0] : ItemStack.EMPTY;
            if (repStack.isEmpty()) continue;

            ResourceLocation ingRL = repStack.getItem().getRegistryName();
            if (ingRL == null) continue;

            if (!ingRL.getNamespace().equals(techModId)) continue;

            boolean isBase = isItemInList(repStack, baseItems);
            boolean isCrafted = progress.hasCrafted(ingRL);
            if (!isBase && !isCrafted) {
                return false;
            }
        }

        return true;
    }
}