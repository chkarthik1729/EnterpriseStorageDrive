package crio.vicara.user;

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
public class UserDao {

    MongoCollection<User> userCollection;

    UserDao() {
        var mongoClient = configureAndGetMongoClient();
        var mongoDatabase = mongoClient.getDatabase("rest-server");
        userCollection = mongoDatabase.getCollection("users", User.class);
    }

    public void save(User user) {
        userCollection.insertOne(user);
    }

    public boolean existsEmail(String email) {
        BasicDBObject filter = new BasicDBObject("_id", email);
        return userCollection.find(filter).first() != null;
    }

    public User findByEmail(String email) {
        BasicDBObject filter = new BasicDBObject("_id", email);
        return userCollection.find(filter).first();
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
