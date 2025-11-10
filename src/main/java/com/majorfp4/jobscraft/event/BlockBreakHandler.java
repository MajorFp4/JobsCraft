package com.majorfp4.jobscraft.event;

import com.majorfp4.jobscraft.config.JobsConfig;
import com.majorfp4.jobscraft.config.Profession;
import com.majorfp4.jobscraft.JobsCraft;
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
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Collection;
import java.util.List;

@Mod.EventBusSubscriber(modid = JobsCraft.MOD_ID)
public class BlockBreakHandler {

    @SubscribeEvent
    public static void onBreakSpeed(PlayerEvent.BreakSpeed event) {
        Player player = event.getPlayer();

        BlockState blockState = event.getState();
        Block block = blockState.getBlock();
        ResourceLocation blockRL = ForgeRegistries.BLOCKS.getKey(block);
        if (blockRL == null) return;

        String blockId = blockRL.toString();

        Scoreboard scoreboard = player.getScoreboard();

        Profession matchingProfession = getProfessionByBlock(blockState, blockId);
        if (matchingProfession == null) {
            return;
        }

        Objective professionObj = scoreboard.getObjective("profession");
        if (professionObj == null) return;

        Score professionScore = scoreboard.getOrCreatePlayerScore(player.getScoreboardName(), professionObj);

        int playerProfessionId = professionScore.getScore();
        int requiredProfessionId = matchingProfession.getId();

        if (playerProfessionId != requiredProfessionId) {

            if (isBlockInList(blockState, blockId, matchingProfession.getExclusiveBlocks())) {
                event.setNewSpeed(0.0F);
                return;
            }

            if (isBlockInList(blockState, blockId, matchingProfession.getRelatedBlocks())) {
                event.setNewSpeed(event.getOriginalSpeed() * 0.3F);
            }
        }
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
            if (isBlockInList(state, blockId, p.getExclusiveBlocks()) || isBlockInList(state, blockId, p.getRelatedBlocks())) {
                return p;
            }
        }
        return null;
    }
}