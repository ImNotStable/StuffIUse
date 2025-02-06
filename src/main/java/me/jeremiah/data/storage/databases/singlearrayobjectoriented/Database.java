package me.jeremiah.data.storage.databases.singlearrayobjectoriented;

import me.jeremiah.data.storage.DatabaseInfo;
import me.jeremiah.data.storage.databases.AbstractDatabase;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public abstract class Database<T extends Serializable> extends AbstractDatabase<T, byte[]> {

  protected Database(@NotNull DatabaseInfo info, @NotNull Class<T> entryClass) {
    super(info, entryClass);
  }

  protected abstract int lookupEntryCount();

  protected abstract byte[] getData();

  @SuppressWarnings("unchecked")
  protected void loadData() {
    byte[] data = getData();
    if (data == null || data.length == 0) return;

    try (ObjectInputStream inputStream = new ObjectInputStream(new ByteArrayInputStream(data))) {
      while (true) {
        T entry = (T) inputStream.readObject();
        add(entry);
      }
    } catch (EOFException ignored) {
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  protected abstract void saveData(byte[] data);

  protected void save() {
    try (ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
         ObjectOutputStream objectStream = new ObjectOutputStream(byteStream)) {
      for (T entry : entries)
        objectStream.writeObject(entry);
      objectStream.flush();
      saveData(byteStream.toByteArray());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

}
