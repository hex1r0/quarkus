package io.quarkus.grpc.runtime.config;

import io.quarkus.runtime.annotations.ConfigGroup;
import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;
import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;
import io.smallrye.config.WithName;

@ConfigMapping(prefix = "quarkus.grpc.server")
@ConfigRoot(phase = ConfigPhase.BUILD_TIME)
public interface GrpcServerBuildTimeConfig {
    /**
     * Whether a health check on gRPC status is published in case the smallrye-health extension is present.
     */
    @WithName("health.enabled")
    @WithDefault("true")
    boolean mpHealthEnabled();

    /**
     * Whether the gRPC health check is exposed.
     */
    @WithName("grpc-health.enabled")
    @WithDefault("true")
    boolean grpcHealthEnabled();

    /**
     * Configures whether the gRPC server is exposed on a separate network port.
     */
    SeparatePortConfig separatePort();

    @ConfigGroup
    interface SeparatePortConfig {
        /**
         * Whether the gRPC server is exposed on a separate network port instead of being multiplexed on the
         * main Quarkus HTTP server (the default).
         * <p>
         * When enabled, the gRPC server is bound to its own TCP listener (see
         * {@code quarkus.grpc.server.separate-port.port}) but <strong>reuses the main Vert.x instance</strong>,
         * so it shares the same event loops and worker thread pool as the HTTP server — only an additional
         * listener is opened, no separate event loop group or server stack is created.
         * <p>
         * This differs from the legacy {@code use-separate-server} mode, which started a dedicated Netty server
         * with its own event loops.
         */
        @WithDefault("false")
        boolean enabled();
    }
}
