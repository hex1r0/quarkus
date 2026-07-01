package io.quarkus.grpc.server;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.grpc.ManagedChannel;
import io.grpc.examples.helloworld.GreeterGrpc;
import io.grpc.examples.helloworld.HelloReply;
import io.grpc.examples.helloworld.HelloReplyOrBuilder;
import io.grpc.examples.helloworld.HelloRequest;
import io.grpc.examples.helloworld.HelloRequestOrBuilder;
import io.grpc.examples.helloworld.HelloWorldProto;
import io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.NettyChannelBuilder;
import io.netty.handler.ssl.SslContext;
import io.quarkus.grpc.server.services.HelloService;
import io.quarkus.test.QuarkusExtensionTest;
import io.smallrye.certs.Format;
import io.smallrye.certs.junit5.Certificate;
import io.smallrye.certs.junit5.Certificates;

/**
 * Verifies that the separate gRPC server port can be secured with TLS using a named TLS registry configuration
 * ({@code quarkus.grpc.server.separate-port.tls-configuration-name}). The server runs on its own port (the default
 * test-port 9001) while sharing the main Vert.x instance, and the client connects over TLS, negotiating HTTP/2 via
 * ALPN.
 */
@Certificates(baseDir = "target/certs", certificates = @Certificate(name = "grpc-sep-tls", password = "wibble", formats = {
        Format.JKS, Format.PEM, Format.PKCS12 }))
public class GrpcSeparatePortTlsTest {

    @RegisterExtension
    static final QuarkusExtensionTest config = new QuarkusExtensionTest()
            .setFlatClassPath(true)
            .setArchiveProducer(
                    () -> ShrinkWrap.create(JavaArchive.class)
                            .addPackage(HelloWorldProto.class.getPackage())
                            .addClasses(GreeterGrpc.class, GreeterGrpc.GreeterBlockingStub.class,
                                    HelloService.class, HelloRequest.class, HelloReply.class,
                                    HelloReplyOrBuilder.class, HelloRequestOrBuilder.class))
            .withConfigurationResource("grpc-server-separate-port-tls-configuration.properties");

    @Test
    public void testGrpcOverTlsOnSeparatePort() throws Exception {
        SslContext sslContext = GrpcSslContexts.forClient()
                .trustManager(new File("target/certs/grpc-sep-tls-ca.crt"))
                .build();
        ManagedChannel channel = NettyChannelBuilder.forAddress("localhost", 9001)
                .sslContext(sslContext)
                .build();
        try {
            String response = GreeterGrpc.newBlockingStub(channel)
                    .sayHello(HelloRequest.newBuilder().setName("World!").build())
                    .getMessage();
            assertThat(response).isEqualTo("Hello World!");
        } finally {
            channel.shutdownNow();
        }
    }
}
