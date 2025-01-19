package me.jeremiah.data.storage.databases;

import me.jeremiah.data.ByteTranslatable;
import me.jeremiah.data.DatabaseInfo;
import org.jetbrains.annotations.NotNull;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;

import java.util.HashMap;
import java.util.Map;

public final class Redis<T> extends Database<T> {

  private final Jedis jedis;

  public Redis(@NotNull DatabaseInfo info, @NotNull Class<T> entryClass) {
    super(info, entryClass);
    jedis = new Jedis(info.getUrl());
  }

  @Override
  protected int lookupEntryCount() {
    return jedis.keys("*").size();
  }

  @Override
  protected Map<ByteTranslatable, byte[]> getData() {
    Map<ByteTranslatable, byte[]> data = new HashMap<>();

    for (String key : jedis.keys("*")) {
      final ByteTranslatable entryId = ByteTranslatable.from(key);
      final byte[] entryData = jedis.get(key).getBytes();
      data.put(entryId, entryData);
    }

    return data;
  }

  @Override
  protected void saveData(Map<ByteTranslatable, byte[]> data) {
    try (Pipeline pipeline = jedis.pipelined()) {
      for (Map.Entry<ByteTranslatable, byte[]> entry : data.entrySet())
        pipeline.set(entry.getKey().bytes(), entry.getValue());
      pipeline.sync();
    }
  }

  @Override
  public void close() {
    jedis.close();
  }

}
