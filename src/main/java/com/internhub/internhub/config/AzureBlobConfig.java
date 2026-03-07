package com.internhub.internhub.config;

import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AzureBlobConfig {
    @Bean
    public BlobServiceClient blobServiceClient(
            @Value("${app.azure.connection-string}") String connectionString
    ) {
        return new BlobServiceClientBuilder()
                .connectionString(connectionString)
                .buildClient();
    }

    @Bean
    public BlobContainerClient blobContainerClient(
            BlobServiceClient blobServiceClient,
            @Value("${app.azure.container}") String containerName
    ) {
        BlobContainerClient container = blobServiceClient.getBlobContainerClient(containerName);
        container.createIfNotExists(); // hits Azurite
        return container;
    }
}
