package com.priceline.hc2.bot.pubsub.service;

import com.google.api.core.ApiFuture;
import com.google.api.core.ApiFutureCallback;
import com.google.api.core.ApiFutures;
import com.google.api.gax.core.CredentialsProvider;
import com.google.api.gax.rpc.TransportChannelProvider;
import com.google.cloud.pubsub.v1.AckReplyConsumer;
import com.google.cloud.pubsub.v1.MessageReceiver;
import com.google.cloud.pubsub.v1.Publisher;
import com.google.cloud.pubsub.v1.Subscriber;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.protobuf.ByteString;
import com.google.pubsub.v1.ProjectSubscriptionName;
import com.google.pubsub.v1.PubsubMessage;
import com.google.pubsub.v1.TopicName;
import com.priceline.hc2.bot.pubsub.config.PubSubConfig;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class PubSubService {
    private final TopicName topicName;
    private final TransportChannelProvider channelProvider;
    private final ProjectSubscriptionName projectSubscriptionName;
    private final String gcpProject;
    private final String subscriptionId;

    private final CredentialsProvider credentialsProvider;
    public PubSubService(String emulatorSubscription, TopicName topicName, ProjectSubscriptionName projectSubscriptionName, TransportChannelProvider channelProvider, CredentialsProvider credentialsProvider) {
        this.gcpProject = topicName.getProject();
        this.subscriptionId = emulatorSubscription;
        this.projectSubscriptionName = projectSubscriptionName;
        this.topicName = topicName;
        this.channelProvider = channelProvider;
        this.credentialsProvider = credentialsProvider;
    }

    public String publish() throws Exception {
        final String[] msgID = {""};
        Publisher publisher = null;
        try {
            // Create a publisher instance with default settings bound to the topic
            publisher = Publisher.newBuilder(topicName)
                    .setChannelProvider(channelProvider)
                    .setCredentialsProvider(credentialsProvider)
                    .build();

            ByteString data = ByteString.copyFromUtf8(PubSubConfig.msg);
            PubsubMessage pubsubMessage = PubsubMessage.newBuilder().setData(data).build();

            // Once published, returns a server-assigned message id (unique within the topic)
            final ApiFuture<String> messageIdFuture = publisher.publish(pubsubMessage);
            ApiFutures.addCallback(messageIdFuture, new ApiFutureCallback<String>() {
                public void onSuccess(String messageId) {
                    System.out.println("Published message ID: " + messageId);
                    msgID[0] = messageId;
                }

                public void onFailure(Throwable t) {
                    System.out.println("failed to publish: " + t);
                }
            }, MoreExecutors.directExecutor());

        } finally {
            if (publisher != null) {
                // When finished with the publisher, shutdown to free up resources.
                publisher.shutdown();
                publisher.awaitTermination(1, TimeUnit.MINUTES);
            }
        }
        return String.format("Message id - %s : %s sent.", msgID[0], PubSubConfig.msg);
    }


    public String receive() {
        AtomicReference<String> msg = new AtomicReference<>("");

        // Instantiate an asynchronous message receiver.
        MessageReceiver receiver =
                (PubsubMessage message, AckReplyConsumer consumer) -> {
                    // Handle incoming message, then ack the received message.
                    System.out.println("Id: " + message.getMessageId());
                    System.out.println("Data: " + message.getData().toStringUtf8());
                    msg.set(message.getMessageId());
                    consumer.ack();
                };

        Subscriber subscriber = null;
        try {
            subscriber = Subscriber.newBuilder(this.projectSubscriptionName, receiver)
                    .setChannelProvider(channelProvider)
                    .setCredentialsProvider(credentialsProvider)
                    .build();
            // Start the subscriber.
            subscriber.startAsync().awaitRunning();
            System.out.printf("Listening for messages on %s:\n", this.projectSubscriptionName);
            // Allow the subscriber to run for 30s unless an unrecoverable error occurs.
            subscriber.awaitTerminated(30, TimeUnit.SECONDS);
        } catch (TimeoutException timeoutException) {
            // Shut down the subscriber after 30s. Stop receiving messages.
            subscriber.stopAsync();
        }
        return msg.get();
    }
}
