package me.jeremiah.minecraft.configuration;

import me.jeremiah.minecraft.messaging.AbstractMessage;
import me.jeremiah.minecraft.messaging.Messages;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class MessagesConfiguration extends AbstractConfiguration implements Locale {

  private final Collection<String> messageCollection;
  private final Map<String, AbstractMessage<?>> messages;

  protected MessagesConfiguration(Collection<String> messageCollection, @NotNull JavaPlugin plugin, @NotNull File configurationFile) {
    super(plugin, configurationFile);
    this.messageCollection = messageCollection;
    this.messages = new HashMap<>();
  }

  @Override
  public void load() {
    messages.clear();
    YamlConfiguration configuration = loadConfiguration();
    messageCollection.stream()
      .map(configuration::getConfigurationSection)
      .filter(Objects::nonNull)
      .forEach(section -> messages.put(section.getName(), Messages.fromYaml(section)));
  }

  @Override
  public @NotNull AbstractMessage<?> getMessage(@NotNull String key) {
    return messages.get(key);
  }

}
