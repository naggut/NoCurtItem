package com.nocurtitem.config;

import com.nocurtitem.NoCurtItemMod;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class BlockedItemsConfig {

    private static final String CONFIG_FILE = "nocurtitem_blocked.txt";
    private static final Set<ResourceLocation> blockedItems = new HashSet<>();

    public static Path getConfigPath() {
        return net.minecraftforge.fml.loading.FMLPaths.CONFIGDIR.get().resolve(CONFIG_FILE);
    }

    public static Set<ResourceLocation> getBlockedItems() {
        return Collections.unmodifiableSet(blockedItems);
    }

    public static boolean isBlocked(ResourceLocation id) {
        return blockedItems.contains(id);
    }

    public static void add(ResourceLocation id) {
        if (id != null && ForgeRegistries.ITEMS.containsKey(id)) {
            blockedItems.add(id);
            save();
        }
    }

    public static void remove(ResourceLocation id) {
        blockedItems.remove(id);
        save();
    }

    public static void load() {
        blockedItems.clear();
        Path path = getConfigPath();
        if (!Files.exists(path)) {
            return;
        }
        try (Stream<String> lines = Files.lines(path)) {
            lines.map(String::trim)
                    .filter(s -> !s.isEmpty() && !s.startsWith("#"))
                    .map(ResourceLocation::tryParse)
                    .filter(id -> id != null)
                    .forEach(blockedItems::add);
        } catch (IOException e) {
            NoCurtItemMod.LOGGER.warn("Failed to load blocked items config: {}", e.getMessage());
        }
    }

    public static void save() {
        Path path = getConfigPath();
        try {
            Files.createDirectories(path.getParent());
            String content = blockedItems.stream()
                    .sorted((a, b) -> a.toString().compareTo(b.toString()))
                    .map(ResourceLocation::toString)
                    .collect(Collectors.joining("\n"));
            Files.write(path, content.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            NoCurtItemMod.LOGGER.warn("Failed to save blocked items config: {}", e.getMessage());
        }
    }
}
