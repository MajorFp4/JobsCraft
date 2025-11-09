package com.majorfp4.jobscraft.config;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.majorfp4.jobscraft.util.FileUtils;

import java.io.File;
import java.nio.file.Path;
import java.util.*;

public class JobsConfig {
    private static final Map<String, Profession> PROFESSIONS = new HashMap<>();

    public static void initialize() {
        Path jobsFolder = FileUtils.createJobsFolder();
        loadAllProfessions(jobsFolder);
    }

    private static void loadAllProfessions(Path jobsFolder) {
        File[] files = jobsFolder.toFile().listFiles((dir, name) -> name.endsWith(".toml"));
        if (files == null || files.length == 0) {
            System.out.println("[JobsMod] Nenhum arquivo de profissão encontrado. Criando exemplo...");
            FileUtils.createExampleProfession(jobsFolder);
            return;
        }

        for (File file : files) {
            try (CommentedFileConfig config = CommentedFileConfig.builder(file).autosave().build()) {
                config.load();
                String name = config.get("name");
                List<String> relatedBlocks = config.getOrElse("related_blocks", Collections.emptyList());
                List<String> exclusiveBlocks = config.getOrElse("exclusive_blocks", Collections.emptyList());

                Profession profession = new Profession(name, relatedBlocks, exclusiveBlocks);
                PROFESSIONS.put(name.toLowerCase(Locale.ROOT), profession);

                System.out.println("[JobsMod] Profissão carregada: " + name);
            } catch (Exception e) {
                System.err.println("[JobsMod] Erro ao carregar " + file.getName() + ": " + e.getMessage());
            }
        }
    }

    public static Profession getProfession(String name) {
        return PROFESSIONS.get(name.toLowerCase(Locale.ROOT));
    }

    public static Collection<Profession> getAllProfessions() {
        return PROFESSIONS.values();
    }
}
