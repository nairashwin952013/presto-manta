/*
 * Copyright (c) 2017, Joyent, Inc. All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.joyent.manta.presto.test;

import com.facebook.presto.spi.ConnectorSession;
import com.facebook.presto.testing.TestingConnectorSession;
import com.google.common.collect.ImmutableMap;
import com.google.inject.Injector;
import com.joyent.manta.client.MantaClient;
import com.joyent.manta.presto.MantaMetadata;
import com.joyent.manta.presto.MantaPrestoTestUtils;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

public class MantaPrestoIntegrationTestUtils {
    public static class IntegrationSetup {
        public final Injector injector;
        public final MantaClient mantaClient;
        public final MantaMetadata instance;
        public final String testPathPrefix;
        public final Supplier<ConnectorSession> sessionSupplier;

        public IntegrationSetup(final Injector injector,
                                final MantaClient mantaClient,
                                final MantaMetadata instance,
                                final String testPathPrefix,
                                final Supplier<ConnectorSession> sessionSupplier) {
            this.injector = injector;
            this.mantaClient = mantaClient;
            this.instance = instance;
            this.testPathPrefix = testPathPrefix;
            this.sessionSupplier = sessionSupplier;
        }
    }

    public static IntegrationSetup setupConfiguration() throws IOException {
        String randomDir = UUID.randomUUID().toString();

        Map<String, String> config = ImmutableMap.of(
                "manta.schema.default", String.format(
                        "~~/stor/java-manta-integration-tests/%s", randomDir));
        Injector injector = MantaPrestoTestUtils.createInjectorInstance(config);

        MantaClient mantaClient = injector.getInstance(MantaClient.class);
        MantaMetadata instance = injector.getInstance(MantaMetadata.class);
        Supplier<ConnectorSession> sessionSupplier = () ->
                new TestingConnectorSession(Collections.emptyList());

        String testPathPrefix = String.format("%s/stor/java-manta-integration-tests/%s/",
                mantaClient.getContext().getMantaHomeDirectory(), randomDir);
        mantaClient.putDirectory(testPathPrefix, true);

        return new IntegrationSetup(injector, mantaClient, instance, testPathPrefix, sessionSupplier);
    }
}
