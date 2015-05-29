package ee.ria.xroad.proxy.testsuite.testcases;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import ee.ria.xroad.proxy.testsuite.Message;
import ee.ria.xroad.proxy.testsuite.MessageTestCase;

import static ee.ria.xroad.common.ErrorCodes.CLIENT_X;
import static ee.ria.xroad.common.ErrorCodes.X_INVALID_SOAP;

/**
 * Client sends query with empty body.
 * Result: Client.* error message.
 */
public class EmptyQuery extends MessageTestCase {

    /**
     * Constructs the test case.
     */
    public EmptyQuery() {
        requestFileName = "empty.query";
    }

    @Override
    protected InputStream getQueryInputStream(String fileName)
            throws Exception {
        return new ByteArrayInputStream(new byte[] {});
    }

    @Override
    protected void validateFaultResponse(Message receivedResponse) {
        assertErrorCode(CLIENT_X, X_INVALID_SOAP);
    }
}