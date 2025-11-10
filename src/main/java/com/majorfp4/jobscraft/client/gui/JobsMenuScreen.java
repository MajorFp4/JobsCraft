package com.majorfp4.jobscraft.client.gui;

import com.majorfp4.jobscraft.config.JobsConfig;
import com.majorfp4.jobscraft.config.Profession;
import com.majorfp4.jobscraft.network.PacketHandler;
import com.majorfp4.jobscraft.network.ServerboundChangeProfessionPacket;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;

import java.util.Collection;

public class JobsMenuScreen extends Screen {

    public JobsMenuScreen() {
        // O título da tela
        super(new TextComponent("Escolha sua Profissão"));
    }

    @Override
    protected void init() {
        // Isso é chamado quando a tela abre
        super.init();

        // 1. Pega todas as profissões
        // Isso funciona porque o cliente TAMBÉM carrega os arquivos .toml
        Collection<Profession> allProfessions = JobsConfig.getAllProfessions();

        int buttonWidth = 150;
        int buttonHeight = 20;
        int startX = (this.width - buttonWidth) / 2; // Centralizado
        int startY = 50; // Começa a 50 pixels do topo

        // 2. Cria um botão para cada profissão
        int i = 0;
        for (Profession prof : allProfessions) {
            int buttonY = startY + (i * (buttonHeight + 5)); // 5 pixels de espaço

            // Pega o nome e o ID da profissão
            Component buttonText = new TextComponent(prof.getName());
            int professionId = prof.getId();

            this.addRenderableWidget(new Button(startX, buttonY, buttonWidth, buttonHeight, buttonText,
                    // 3. Define a ação do clique
                    (button) -> {
                        // Envia o pacote para o servidor com o ID da profissão
                        PacketHandler.INSTANCE.sendToServer(new ServerboundChangeProfessionPacket(professionId));

                        // Fecha a tela
                        this.minecraft.setScreen(null);
                    }
            ));
            i++;
        }
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
        // Desenha o fundo escuro (padrão)
        this.renderBackground(poseStack);

        // Desenha o título da tela
        drawCenteredString(poseStack, this.font, this.title, this.width / 2, 20, 0xFFFFFF); // 0xFFFFFF = Cor Branca

        // Desenha os botões e outros widgets
        super.render(poseStack, mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean isPauseScreen() {
        // Retorne 'false' se você quer que o jogo continue rodando
        return false;
    }
}
