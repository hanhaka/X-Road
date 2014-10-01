package ee.cyber.sdsb.signer.tokenmanager.module;

import lombok.Data;

@Data
public class HardwareModuleType implements ModuleType {

    private final String type;

    private final String pkcs11LibraryPath;

    private final boolean pinVerificationPerSigning;

    private final boolean batchSingingEnabled;

    private final boolean forceReadOnly;

}
