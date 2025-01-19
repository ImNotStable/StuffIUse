package me.jeremiah.minecraft.messaging;

import me.jeremiah.minecraft.hooks.PAPI;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.title.Title;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class TitleMessage extends AbstractMessage<Title> {

  private @NotNull String title;
  private @NotNull String subtitle;
  private @NotNull Title.@NotNull Times times;

  TitleMessage(@NotNull String title, @NotNull String subtitle, @NotNull Title.@NotNull Times times) {
    this.title = title;
    this.subtitle = subtitle;
    this.times = times;
  }

  public void title(@NotNull String title) {
    this.title = title;
  }

  public @NotNull String title() {
    return title;
  }

  public void subtitle(@NotNull String subtitle) {
    this.subtitle = subtitle;
  }

  public @NotNull String subtitle() {
    return subtitle;
  }

  public void times(@NotNull Title.@NotNull Times times) {
    this.times = times;
  }

  public @NotNull Title.Times times() {
    return times;
  }

  @NotNull Title parse(@Nullable final Player player, @NotNull final TagResolver @NotNull ... resolvers) {
    Component title = MiniMessage.miniMessage().deserialize(PAPI.setAllPlaceholders(player, this.title, true), resolvers);
    Component subtitle = MiniMessage.miniMessage().deserialize(PAPI.setAllPlaceholders(player, this.subtitle, true), resolvers);
    return Title.title(title, subtitle, times);
  }

  @Override
  void send(@NotNull final Audience target, @NotNull final Title title) {
    target.showTitle(title);
  }

}

