package me.jeremiah.minecraft.messaging;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

public final class EmptyMessage extends AbstractMessage<Void> {

  EmptyMessage() {
    super();
  }

  @Override
  @NotNull Void parse(@Nullable final Player player, @NotNull TagResolver @NotNull ... resolvers) {
    return null;
  }

  @Override
  void send(@NotNull Audience targets, @NotNull Void message) {}

  @Override
  public void send(@NotNull Audience target, TagResolver @NotNull ... resolvers) {}

  @Override
  public void send(@NotNull Collection<? extends @NotNull Audience> targets, TagResolver @NotNull ... resolvers) {}

  @Override
  public void send() {}

}
