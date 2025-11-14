package com.majorfp4.jobscraft.config;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.majorfp4.jobscraft.util.FileUtils;

import java.io.File;
import java.nio.file.Path;
import java.util.*;

public class JobsConfig {

    private static final Map<Integer, Profession> PROFESSIONS_BY_ID = new HashMap<>();

    public static void initialize() {
        Path jobsFolder = FileUtils.createJobsFolder();
        loadAllProfessions(jobsFolder);
    }

    private static void loadAllProfessions(Path jobsFolder) {
        PROFESSIONS_BY_ID.clear();
        File[] files = jobsFolder.toFile().listFiles((dir, name) -> name.endsWith(".toml"));
        if (files == null || files.length == 0) {
            System.out.println("[JobsMod] Nenhum arquivo de profissão encontrado. Criando exemplo...");
            FileUtils.createExampleProfession(jobsFolder);
            loadAllProfessions(jobsFolder);
            return;
        }

        for (File file : files) {
            try (CommentedFileConfig config = CommentedFileConfig.builder(file).autosave().build()) {
                config.load();

                int id = config.get("iD");
                String name = config.get("name");
                List<String> relatedBlocks = config.getOrElse("related_blocks", Collections.emptyList());
                List<String> exclusiveBlocks = config.getOrElse("exclusive_blocks", Collections.emptyList());
                List<String> exclusiveItems = config.getOrElse("exclusive_items", Collections.emptyList());

                String technicalMod = config.getOrElse("technical_mod", "none");

                List<String> baseItems = config.getOrElse("base_items", Collections.emptyList());

                Profession profession = new Profession(id, name, relatedBlocks, exclusiveBlocks, exclusiveItems,
                        technicalMod, baseItems);

                PROFESSIONS_BY_ID.put(profession.getId(), profession);

                System.out.println("[JobsMod] Profissão carregada: " + name + " (ID: " + id + ")");
            } catch (Exception e) {
                System.err.println("[JobsMod] Erro ao carregar " + file.getName() + ": " + e.getMessage());
            }
        }
    }

    public static Profession getProfession(int id) {
        return PROFESSIONS_BY_ID.get(id);
    }

    public static Collection<Profession> getAllProfessions() {
        return PROFESSIONS_BY_ID.values();
    }
}