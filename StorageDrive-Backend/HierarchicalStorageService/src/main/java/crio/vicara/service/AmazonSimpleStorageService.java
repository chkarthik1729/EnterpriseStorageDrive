package crio.vicara.service;

import com.amazonaws.HttpMethod;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.ObjectMetadata;
import crio.vicara.FlatStorageSystem;
import crio.vicara.StorageServiceDetails;

import java.io.InputStream;
import java.net.URL;
import java.util.Date;

public class AmazonSimpleStorageService implements FlatStorageSystem {

    private static final AmazonSimpleStorageService instance = new AmazonSimpleStorageService();

    private static StorageServiceDetails storageProviderDetails;
    private static AmazonS3 s3Client;
    private static final String bucketName = "crio-do-vicara-t7";

    private AmazonSimpleStorageService() {
        s3Client = AmazonS3ClientBuilder
                .standard()
                .withCredentials(DefaultAWSCredentialsProviderChain.getInstance())
                .withRegion("ap-south-1")
                .build();

        storageProviderDetails = StorageServiceDetails
                .with()
                .name("Amazon Simple Storage Service")
                .hyphenatedName("amazon-s3")
                .websiteUrl("https://aws.amazon.com/s3/")
                .logoUrl("https://d1.awsstatic.com/icons/jp/console_s3_icon.64795d08c5e23e92c12fe08c2dd5bd99255af047.png");
    }

    public static AmazonSimpleStorageService getInstance() {
        return instance;
    }

    @Override
    public StorageServiceDetails getStorageProviderDetails() {
        return storageProviderDetails;
    }

    @Override
    public void putObject(String key, InputStream inputStream, long contentLength) {
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(contentLength);
        s3Client.putObject(bucketName, key, inputStream, metadata);
    }

    @Override
    public void putObject(String key, InputStream inputStream) {
        s3Client.putObject(bucketName, key, inputStream, new ObjectMetadata());
    }

    @Override
    public void putObject(String key, String content) {
        s3Client.putObject(bucketName, key, content);
    }

    @Override
    public void deleteObject(String key) {
        s3Client.deleteObject(bucketName, key);
    }

    @Override
    public InputStream getObject(String key) {
        return s3Client.getObject(bucketName, key).getObjectContent();
    }

    @Override
    public URL getObjectURL(String key, long urlExpirySeconds) {
        long currTime = new Date().getTime();
        Date expiresAt = new Date(currTime + urlExpirySeconds * 1000);
        GeneratePresignedUrlRequest generatePresignedUrlRequest = new GeneratePresignedUrlRequest(bucketName, key)
                .withMethod(HttpMethod.GET)
                .withExpiration(expiresAt);
        return s3Client.generatePresignedUrl(generatePresignedUrlRequest);
    }

    @Override
    public long getLength(String fileId) {
        return s3Client.getObjectMetadata(bucketName, fileId).getContentLength();
    }

    @Override
    public boolean exists(String fileId) {
        return s3Client.doesObjectExist(bucketName, fileId);
    }

    @Override
    public void clearAll() {
        ObjectListing objectListing = s3Client.listObjects(bucketName);
        while (objectListing.isTruncated()) {
            objectListing
                    .getObjectSummaries()
                    .forEach(
                            s3ObjectSummary -> s3Client.deleteObject(bucketName, s3ObjectSummary.getKey())
                    );
            objectListing = s3Client.listNextBatchOfObjects(objectListing);
        }
    }
}
