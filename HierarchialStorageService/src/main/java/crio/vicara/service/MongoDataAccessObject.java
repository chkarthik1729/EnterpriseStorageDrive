package crio.vicara.service;

import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoCollection;

import crio.vicara.ChildFile;
import crio.vicara.File;

public class MongoDataAccessObject {

    private final MongoCollection<File> fileCollection;

    MongoDataAccessObject(MongoCollection<File> fileCollection) {
        this.fileCollection = fileCollection;
    }

    String addToFileCollection(File file) {
        return fileCollection
                .insertOne(file)
                .getInsertedId()
                .asString()
                .getValue();
    }

    File findFile(String id) {
        BasicDBObject query = new BasicDBObject();
        query.put("_id", id);
        return fileCollection.find(query).first();
    }

    void addChildFileToParent(String parentId, ChildFile childFile) {
        BasicDBObject query = new BasicDBObject();
        query.put("_id", parentId);
        BasicDBObject pushData = new BasicDBObject("$push", new BasicDBObject("children", childFile));
        fileCollection.findOneAndUpdate(query, pushData);
    }

    ChildFile findChildInParent(String parentId, String fileName) {
        BasicDBObject filter = new BasicDBObject();
        filter.put("_id", parentId);
        filter.put("children.fileName", fileName);
        return fileCollection.find(filter, ChildFile.class).first();
    }

    void updateChildFileNameInParent(String parentId, String oldFileName, String newFileName) {
        BasicDBObject filter = new BasicDBObject();
        filter.put("_id", parentId);
        filter.put("children.fileName", oldFileName);
        BasicDBObject updateData = new BasicDBObject("$set", new BasicDBObject("children.fileName", newFileName));
        fileCollection.findOneAndUpdate(filter, updateData);
    }

    void updateLastModified(String fileId, long lastModifiedTime) {
        BasicDBObject filter = new BasicDBObject();
        filter.put("_id", fileId);
        BasicDBObject updateData = new BasicDBObject("$set", new BasicDBObject("lastModifiedTime", lastModifiedTime));
        fileCollection.findOneAndUpdate(filter, updateData);
    }

    void updateFileName(String fileId, String newName) {
        BasicDBObject filter = new BasicDBObject();
        filter.put("_id", fileId);
        BasicDBObject updateData = new BasicDBObject("$set", new BasicDBObject("fileName", newName));
        fileCollection.findOneAndUpdate(filter, updateData);
    }

    void delete(String fileId) {
        BasicDBObject filter = new BasicDBObject();
        filter.put("_id", fileId);
        fileCollection.deleteOne(filter);
    }

    void deleteChildFromParent(String parentId, String childName) {
        BasicDBObject filter = new BasicDBObject();
        filter.put("_id", parentId);
        BasicDBObject deleteData = new BasicDBObject("$pull", new BasicDBObject("children.fileName", childName));
        fileCollection.findOneAndUpdate(filter, deleteData);
    }
}
