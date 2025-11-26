package com.majorfp4.jobscraft.client.jei;

import com.majorfp4.jobscraft.JobsCraft;
import com.majorfp4.jobscraft.client.ClientCache;
import com.majorfp4.jobscraft.config.JobsConfig;
import com.majorfp4.jobscraft.config.Profession;
import com.majorfp4.jobscraft.util.RecipeHelper;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.recipe.IFocus;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

@JeiPlugin
public class JobsCraftJEIPlugin implements IModPlugin {

    private static final ResourceLocation PLUGIN_UID = new ResourceLocation(JobsCraft.MOD_ID, "main");
    private static IJeiRuntime jeiRuntime;

    // Cache for items that should be visible because their recipe is visible
    private static final Set<ResourceLocation> VISIBLE_ITEMS_CACHE = new HashSet<>();

    @Override
    public ResourceLocation getPluginUid() {
        return PLUGIN_UID;
    }

    @Override
    public void onRuntimeAvailable(IJeiRuntime runtime) {
        JobsCraftJEIPlugin.jeiRuntime = runtime;
        IIngredientManager ingredientManager = runtime.getIngredientManager();

        JobsCraftIngredientHider.onIngredientsUpdated(ingredientManager);
        updateRecipeVisibility();
    }

    public static void refreshJEIFilter() {
        if (jeiRuntime != null) {
            rebuildVisibleItemsCache();
            JobsCraftIngredientHider.onIngredientsUpdated(jeiRuntime.getIngredientManager());
            updateRecipeVisibility();

            // Force UI Refresh by toggling the filter text
            // We need to change it to something else and back to force JEI to process the
            // update
            try {
                String currentFilter = jeiRuntime.getIngredientFilter().getFilterText();
                // Append a space to force a change
                jeiRuntime.getIngredientFilter().setFilterText(currentFilter + " ");
                // Immediately restore the original filter
                jeiRuntime.getIngredientFilter().setFilterText(currentFilter);
            } catch (Exception e) {
                System.err.println("[JobsCraft] Failed to refresh JEI filter: " + e.getMessage());
            }
        }
    }

    private static void rebuildVisibleItemsCache() {
        VISIBLE_ITEMS_CACHE.clear();
        if (Minecraft.getInstance().level == null)
            return;

        Profession profession = JobsConfig.getProfession(ClientCache.CURRENT_PROFESSION_ID);
        if (profession == null) {
            System.out.println("[JobsCraft] DEBUG: rebuildVisibleItemsCache - Profession is null for ID: "
                    + ClientCache.CURRENT_PROFESSION_ID);
            return;
        }

        String techModId = profession.getTechnicalMod();
        System.out.println("[JobsCraft] DEBUG: rebuildVisibleItemsCache - Profession: " + profession.getName()
                + ", TechMod: " + techModId);
        System.out.println("[JobsCraft] DEBUG: Crafted Items Count: " + ClientCache.CRAFTED_ITEMS.size());
        if (ClientCache.CRAFTED_ITEMS.stream().anyMatch(rl -> rl.getPath().contains("alloy_infused"))) {
            System.out.println("[JobsCraft] DEBUG: Crafted Items contains alloy_infused");
        } else {
            System.out.println("[JobsCraft] DEBUG: Crafted Items DOES NOT contain alloy_infused");
        }

        if (techModId.equals("none"))
            return;

        RecipeManager recipeManager = Minecraft.getInstance().level.getRecipeManager();
        int totalRecipes = 0;
        int techModRecipes = 0;
        boolean foundTarget = false;

        for (Recipe<?> recipe : recipeManager.getRecipes()) {
            totalRecipes++;
            ItemStack result = recipe.getResultItem();
            if (result.isEmpty())
                continue;

            ResourceLocation resultId = ForgeRegistries.ITEMS.getKey(result.getItem());
            if (resultId == null)
                continue;

            if (resultId.getPath().contains("advanced_control_circuit")) {
                foundTarget = true;
                System.out.println("[JobsCraft] DEBUG: Found Advanced Control Circuit recipe. Namespace: "
                        + resultId.getNamespace());
            }

            // Only care about items from the technical mod
            if (!resultId.getNamespace().equals(techModId))
                continue;

            techModRecipes++;

            if (RecipeHelper.canSee(profession, ClientCache.CRAFTED_ITEMS, recipe)) {
                VISIBLE_ITEMS_CACHE.add(resultId);
            }
        }
        System.out.println("[JobsCraft] Rebuilt Visible Items Cache. Size: " + VISIBLE_ITEMS_CACHE.size());
        System.out.println("[JobsCraft] DEBUG: Total Recipes: " + totalRecipes + ", TechMod Recipes: " + techModRecipes
                + ", Found Target: " + foundTarget);
    }

