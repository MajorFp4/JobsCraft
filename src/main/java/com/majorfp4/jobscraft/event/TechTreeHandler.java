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

        Scoreboard scoreboard = player.getScoreboard();
        Objective professionObj = scoreboard.getObjective("profession");
        if (professionObj == null) {
            return;
        }
        Score professionScore = scoreboard.getOrCreatePlayerScore(player.getScoreboardName(), professionObj);
        int professionId = professionScore.getScore();
        Profession profession = JobsConfig.getProfession(professionId);
        if (profession == null) {
            return;
        }

        String techModId = profession.getTechnicalMod();
        if (techModId.equals("none")) {
            return;
        }

        if (!craftedItemId.getNamespace().equals(techModId)) {
            return;
        }

        player.getCapability(TechProgressionCapability.PLAYER_PROGRESS).ifPresent(progress -> {

            // A checagem só precisa ser feita UMA VEZ
            if (!progress.hasCrafted(craftedItemId)) {
                System.out.println("[JobsCraft] Jogador " + player.getName().getString() + " desbloqueou: " + craftedItemId);
                progress.addCraftedItem(craftedItemId);

                // Envia a lista de progresso ATUALIZADA para o cliente
                PacketHandler.INSTANCE.send(
                        PacketDistributor.PLAYER.with(() -> player),
                        new ClientboundSyncProgressPacket(progress.getCraftedItems())
                );
            }
        });
    }
}