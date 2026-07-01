package io.quarkus.grpc.auth;

import org.junit.jupiter.api.extension.RegisterExtension;

import io.quarkus.test.QuarkusExtensionTest;

/**
 * Runs the full gRPC authentication/authorization suite against a gRPC server exposed on a separate port
 * ({@code quarkus.grpc.server.separate-port.enabled=true}).
 * <p>
 * The dedicated router reuses the same request pipeline as the main HTTP server (the incoming request is paused
 * before the security filters run and wrapped in a resuming, duplicated-context request), so both unauthenticated
 * requests (rejected with {@code UNAUTHENTICATED}) and successful secured calls behave the same as when gRPC is
 * multiplexed on the main HTTP server. The client uses {@code test-port} because, in test mode, the gRPC client
 * ignores {@code port} (see {@code io.quarkus.grpc.runtime.supports.Channels}).
 */
public class GrpcSeparatePortAuthTest extends GrpcAuthTestBase {

    @RegisterExtension
    static final QuarkusExtensionTest config = createQuarkusExtensionTest("""
            quarkus.grpc.server.separate-port.enabled=true
            quarkus.grpc.clients.securityClient.host=localhost
            quarkus.grpc.clients.securityClient.test-port=9001
            """, true);

}
