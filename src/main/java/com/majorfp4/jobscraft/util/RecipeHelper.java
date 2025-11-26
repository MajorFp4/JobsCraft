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
import java.util.Set;

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

    public static List<Recipe<?>> getAllowedRecipes(ServerPlayer player, Profession profession,
            PlayerProgress progress) {
        List<Recipe<?>> allRecipes = new ArrayList<>(player.server.getRecipeManager().getRecipes());
        List<Recipe<?>> allowedRecipes = new ArrayList<>();

        String techModId = profession.getTechnicalMod();

        for (Recipe<?> recipe : allRecipes) {
            ItemStack resultItem = recipe.getResultItem();
            if (resultItem.isEmpty())
                continue;

            ResourceLocation itemRL = ForgeRegistries.ITEMS.getKey(resultItem.getItem());
            if (itemRL == null)
                continue;

            String itemModId = itemRL.getNamespace();

            if (!itemModId.equals(techModId)) {
                allowedRecipes.add(recipe);
                continue;
            }

            if (techModId.equals("none")) {
                continue;
            }

            // Use canSee to determine visibility in JEI/Recipe Book
            if (canSee(profession, progress, recipe)) {
                allowedRecipes.add(recipe);
            }
        }
        return allowedRecipes;
    }

    // Returns true if the player has researched ALL required ingredients (or they
    // are base items)
    public static boolean canCraft(Profession prof, PlayerProgress progress, Recipe<?> recipe) {
        String techModId = prof.getTechnicalMod();
        List<String> baseItems = prof.getBaseItems();

        for (Ingredient ingredient : recipe.getIngredients()) {
            if (ingredient.isEmpty())
                continue;

            ItemStack repStack = ingredient.getItems().length > 0 ? ingredient.getItems()[0] : ItemStack.EMPTY;
            if (repStack.isEmpty())
                continue;

            ResourceLocation ingRL = repStack.getItem().getRegistryName();
            if (ingRL == null)
                continue;

            if (!ingRL.getNamespace().equals(techModId))
                continue;

            boolean isBase = isItemInList(repStack, baseItems);
            boolean isCrafted = progress.hasCrafted(ingRL);

            // If any restricted ingredient is NOT known, they cannot craft it
            if (!isBase && !isCrafted) {
                return false;
            }
        }

        return true;
    }

    // Returns true if the player has researched AT LEAST ONE required ingredient
    // (or it is a base item)
    public static boolean canSee(Profession prof, PlayerProgress progress, Recipe<?> recipe) {
        return canSee(prof, progress.getCraftedItems(), recipe);
    }

    public static boolean canSee(Profession prof, Set<ResourceLocation> craftedItems, Recipe<?> recipe) {
        String techModId = prof.getTechnicalMod();
        List<String> baseItems = prof.getBaseItems();
        boolean hasRestrictedIngredients = false;
        boolean knowsAtLeastOne = false;

        ItemStack resultItem = recipe.getResultItem();
        boolean isTarget = !resultItem.isEmpty()
                && resultItem.getItem().getRegistryName().getPath().contains("advanced_control_circuit");

        if (isTarget) {
            System.out.println("DEBUG: Checking visibility for Advanced Control Circuit");
            System.out.println("  - TechMod: " + techModId);
            System.out.println("  - Crafted Items Size: " + craftedItems.size());
        }

        for (Ingredient ingredient : recipe.getIngredients()) {
            if (ingredient.isEmpty())
                continue;

            ItemStack repStack = ingredient.getItems().length > 0 ? ingredient.getItems()[0] : ItemStack.EMPTY;
            if (repStack.isEmpty())
                continue;

            ResourceLocation ingRL = repStack.getItem().getRegistryName();
            if (ingRL == null)
                continue;

            // Only check ingredients from the technical mod
            if (!ingRL.getNamespace().equals(techModId))
                continue;

            hasRestrictedIngredients = true;
            boolean isBase = isItemInList(repStack, baseItems);
            boolean isCrafted = craftedItems.contains(ingRL);

            if (isTarget) {
                System.out.println("  - Ingredient: " + ingRL + " (Base: " + isBase + ", Crafted: " + isCrafted + ")");
            }

            if (isBase || isCrafted) {
                knowsAtLeastOne = true;
            }
        }

        if (!hasRestrictedIngredients) {
            if (isTarget)
                System.out.println("  - No restricted ingredients found.");
            // If there are no restricted ingredients, we must check if the output item
            // itself is restricted.
            // If it is, we only show the recipe if the player has unlocked the item.
            if (!resultItem.isEmpty()) {
                ResourceLocation resultRL = resultItem.getItem().getRegistryName();
                if (resultRL != null && resultRL.getNamespace().equals(techModId)) {
                    boolean isBase = isItemInList(resultItem, baseItems);
                    boolean isCrafted = craftedItems.contains(resultRL);
                    if (isTarget)
                        System.out.println("  - Output restricted. Visible: " + (isBase || isCrafted));
                    return isBase || isCrafted;
                }
            }
            return true;
        }

        if (isTarget) {
            System.out.println("  - Has Restricted Ingredients: " + hasRestrictedIngredients);
            System.out.println("  - Knows At Least One: " + knowsAtLeastOne);
        }

        return knowsAtLeastOne;
    }
}