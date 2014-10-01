package ee.cyber.sdsb.proxy.testsuite.testcases;

import ee.cyber.sdsb.proxy.testsuite.Message;
import ee.cyber.sdsb.proxy.testsuite.MessageTestCase;

/**
 * The simplest case -- normal message and normal response.
 * Result: client receives message.
 */
public class MetadataAllowedMethods extends MessageTestCase {
    public MetadataAllowedMethods() {
        requestFileName = "allowedMethods.query";
    }

    @Override
    protected void validateNormalResponse(Message receivedResponse)
            throws Exception {
        // Normal response, nothing more to check here.
    }
}
