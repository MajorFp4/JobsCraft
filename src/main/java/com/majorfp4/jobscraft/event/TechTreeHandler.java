package com.majorfp4.jobscraft.event;

import com.majorfp4.jobscraft.JobsCraft;
import com.majorfp4.jobscraft.capability.TechProgressionCapability;
import com.majorfp4.jobscraft.config.JobsConfig;
import com.majorfp4.jobscraft.config.Profession;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.Score;
import net.minecraft.world.scores.Scoreboard;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import com.majorfp4.jobscraft.network.ClientboundSyncProgressPacket;
import com.majorfp4.jobscraft.network.PacketHandler;
import net.minecraftforge.network.PacketDistributor;

@Mod.EventBusSubscriber(modid = JobsCraft.MOD_ID)
public class TechTreeHandler {

    /**
     * Disparado no SERVIDOR sempre que um jogador fabrica um item.
     * Este é o "gatilho" da nossa tech tree.
     */
    @SubscribeEvent
    public static void onItemCrafted(PlayerEvent.ItemCraftedEvent event) {
        // 1. OBTER DADOS
        // A lógica de progresso deve rodar apenas no servidor.
        if (!(event.getPlayer() instanceof ServerPlayer player)) {
            return;
        }

        ItemStack craftedStack = event.getCrafting();
        if (craftedStack.isEmpty()) {
            return;
        }

        ResourceLocation craftedItemId = ForgeRegistries.ITEMS.getKey(craftedStack.getItem());
        if (craftedItemId == null) {
            return;
        }

        // 2. VERIFICAR A PROFISSÃO
        Scoreboard scoreboard = player.getScoreboard();
        Objective professionObj = scoreboard.getObjective("profession");
        if (professionObj == null) {
            return; // Placar não existe
        }

        Score professionScore = scoreboard.getOrCreatePlayerScore(player.getScoreboardName(), professionObj);
        int professionId = professionScore.getScore();

        // Pega a profissão atual do jogador
        Profession profession = JobsConfig.getProfession(professionId);
        if (profession == null) {
            return;
        }

        // 3. VERIFICAR A LÓGICA DA TECH TREE
        // Verificamos se esta profissão USA a tech tree
        String techModId = profession.getTechnicalMod();
        if (techModId.equals("none")) {
            return; // Esta profissão (ex: Mineiro) não usa a tech tree
        }

        // Verificamos se o item fabricado pertence ao Mod da tech tree
        // (ex: techModId = "mekanism", craftedItemId = "mekanism:metallurgic_infuser")
        if (!craftedItemId.getNamespace().equals(techModId)) {
            return; // Item fabricado não é do mod relevante
        }

        // 4. SALVAR O PROGRESSO
        // Se chegamos aqui, o jogador fabricou um item da sua tech tree.
        player.getCapability(TechProgressionCapability.PLAYER_PROGRESS).ifPresent(progress -> {

            // Verificamos se ele já fabricou este item antes
            if (!progress.hasCrafted(craftedItemId)) {
                // É a primeira vez!
                System.out.println("[JobsCraft] Jogador " + player.getName().getString() + " desbloqueou: " + craftedItemId);

                // Adiciona o item à "memória" (Capability)
                progress.addCraftedItem(craftedItemId);

                // TODO: Enviar pacote ao cliente para atualizar o JEI
            }
            if (!progress.hasCrafted(craftedItemId)) {
                // É a primeira vez!
                System.out.println("[JobsCraft] Jogador " + player.getName().getString() + " desbloqueou: " + craftedItemId);

                progress.addCraftedItem(craftedItemId);

                // --- SUBSTITUA O "TODO" ---
                // Envia a lista de progresso ATUALIZADA para o cliente
                PacketHandler.INSTANCE.send(
                        PacketDistributor.PLAYER.with(() -> player),
                        new ClientboundSyncProgressPacket(progress.getCraftedItems()) // ou getCraftedItems()
                );
                // --------------------------
            }
        });
    }
}