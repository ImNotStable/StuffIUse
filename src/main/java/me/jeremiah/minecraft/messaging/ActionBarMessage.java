package me.jeremiah.minecraft.messaging;

import me.jeremiah.minecraft.hooks.PAPI;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class ActionBarMessage extends AbstractMessage<Component> {

  private @NotNull String message;

  ActionBarMessage(@NotNull String message) {
    this.message = message;
  }

  public void message(@NotNull String message) {
    this.message = message;
  }

  public @NotNull String message() {
    return this.message;
  }

  @Override
  @NotNull
  Component parse(@Nullable final Player player, @NotNull final TagResolver @NotNull ... resolvers) {
    return MiniMessage.miniMessage().deserialize(PAPI.setAllPlaceholders(player, message, true), resolvers);
  }

  @Override
  void send(@NotNull final Audience target, @NotNull final Component message) {
    target.sendActionBar(message);
  }

}
