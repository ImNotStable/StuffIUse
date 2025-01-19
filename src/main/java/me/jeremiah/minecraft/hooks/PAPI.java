package me.jeremiah.minecraft.hooks;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class PAPI {

  private static boolean enabled = false;

  public static void load() {
    if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI"))
      enabled = true;
  }

  public static @NotNull String setAllPlaceholders(@NotNull String text) {
    return setAllPlaceholders(text, false);
  }

  public static @NotNull String setAllPlaceholders(@Nullable final OfflinePlayer player, @NotNull String text) {
    return setAllPlaceholders(player, text, false);
  }

  public static @NotNull String setAllPlaceholders(@NotNull String text, boolean deep) {
    return setAllPlaceholders(null, text, deep);
  }

  public static @NotNull String setAllPlaceholders(@Nullable final OfflinePlayer player, @NotNull String text, boolean deep) {
    if (!enabled)
      return text;
    text = setPlaceholders(player, text);
    text = setBracketPlaceholders(player, text);
    if (deep && (PlaceholderAPI.getPlaceholderPattern().matcher(text).find() || PlaceholderAPI.getBracketPlaceholderPattern().matcher(text).find()))
      text = setAllPlaceholders(player, text, true);
    return text;
  }

  public static @NotNull String setPlaceholders(@NotNull final String text) {
    return setPlaceholders(null, text);
  }

  public static @NotNull List<@NotNull String> setPlaceholders(@NotNull final List<@NotNull String> text) {
    return setPlaceholders(null, text);
  }

  public static @NotNull String setPlaceholders(@Nullable final OfflinePlayer player, @NotNull final String text) {
    if (!enabled)
      return text;
    return PlaceholderAPI.setPlaceholders(player, text);
  }

  public static @NotNull List<@NotNull String> setPlaceholders(@Nullable final OfflinePlayer player, @NotNull final List<@NotNull String> text) {
    if (!enabled)
      return text;
    return PlaceholderAPI.setPlaceholders(player, text);
  }

  public static @NotNull String setBracketPlaceholders(@NotNull final String text) {
    return setBracketPlaceholders(null, text);
  }

  public static @NotNull String setBracketPlaceholders(@Nullable final OfflinePlayer player, @NotNull final String text) {
    if (!enabled)
      return text;
    return PlaceholderAPI.setBracketPlaceholders(player, text);
  }

  public static @NotNull List<@NotNull String> setBracketPlaceholders(@NotNull final List<@NotNull String> text) {
    return setBracketPlaceholders(null, text);
  }

  public static @NotNull List<@NotNull String> setBracketPlaceholders(@Nullable final OfflinePlayer player, @NotNull final List<@NotNull String> text) {
    if (!enabled)
      return text;
    return PlaceholderAPI.setBracketPlaceholders(player, text);
  }

  public static String setRelationalPlaceholders(@NotNull final Player one, @NotNull final Player two, @NotNull final String text) {
    if (!enabled)
      return text;
    return PlaceholderAPI.setRelationalPlaceholders(one, two, text);
  }

  public static @NotNull List<@NotNull String> setRelationalPlaceholders(@NotNull final Player one, @NotNull final Player two, @NotNull final List<@NotNull String> text) {
    if (!enabled)
      return text;
    return PlaceholderAPI.setRelationalPlaceholders(one, two, text);
  }

}
