package com.majorfp4.jobscraft.event;

import com.majorfp4.jobscraft.JobsCraft;
import com.majorfp4.jobscraft.config.JobsConfig;
import com.majorfp4.jobscraft.config.Profession;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.Score;
import net.minecraft.world.scores.Scoreboard;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Collection;
import java.util.List;

@Mod.EventBusSubscriber(modid = JobsCraft.MOD_ID)
public class ItemUseHandler {

    /**
     * Chamado quando um jogador clica com o botão direito no AR
     */
    @SubscribeEvent
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        // A lógica é a mesma, então chamamos o método principal
        checkItemUse(event, event.getPlayer(), event.getItemStack());
    }

    /**
     * Chamado quando um jogador clica com o botão direito em um BLOCO
     * (É aqui que a enxada é pega)
     */
    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        // A lógica é a mesma, então chamamos o método principal
        checkItemUse(event, event.getPlayer(), event.getItemStack());
    }

    /**
     * Método auxiliar que centraliza a lógica de verificação
     */
    private static void checkItemUse(PlayerInteractEvent event, Player player, ItemStack stack) {
        if (player.level.isClientSide()) {
            return; // Lógica de cancelamento deve rodar apenas no servidor
        }
        if (stack.isEmpty()) {
            return; // Não está segurando nada
        }

        // 1. Encontra a profissão que o ITEM exige
        Profession requiredProfession = getProfessionByItem(stack);
        if (requiredProfession == null) {
            return; // Item é "neutro", permite o uso
        }

        // 2. O item é exclusivo. O jogador tem a profissão?
        Scoreboard scoreboard = player.getScoreboard();
        Objective professionObj = scoreboard.getObjective("profession");
        if (professionObj == null) {
            return; // Placares não carregaram
        }

        Score professionScore = scoreboard.getOrCreatePlayerScore(player.getScoreboardName(), professionObj);
        int playerProfessionId = professionScore.getScore();
        int requiredProfessionId = requiredProfession.getId();

        // 3. A LÓGICA
        if (playerProfessionId != requiredProfessionId) {
            // O jogador NÃO TEM a profissão correta! Cancela o evento.
            event.setCanceled(true);

            // Envia a mensagem de erro
            player.displayClientMessage(new TextComponent("Você não tem a profissão para usar este item."), true);
        }
        // Se os IDs forem iguais, o 'if' é ignorado e o item funciona.
    }

    /**
     * Verifica se um item está em uma lista, checando por Tags (#) ou IDs diretos.
     */
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

    /**
     * Encontra a primeira profissão que lista este item como exclusivo.
     */
    private static Profession getProfessionByItem(ItemStack stack) {
        Collection<Profession> professions = JobsConfig.getAllProfessions();
        for (Profession p : professions) {
            for (String itemEntry : p.getExclusiveItems()) {
                if (isItemInList(stack, itemEntry)) {
                    return p;
                }
            }
        }
        return null;
    }
}