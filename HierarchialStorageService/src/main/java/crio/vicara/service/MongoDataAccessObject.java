package crio.vicara.service;

import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoCollection;

import com.mongodb.client.model.UpdateOptions;
import crio.vicara.ChildFile;
import crio.vicara.File;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MongoDataAccessObject {

    private final MongoCollection<File> fileCollection;

    MongoDataAccessObject(MongoCollection<File> fileCollection) {
        this.fileCollection = fileCollection;
    }

    String addFile(File file) {
        return Objects.requireNonNull(fileCollection
                .insertOne(file)
                .getInsertedId())
                .asString()
                .getValue();
    }

    File findFile(String id) {
        BasicDBObject query = new BasicDBObject();
        query.put("_id", id);
        File file = fileCollection.find(query).first();

        if (file != null && file.getChildren() == null)
            file.setChildren(new ArrayList<>(0));
        return file;
    }

    void addChildFileToParent(String parentId, ChildFile childFile) {
        BasicDBObject query = new BasicDBObject();
        query.put("_id", parentId);
        BasicDBObject pushData = new BasicDBObject("$push", new BasicDBObject("children", childFile));
        fileCollection.findOneAndUpdate(query, pushData);
    }

    ChildFile findChildInParent(String parentId, String fileName) {
        BasicDBObject idFilter = new BasicDBObject().append("_id", parentId);
        BasicDBObject matchId = new BasicDBObject().append("$match", idFilter);
        BasicDBObject unwindChildren = new BasicDBObject().append("$unwind", "$children");
        BasicDBObject childNameFilter = new BasicDBObject().append("children.fileName", fileName);
        BasicDBObject matchChildName = new BasicDBObject().append("$match", childNameFilter);
        BasicDBObject projections = new BasicDBObject()
                .append("_id", 0)
                .append("fileId", "$children.fileId")
                .append("fileName", "$children.fileName")
                .append("directory", "$children.directory");
        BasicDBObject projectionStage = new BasicDBObject().append("$project", projections);

        List<ChildFile> childFiles = fileCollection.aggregate(
                List.of(matchId, unwindChildren, matchChildName, projectionStage),
                ChildFile.class
        ).into(new ArrayList<>());

        return childFiles.size() > 0 ? childFiles.get(0) : null;
    }

    void updateChildFileNameInParent(String parentId, String oldFileName, String newFileName) {
        BasicDBObject idFilter = new BasicDBObject("_id", parentId);
        BasicDBObject updateOperation = new BasicDBObject("$set",
                new BasicDBObject(
                        "children.$[file].fileName", newFileName
                ));
        UpdateOptions updateArrayFilter = new UpdateOptions().arrayFilters(
                List.of(new BasicDBObject("file.fileName", oldFileName))
        );
        fileCollection.updateOne(idFilter, updateOperation, updateArrayFilter);
    }

    void updateLastModified(String fileId, long lastModifiedTime) {
        BasicDBObject filter = new BasicDBObject("_id", fileId);
        BasicDBObject updateData = new BasicDBObject("$set", new BasicDBObject("lastModifiedTime", lastModifiedTime));
        fileCollection.findOneAndUpdate(filter, updateData);
    }

    void updateParentId(String fileId, String newParentId) {
        BasicDBObject filter = new BasicDBObject("_id", fileId);
        BasicDBObject updateData = new BasicDBObject("$set", new BasicDBObject("parentId", newParentId));
        fileCollection.findOneAndUpdate(filter, updateData);
    }

    void updateFileName(String fileId, String newName) {
        BasicDBObject filter = new BasicDBObject("_id", fileId);
        BasicDBObject updateData = new BasicDBObject("$set", new BasicDBObject("fileName", newName));
        fileCollection.findOneAndUpdate(filter, updateData);
    }

    void delete(String fileId) {
        BasicDBObject filter = new BasicDBObject("_id", fileId);
        fileCollection.deleteOne(filter);
    }

    void deleteChildFromParent(String parentId, String childName) {
        BasicDBObject filter = new BasicDBObject("_id", parentId);
        BasicDBObject deleteData = new BasicDBObject("$pull",
                new BasicDBObject("children",
                        new BasicDBObject("fileName", childName)
                )
        );
        fileCollection.findOneAndUpdate(filter, deleteData);
    }

    void clearAllDocuments() {
        fileCollection.drop();
    }
}
