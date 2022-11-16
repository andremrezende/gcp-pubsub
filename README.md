# GCP PubSub emulator Java example
This java project is created to explain how one can use the GCP PubSub emulator on local machine for development.

## Installing & starting pubsub emulator

To install please execute following commands in your local machine

gcloud components install pubsub-emulator
gcloud components update

To start please execute following command
gcloud beta emulators pubsub start

## Communicating with PubSub emulator with Java API
In order to communicate with PubSub emulator you have to change your code little bit compared to the cloud PubSub code.

Here is the code you need to setup before calling Admin client

String hostport = "localhost:8538";
ManagedChannel channel = ManagedChannelBuilder.forTarget(hostport).usePlaintext(true).build();

channelProvider = FixedTransportChannelProvider.create(GrpcTransportChannel.create(channel));
credentialsProvider = NoCredentialsProvider.create();
topicAdminClient = TopicAdminClient
.create(TopicAdminSettings.newBuilder().setTransportChannelProvider(Constants.channelProvider)
.setCredentialsProvider(Constants.credentialsProvider).build());


To create Topics & subscription look for the code in the class .TopicManage
To publish the messages look for the code in the class .TopicPublisher
To subscribe the messages look for the code in the class .TopicSubscriber

## Configurations
All the configurations are available into application.properties
server.port=9090  #API port

# GCP Configuration
hc2bot.gcp.project=test-project #GCP Project name
hc2bot.gcp.pubsub.emulator=0.0.0.0:8538 #GCP PubSub emulator host:port
hc2bot.gcp.pubsub.emulator.host=[::1]:8538 #GCP PubSub emulator host:port
hc2bot.processor.topic=test-project-processor-events #GCP PubSub Topic name
hc2bot.processor.subscription=test-project-processor-events-sub #GCP PubSub subscription name

## Test
GET localhost:9091/publish
GET localhost:9091/receive