package com.majorfp4.jobscraft.client.jei;

import com.majorfp4.jobscraft.JobsCraft;
import com.majorfp4.jobscraft.client.ClientCache;
import com.majorfp4.jobscraft.config.JobsConfig;
import com.majorfp4.jobscraft.config.Profession;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.gui.handlers.IRecipeSlotClickListener;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.api.runtime.IJeiRuntime;
import mezz.jei.api.runtime.IRecipesGui;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraftforge.registries.ForgeRegistries;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

@JeiPlugin
public class JobsCraftJEIPlugin implements IModPlugin {

    private static final ResourceLocation PLUGIN_UID = new ResourceLocation(JobsCraft.MOD_ID, "main");
    private static IJeiRuntime jeiRuntime;
    private static final List<ItemStack> MASTER_ITEM_STACK_LIST = new ArrayList<>();

    @Override
    public ResourceLocation getPluginUid() {
        return PLUGIN_UID;
    }

    @Override
    public void onRuntimeAvailable(IJeiRuntime runtime) {
        JobsCraftJEIPlugin.jeiRuntime = runtime;
        IIngredientManager ingredientManager = runtime.getIngredientManager();
        IIngredientType<ItemStack> itemType = ingredientManager.getIngredientType(ItemStack.class);
        MASTER_ITEM_STACK_LIST.clear();
        MASTER_ITEM_STACK_LIST.addAll(ingredientManager.getAllIngredients(itemType));
        JobsCraftIngredientHider.onIngredientsUpdated(ingredientManager);
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        // Pede ao JEI para usar nossa lógica para validar TODAS as receitas de fabricação
        registration.addRecipeHidingValidator(recipe -> {
            if (recipe == null) {
                return false; // Não esconder
            }

            // Pede ao nosso helper do CLIENTE para verificar.
            // Retorna 'true' (esconder) se a receita NÃO estiver desbloqueada.
            return !RecipeValidationHelper.isRecipeUnlocked(recipe);
        });
    }

    @Override
    public void registerGuiHandlers(IGuiHandlerRegistration registration) {
        // Adiciona um listener global de clique
        registration.addRecipeSlotClickListener(new IRecipeSlotClickListener() {
            @Override
            public boolean onRecipeSlotClick(IRecipesGui recipesGui, mezz.jei.api.recipe.IFocus<?> focus, RecipeIngredientRole role) {
                if (focus != null && (role == RecipeIngredientRole.INPUT || role == RecipeIngredientRole.OUTPUT)) {

                    ItemStack itemStack = focus.getTypedValue().getIngredient(mezz.jei.api.ingredients.VanillaTypes.ITEM_STACK).orElse(null);
                    if (itemStack == null || itemStack.isEmpty()) {
                        return false;
                    }

                    // Se o item NÃO estiver visível (pela nossa lógica de hider),
                    // o clique é cancelado.
                    if (!JobsCraftJEIPlugin.isItemVisible(itemStack)) {
                        return true; // Cancela o clique (bloqueia R/U)
                    }
                }
                return false; // Permite o clique
            }
        });
    }

    public static void refreshJEIFilter() {
        if (jeiRuntime != null) {
            JobsCraftIngredientHider.onIngredientsUpdated(jeiRuntime.getIngredientManager());
            jeiRuntime.getRecipeManager().unhideRecipes(RecipeTypes.CRAFTING, jeiRuntime.getRecipeManager().getRecipes(RecipeTypes.CRAFTING));
            jeiRuntime.getRecipeManager().hideRecipes(RecipeTypes.CRAFTING, jeiRuntime.getRecipeManager().getRecipes(RecipeTypes.CRAFTING));
        }
    }

    public static List<ItemStack> getMasterItemStackList() {
        return MASTER_ITEM_STACK_LIST;
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

    public static boolean isItemVisible(ItemStack itemStack) {

        ResourceLocation itemRL = ForgeRegistries.ITEMS.getKey(itemStack.getItem());
        if (itemRL == null) return true;
        String itemModId = itemRL.getNamespace();
        Profession currentProfession = JobsConfig.getProfession(ClientCache.CURRENT_PROFESSION_ID);

        if (currentProfession == null) {
            Set<String> allTechModIds = JobsConfig.getAllProfessions().stream()
                    .map(Profession::getTechnicalMod)
                    .filter(id -> !id.equals("none"))
                    .collect(Collectors.toSet());
            return !allTechModIds.contains(itemModId);
        }
        String currentTechModId = currentProfession.getTechnicalMod();
        if (!currentTechModId.equals("none")) {
            if (!itemModId.equals(currentTechModId) && !itemModId.equals("minecraft")) {
                boolean isOtherTechMod = JobsConfig.getAllProfessions().stream()
                        .anyMatch(p -> p.getTechnicalMod().equals(itemModId));
                if (isOtherTechMod) {
                    return false;
                }
            }
            if (itemModId.equals(currentTechModId)) {
                boolean isBaseItem = isBaseItem(currentProfession, itemStack);
                boolean hasBeenCrafted = ClientCache.CRAFTED_ITEMS.contains(itemRL);
                return isBaseItem || hasBeenCrafted;
            }
        } else {
            Set<String> allTechModIds = JobsConfig.getAllProfessions().stream()
                    .map(Profession::getTechnicalMod)
                    .filter(id -> !id.equals("none"))
                    .collect(Collectors.toSet());
            return !allTechModIds.contains(itemModId);
        }
        return true;
    }
}