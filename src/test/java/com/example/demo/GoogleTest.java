package com.example.demo;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.pubsub.v1.AckReplyConsumer;
import com.google.cloud.pubsub.v1.MessageReceiver;
import com.google.cloud.pubsub.v1.Subscriber;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Bucket;
import com.google.cloud.storage.BucketInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.google.pubsub.v1.ProjectSubscriptionName;
import com.google.pubsub.v1.PubsubMessage;
import org.junit.jupiter.api.Test;

public class GoogleTest {

    private String GOOGLE_CLOUD_KEY_PATH = "/Users/daiyonghui/tools/google/cloudKey/nftygifts-dev-67ad4564dfbc.json";

    @Test
    public void storageTest() throws IOException {
        //uploadFile("nftygifts-dev", "photo_pool", "test_image.png", "/Users/daiyonghui/Desktop/human.jpeg");
        downloadObject("nftygifts-dev", "photo_pool","test_image.png","/Users/daiyonghui/Desktop/human2.jpeg");
    }

    public void uploadFile(String projectId, String bucketName, String objectName, String filePath) throws IOException {
        Storage storage = StorageOptions.newBuilder().setCredentials(getCredentials()).setProjectId("nftygifts-dev").build().getService();

        BlobId blobId = BlobId.of(bucketName, objectName);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId).build();
        storage.create(blobInfo, Files.readAllBytes(Paths.get(filePath)));

        System.out.println(
                "File " + filePath + " uploaded to bucket " + bucketName + " as " + objectName);
    }

    public void downloadObject(String projectId, String bucketName, String objectName, String destFilePath) {
        Storage storage = StorageOptions.newBuilder().setCredentials(getCredentials()).setProjectId("nftygifts-dev").build().getService();


        Blob blob = storage.get(BlobId.of(bucketName, objectName));
        blob.downloadTo(Paths.get(destFilePath));

        System.out.println(
                "Downloaded object "
                        + objectName
                        + " from bucket name "
                        + bucketName
                        + " to "
                        + destFilePath);
    }

    private GoogleCredentials getCredentials() {
        try {
            return GoogleCredentials.fromStream(new FileInputStream(GOOGLE_CLOUD_KEY_PATH));
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }


}
