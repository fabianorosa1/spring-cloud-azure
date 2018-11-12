/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.microsoft.azure.spring.cloud.config;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.microsoft.azure.spring.cloud.config.domain.KeyValueItem;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;

import org.springframework.lang.Nullable;

@Slf4j
@AllArgsConstructor
public class ConfigServiceTemplate implements ConfigServiceOperations {
    private static final String LOAD_FAILURE_MSG = "Failed to load keys from Azure Config Service.";
    private static final String LOAD_FAILURE_VERBOSE_MSG = LOAD_FAILURE_MSG + " With status code: %s, reason: %s";

    private static final ObjectMapper mapper = new ObjectMapper();

    private final ConfigHttpClient configClient;
    private final String storeEndpoint;
    private final String credential;
    private final String secret;

    @Override
    public List<KeyValueItem> getKeys(@Nullable String prefix, @Nullable String label) {
        String requestUri = new RestAPIBuilder().withEndpoint(storeEndpoint).buildKVApi(prefix, label);
        HttpGet httpGet = new HttpGet(requestUri);

        log.debug("Loading key-value items from Azure Config service at [{}].", requestUri);
        try (CloseableHttpResponse response = configClient.execute(httpGet, credential, secret)) {
            int statusCode = response.getStatusLine().getStatusCode();

            if (statusCode == HttpStatus.SC_OK) {
                ObjectNode node = mapper.readValue(response.getEntity().getContent(), ObjectNode.class);

                // Returned key-value items are stored in the JSON field "items"
                return mapper.readValue(node.get("items").toString(),
                        mapper.getTypeFactory().constructCollectionType(List.class, KeyValueItem.class));
            } else {
                throw new IllegalStateException(String.format(LOAD_FAILURE_VERBOSE_MSG, statusCode,
                        response.getStatusLine().getReasonPhrase()));
            }
        }
        catch (IOException | URISyntaxException e) {
            log.error(LOAD_FAILURE_MSG, e);
            // TODO (wp) wrap exception as config service specific exception in order to provide fail-fast etc.
            // features?
            throw new IllegalStateException(LOAD_FAILURE_MSG, e);
        }
    }
}