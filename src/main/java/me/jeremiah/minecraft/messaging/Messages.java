package me.jeremiah.minecraft.messaging;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.title.Title;
import net.kyori.adventure.util.Ticks;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public final class Messages {

  private Messages() {
    throw new UnsupportedOperationException();
  }

  private static final EmptyMessage EMPTY_MESSAGE = new EmptyMessage();
  private static final String DEFAULT_MESSAGE = "<red>Undefined Message</red>";
  private static final String DEFAULT_SOUND = "minecraft:block.note_block.pling";

  public static @NotNull AbstractMessage<?> fromYaml(@NotNull ConfigurationSection section) {
    String type = section.getString("type", "empty");

    return switch (type.toLowerCase()) {
      case "group" -> {
        ConfigurationSection messagesSection = section.getConfigurationSection("messages");

        if (messagesSection == null)
          yield Messages.group();

        Set<String> messagePaths = messagesSection.getKeys(false);

        List<? extends AbstractMessage<?>> messages = messagePaths.stream()
          .map(messagesSection::getConfigurationSection).filter(Objects::nonNull)
          .map(Messages::fromYaml)
          .toList();

        yield Messages.group(messages);
      }
      case "chat" -> {
        if (section.contains("message")) {
          String message = section.getString("message");
          yield Messages.chat(message);
        }
        if (section.contains("messages")) {
          List<String> messages = section.getStringList("messages").stream().map(String::valueOf).toList();
          yield Messages.chat(messages);
        }
        yield Messages.chat(DEFAULT_MESSAGE);
      }
      case "actionbar" -> {
        String message = section.getString("message", DEFAULT_MESSAGE);

        yield Messages.actionBar(message);
      }
      case "title" -> {
        String title = section.getString("title", DEFAULT_MESSAGE);
        String subtitle = section.getString("subtitle", DEFAULT_MESSAGE);
        long fadeIn = section.getLong("fade-in", 10);
        long stay = section.getLong("stay", 70);
        long fadeOut = section.getLong("fade-out", 20);

        Title.Times times = Title.Times.times(
          Ticks.duration(fadeIn),
          Ticks.duration(stay),
          Ticks.duration(fadeOut)
        );

        yield Messages.title(title, subtitle, times);
      }
      case "sound" -> {
        String sound = section.getString("sound", DEFAULT_SOUND);
        float volume = (float) section.getDouble("volume", 1.0);
        float pitch = (float) section.getDouble("pitch", 1.0);

        yield Messages.sound(sound, volume, pitch);
      }
      case "empty" -> Messages.empty();
      default -> throw new IllegalArgumentException("Unknown message type: " + type);
    };
  }

  public static EmptyMessage empty() {
    return EMPTY_MESSAGE;
  }

  public static MessageGroup group(AbstractMessage<?>... messages) {
    return group(List.of(messages));
  }

  public static MessageGroup group(List<? extends AbstractMessage<?>> messages) {
    return new MessageGroup(messages);
  }

  public static ChatMessage chat(Component... messages) {
    String[] serialized = Arrays.stream(messages).map(MiniMessage.miniMessage()::serialize).toArray(String[]::new);
    return chat(serialized);
  }

  public static ChatMessage chat(String... messages) {
    return new ChatMessage(messages);
  }

  public static ChatMessage chat(List<String> messages) {
    return new ChatMessage(messages);
  }

  public static ActionBarMessage actionBar(Component message) {
    return new ActionBarMessage(MiniMessage.miniMessage().serialize(message));
  }

  public static ActionBarMessage actionBar(String message) {
    return new ActionBarMessage(message);
  }

  public static TitleMessage title(Title title) {
    return title(title.title(), title.subtitle(), title.times());
  }

  public static TitleMessage title(Component title) {
    return title(MiniMessage.miniMessage().serialize(title));
  }

  public static TitleMessage title(Component title, Component subtitle) {
    return title(MiniMessage.miniMessage().serialize(title), MiniMessage.miniMessage().serialize(subtitle));
  }

  public static TitleMessage title(Component title, Component subtitle, Title.Times times) {
    return title(MiniMessage.miniMessage().serialize(title), MiniMessage.miniMessage().serialize(subtitle), times);
  }

  public static TitleMessage title(String title) {
    return title(title, "");
  }

  public static TitleMessage title(String title, String subtitle) {
    return title(title, subtitle, Title.DEFAULT_TIMES);
  }

  public static TitleMessage title(String title, String subtitle, Title.Times times) {
    return new TitleMessage(title, subtitle, times);
  }

  public static SoundMessage sound(String sound) {
    return sound(sound, Sound.Source.MASTER);
  }

  public static SoundMessage sound(String sound, Sound.Source source) {
    return sound(sound, source, 1.0f, 1.0f);
  }

  public static SoundMessage sound(String sound, float volume, float pitch) {
    return sound(sound, Sound.Source.MASTER, volume, pitch);
  }

  @SuppressWarnings("PatternValidation")
  public static SoundMessage sound(String sound, Sound.Source source, float volume, float pitch) {
    return sound(Sound.sound(Key.key(sound), source, volume, pitch));
  }

  public static SoundMessage sound(Sound sound) {
    return new SoundMessage(sound.name(), sound.source(), sound.volume(), sound.pitch());
  }

}
