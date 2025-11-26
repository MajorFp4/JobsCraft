package com.majorfp4.jobscraft.event;

import com.majorfp4.jobscraft.JobsCraft;
import com.majorfp4.jobscraft.capability.PlayerProgress;
import com.majorfp4.jobscraft.capability.TechProgressionCapability;
import com.majorfp4.jobscraft.config.JobsConfig;
import com.majorfp4.jobscraft.config.Profession;
import com.majorfp4.jobscraft.util.RecipeHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.Score;
import net.minecraft.world.scores.Scoreboard;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Optional;

@Mod.EventBusSubscriber(modid = JobsCraft.MOD_ID)
public class CraftingRestrictionHandler {

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.player.level.isClientSide) {
            return;
        }

        if (!(event.player instanceof ServerPlayer player)) {
            return;
        }

        if (player.containerMenu instanceof CraftingMenu craftingMenu) {
            checkCraftingResult(player, craftingMenu);
        }
    }

    private static void checkCraftingResult(ServerPlayer player, CraftingMenu craftingMenu) {
        Slot resultSlot = craftingMenu.getSlot(0);
        ItemStack resultStack = resultSlot.getItem();

        if (resultStack.isEmpty()) {
            return;
        }

        // We need to find the recipe that matches the current grid
        // CraftingMenu doesn't expose the container directly in a public way easily
        // without AT,
        // but we can construct a dummy container or try to get it if accessible.
        // Actually, CraftingMenu has 'craftSlots' which is the CraftingContainer.
        // But it is protected.
        // However, we can iterate slots 1-9 (indices 1 to 9) to get the items.

        // Wait, to use RecipeManager.getRecipeFor, we need a Container.
        // We can create a temporary implementation or use the one from the menu if we
        // can access it.
        // Since we can't easily access 'craftSlots', let's rely on the fact that the
        // result slot is populated.
        // If the result slot is populated, a recipe WAS found by the vanilla logic.
        // We just need to find WHICH recipe it was.

        // We can try to match again using the player's current view of the container.
        // But creating a container wrapper is easier.

        Container dummyContainer = new Container() {
            @Override
            public int getContainerSize() {
                return 9;
            }

            @Override
            public boolean isEmpty() {
                return false;
            }

            @Override
            public ItemStack getItem(int index) {
                // CraftingMenu: Slot 0 is Result. Slots 1-9 are Matrix (3x3).
                if (index >= 0 && index < 9) {
                    return craftingMenu.getSlot(index + 1).getItem();
                }
                return ItemStack.EMPTY;
            }

            @Override
            public ItemStack removeItem(int index, int count) {
                return ItemStack.EMPTY; // Not needed for check
            }

            @Override
            public ItemStack removeItemNoUpdate(int index) {
                return ItemStack.EMPTY; // Not needed
            }

            @Override
            public void setItem(int index, ItemStack stack) {
                // Not needed
            }

            @Override
            public void setChanged() {
            }

            @Override
            public boolean stillValid(net.minecraft.world.entity.player.Player player) {
                return true;
            }

            @Override
            public void clearContent() {
            }
        };

        // Note: getRecipeFor expects a Container. For Crafting, it expects
        // CraftingContainer.
        // Our dummy container implements Container, but not CraftingContainer.
        // This might cause issues if recipes cast to CraftingContainer.
        // Most vanilla recipes do.
        // So we should try to use reflection to get the real 'craftSlots' field if
        // possible,
        // OR assume standard 3x3 and implement CraftingContainer interface?
        // CraftingContainer is a class, not interface. We can't extend it easily
        // anonymously without constructor args.
        // Constructor: CraftingContainer(AbstractContainerMenu menu, int width, int
        // height)

        // Reflection is safer here to get the real container.
        // Field name in SRG: field_75162_e (1.12) -> craftSlots?
        // In 1.18/1.19 mappings, it is usually 'craftSlots'.
        // Let's try to access it via reflection or just assume we can't and try a
        // different approach.

        // Alternative: Iterate all recipes and check matches(dummyContainer)?
        // But matches() takes CraftingContainer too.

        // OK, let's use ObfuscationReflectionHelper or just standard reflection.
        // Field name is likely "craftSlots" or "inputSlots".
        // Let's try "craftSlots" first.

        net.minecraft.world.inventory.CraftingContainer craftSlots = null;
        try {
            java.lang.reflect.Field field = CraftingMenu.class.getDeclaredField("craftSlots"); // Mapped name
            field.setAccessible(true);
            craftSlots = (net.minecraft.world.inventory.CraftingContainer) field.get(craftingMenu);
        } catch (Exception e) {
            try {
                // Try SRG name or other common names if needed.
                // For now, let's assume 'craftSlots' works in dev env.
                // If it fails, we might need 'f_39353_' (Mojmap) or similar.
                // Let's try to find the field by type.
                for (java.lang.reflect.Field f : CraftingMenu.class.getDeclaredFields()) {
                    if (f.getType() == net.minecraft.world.inventory.CraftingContainer.class) {
                        f.setAccessible(true);
                        craftSlots = (net.minecraft.world.inventory.CraftingContainer) f.get(craftingMenu);
                        break;
                    }
                }
            } catch (Exception ex) {
                // Fail silently or log
            }
        }

        if (craftSlots == null)
            return;

        Optional<Recipe<Container>> recipeOpt = (Optional) player.level.getRecipeManager()
                .getRecipeFor(RecipeType.CRAFTING, craftSlots, player.level);

        if (recipeOpt.isPresent()) {
            Recipe<?> recipe = recipeOpt.get();

            // Now check permissions
            Scoreboard scoreboard = player.getScoreboard();
            Objective professionObj = scoreboard.getObjective("profession");
            if (professionObj == null)
                return;

            Score professionScore = scoreboard.getOrCreatePlayerScore(player.getScoreboardName(), professionObj);
            int professionId = professionScore.getScore();
            Profession profession = JobsConfig.getProfession(professionId);

            if (profession == null)
                return;

            player.getCapability(TechProgressionCapability.PLAYER_PROGRESS).ifPresent(progress -> {
                if (!RecipeHelper.canCraft(profession, progress, recipe)) {
                    // Block it!
                    resultSlot.set(ItemStack.EMPTY);
                    // We also need to notify the client so it doesn't look like a ghost item
                    player.connection.send(new net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket(
                            craftingMenu.containerId, craftingMenu.incrementStateId(), 0, ItemStack.EMPTY));
                }
            });
        }
    }
}
