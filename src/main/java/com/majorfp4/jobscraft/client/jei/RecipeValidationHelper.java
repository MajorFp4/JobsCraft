package com.majorfp4.jobscraft.client.jei;

import com.majorfp4.jobscraft.client.ClientCache;
import com.majorfp4.jobscraft.config.JobsConfig;
import com.majorfp4.jobscraft.config.Profession;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraftforge.registries.ForgeRegistries;
import java.util.List;
import java.util.Set;

public class RecipeValidationHelper {

    public static boolean isRecipeUnlocked(Recipe<?> recipe) {
        Profession prof = JobsConfig.getProfession(ClientCache.CURRENT_PROFESSION_ID);
        ResourceLocation outputRL = recipe.getResultItem().getItem().getRegistryName();

        if (outputRL == null) return true;

        String outputModId = outputRL.getNamespace();

        String techModId = JobsConfig.getAllProfessions().stream()
                .map(Profession::getTechnicalMod)
                .filter(id -> id.equals(outputModId))
                .findFirst()
                .orElse(null);

        if (techModId == null) return true;

        if (prof == null || !prof.getTechnicalMod().equals(techModId)) {
            return false;
        }

        Set<ResourceLocation> craftedItems = ClientCache.CRAFTED_ITEMS;
        List<String> baseItems = prof.getBaseItems();

        for (Ingredient ingredient : recipe.getIngredients()) {
            if (ingredient.isEmpty()) continue;

            ItemStack repStack = ingredient.getItems().length > 0 ? ingredient.getItems()[0] : ItemStack.EMPTY;
            if (repStack.isEmpty()) continue;

            ResourceLocation ingRL = repStack.getItem().getRegistryName();
            if (ingRL == null) continue;

            if (!ingRL.getNamespace().equals(techModId)) continue;

            boolean isBase = isItemInList(repStack, baseItems);
            boolean isCrafted = craftedItems.contains(ingRL);

            if (!isBase && !isCrafted) {
                return false;
            }
        }

        return true;
    }

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
}