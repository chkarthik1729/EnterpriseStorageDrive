package crio.vicara.service.permission;

import com.mongodb.BasicDBObject;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.springframework.stereotype.Component;

import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

@Component
public class PermissionsDao {

    private final MongoCollection<FilePermissions> mongoCollection;

    public PermissionsDao() {
        var mongoClient = configureAndGetMongoClient();
        var mongoDatabase = mongoClient.getDatabase("rest-server");
        mongoCollection = mongoDatabase.getCollection("filePermissions", FilePermissions.class);
    }

    public void savePermissions(FilePermissions permissions) {
        mongoCollection.insertOne(permissions);
    }

    public FilePermissions getPermissions(String fileId) {
        return mongoCollection.find(new BasicDBObject("_id", fileId)).first();
    }

    private static MongoClient configureAndGetMongoClient() {
        var connectionString = new ConnectionString("mongodb://localhost");
        var pojoCodecRegistry = fromProviders(PojoCodecProvider.builder().automatic(true).build());
        var codecRegistry = fromRegistries(MongoClientSettings.getDefaultCodecRegistry(), pojoCodecRegistry);
        var clientSettings = MongoClientSettings.builder()
                .applyConnectionString(connectionString)
                .codecRegistry(codecRegistry)
                .build();
        return MongoClients.create(clientSettings);
    }
}
