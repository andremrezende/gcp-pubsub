package br.com.rezende.pubsub.config;

import com.google.api.gax.core.CredentialsProvider;
import com.google.api.gax.core.NoCredentialsProvider;
import com.google.api.gax.grpc.GrpcTransportChannel;
import com.google.api.gax.rpc.FixedTransportChannelProvider;
import com.google.api.gax.rpc.NotFoundException;
import com.google.api.gax.rpc.TransportChannelProvider;
import com.google.cloud.pubsub.v1.SubscriptionAdminClient;
import com.google.cloud.pubsub.v1.SubscriptionAdminSettings;
import com.google.cloud.pubsub.v1.TopicAdminClient;
import com.google.cloud.pubsub.v1.TopicAdminSettings;
import com.google.pubsub.v1.ProjectSubscriptionName;
import com.google.pubsub.v1.ProjectTopicName;
import com.google.pubsub.v1.PushConfig;
import com.google.pubsub.v1.TopicName;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

@Configuration
public class PubSubConfig {
    public static final String msg="{\"name\":\"hc2bot-BS-AGODA-R-g101a\",\"supplier\":\"AGORDA-R\",\"los\":\"1-7\",\"ap\":\"0-0\",\"score\":\"0-\",\"batchinSizeBeforeDelay\":\"20\",\"batchinDelayInMilliseconds\":\"2045\",\"pageSize\":\"2780\",\"destinationTopic\":\"itin_status_events\",\"messageType\":\"HotelItineraryExpired\"}";

    @Value("${hc2bot.gcp.project}")
    private String gcpProject;

    @Value("${hc2bot.gcp.pubsub.emulator.host}")
    private String emulatorHost;

    @Value("${hc2bot.gcp.pubsub.emulator}")
    private String emulator;

    @Value("${hc2bot.processor.subscription}")
    private String emulatorSubscription;

    @Value("${hc2bot.processor.topic}")
    private String topicID;

    @Bean
    public CredentialsProvider credentialsProvider() {
        return NoCredentialsProvider.create();
    }

    @Bean
    public  TransportChannelProvider channelProvider() {
        ManagedChannel channel = ManagedChannelBuilder.forTarget(emulator).usePlaintext().build();
        return FixedTransportChannelProvider.create(GrpcTransportChannel.create(channel));
    }

    @Bean
    public TopicName topicName(TransportChannelProvider channelProvider, CredentialsProvider credentialsProvider) throws IOException {
        try (TopicAdminClient topicAdminClient = TopicAdminClient.create(
                TopicAdminSettings.newBuilder()
                .setTransportChannelProvider(channelProvider)
                .setCredentialsProvider(credentialsProvider)
                .build())) {
            TopicName topicName = TopicName.of(gcpProject, topicID);
            try {
              topicAdminClient.deleteTopic(topicName);
            } catch(NotFoundException e) {
                //Do nothing
            }
            topicAdminClient.createTopic(topicName);
            return topicName;
        }
    }

    @Bean
    public String emulatorSubscription() {
        return emulatorSubscription;
    }

    @Bean
    public ProjectSubscriptionName createSubscription(TransportChannelProvider channelProvider,CredentialsProvider credentialsProvider) throws Exception {
        try (SubscriptionAdminClient subscriptionAdminClient = SubscriptionAdminClient.create(
                SubscriptionAdminSettings.newBuilder()
                .setTransportChannelProvider(channelProvider)
                .setCredentialsProvider(credentialsProvider)
                        .build())) {
            ProjectTopicName topicName = ProjectTopicName.of(gcpProject, topicID);
            ProjectSubscriptionName subscriptionName = ProjectSubscriptionName.of(gcpProject,
                    emulatorSubscription);
            // create a pull subscription with default acknowledgement deadline
            subscriptionAdminClient.createSubscription(subscriptionName, topicName,
                    PushConfig.getDefaultInstance(), 0);
            return subscriptionName;
        }
    }
}
