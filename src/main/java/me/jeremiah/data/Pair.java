package me.jeremiah.data;

import java.util.Map;

public record Pair<L, R>(L left, R right) {

  public static <L, R> Pair<L, R> of(L left, R right) {
    return new Pair<>(left, right);
  }

  public static <L, R> Pair<L, R> of(Map.Entry<L, R> entry) {
    return new Pair<>(entry.getKey(), entry.getValue());
  }

  public Pair(Map.Entry<L, R> entry) {
    this(entry.getKey(), entry.getValue());
  }

  public void putInto(Map<L, R> map) {
    map.put(left, right);
  }

}
