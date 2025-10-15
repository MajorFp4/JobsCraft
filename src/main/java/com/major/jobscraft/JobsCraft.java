package com.major.jobscraft;

import net.minecraft.block.Block;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardCriterion;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod("jobscraft")
public class JobsCraft {

    public static final String MOD_ID = "jobscraft";
    private static final Logger LOGGER = LogManager.getLogger();

    public JobsCraft() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
        MinecraftForge.EVENT_BUS.register(this);
    }

    private void setup(final FMLCommonSetupEvent event) {
        LOGGER.info("JobsCraft está sendo carregado!");
    }

    /**
     * Este evento é chamado toda vez que um jogador entra em um mundo.
     * Usaremos para criar os scoreboards se eles não existirem para o jogador.
     */
    @SubscribeEvent
    public void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        PlayerEntity player = event.getPlayer();
        // O código só deve rodar no lado do servidor
        if (player.world.isRemote()) return;

        ServerWorld world = (ServerWorld) player.getEntityWorld();
        Scoreboard scoreboard = world.getScoreboard();

        // Nomes dos nossos objetivos
        String skillLevelObjectiveName = "jc_skill_level";
        String professionObjectiveName = "jc_profession";

        // Garante que o objetivo para "Skill Level" exista
        if (scoreboard.getObjective(skillLevelObjectiveName) == null) {
            scoreboard.addObjective(skillLevelObjectiveName, ScoreboardCriterion.DUMMY, new StringTextComponent("Skill Level"));
            LOGGER.info("Scoreboard 'jc_skill_level' criado.");
        }

        // Garante que o objetivo para "Profession" exista
        if (scoreboard.getObjective(professionObjectiveName) == null) {
            scoreboard.addObjective(professionObjectiveName, ScoreboardCriterion.DUMMY, new StringTextComponent("Profession"));
            LOGGER.info("Scoreboard 'jc_profession' criado.");
        }

        // Inicializa a pontuação do jogador para 1 se ele ainda não tiver uma
        ScoreboardObjective skillObjective = scoreboard.getObjective(skillLevelObjectiveName);
        String playerName = player.getScoreboardName();

        if (!scoreboard.playerHasObjective(playerName, skillObjective)) {
            LOGGER.info("Inicializando Skill Level para o jogador " + playerName);
            // Começar em 1 é importante para evitar divisão por zero na sua fórmula!
            scoreboard.getOrCreateScore(playerName, skillObjective).setScorePoints(1);
        }
    }

    /**
     * Este evento é chamado sempre que um jogador quebra um bloco.
     */
    @SubscribeEvent
    public void onBlockBreak(BlockEvent.BreakEvent event) {
        // Garante que a lógica rode apenas no servidor
        if (event.getPlayer().world.isRemote()) {
            return;
        }

        PlayerEntity player = event.getPlayer();

        // TODO: Adicionar a lógica aqui para verificar se a profissão do jogador
        //       permite ganhar XP ao quebrar este bloco específico.

        // Por enquanto, vamos dar XP por quebrar qualquer bloco para testar a fórmula.
        addSkillExperience(player);
    }

    /**
     * Calcula e adiciona a experiência de skill ao jogador com base na sua fórmula.
     */
    private void addSkillExperience(PlayerEntity player) {
        ServerWorld world = (ServerWorld) player.getEntityWorld();
        Scoreboard scoreboard = world.getScoreboard();
        ScoreboardObjective skillObjective = scoreboard.getObjective("jc_skill_level");

        // Verificação de segurança caso o objetivo não exista por algum motivo
        if (skillObjective == null) {
            LOGGER.error("Objetivo 'jc_skill_level' não encontrado. Não foi possível adicionar XP.");
            return;
        }

        String playerName = player.getScoreboardName();
        Score score = scoreboard.getOrCreateScore(playerName, skillObjective);
        int currentSkillLevel = score.getScorePoints();

        // Evita divisão por zero
        if (currentSkillLevel <= 0) {
            currentSkillLevel = 1;
        }

        // --- APLICANDO SUA FÓRMULA ---
        // Fórmula: ((1.1^(10/skill level)) - 1) * 800
        double exponent = 10.0 / currentSkillLevel;
        double xpGainedDouble = (Math.pow(1.1, exponent) - 1) * 800;

        // O scoreboard só aceita inteiros, então arredondamos o resultado.
        int xpGained = (int) Math.round(xpGainedDouble);

        // Garante que o jogador sempre ganhe pelo menos 1 de XP.
        if (xpGained < 1) {
            xpGained = 1;
        }

        // Adiciona a nova pontuação ao jogador
        score.increaseScore(xpGained);

        // Feedback para o jogador e para o console
        player.sendMessage(new StringTextComponent(String.format("§a+%d Skill XP", xpGained)));
        LOGGER.info(String.format("Jogador %s ganhou %d de XP. Novo Skill Level: %d", playerName, xpGained, score.getScorePoints()));
    }

    @SubscribeEvent
    public void onServerStarting(FMLServerStartingEvent event) {
        LOGGER.info("JobsCraft - Servidor iniciando.");
    }

    @Mod.EventBusSubscriber(modid = MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class RegistryEvents {
        @SubscribeEvent
        public static void onBlocksRegistry(final RegistryEvent.Register<Block> blockRegistryEvent) {
            LOGGER.info("JobsCraft - Registrando blocos...");
        }
    }
}