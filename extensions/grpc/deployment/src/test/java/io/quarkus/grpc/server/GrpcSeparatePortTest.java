package io.quarkus.grpc.server;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.concurrent.TimeUnit;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import io.grpc.examples.helloworld.GreeterGrpc;
import io.grpc.examples.helloworld.HelloReply;
import io.grpc.examples.helloworld.HelloReplyOrBuilder;
import io.grpc.examples.helloworld.HelloRequest;
import io.grpc.examples.helloworld.HelloRequestOrBuilder;
import io.quarkus.grpc.GrpcClient;
import io.quarkus.grpc.server.services.HelloService;
import io.quarkus.test.QuarkusExtensionTest;

/**
 * Verifies that, when {@code quarkus.grpc.server.separate-port.enabled=true}, the gRPC server is exposed on its own
 * dedicated port (the default test-port 9001) — separate from the main HTTP server (8081) — while still reusing the
 * main Vert.x instance (shared event loops). The gRPC client targets 9001 and must reach the service there.
 * <p>
 * It also verifies the converse: because the gRPC services are bound only to the dedicated port (and not to the main
 * HTTP router), a gRPC call to the main HTTP port (8081 in test mode) must fail rather than reach the service.
 */
public class GrpcSeparatePortTest {

    @RegisterExtension
    static final QuarkusExtensionTest config = new QuarkusExtensionTest().setArchiveProducer(
            () -> ShrinkWrap.create(JavaArchive.class)
                    .addPackage(io.grpc.examples.helloworld.HelloWorldProto.class.getPackage())
                    .addClasses(GreeterGrpc.class, GreeterGrpc.GreeterBlockingStub.class,
                            HelloService.class, HelloRequest.class, HelloReply.class,
                            HelloReplyOrBuilder.class, HelloRequestOrBuilder.class))
            .withConfigurationResource("grpc-server-separate-port-config.properties");

    @GrpcClient("hello-service")
    GreeterGrpc.GreeterBlockingStub service;

    @Test
    public void testGrpcReachableOnSeparatePort() {
        String response = service.sayHello(HelloRequest.newBuilder().setName("World!").build())
                .getMessage();
        assertThat(response).isEqualTo("Hello World!");
    }

    @Test
    public void testGrpcNotServedOnMainHttpPort() {
        // The gRPC services are bound only to the dedicated port, so a gRPC call to the main HTTP port (8081 in
        // test mode) must fail rather than reach the service. A deadline guards against the call hanging.
        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 8081)
                .usePlaintext()
                .build();
        try {
            GreeterGrpc.GreeterBlockingStub mainPortStub = GreeterGrpc.newBlockingStub(channel);
            assertThatThrownBy(() -> mainPortStub.withDeadlineAfter(10, TimeUnit.SECONDS)
                    .sayHello(HelloRequest.newBuilder().setName("World!").build()))
                    .isInstanceOf(StatusRuntimeException.class);
        } finally {
            channel.shutdownNow();
        }
    }
}
