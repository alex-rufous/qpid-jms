/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.qpid.jms.transports.netty;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.net.URI;

import org.apache.qpid.jms.transports.Transport;
import org.apache.qpid.jms.transports.TransportListener;
import org.apache.qpid.jms.transports.TransportOptions;
import org.apache.qpid.jms.transports.TransportSslOptions;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Test basic functionality of the Netty based SSL Transport.
 */
public class NettySslTransportTest extends NettyTcpTransportTest {

    private static final Logger LOG = LoggerFactory.getLogger(NettySslTransportTest.class);

    public static final String PASSWORD = "password";
    public static final String SERVER_KEYSTORE = "src/test/resources/broker-jks.keystore";
    public static final String SERVER_TRUSTSTORE = "src/test/resources/broker-jks.truststore";
    public static final String CLIENT_KEYSTORE = "src/test/resources/client-jks.keystore";
    public static final String CLIENT_TRUSTSTORE = "src/test/resources/client-jks.truststore";
    public static final String KEYSTORE_TYPE = "jks";

    @Test(timeout = 60 * 1000)
    public void testConnectToServerWithoutTrustStoreFails() throws Exception {
        try (NettyEchoServer server = new NettyEchoServer(createServerOptions())) {
            server.start();

            int port = server.getServerPort();
            URI serverLocation = new URI("tcp://localhost:" + port);

            Transport transport = createTransport(serverLocation, testListener, createClientOptionsTrustNone());
            try {
                transport.connect();
                fail("Should not have connected to the server");
            } catch (Exception e) {
                LOG.info("Connection failed to untrusted test server.");
            }

            assertFalse(transport.isConnected());

            transport.close();
        }

        assertTrue(exceptions.isEmpty());
    }

    @Test(timeout = 60 * 1000)
    public void testConnectToServerClientTrustsAll() throws Exception {
        try (NettyEchoServer server = new NettyEchoServer(createServerOptions())) {
            server.start();

            int port = server.getServerPort();
            URI serverLocation = new URI("tcp://localhost:" + port);

            Transport transport = createTransport(serverLocation, testListener, createClientOptionsTrustAll());
            try {
                transport.connect();
                LOG.info("Connection established to untrusted test server.");
            } catch (Exception e) {
                fail("Should have connected to the server");
            }

            assertTrue(transport.isConnected());

            transport.close();
        }

        assertTrue(exceptions.isEmpty());
    }

    @Override
    protected Transport createTransport(URI serverLocation, TransportListener listener, TransportOptions options) {
        return new NettySslTransport(listener, serverLocation, options);
    }

    @Override
    protected TransportSslOptions createClientOptions() {
        TransportSslOptions options = TransportSslOptions.INSTANCE.clone();

        options.setKeyStoreLocation(CLIENT_KEYSTORE);
        options.setTrustStoreLocation(CLIENT_TRUSTSTORE);
        options.setStoreType(KEYSTORE_TYPE);
        options.setKeyStorePassword(PASSWORD);
        options.setTrustStorePassword(PASSWORD);

        return options;
    }

    @Override
    protected TransportSslOptions createServerOptions() {
        TransportSslOptions options = TransportSslOptions.INSTANCE.clone();

        options.setKeyStoreLocation(SERVER_KEYSTORE);
        options.setTrustStoreLocation(SERVER_TRUSTSTORE);
        options.setStoreType(KEYSTORE_TYPE);
        options.setKeyStorePassword(PASSWORD);
        options.setTrustStorePassword(PASSWORD);

        return options;
    }

    protected TransportSslOptions createClientOptionsTrustNone() {
        TransportSslOptions options = TransportSslOptions.INSTANCE.clone();

        options.setKeyStoreLocation(CLIENT_KEYSTORE);
        options.setKeyStorePassword(PASSWORD);
        options.setStoreType(KEYSTORE_TYPE);
        options.setTrustAll(false);

        return options;
    }

    protected TransportSslOptions createClientOptionsTrustAll() {
        TransportSslOptions options = TransportSslOptions.INSTANCE.clone();

        options.setKeyStoreLocation(CLIENT_KEYSTORE);
        options.setKeyStorePassword(PASSWORD);
        options.setStoreType(KEYSTORE_TYPE);
        options.setTrustAll(true);

        return options;
    }
}
