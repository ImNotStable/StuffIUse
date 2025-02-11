package me.jeremiah.minecraft.messaging;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;

public final class MessageGroup extends AbstractMessage<Void> {

  private final List<? extends AbstractMessage<?>> messages;

  MessageGroup(List<? extends AbstractMessage<?>> messages) {
    this.messages = messages;
  }

  @Override
  public AbstractMessage<Void> targets(@NotNull Collection<? extends @NotNull Audience> targets) {
    messages.forEach(message -> message.targets(targets));
    return this;
  }

  @Override
  @NotNull
  Void parse(@Nullable final Player player, @NotNull TagResolver @NotNull ... resolvers) {
    return null;
  }

  @Override
  void send(@NotNull final Audience target, @NotNull Void messages) {
    this.messages.forEach(message -> message.send(target));
  }

  @Override
  public void send(@NotNull final Audience target, final TagResolver @NotNull ... resolvers) {
    messages.forEach(message -> message.send(target, resolvers));
  }

  @Override
  public void send(@NotNull final Collection<? extends @NotNull Audience> targets, final TagResolver @NotNull ... resolvers) {
    messages.forEach(message -> message.send(targets, resolvers));
  }

  @Override
  public void send() {
    messages.forEach(AbstractMessage::send);
  }

}
