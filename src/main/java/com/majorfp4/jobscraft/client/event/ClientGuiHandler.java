package com.majorfp4.jobscraft.client.event;

import com.majorfp4.jobscraft.client.gui.JobsMenuScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.TextComponent;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class ClientGuiHandler {

    @SubscribeEvent
    public void onScreenInit(ScreenEvent.InitScreenEvent.Post event) {
        // Checa se a tela que acabou de abrir é o inventário do jogador
        if (event.getScreen() instanceof InventoryScreen) {

            InventoryScreen inventoryScreen = (InventoryScreen) event.getScreen();

            // Posição do botão (canto superior esquerdo da GUI do inventário)
            int x = inventoryScreen.getGuiLeft() + -390; // Posição X
            int y = inventoryScreen.getGuiTop() + +310;  // Posição Y
            int width = 40;
            int height = 20;

            // Cria o botão "Jobs"
            Button jobsButton = new Button(x, y, width, height, new TextComponent("Jobs"),
                    (button) -> {
                        // Ação: Abrir nossa nova tela de menu
                        Minecraft.getInstance().setScreen(new JobsMenuScreen());
                    }
            );

            event.addListener(jobsButton);
        }
    }
}