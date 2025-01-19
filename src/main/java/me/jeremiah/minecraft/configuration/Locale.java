package me.jeremiah.minecraft.configuration;

import me.jeremiah.minecraft.messaging.AbstractMessage;
import me.jeremiah.minecraft.messaging.Messages;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.jetbrains.annotations.NotNull;

public interface Locale {

  void load();

  @NotNull AbstractMessage<?> getMessage(@NotNull String key);

  default void sendParsedMessage(@NotNull Audience audience, @NotNull String @NotNull ... messages) {
    sendParsedMessage(audience, Messages.chat(messages));
  }

  default void sendParsedMessage(@NotNull Audience audience, @NotNull String key, @NotNull String @NotNull ... tags) {
    sendParsedMessage(audience, getMessage(key), tags);
  }

  default void sendParsedMessage(@NotNull Audience audience, @NotNull AbstractMessage<?> message, @NotNull String @NotNull ... tags) {
    if (tags.length == 0) {
      message.send(audience);
      return;
    }

    if (tags.length % 2 != 0)
      throw new IllegalArgumentException("Tags must be in pairs.");

    TagResolver[] tagResolvers = new TagResolver[tags.length / 2];
    for (int i = 0; i < tags.length; i++)
      tagResolvers[i / 2] = TagResolver.resolver(tags[i], Tag.selfClosingInserting(Component.text(tags[++i])));
    message.send(audience, tagResolvers);
  }

}
