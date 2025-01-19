package me.jeremiah.minecraft.configuration;

import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;

public abstract class AbstractMutableConfiguration extends AbstractConfiguration {

  protected AbstractMutableConfiguration(@NotNull JavaPlugin plugin, @NotNull File configurationFile) {
    super(plugin, configurationFile);
  }

  public abstract void save();

}
