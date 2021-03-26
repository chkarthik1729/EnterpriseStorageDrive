package crio.vicara.service;

import com.amazonaws.HttpMethod;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import crio.vicara.FlatStorageProvider;
import crio.vicara.StorageProviderDetails;

import java.io.InputStream;
import java.net.URL;
import java.util.Date;

public class AmazonSimpleStorageService implements FlatStorageProvider {

    private static final AmazonSimpleStorageService instance = new AmazonSimpleStorageService();

    private static AmazonS3 s3Client;
    private static final String bucketName = "crio-do-vicara-t7";

    private AmazonSimpleStorageService() {
        s3Client = AmazonS3ClientBuilder
                .standard()
                .withCredentials(DefaultAWSCredentialsProviderChain.getInstance())
                .withRegion("ap-south-1")
                .build();
    }

    public static AmazonSimpleStorageService getInstance() {
        return instance;
    }

    @Override
    public StorageProviderDetails getStorageProviderDetails() {
        //TODO: Read from Config Files
        return null;
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
        Date expiresAt = new Date(currTime + urlExpirySeconds);
        GeneratePresignedUrlRequest generatePresignedUrlRequest = new GeneratePresignedUrlRequest(bucketName, key)
                .withMethod(HttpMethod.GET)
                .withExpiration(expiresAt);
        return s3Client.generatePresignedUrl(generatePresignedUrlRequest);
    }
}
