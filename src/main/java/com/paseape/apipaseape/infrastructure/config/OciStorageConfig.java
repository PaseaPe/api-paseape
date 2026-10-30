package com.paseape.apipaseape.infrastructure.config;

import com.oracle.bmc.Region;
import com.oracle.bmc.auth.AuthenticationDetailsProvider;
import com.oracle.bmc.auth.SimpleAuthenticationDetailsProvider;
import com.oracle.bmc.auth.SimplePrivateKeySupplier;
import com.oracle.bmc.objectstorage.ObjectStorageClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.InputStream;

@Configuration
public class OciStorageConfig {

    @Value("${app.oci.tenancy-ocid}")
    private String tenancyId;

    @Value("${app.oci.user-ocid}")
    private String userId;

    @Value("${app.oci.fingerprint}")
    private String fingerprint;

    @Value("${app.oci.private-key-path}")
    private String privateKeyPath;

    @Value("${app.oci.region}")
    private String region;

    @Bean
    @ConditionalOnProperty(name = "app.oci.namespace")
    public ObjectStorageClient objectStorageClient() {
        AuthenticationDetailsProvider provider = SimpleAuthenticationDetailsProvider.builder()
                .tenantId(tenancyId)
                .userId(userId)
                .fingerprint(fingerprint)
                .privateKeySupplier(new SimplePrivateKeySupplier(privateKeyPath))
                .region(Region.fromRegionId(region))
                .build();

        return ObjectStorageClient.builder()
                .region(Region.fromRegionId(region))
                .build(provider);
    }
}