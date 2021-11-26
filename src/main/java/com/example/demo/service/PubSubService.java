package com.example.demo.service;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.google.api.core.ApiFuture;
import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.pubsub.v1.AckReplyConsumer;
import com.google.cloud.pubsub.v1.MessageReceiver;
import com.google.cloud.pubsub.v1.Publisher;
import com.google.cloud.pubsub.v1.Subscriber;
import com.google.protobuf.ByteString;
import com.google.pubsub.v1.ProjectSubscriptionName;
import com.google.pubsub.v1.PubsubMessage;
import com.google.pubsub.v1.TopicName;
import org.springframework.stereotype.Service;

@Service
public class PubSubService {

//    @Value("${google.project_id")
    private String GOOGLE_PROJECT_ID = "nftygifts-dev";

//    @Value("${google.topic_id}")
    private String GOOGLE_TOPIC_ID = "topic_test";

//    @Value("${google.sub_id}")
    private String GOOGLE_SUB_ID = "topic-sub2";

//    @Value("${google.cloud_key_path}")
    private String GOOGLE_CLOUD_KEY_PATH = "/Users/daiyonghui/tools/google/cloudKey/nftygifts-dev-67ad4564dfbc.json";

    public void sendMessage(String message) throws IOException, InterruptedException, ExecutionException {
        TopicName topicName = TopicName.of(GOOGLE_PROJECT_ID, GOOGLE_TOPIC_ID);
        Publisher publisher = null;

        try {
            publisher = Publisher
                    .newBuilder(topicName)
                    .setCredentialsProvider(FixedCredentialsProvider.create(getCredentials()))
                    .build();
            //String message = "{\"msg\":\"test123\"}";
            ByteString data = ByteString.copyFromUtf8(message);
            PubsubMessage pubsubMessage = PubsubMessage.newBuilder().setData(data).build();

            ApiFuture<String> messageIdFuture = publisher.publish(pubsubMessage);
            String messageId = messageIdFuture.get();
            System.out.println("Published message ID: " + messageId);
        } finally {
            if (publisher != null) {
                publisher.shutdown();
                publisher.awaitTermination(1, TimeUnit.MINUTES);
            }
        }
    }

    public String receiveMessage() {
        ProjectSubscriptionName subscriptionName = ProjectSubscriptionName.of(GOOGLE_PROJECT_ID, GOOGLE_SUB_ID);
        MessageReceiver receiver =
                (PubsubMessage message, AckReplyConsumer consumer) -> {
                    System.out.println("Id: " + message.getMessageId());
                    System.out.println("Data: " + message.getData().toStringUtf8());
                    consumer.ack();
                };

        Subscriber subscriber = null;
        try {
            subscriber = Subscriber
                    .newBuilder(subscriptionName, receiver)
                    .setCredentialsProvider(FixedCredentialsProvider.create(getCredentials()))
                    .build();
            subscriber.startAsync().awaitRunning();
            System.out.printf("Listening for messages on %s:\n", subscriptionName.toString());
            subscriber.awaitTerminated(30, TimeUnit.SECONDS);

            return subscriptionName.toString();
        } catch (TimeoutException timeoutException) {
            subscriber.stopAsync();
        }

        return "no message";
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
