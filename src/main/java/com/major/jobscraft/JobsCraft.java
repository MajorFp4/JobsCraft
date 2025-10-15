package com.major.jobscraft;

import net.minecraft.block.Block;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.Scoreboard;
// IMPORT CORRETO E FINAL PARA A SUA VERSÃO
import net.minecraft.scoreboard.ScoreCriteria;
import net.minecraft.scoreboard.ScoreObjective;
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

    @SubscribeEvent
    public void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        PlayerEntity player = event.getPlayer();
        if (player.world.isRemote()) return;

        ServerWorld world = (ServerWorld) player.getEntityWorld();
        Scoreboard scoreboard = world.getScoreboard();

        String skillLevelObjectiveName = "jc_skill_level";
        String professionObjectiveName = "jc_profession";

        // NOME CORRETO PARA A SUA VERSÃO: ScoreCriteria (sem I e no plural)
        if (scoreboard.getObjective(skillLevelObjectiveName) == null) {
            scoreboard.addObjective(skillLevelObjectiveName, ScoreCriteria.DUMMY, new StringTextComponent("Skill Level"), );
        }
        if (scoreboard.getObjective(professionObjectiveName) == null) {
            scoreboard.addObjective(professionObjectiveName, ScoreCriteria.DUMMY, new StringTextComponent("Profession"));
        }

        // NOME CORRETO PARA A SUA VERSÃO: ScoreObjective
        ScoreObjective skillObjective = scoreboard.getObjective(skillLevelObjectiveName);
        String playerName = player.getScoreboardName();

        if (!scoreboard.playerHasObjective(playerName, skillObjective)) {
            scoreboard.getOrCreateScore(playerName, skillObjective).setScorePoints(1);
        }
    }

    @SubscribeEvent
    public void onBlockBreak(BlockEvent.BreakEvent event) {
        if (event.getPlayer().world.isRemote()) return;
        addSkillExperience(event.getPlayer());
    }

    private void addSkillExperience(PlayerEntity player) {
        ServerWorld world = (ServerWorld) player.getEntityWorld();
        Scoreboard scoreboard = world.getScoreboard();

        // NOME CORRETO PARA A SUA VERSÃO: ScoreObjective
        ScoreObjective skillObjective = scoreboard.getObjective("jc_skill_level");
        if (skillObjective == null) return;

        String playerName = player.getScoreboardName();
        Score score = scoreboard.getOrCreateScore(playerName, skillObjective);
        int currentSkillLevel = score.getScorePoints() <= 0 ? 1 : score.getScorePoints();

        double exponent = 10.0 / currentSkillLevel;
        double xpGainedDouble = (Math.pow(1.1, exponent) - 1) * 800;
        int xpGained = Math.max(1, (int) Math.round(xpGainedDouble));

        score.increaseScore(xpGained);

        player.sendMessage(new StringTextComponent(String.format("§a+%d Skill XP", xpGained)));
        LOGGER.info(String.format("Jogador %s ganhou %d de XP. Novo Skill Level: %d", playerName, xpGained, score.getScorePoints()));
    }

    @SubscribeEvent
    public void onServerStarting(FMLServerStartingEvent event) {}

    @Mod.EventBusSubscriber(modid = MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class RegistryEvents {
        @SubscribeEvent
        public static void onBlocksRegistry(final RegistryEvent.Register<Block> blockRegistryEvent) {}
    }
}