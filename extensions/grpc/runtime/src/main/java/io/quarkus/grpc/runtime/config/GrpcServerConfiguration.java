package io.quarkus.grpc.runtime.config;

import java.util.Optional;
import java.util.OptionalInt;

import io.quarkus.runtime.annotations.ConfigGroup;
import io.smallrye.config.WithDefault;

@ConfigGroup
public interface GrpcServerConfiguration {

    /**
     * The max inbound message size in bytes.
     */
    OptionalInt maxInboundMessageSize();

    /**
     * Enables the gRPC Reflection Service.
     * By default, the reflection service is only exposed in {@code dev} mode.
     * This setting allows overriding this choice and enable the reflection service every time.
     */
    @WithDefault("false")
    boolean enableReflectionService();

    /**
     * gRPC compression, e.g. "gzip"
     */
    Optional<String> compression();

    /**
     * Runtime configuration of the separate gRPC server port.
     * <p>
     * Only used when {@code quarkus.grpc.server.separate-port.enabled} is {@code true}. When the gRPC server
     * is multiplexed on the main HTTP server (the default), these settings are ignored and the gRPC server
     * listens on {@code quarkus.http.port}.
     */
    SeparatePortConfig separatePort();

    @ConfigGroup
    interface SeparatePortConfig {
        /**
         * The port the separate gRPC server is bound to.
         * <p>
         * Defaults to {@code 9000}, matching the port used by the previous standalone gRPC server. Note that the
         * management interface also defaults to {@code 9000}/{@code 9001}, so if both are enabled, configure a
         * different port for one of them to avoid a clash.
         */
        @WithDefault("9000")
        int port();

        /**
         * The port the separate gRPC server is bound to in test mode.
         */
        @WithDefault("9001")
        int testPort();

        /**
         * The host the separate gRPC server is bound to.
         */
        @WithDefault("0.0.0.0")
        String host();

        /**
         * The name of the TLS configuration (bucket) to use to secure the separate gRPC server.
         * <p>
         * The configuration must be registered under the given name in the TLS registry, e.g. via
         * {@code quarkus.tls.<name>.*} properties. When set, the separate gRPC server is exposed over TLS and
         * negotiates HTTP/2 using ALPN. When absent, the separate gRPC server uses plain text (HTTP/2 cleartext).
         */
        Optional<String> tlsConfigurationName();
    }
}
