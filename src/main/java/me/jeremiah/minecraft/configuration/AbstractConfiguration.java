package me.jeremiah.minecraft.configuration;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;

public abstract class AbstractConfiguration {

  private final @NotNull JavaPlugin plugin;
  private final @NotNull File configurationFile;

  protected AbstractConfiguration(@NotNull JavaPlugin plugin, @NotNull File configurationFile) {
    this.plugin = plugin;
    this.configurationFile = configurationFile;
    loadDefaultResource();
  }

  public @NotNull JavaPlugin getPlugin() {
    return plugin;
  }

  public @NotNull File getConfigurationFile() {
    return configurationFile;
  }

  public void loadDefaultResource() {
    if (configurationFile.exists())
      return;
    if (!configurationFile.mkdirs()) {
      plugin.getLogger().severe("Failed to create configuration directory.");
      return;
    }
    String fileName = configurationFile.getName();
    if (plugin.getResource(fileName) == null) {
      plugin.getLogger().severe("Default configuration file for \"%s\" not found.".formatted(fileName));
      return;
    }
    plugin.saveResource(fileName, false);
  }

  public @NotNull YamlConfiguration loadConfiguration() {
    return YamlConfiguration.loadConfiguration(configurationFile);
  }

  public abstract void load();

}
