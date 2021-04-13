package crio.vicara.service.favourites;

import com.mongodb.BasicDBObject;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.ReplaceOptions;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.springframework.stereotype.Component;

import java.util.List;

import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

@Component
public class Favourites {

    MongoCollection<UserFavourites> mongoCollection;

    Favourites() {
        var mongoClient = configureAndGetMongoClient();
        var mongoDatabase = mongoClient.getDatabase("rest-server");
        mongoCollection = mongoDatabase.getCollection("user-favourites", UserFavourites.class);
    }

    public void addFavourite(String fileId, String userEmail) {
        var userFavourites = mongoCollection.find(new BasicDBObject("_id", userEmail)).first();

        if (userFavourites == null)
            userFavourites = new UserFavourites(userEmail, List.of(fileId));
        else {
            List<String> favouriteFiles = userFavourites.getFileIds() == null ? List.of() : userFavourites.getFileIds();
            favouriteFiles.add(fileId);
            userFavourites.setFileIds(favouriteFiles);
        }

        mongoCollection.replaceOne(
                new BasicDBObject("_id", userEmail),
                userFavourites,
                new ReplaceOptions().upsert(true)
        );
    }

    public List<String> getFavourites(String userEmail) {
        var userFavourites = mongoCollection.find(new BasicDBObject("_id", userEmail)).first();
        if (userFavourites == null) return List.of();
        return userFavourites.getFileIds() == null ? List.of() : userFavourites.getFileIds();
    }

    public boolean isFavourite(String fileId, String userEmail) {
        var userFavourites = mongoCollection.find(new BasicDBObject("_id", userEmail)).first();
        if (userFavourites == null) return false;
        if (userFavourites.getFileIds() == null) return false;
        return userFavourites.getFileIds().contains(fileId);
    }

    public void removeFavourite(String fileId, String userEmail) {
        var userFavourites = mongoCollection.find(new BasicDBObject("_id", userEmail)).first();
        if (userFavourites == null) return;
        if (userFavourites.getFileIds() == null) return;
        userFavourites.getFileIds().remove(fileId);

        mongoCollection.replaceOne(
                new BasicDBObject("_id", userEmail),
                userFavourites,
                new ReplaceOptions().upsert(true)
        );
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

