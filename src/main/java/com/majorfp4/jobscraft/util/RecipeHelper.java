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
import net.minecraft.world.item.crafting.Recipe;
import net.minecraftforge.registries.ForgeRegistries;
import java.util.ArrayList;
import java.util.List;

public class RecipeHelper {

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

        String techModId = profession.getTechnicalMod();
        if (techModId.equals("none")) {
            return allRecipes;
        }

        List<Recipe<?>> allowedRecipes = new ArrayList<>();

        for (Recipe<?> recipe : allRecipes) {
            ItemStack resultItem = recipe.getResultItem();
            if (resultItem.isEmpty()) {
                continue;
            }

            ResourceLocation itemRL = ForgeRegistries.ITEMS.getKey(resultItem.getItem());
            if (itemRL == null) {
                continue;
            }

            String itemModId = itemRL.getNamespace();

            if (!itemModId.equals(techModId)) {
                allowedRecipes.add(recipe);
                continue;
            }

            boolean isBaseItem = isBaseItem(profession, resultItem);
            boolean hasBeenCrafted = progress.hasCrafted(itemRL);

            if (isBaseItem || hasBeenCrafted) {
                allowedRecipes.add(recipe);
            }
        }
        return allowedRecipes;
    }
}