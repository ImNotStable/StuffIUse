package me.jeremiah.minecraft.messaging;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public abstract class AbstractMessage<M> {

  private @NotNull Collection<? extends @NotNull Audience> targets = List.of();

  public AbstractMessage<M> toAll() {
    return targets(Bukkit.getOnlinePlayers());
  }

  public AbstractMessage<M> toAllExcept(@NotNull final Audience @NotNull... targets) {
    List<Audience> onlinePlayers = new ArrayList<>(Bukkit.getOnlinePlayers());
    onlinePlayers.removeAll(List.of(targets));
    return targets(onlinePlayers);
  }

  public AbstractMessage<M> targets(@NotNull final Audience @NotNull... targets) {
    return targets(List.of(targets));
  }

  public AbstractMessage<M> targets(@NotNull final Collection<? extends @NotNull Audience> targets) {
    this.targets = List.copyOf(targets);
    return this;
  }

  @NotNull M parse(@NotNull final TagResolver @NotNull ... resolvers) {
    return parse(null, resolvers);
  }

  abstract @NotNull M parse(@Nullable final Player player, @NotNull TagResolver @NotNull... resolvers);

  abstract void send(@NotNull final Audience targets, @NotNull final M message);

  public void send(@NotNull final Audience target, @NotNull final TagResolver @NotNull... resolvers) {
    if (target instanceof Player player)
      send(target, parse(player, resolvers));
    else
      send(target, parse(resolvers));
  }

  public void send(@NotNull final Collection<? extends @NotNull Audience> targets, @NotNull final TagResolver @NotNull... resolvers) {
    targets.forEach(this::send);
  }

  public void send() {
    send(targets);
  }

}
