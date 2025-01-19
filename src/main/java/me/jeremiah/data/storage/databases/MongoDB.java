package me.jeremiah.data.storage.databases;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoCredential;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.BulkWriteOptions;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.UpdateOneModel;
import com.mongodb.client.model.UpdateOptions;
import me.jeremiah.data.ByteTranslatable;
import me.jeremiah.data.DatabaseInfo;
import org.bson.Document;
import org.bson.UuidRepresentation;
import org.bson.types.Binary;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class MongoDB<T> extends Database<T> {

  private final MongoClient client;
  private final MongoCollection<Document> accounts;

  public MongoDB(@NotNull DatabaseInfo info, @NotNull Class<T> entryClass) {
    super(info, entryClass);

    MongoClientSettings settings = MongoClientSettings.builder()
      .applyConnectionString(new ConnectionString("mongodb://%s/%s".formatted(info.getUrl(), info.getName())))
      .uuidRepresentation(UuidRepresentation.STANDARD)
      .credential(MongoCredential.createCredential(info.getUsername(), info.getName(), info.getPassword().toCharArray()))
      .build();

    client = MongoClients.create(settings);
    accounts = client.getDatabase(info.getName()).getCollection("accounts");
    accounts.createIndex(new Document("entry_id", 1), new IndexOptions().unique(true));

    setup();
  }

  @Override
  protected int lookupEntryCount() {
    return (int) accounts.countDocuments();
  }

  @Override
  protected Map<ByteTranslatable, byte[]> getData() {
    Map<ByteTranslatable, byte[]> data = new HashMap<>();
    for (Document document : accounts.find()) {
      ByteTranslatable entryId = ByteTranslatable.from(document.get("entry_id", Binary.class).getData());
      byte[] entryData = document.get("entry_data", Binary.class).getData();
      data.put(entryId, entryData);
    }
    return data;
  }

  @Override
  protected void saveData(Map<ByteTranslatable, byte[]> data) {
    List<UpdateOneModel<Document>> writeModels = new ArrayList<>();

    for (Map.Entry<ByteTranslatable, byte[]> entry : data.entrySet()) {
      writeModels.add(new UpdateOneModel<>(
        new Document("entry_id", new Binary(entry.getKey().bytes())),
        new Document("entry_data", new Binary(entry.getValue())),
        new UpdateOptions().upsert(true)
      ));
    }

    accounts.bulkWrite(writeModels, new BulkWriteOptions().ordered(false));
  }

  @Override
  public void close() {
    super.close();
    client.close();
  }

}
