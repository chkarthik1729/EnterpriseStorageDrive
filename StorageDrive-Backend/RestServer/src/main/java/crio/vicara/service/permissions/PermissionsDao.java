package crio.vicara.service.permissions;

import com.mongodb.BasicDBObject;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.ReplaceOptions;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

@Component
public class PermissionsDao {

    private final MongoCollection<FilePermissions> permissionsCollection;
    private final MongoCollection<SharedFiles> sharedWithMeCollection;

    public PermissionsDao() {
        var mongoClient = configureAndGetMongoClient();
        var mongoDatabase = mongoClient.getDatabase("rest-server");
        permissionsCollection = mongoDatabase.getCollection("filePermissions", FilePermissions.class);
        sharedWithMeCollection = mongoDatabase.getCollection("sharedWithMe", SharedFiles.class);
    }

    public void savePermissions(FilePermissions permissions) {
        permissionsCollection.replaceOne(
                new BasicDBObject("_id", permissions.getFileId()),
                permissions,
                new ReplaceOptions().upsert(true)
        );
    }

    public FilePermissions getPermissions(String fileId) {
        return permissionsCollection.find(new BasicDBObject("_id", fileId)).first();
    }

    public SharedFiles getFilesSharedWith(String userEmail) {
        BasicDBObject query = new BasicDBObject()
                .append("_id", userEmail);
        return sharedWithMeCollection.find(query).first();
    }

    public void removeFileSharedWithMe(String fileId, String userEmail) {
        if (getFilesSharedWith(userEmail) == null) return;

        BasicDBObject query = new BasicDBObject()
                .append("_id", userEmail);
        BasicDBObject pullData = new BasicDBObject("$pull", new BasicDBObject("filesSharedWithMe", fileId));
        sharedWithMeCollection.findOneAndUpdate(query, pullData);
    }

    public void shareFileWith(String fileId, String userEmail) {
        if (getFilesSharedWith(userEmail) == null) {
            SharedFiles sharedFiles = new SharedFiles();
            sharedFiles.setUserEmail(userEmail);
            sharedWithMeCollection.insertOne(sharedFiles);
        }

        BasicDBObject query = new BasicDBObject()
                .append("_id", userEmail);
        BasicDBObject pushData = new BasicDBObject("$push", new BasicDBObject("filesSharedWithMe", fileId));
        sharedWithMeCollection.findOneAndUpdate(query, pushData);
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
