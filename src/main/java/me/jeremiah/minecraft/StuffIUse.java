package me.jeremiah.minecraft;

import com.google.common.base.Preconditions;
import me.jeremiah.minecraft.hooks.PAPI;
import org.bukkit.plugin.java.JavaPlugin;

public final class StuffIUse {

  private StuffIUse() {
    throw new IllegalStateException("Utility class");
  }

  private static JavaPlugin plugin;

  public static void load(JavaPlugin plugin) {
    Preconditions.checkState(StuffIUse.plugin == null, "Plugin is already loaded");
    StuffIUse.plugin = plugin;
    PAPI.load();
  }

  public static JavaPlugin getPlugin() {
    Preconditions.checkState(StuffIUse.plugin != null, "Plugin is not loaded");
    return StuffIUse.plugin;
  }

}
