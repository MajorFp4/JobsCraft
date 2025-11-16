package com.majorfp4.jobscraft.event;

import com.majorfp4.jobscraft.JobsCraft;
import com.majorfp4.jobscraft.capability.TechProgressionCapability;
import com.majorfp4.jobscraft.config.JobsConfig;
import com.majorfp4.jobscraft.config.Profession;
import com.majorfp4.jobscraft.util.RecipeHelper;
import net.minecraft.network.protocol.game.ClientboundUpdateRecipesPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.Score;
import net.minecraft.world.scores.Scoreboard;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import java.util.List;

@Mod.EventBusSubscriber(modid = JobsCraft.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class RecipeHandler {

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getPlayer() instanceof ServerPlayer player) {
            player.server.execute(() -> sendUpdatedRecipes(player));
        }
    }

    public static void sendUpdatedRecipes(ServerPlayer player) {
        Scoreboard scoreboard = player.getScoreboard();
        Objective professionObj = scoreboard.getObjective("profession");
        if (professionObj == null) return;
        Score professionScore = scoreboard.getOrCreatePlayerScore(player.getScoreboardName(), professionObj);
        Profession profession = JobsConfig.getProfession(professionScore.getScore());

        if (profession == null) {
            profession = JobsConfig.getProfession(0);
            if(profession == null) return;
        }

        final Profession finalProfession = profession;

        player.getCapability(TechProgressionCapability.PLAYER_PROGRESS).ifPresent(progress -> {

            List<Recipe<?>> recipesToSend = RecipeHelper.getAllowedRecipes(player, finalProfession, progress);

            player.connection.send(new ClientboundUpdateRecipesPacket(recipesToSend));

            System.out.println("[JobsCraft] Receitas atualizadas para " + player.getName().getString() +
                    ". Enviando " + recipesToSend.size() + " receitas.");
        });
    }
}