package com.majorfp4.jobscraft.event;

import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.Score;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.criteria.ObjectiveCriteria; // <-- IMPORTAÇÃO ADICIONADA
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import com.majorfp4.jobscraft.JobsCraft;

@Mod.EventBusSubscriber(modid = JobsCraft.MOD_ID)
public class PlayerJoinHandler {

    private static final String PROFESSION_OBJECTIVE = "profession";
    private static final String SKILL_OBJECTIVE = "skill";

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getPlayer() instanceof ServerPlayer serverPlayer)) return;

        Scoreboard scoreboard = serverPlayer.getScoreboard();

        // Usamos a versão que recebe o nome da criteria ("dummy") para compatibilidade
        Objective professionObj = scoreboard.getObjective(PROFESSION_OBJECTIVE);
        if (professionObj == null) {
            // LINHA CORRIGIDA
            professionObj = scoreboard.addObjective(PROFESSION_OBJECTIVE, ObjectiveCriteria.DUMMY,
                    new TextComponent("Profession"), ObjectiveCriteria.RenderType.INTEGER);
        }

        Objective skillObj = scoreboard.getObjective(SKILL_OBJECTIVE);
        if (skillObj == null) {
            // LINHA CORRIGIDA
            skillObj = scoreboard.addObjective(SKILL_OBJECTIVE, ObjectiveCriteria.DUMMY,
                    new TextComponent("Skill"), ObjectiveCriteria.RenderType.INTEGER);
        }

        // Usar getOrCreatePlayerScore que costuma existir em 1.18.2 mappings
        Score professionScore = scoreboard.getOrCreatePlayerScore(serverPlayer.getScoreboardName(), professionObj);
        Score skillScore = scoreboard.getOrCreatePlayerScore(serverPlayer.getScoreboardName(), skillObj);

        // Se for a primeira vez, default: profession = 0 (NONE), skill = 1
        if (professionScore.getScore() == 0) {
            professionScore.setScore(0);
        }

        if (skillScore.getScore() == 0) {
            skillScore.setScore(1);
        }

        serverPlayer.displayClientMessage(new TextComponent("Profissão: NONE | Skill: 1"), false);
    }
}