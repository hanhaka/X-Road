package ee.cyber.sdsb.proxy.testsuite;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.security.Principal;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509ExtendedKeyManager;
import javax.net.ssl.X509TrustManager;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.server.nio.SelectChannelConnector;
import org.eclipse.jetty.server.ssl.SslSelectChannelConnector;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ee.cyber.sdsb.common.TestCertUtil;
import ee.cyber.sdsb.common.TestCertUtil.PKCS12;
import ee.cyber.sdsb.common.util.CryptoUtils;
import ee.cyber.sdsb.common.util.StartStop;

import static ee.cyber.sdsb.common.ErrorCodes.translateException;

@SuppressWarnings("unchecked")
class DummyService extends Server implements StartStop {

    private static final Logger LOG =
            LoggerFactory.getLogger(DummyService.class);

    private static X509Certificate serverCert;
    private static PrivateKey serverKey;

    DummyService() {
        super();
        try {
            setupConnectors();
            setHandler(new ServiceHandler());
        } catch (Exception e) {
            throw translateException(e);
        }
    }

    private void setupConnectors() throws Exception {
        SelectChannelConnector connector = new SelectChannelConnector();
        connector.setName("httpConnector");
        connector.setPort(ProxyTestSuite.SERVICE_PORT);
        connector.setSoLingerTime(0);
        connector.setMaxIdleTime(0);
        addConnector(connector);

        SelectChannelConnector sslConnector = createSslConnector();
        sslConnector.setName("httpsConnector");
        sslConnector.setPort(ProxyTestSuite.SERVICE_SSL_PORT);
        sslConnector.setSoLingerTime(0);
        sslConnector.setMaxIdleTime(0);
        sslConnector.setAcceptors(2 * Runtime.getRuntime().availableProcessors());
        addConnector(sslConnector);
    }

    private SelectChannelConnector createSslConnector() throws Exception {
        PKCS12 consumer = TestCertUtil.getConsumer();
        serverCert = consumer.cert;
        serverKey = consumer.key;

        SslContextFactory cf = new SslContextFactory(false);
        cf.setNeedClientAuth(true);

        cf.setIncludeCipherSuites(CryptoUtils.INCLUDED_CIPHER_SUITES);
        cf.setSessionCachingEnabled(true);

        SSLContext ctx = SSLContext.getInstance(CryptoUtils.SSL_PROTOCOL);
        ctx.init(new KeyManager[] { new DummyServiceKeyManager() },
                new TrustManager[] { new DummyServiceTrustManager() },
                new SecureRandom());

        cf.setSslContext(ctx);

        return new SslSelectChannelConnector(cf);
    }

    private class ServiceHandler extends AbstractHandler {
        @Override
        public void handle(String target, Request baseRequest,
                HttpServletRequest request, HttpServletResponse response)
                throws IOException, ServletException {
            LOG.debug("Service simulator received request {}, contentType={}",
                    target, request.getContentType());
            try {
                // check if the test case implements custom service response
                AbstractHandler handler = currentTestCase().getServiceHandler();
                if (handler != null) {
                    handler.handle(target, baseRequest, request, response);
                    return;
                }

                Message receivedRequest = new Message(
                        request.getInputStream(), request.getContentType());

                String encoding = request.getCharacterEncoding();
                LOG.debug("Request: encoding={}, soap={}", encoding,
                        receivedRequest.getSoap());

                currentTestCase().onReceiveRequest(receivedRequest);

                String responseFile = currentTestCase().getResponseFile();
                if (responseFile != null) {
                    try {
                        sendResponseFromFile(responseFile, response);
                    } catch (Exception e) {
                        LOG.error("An error has occurred when sending response " +
                                "from file '{}': {}", responseFile, e);
                    }
                } else {
                    LOG.error("Unknown request {}", target);
                }
            } catch (Exception ex) {
                LOG.error("Error when reading request", ex);
            } finally {
                baseRequest.setHandled(true);
            }
        }

        private void sendResponseFromFile(String fileName,
                HttpServletResponse response) throws Exception {
            String responseContentType =
                    currentTestCase().getResponseContentType();

            LOG.debug("Sending response, content-type = {}",
                    responseContentType);

            response.setContentType(responseContentType);
            response.setStatus(HttpServletResponse.SC_OK);

            String file = MessageTestCase.QUERIES_DIR + '/' + fileName;

            try (InputStream fileIs = new FileInputStream(file);
                    InputStream responseIs =
                            currentTestCase().changeQueryId(fileIs)) {
                IOUtils.copy(responseIs, response.getOutputStream());
            }

            try (InputStream fis = currentTestCase().changeQueryId(
                    new FileInputStream(file))) {
                currentTestCase().onSendResponse(
                        new Message(fis, responseContentType));
            } catch (Exception e) {
                LOG.error("Error when sending response from file '{}': {}",
                        file, e.toString());
            }
        }
    }

    private static MessageTestCase currentTestCase() {
        return ProxyTestSuite.currentTestCase;
    }

    private static class DummyServiceKeyManager extends X509ExtendedKeyManager {

        private static final String ALIAS = "AuthKeyManager";

        @Override
        public String chooseClientAlias(String[] keyType, Principal[] issuers,
                Socket socket) {
            LOG.debug("chooseClientAlias");
            return ALIAS;
        }

        @Override
        public String chooseServerAlias(String keyType, Principal[] issuers,
                Socket socket) {
            LOG.debug("chooseServerAlias");
            return ALIAS;
        }

        @Override
        public X509Certificate[] getCertificateChain(String alias) {
            return new X509Certificate[] { serverCert };
        }

        @Override
        public String[] getClientAliases(String keyType, Principal[] issuers) {
            LOG.debug("getClientAliases");
            return null;
        }

        @Override
        public PrivateKey getPrivateKey(String alias) {
            LOG.debug("getPrivateKey {} - {}", alias, serverKey);
            return serverKey;
        }

        @Override
        public String[] getServerAliases(String keyType, Principal[] issuers) {
            LOG.debug("getServerAliases");
            return null;
        }

        @Override
        public String chooseEngineClientAlias(String[] keyType, Principal[] issuers,
                SSLEngine engine) {
            LOG.debug("chooseEngineClientAlias");
            return ALIAS;
        }

        @Override
        public String chooseEngineServerAlias(String keyType, Principal[] issuers,
                SSLEngine engine) {
            LOG.debug("chooseEngineServerAlias");
            return ALIAS;
        }
    }

    private static class DummyServiceTrustManager implements X509TrustManager {

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            LOG.debug("getAcceptedIssuers {}", serverCert);
            return new X509Certificate[] { serverCert };
        }

        @Override
        public void checkClientTrusted(X509Certificate[] certs, String authType)
                throws CertificateException {
            LOG.debug("checkClientTrusted");
        }

        @Override
        public void checkServerTrusted(X509Certificate[] certs, String authType)
                throws CertificateException {
            LOG.debug("checkServerTrusted");
        }
    }
}
