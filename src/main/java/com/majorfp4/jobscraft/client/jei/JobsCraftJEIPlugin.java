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
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

// REMOVEMOS O 'IIngredientManagerListener'
@JeiPlugin
public class JobsCraftJEIPlugin implements IModPlugin {

    private static final ResourceLocation PLUGIN_UID = new ResourceLocation(JobsCraft.MOD_ID, "main");
    private static IJeiRuntime jeiRuntime;

    @Override
    public ResourceLocation getPluginUid() {
        return PLUGIN_UID;
    }

    /**
     * Chamado quando o JEI está pronto.
     */
    @Override
    public void onRuntimeAvailable(IJeiRuntime runtime) {
        JobsCraftJEIPlugin.jeiRuntime = runtime;

        // REMOVEMOS a linha do "addUpdateListener"

        // Aplicamos o filtro uma vez quando o jogo inicia
        JobsCraftIngredientHider.onIngredientsUpdated(runtime.getIngredientManager());
    }

    // REMOVEMOS O MÉTODO 'onIngredientsUpdate' INTEIRO
    // (Ele causava o erro @Override)

    /**
     * Este é o "gatilho" que podemos chamar de qualquer lugar para
     * forçar o JEI a re-filtrar sua lista de itens.
     */
    public static void refreshJEIFilter() {
        if (jeiRuntime != null) {
            // CORREÇÃO: O "refresh" é só chamar nossa lógica de esconder/mostrar de novo
            JobsCraftIngredientHider.onIngredientsUpdated(jeiRuntime.getIngredientManager());
        }
    }

    private static boolean isItemInList(ItemStack stack, String listEntry) {
        if (listEntry.startsWith("#")) {
            // É uma Tag
            String tagName = listEntry.substring(1);
            TagKey<Item> tagKey = ItemTags.create(new ResourceLocation(tagName));
            return stack.is(tagKey);
        } else {
            // É um Item ID
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

    /**
     * Esta é a lógica principal do seu mod!
     * (Esta função estava CORRETA e não precisa de mudanças)
     */
    public static boolean isItemVisible(ItemStack itemStack) {
        ResourceLocation itemRL = ForgeRegistries.ITEMS.getKey(itemStack.getItem());
        if (itemRL == null) {
            return true;
        }
        String itemModId = itemRL.getNamespace();

        Profession currentProfession = JobsConfig.getProfession(ClientCache.CURRENT_PROFESSION_ID);

        // Se o jogador é "NONE" (ID 0)
        if (currentProfession == null) {
            Set<String> allTechModIds = JobsConfig.getAllProfessions().stream()
                    .map(Profession::getTechnicalMod)
                    .filter(id -> !id.equals("none"))
                    .collect(Collectors.toSet());

            return !allTechModIds.contains(itemModId);
        }

        // 2. O JOGADOR TEM UMA PROFISSÃO
        String currentTechModId = currentProfession.getTechnicalMod();

        // 3. SE FOR UMA PROFISSÃO TÉCNICA (ex: "mekanism")
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
                // Agora usamos a nova função que entende Tags
                boolean isBaseItem = isBaseItem(currentProfession, itemStack);
                boolean hasBeenCrafted = ClientCache.CRAFTED_ITEMS.contains(itemRL);

                return isBaseItem || hasBeenCrafted;
            }

        } else {
            // 4. SE FOR UMA PROFISSÃO NÃO-TÉCNICA (ex: "Mineiro")
            Set<String> allTechModIds = JobsConfig.getAllProfessions().stream()
                    .map(Profession::getTechnicalMod)
                    .filter(id -> !id.equals("none"))
                    .collect(Collectors.toSet());

            return !allTechModIds.contains(itemModId);
        }

        return true; // Mostra itens (ex: Vanilla)
    }
}