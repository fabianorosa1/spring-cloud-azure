/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.messaging.config;

import com.microsoft.azure.spring.integration.core.api.SubscribeByGroupOperation;
import com.microsoft.azure.spring.messaging.container.DefaultAzureListenerContainerFactory;
import com.microsoft.azure.spring.messaging.container.ListenerContainerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Warren Zhu
 */
@Configuration
public class AzureMessagingConfiguration {

    @Bean
    public ListenerContainerFactory azureListenerContainerFactory(SubscribeByGroupOperation subscribeByGroupOperation) {
        return new DefaultAzureListenerContainerFactory(subscribeByGroupOperation);
    }
}
