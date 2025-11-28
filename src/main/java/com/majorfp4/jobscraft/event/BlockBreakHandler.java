package com.majorfp4.jobscraft.event;

import com.majorfp4.jobscraft.JobsCraft;
import com.majorfp4.jobscraft.client.ClientCache;
import com.majorfp4.jobscraft.config.JobsConfig;
import com.majorfp4.jobscraft.config.JobsCraftConfig;
import com.majorfp4.jobscraft.config.Profession;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.Score;
import net.minecraft.world.scores.Scoreboard;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Collection;
import java.util.List;

@Mod.EventBusSubscriber(modid = JobsCraft.MOD_ID)
public class BlockBreakHandler {

    private static final String SKILL_OBJECTIVE = "skill";

    @SubscribeEvent
    public static void onBreakSpeed(PlayerEvent.BreakSpeed event) {
        Player player = event.getPlayer();
        if (player == null)
            return;

        BlockState blockState = event.getState();
        Block block = blockState.getBlock();
        ResourceLocation blockRL = ForgeRegistries.BLOCKS.getKey(block);
        if (blockRL == null)
            return;

        String blockId = blockRL.toString();
        Scoreboard scoreboard = player.getScoreboard();

        Profession matchingProfession = getProfessionByBlock(blockState, blockId);
        if (matchingProfession == null) {
            return;
        }

        int playerProfessionId;
        if (player.level.isClientSide()) {
            playerProfessionId = ClientCache.CURRENT_PROFESSION_ID;
        } else {
            Objective professionObj = scoreboard.getObjective("profession");
            if (professionObj == null)
                return;
            Score professionScore = scoreboard.getOrCreatePlayerScore(player.getScoreboardName(), professionObj);
            playerProfessionId = professionScore.getScore();
        }

        int requiredProfessionId = matchingProfession.getId();
        float multiplier = 1.0f;
        boolean changed = false;

        if (playerProfessionId == requiredProfessionId) {
            // Profissão Correta: Velocidade baseada no skill level atual
            int skillLevel = getPlayerSkillLevel(player);
            multiplier = (float) (skillLevel / JobsCraftConfig.ioFactor.get());
            changed = true;
        } else {
            // Profissão Incorreta

            // Se o bloco é exclusivo, velocidade é 0
            if (isBlockInList(blockState, blockId, matchingProfession.getExclusiveBlocks())) {
                event.setNewSpeed(0.0F);
                return;
            }

            // Se o bloco é relacionado, aplica penalidade baseada no skill level BASE
            if (isBlockInList(blockState, blockId, matchingProfession.getRelatedBlocks())) {
                multiplier = (float) (JobsCraftConfig.baseSkill.get() / JobsCraftConfig.ioFactor.get());
                changed = true;
            }
        }

        if (changed) {
            event.setNewSpeed(event.getOriginalSpeed() * multiplier);
        }
    }

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        Player player = event.getPlayer();
        // Garante que o XP só seja adicionado no servidor
        if (player == null || player.getLevel().isClientSide())
            return;

        BlockState blockState = event.getState();
        Block block = blockState.getBlock();
        ResourceLocation blockRL = ForgeRegistries.BLOCKS.getKey(block);
        if (blockRL == null)
            return;

        String blockId = blockRL.toString();

        Profession matchingProfession = getProfessionByBlock(blockState, blockId);
        if (matchingProfession == null)
            return;

        Scoreboard scoreboard = player.getScoreboard();
        Objective professionObj = scoreboard.getObjective("profession");
        if (professionObj == null)
            return;

        Score professionScore = scoreboard.getOrCreatePlayerScore(player.getScoreboardName(), professionObj);
        int playerProfessionId = professionScore.getScore();

        // Verifica se o jogador tem a profissão correta para ganhar XP
        if (playerProfessionId == matchingProfession.getId()) {
            boolean isExclusive = isBlockInList(blockState, blockId, matchingProfession.getExclusiveBlocks());
            boolean isRelated = isBlockInList(blockState, blockId, matchingProfession.getRelatedBlocks());

            // Ganha XP se for bloco exclusivo ou relacionado
            if (isExclusive || isRelated) {

                int currentSkill = getPlayerSkillLevel(player);
                int xpGain = calculateXpGain(currentSkill);

                if (xpGain > 0) {
                    addSkillXp(player, xpGain);
                }
            }
        }
    }

    private static int getPlayerSkillLevel(Player player) {
        Scoreboard scoreboard = player.getScoreboard();
        Objective skillObj = scoreboard.getObjective(SKILL_OBJECTIVE);

        if (skillObj == null) {
            return JobsCraftConfig.baseSkill.get();
        }

        Score skillScore = scoreboard.getOrCreatePlayerScore(player.getScoreboardName(), skillObj);

        if (skillScore.getScore() == 0) {
            skillScore.setScore(JobsCraftConfig.baseSkill.get());
        }

        return skillScore.getScore();
    }

    private static void addSkillXp(Player player, int amount) {
        Scoreboard scoreboard = player.getScoreboard();
        Objective skillObj = scoreboard.getObjective(SKILL_OBJECTIVE);
        if (skillObj == null)
            return;

        Score skillScore = scoreboard.getOrCreatePlayerScore(player.getScoreboardName(), skillObj);
        skillScore.setScore(skillScore.getScore() + amount);
    }

    private static int calculateXpGain(int currentSkill) {
        double x = currentSkill / 150.0;
        double factor = Math.pow(0.8, x - 10.0) / JobsCraftConfig.yFactor.get();
        double result = factor * 150.0;
        return (int) Math.round(result);
    }

    private static boolean isBlockInList(BlockState state, String blockId, List<String> list) {
        for (String entry : list) {
            if (entry.startsWith("#")) {
                String tagName = entry.substring(1);
                TagKey<Block> tagKey = BlockTags.create(new ResourceLocation(tagName));
                if (state.is(tagKey)) {
                    return true;
                }
            } else {
                if (entry.equals(blockId)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static Profession getProfessionByBlock(BlockState state, String blockId) {
        Collection<Profession> professions = JobsConfig.getAllProfessions();
        for (Profession p : professions) {
            if (isBlockInList(state, blockId, p.getExclusiveBlocks())
                    || isBlockInList(state, blockId, p.getRelatedBlocks())) {
                return p;
            }
        }
        return null;
    }
}