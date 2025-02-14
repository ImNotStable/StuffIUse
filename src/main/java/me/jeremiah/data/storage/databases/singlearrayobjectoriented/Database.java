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

public abstract class Database<ENTRY extends Serializable> extends AbstractDatabase<ENTRY, byte[]> {

  protected Database(@NotNull DatabaseInfo info, @NotNull Class<ENTRY> entryClass) {
    super(info, entryClass);
  }

  @Override
  @SuppressWarnings("unchecked")
  protected void loadData() {
    byte[] data = getData();
    if (data == null || data.length == 0) return;

    try (ObjectInputStream inputStream = new ObjectInputStream(new ByteArrayInputStream(data))) {
      while (true) {
        ENTRY entry = (ENTRY) inputStream.readObject();
        add(entry);
      }
    } catch (EOFException ignored) {
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  protected void save() {
    try (ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
         ObjectOutputStream objectStream = new ObjectOutputStream(byteStream)) {
      for (ENTRY entry : entries)
        objectStream.writeObject(entry);
      objectStream.flush();
      saveData(byteStream.toByteArray());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

}
