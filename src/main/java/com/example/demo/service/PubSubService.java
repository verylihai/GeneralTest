package com.example.demo.service;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.example.demo.utils.TimeHelper;
import com.google.api.core.ApiFuture;
import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.pubsub.v1.AckReplyConsumer;
import com.google.cloud.pubsub.v1.MessageReceiver;
import com.google.cloud.pubsub.v1.Publisher;
import com.google.cloud.pubsub.v1.Subscriber;
import com.google.cloud.pubsub.v1.SubscriptionAdminClient;
import com.google.protobuf.ByteString;
import com.google.pubsub.v1.ProjectSubscriptionName;
import com.google.pubsub.v1.PubsubMessage;
import com.google.pubsub.v1.PushConfig;
import com.google.pubsub.v1.Subscription;
import com.google.pubsub.v1.TopicName;
import org.springframework.stereotype.Service;

@Service
public class PubSubService {

//    @Value("${google.project_id")
    private String GOOGLE_PROJECT_ID = "nfty-gifts-79919";

//    @Value("${google.topic_id}")
    private String GOOGLE_TOPIC_ID = "nftygifts-order-topic";

//    @Value("${google.sub_id}")
    private String GOOGLE_SUB_ID = "mint-sub-test";

//    @Value("${google.cloud_key_path}")
    private String GOOGLE_CLOUD_KEY_PATH = "/Users/daiyonghui/tools/google/cloudKey/nfty-gifts-79919-dbfece8d9818.json";

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
            System.out.println(TimeHelper.getCurrentTime() + "Send Message to queue: " + message + "#### id: "+ messageId);
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

    public void createSubsc() {
        try (SubscriptionAdminClient subscriptionAdminClient = SubscriptionAdminClient.create()) {
            TopicName topicName = TopicName.of(GOOGLE_PROJECT_ID, "nftygifts-mint-topic");
            ProjectSubscriptionName subscriptionName =
                    ProjectSubscriptionName.of(GOOGLE_PROJECT_ID, "mint-sub-test");
            // Create a pull subscription with default acknowledgement deadline of 10 seconds.
            // Messages not successfully acknowledged within 10 seconds will get resent by the server.
            Subscription subscription =
                    subscriptionAdminClient.createSubscription(
                            subscriptionName, topicName, PushConfig.getDefaultInstance(), 10);
            System.out.println("Created pull subscription: " + subscription.getName());
        } catch (IOException e) {
            e.printStackTrace();
        }

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
