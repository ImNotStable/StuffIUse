package me.jeremiah.minecraft.messaging;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class SoundMessage extends AbstractMessage<Sound> {

  private @NotNull Key sound;
  private @NotNull Sound.@NotNull Source source;
  private float volume;
  private float pitch;

  SoundMessage(@NotNull Key sound, Sound.@NotNull Source source, float volume, float pitch) {
    this.sound = sound;
    this.source = source;
    this.volume = volume;
    this.pitch = pitch;
  }

  public void sound(@NotNull Key sound) {
    this.sound = sound;
  }

  public @NotNull Key sound() {
    return sound;
  }

  public void source(Sound.@NotNull Source source) {
    this.source = source;
  }

  public Sound.@NotNull Source source() {
    return source;
  }

  public void volume(float volume) {
    this.volume = volume;
  }

  public float volume() {
    return volume;
  }

  public void pitch(float pitch) {
    this.pitch = pitch;
  }

  public float pitch() {
    return pitch;
  }

  @Override
  @NotNull
  Sound parse(@Nullable final Player player, @NotNull final TagResolver @NotNull ... resolvers) {
    return Sound.sound(sound, source, volume, pitch);
  }

  @Override
  void send(@NotNull final Audience target, @NotNull final Sound message) {
    target.playSound(message);
  }

}
