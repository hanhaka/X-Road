package ee.cyber.sdsb.common;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.EnumSet;
import java.util.Set;

import static java.nio.file.attribute.PosixFilePermission.*;

/**
 * Default filepaths for application configuration and artifacts based on FHS.
 */
public class DefaultFilepaths {

    static final String CONF_PATH = "/etc/sdsb/";

    static final String SERVER_DATABASE_PROPERTIES = "db.properties";

    static final String KEY_CONFIGURATION_FILE = "keyconf.xml";

    static final String GLOBAL_CONFIGURATION_FILE = "globalconf.xml";

    static final String DEVICE_CONFIGURATION_FILE = "devices.ini";

    static final String LOG_PATH = "/var/log/sdsb/";

    static final String SECURE_LOG_PATH = "/var/lib/sdsb/";

    static final String OCSP_CACHE_PATH = "/var/cache/sdsb/";

    static final String CONF_BACKUP_PATH = "/var/lib/sdsb/backup/";

    static final String V5_IMPORT_PATH = "/var/lib/sdsb/import";

    static final String SECURE_LOG_FILE = SECURE_LOG_PATH + "slog";

    static final String TEMP_FILES_PATH = "/var/tmp/sdsb/";

    static final String ASYNC_DB_PATH = "/var/spool/sdsb/";

    static final String ASYNC_SENDER_CONFIGURATION_FILE =
            "async-sender.properties";

    static final String PROXY_MONITOR_AGENT_CONFIGURATION_FILE =
            "monitor-agent.ini";

    private static FileAttribute<Set<PosixFilePermission>> permissions =
            PosixFilePermissions.asFileAttribute(EnumSet.of(
                    OWNER_READ, OWNER_WRITE, GROUP_READ, GROUP_WRITE));

    public static Path createTempFile(String prefix, String suffix)
            throws IOException {
        Path tempDirPath = Paths.get(SystemProperties.getTempFilesPath());
        return createTempFile(tempDirPath, prefix, suffix);
    }

    public static Path createTempFile(Path tempDirPath, String prefix,
            String suffix) throws IOException {
        if (!Files.exists(tempDirPath)) {
            Files.createDirectory(tempDirPath);
        }

        return Files.createTempFile(tempDirPath, prefix, suffix, permissions);
    }
}
