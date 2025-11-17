package com.majorfp4.jobscraft.client.event;

import com.majorfp4.jobscraft.JobsCraft;
import com.majorfp4.jobscraft.client.gui.JobsMenuScreen;
import com.majorfp4.jobscraft.client.jei.JobsCraftJEIPlugin;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.TextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.event.TagsUpdatedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = JobsCraft.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ClientGuiHandler {

    @SubscribeEvent
    public void onScreenInit(ScreenEvent.InitScreenEvent.Post event) {
        if (event.getScreen() instanceof InventoryScreen) {

            InventoryScreen inventoryScreen = (InventoryScreen) event.getScreen();

            int x = inventoryScreen.getGuiLeft() + -390;
            int y = inventoryScreen.getGuiTop() + +270;
            int width = 40;
            int height = 20;

            Button jobsButton = new Button(x, y, width, height, new TextComponent("Jobs"),
                    (button) -> {
                        Minecraft.getInstance().setScreen(new JobsMenuScreen());
                    }
            );

            event.addListener(jobsButton);
        }
    }

    @SubscribeEvent
    public void onTagsUpdated(TagsUpdatedEvent event) {
        System.out.println("[JobsCraft] Tags do cliente sincronizadas. Atualizando filtro do JEI.");

        JobsCraftJEIPlugin.refreshJEIFilter();
    }
}