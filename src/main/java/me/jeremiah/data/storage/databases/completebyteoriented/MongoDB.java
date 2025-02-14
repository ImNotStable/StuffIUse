package me.jeremiah.data.storage.databases.completebyteoriented;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoCredential;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.BulkWriteOptions;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.InsertOneModel;
import me.jeremiah.data.ByteTranslatable;
import me.jeremiah.data.storage.DatabaseInfo;
import org.bson.Document;
import org.bson.UuidRepresentation;
import org.bson.types.Binary;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

public final class MongoDB<ENTRY> extends Database<ENTRY> {

  private final MongoClient client;
  private final MongoCollection<Document> accounts;

  public MongoDB(@NotNull DatabaseInfo info, @NotNull Class<ENTRY> entryClass) {
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
  protected Collection<ByteTranslatable> getData() {
    Collection<ByteTranslatable> data = new HashSet<>();
    for (Document document : accounts.find()) {
      ByteTranslatable entry = ByteTranslatable.fromByteArray(document.get("entry", Binary.class).getData());
      data.add(entry);
    }
    return data;
  }

  @Override
  protected void saveData(Collection<ByteTranslatable> data) {
    accounts.deleteMany(new Document());

    List<InsertOneModel<Document>> writeModels = new ArrayList<>();

    for (ByteTranslatable entry : data) {
      writeModels.add(new InsertOneModel<>(
        new Document("entry", new Binary(entry.bytes()))
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
