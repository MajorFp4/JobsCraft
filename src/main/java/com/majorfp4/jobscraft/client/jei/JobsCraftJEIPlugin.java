package com.majorfp4.jobscraft.client.jei;

import com.majorfp4.jobscraft.JobsCraft;
import com.majorfp4.jobscraft.client.ClientCache;
import com.majorfp4.jobscraft.config.JobsConfig;
import com.majorfp4.jobscraft.config.Profession;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.runtime.IJeiRuntime;
import mezz.jei.api.runtime.IIngredientManager;
import net.minecraft.resources.ResourceLocation;
// --- IMPORTAÇÕES NECESSÁRIAS ---
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List; // <-- IMPORTAÇÃO NECESSÁRIA
import java.util.Set;
import java.util.stream.Collectors;

@JeiPlugin
public class JobsCraftJEIPlugin implements IModPlugin {

    private static final ResourceLocation PLUGIN_UID = new ResourceLocation(JobsCraft.MOD_ID, "main");
    private static IJeiRuntime jeiRuntime;

    @Override
    public ResourceLocation getPluginUid() {
        return PLUGIN_UID;
    }

    @Override
    public void onRuntimeAvailable(IJeiRuntime runtime) {
        JobsCraftJEIPlugin.jeiRuntime = runtime;
        JobsCraftIngredientHider.onIngredientsUpdated(runtime.getIngredientManager());
    }

    public static void refreshJEIFilter() {
        if (jeiRuntime != null) {
            JobsCraftIngredientHider.onIngredientsUpdated(jeiRuntime.getIngredientManager());
        }
    }

    // --- LÓGICA DE CHECAGEM DE TAG (DO ITEMUSEHANDLER) ---
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

    // --- MÉTODO AUXILIAR PARA BASE_ITEMS ---
    private static boolean isBaseItem(Profession prof, ItemStack stack) {
        for (String baseItemEntry : prof.getBaseItems()) {
            if (isItemInList(stack, baseItemEntry)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Lógica principal de visibilidade do JEI
     */
    public static boolean isItemVisible(ItemStack itemStack) {
        ResourceLocation itemRL = ForgeRegistries.ITEMS.getKey(itemStack.getItem());
        if (itemRL == null) {
            return true; // Mostra itens sem ID
        }
        String itemModId = itemRL.getNamespace();

        Profession currentProfession = JobsConfig.getProfession(ClientCache.CURRENT_PROFESSION_ID);

        // --- CASO 1: SEM PROFISSÃO (ID 0) ---
        if (currentProfession == null) {
            Set<String> allTechModIds = JobsConfig.getAllProfessions().stream()
                    .map(Profession::getTechnicalMod)
                    .filter(id -> !id.equals("none"))
                    .collect(Collectors.toSet());

            // Se o item pertence a um mod técnico, esconde
            return !allTechModIds.contains(itemModId);
        }

        // --- CASO 2: COM PROFISSÃO ---
        String currentTechModId = currentProfession.getTechnicalMod();

        // SE FOR PROFISSÃO TÉCNICA (ex: "mekanism")
        if (!currentTechModId.equals("none")) {

            // Esconde itens de OUTROS mods técnicos
            if (!itemModId.equals(currentTechModId) && !itemModId.equals("minecraft")) {
                boolean isOtherTechMod = JobsConfig.getAllProfessions().stream()
                        .anyMatch(p -> p.getTechnicalMod().equals(itemModId));
                if (isOtherTechMod) {
                    return false;
                }
            }

            // Se o item for do MOD CORRETO (ex: "mekanism")
            if (itemModId.equals(currentTechModId)) {

                // --- LÓGICA CORRIGIDA ---
                boolean isBaseItem = isBaseItem(currentProfession, itemStack);
                boolean hasBeenCrafted = ClientCache.CRAFTED_ITEMS.contains(itemRL);

                // Mostra o item se ele for "básico" OU se já foi fabricado
                return isBaseItem || hasBeenCrafted;
            }

        } else { // SE FOR PROFISSÃO NÃO-TÉCNICA (ex: "Mineiro")

            // Esconde TODOS os itens de TODOS os mods técnicos
            Set<String> allTechModIds = JobsConfig.getAllProfessions().stream()
                    .map(Profession::getTechnicalMod)
                    .filter(id -> !id.equals("none"))
                    .collect(Collectors.toSet());

            return !allTechModIds.contains(itemModId);
        }

        return true; // Mostra tudo o que sobrou (ex: Itens Vanilla)
    }
}