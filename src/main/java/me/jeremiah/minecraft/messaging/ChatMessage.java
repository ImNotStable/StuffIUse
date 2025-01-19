package me.jeremiah.minecraft.messaging;

import me.jeremiah.minecraft.hooks.PAPI;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;

public final class ChatMessage extends AbstractMessage<List<Component>> {

  private final @NotNull List<@NotNull String> messages;

  ChatMessage(String... messages) {
    this.messages = Arrays.asList(messages);
  }

  ChatMessage(@NotNull List<@NotNull String> messages) {
    this.messages = messages;
  }

  @Override
  @NotNull
  List<Component> parse(@Nullable final Player player, @NotNull final TagResolver @NotNull ... resolvers) {
    return messages.stream()
      .map(message -> PAPI.setAllPlaceholders(player, message, true))
      .map(message -> MiniMessage.miniMessage().deserialize(message, resolvers))
      .toList();
  }

  @Override
  void send(@NotNull final Audience target, @NotNull final List<Component> messages) {
    messages.forEach(target::sendMessage);
  }

}
