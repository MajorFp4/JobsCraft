package com.majorfp4.jobscraft.event;

import com.majorfp4.jobscraft.JobsCraft;
import com.majorfp4.jobscraft.capability.PlayerProgress;
import com.majorfp4.jobscraft.capability.TechProgressionCapability;
import com.majorfp4.jobscraft.config.JobsConfig;
import com.majorfp4.jobscraft.config.Profession;
import com.majorfp4.jobscraft.network.ClientboundSyncProgressPacket;
import com.majorfp4.jobscraft.network.PacketHandler;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.Score;
import net.minecraft.world.scores.Scoreboard;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.*;

@Mod.EventBusSubscriber(modid = JobsCraft.MOD_ID)
public class TechTreeHandler {

    // Cache: Machine Item -> Set of Output Item IDs
    private static final Map<Item, Set<ResourceLocation>> MACHINE_OUTPUTS_CACHE = new HashMap<>();
    private static boolean cacheBuilt = false;

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

        // Ensure cache is built
        if (!cacheBuilt) {
            buildCache(player.getServer().getRecipeManager());
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

        // Check if the crafted item is a machine that should unlock recipes
        if (MACHINE_OUTPUTS_CACHE.containsKey(craftedStack.getItem())) {
            Set<ResourceLocation> outputs = MACHINE_OUTPUTS_CACHE.get(craftedStack.getItem());
            player.getCapability(TechProgressionCapability.PLAYER_PROGRESS).ifPresent(progress -> {
                unlockMachineOutputs(player, progress, outputs);
            });
        }

        if (!craftedItemId.getNamespace().equals(techModId)) {
            return;
        }

        player.getCapability(TechProgressionCapability.PLAYER_PROGRESS).ifPresent(progress -> {
            if (!progress.hasCrafted(craftedItemId)) {
                System.out.println(
                        "[JobsCraft] Jogador " + player.getName().getString() + " desbloqueou: " + craftedItemId);
                progress.addCraftedItem(craftedItemId);

                PacketHandler.INSTANCE.send(
                        PacketDistributor.PLAYER.with(() -> player),
                        new ClientboundSyncProgressPacket(progress.getCraftedItems()));

                // Trigger recipe update
                RecipeHandler.sendUpdatedRecipes(player);
            }
        });
    }

    private static void buildCache(RecipeManager recipeManager) {
        System.out.println("[JobsCraft] Building Machine Outputs Cache...");
        MACHINE_OUTPUTS_CACHE.clear();

        for (Recipe<?> recipe : recipeManager.getRecipes()) {
            // Ignore standard crafting table recipes (vanilla or modded tables using this
            // type)
            if (recipe.getType() == net.minecraft.world.item.crafting.RecipeType.CRAFTING) {
                continue;
            }

            // Get the "Toast Symbol" (Icon) of the recipe.
            // This usually represents the workstation (machine) used to craft it.
            ItemStack icon = recipe.getToastSymbol();

            if (icon.isEmpty())
                continue;

            Item machineItem = icon.getItem();

            // Skip vanilla Crafting Table explicitly as a fallback
            if (machineItem == Items.CRAFTING_TABLE)
                continue;

            ItemStack result = recipe.getResultItem();
            if (result.isEmpty())
                continue;

            ResourceLocation resultId = ForgeRegistries.ITEMS.getKey(result.getItem());
            if (resultId == null)
                continue;

            MACHINE_OUTPUTS_CACHE.computeIfAbsent(machineItem, k -> new HashSet<>()).add(resultId);
        }

        cacheBuilt = true;
        System.out
                .println("[JobsCraft] Cache built. Found " + MACHINE_OUTPUTS_CACHE.size() + " machines with recipes.");
    }

    private static void unlockMachineOutputs(ServerPlayer player, PlayerProgress progress,
            Set<ResourceLocation> outputs) {
        int unlockedCount = 0;

        for (ResourceLocation itemId : outputs) {
            if (!progress.hasCrafted(itemId)) {
                System.out.println("[JobsCraft] DEBUG: Unlocking item via machine: " + itemId);
                progress.addCraftedItem(itemId);
                unlockedCount++;
            } else {
                System.out.println("[JobsCraft] DEBUG: Item already unlocked: " + itemId);
            }
        }

        if (unlockedCount > 0) {
            System.out.println("[JobsCraft] Máquina fabricada! Desbloqueando " + unlockedCount + " saídas para "
                    + player.getName().getString());
            PacketHandler.INSTANCE.send(
                    PacketDistributor.PLAYER.with(() -> player),
                    new ClientboundSyncProgressPacket(progress.getCraftedItems()));

            // Trigger recipe update
            RecipeHandler.sendUpdatedRecipes(player);
        }
    }
}