    private static void updateRecipeVisibility() {
        if (jeiRuntime == null)
            return;

        RecipeType<CraftingRecipe> craftingType = RecipeTypes.CRAFTING;

        @SuppressWarnings("unchecked")
        IRecipeCategory<CraftingRecipe> craftingCategory = (IRecipeCategory<CraftingRecipe>) jeiRuntime
                .getRecipeManager().getRecipeCategory(craftingType.getUid(), false);

        if (craftingCategory == null) {
            System.err.println("[JobsCraft] Falha ao encontrar a categoria de Crafting do JEI!");
            return;
        }

        List<IFocus<?>> focuses = Collections.emptyList();
        @SuppressWarnings("deprecation")
        List<CraftingRecipe> allRecipes = jeiRuntime.getRecipeManager().getRecipes(
                craftingCategory,
                focuses,
                true);

        List<CraftingRecipe> recipesToHide = new ArrayList<>();
        List<CraftingRecipe> recipesToShow = new ArrayList<>();

        for (CraftingRecipe recipe : allRecipes) {
            if (RecipeValidationHelper.isRecipeUnlocked(recipe)) {
                recipesToShow.add(recipe);
            } else {
                recipesToHide.add(recipe);
            }
        }

        if (!recipesToHide.isEmpty()) {
            jeiRuntime.getRecipeManager().hideRecipes(craftingType, recipesToHide);
        }
        if (!recipesToShow.isEmpty()) {
            jeiRuntime.getRecipeManager().unhideRecipes(craftingType, recipesToShow);
        }
    }

    private static boolean isItemInList(ItemStack stack, String listEntry) {
        listEntry = listEntry.trim();
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

    private static boolean isBaseResource(Profession prof, ResourceLocation id) {
        String idStr = id.toString();
        for (String baseItemEntry : prof.getBaseItems()) {
            if (baseItemEntry.trim().equals(idStr)) {
                return true;
            }
        }
        return false;
    }

    private static int debugCounter = 0;

    private static boolean shouldHideMod(String modId) {
        if (modId.equals("minecraft"))
            return false;

        Profession currentProfession = JobsConfig.getProfession(ClientCache.CURRENT_PROFESSION_ID);

        if (currentProfession == null) {
            if (modId.equals("mekanism") && debugCounter < 10) {
                System.out.println("DEBUG: Hiding mekanism because currentProfession is NULL. ID: "
                        + ClientCache.CURRENT_PROFESSION_ID);
                debugCounter++;
            }
            return true;
        }

        String currentTechModId = currentProfession.getTechnicalMod();

        if (!currentTechModId.equals("none")) {
            if (modId.equals(currentTechModId)) {
                return false;
            }
            if (modId.equals("mekanism") && debugCounter < 10) {
                System.out.println("DEBUG: Hiding mekanism. Current Tech Mod: '" + currentTechModId + "' vs Target: '"
                        + modId + "'");
                debugCounter++;
            }
            return true;
        }

        return true;
    }

    public static boolean isItemVisible(ItemStack itemStack) {
        ResourceLocation itemRL = ForgeRegistries.ITEMS.getKey(itemStack.getItem());
        if (itemRL == null)
            return true;
        String itemModId = itemRL.getNamespace();

        if (shouldHideMod(itemModId)) {
            return false;
        }

        Profession currentProfession = JobsConfig.getProfession(ClientCache.CURRENT_PROFESSION_ID);
        if (currentProfession != null) {
            String currentTechModId = currentProfession.getTechnicalMod();
            if (itemModId.equals(currentTechModId)) {
                boolean isBaseItem = isBaseItem(currentProfession, itemStack);
                boolean hasBeenCrafted = ClientCache.CRAFTED_ITEMS.contains(itemRL);
                boolean isVisibleViaRecipe = VISIBLE_ITEMS_CACHE.contains(itemRL);

                return isBaseItem || hasBeenCrafted || isVisibleViaRecipe;
            }
        }

        return true;
    }

    public static boolean isResourceVisible(ResourceLocation resourceId) {
        if (resourceId == null)
            return true;
        String modId = resourceId.getNamespace();

        if (shouldHideMod(modId)) {
            return false;
        }

        Profession currentProfession = JobsConfig.getProfession(ClientCache.CURRENT_PROFESSION_ID);
        if (currentProfession != null) {
            String currentTechModId = currentProfession.getTechnicalMod();
            if (modId.equals(currentTechModId)) {
                boolean isBase = isBaseResource(currentProfession, resourceId);
                boolean isUnlocked = ClientCache.CRAFTED_ITEMS.contains(resourceId);
                boolean isVisibleViaRecipe = VISIBLE_ITEMS_CACHE.contains(resourceId);

                return isBase || isUnlocked || isVisibleViaRecipe;
            }
        }

        return true;
    }
}
