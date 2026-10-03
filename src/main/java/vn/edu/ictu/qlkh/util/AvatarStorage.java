package vn.edu.ictu.qlkh.util;

import java.io.File;
import java.io.IOException;

public final class AvatarStorage {

    private AvatarStorage() {
    }

    public static File getDirectory() throws IOException {
        String catalinaBase = System.getProperty("catalina.base");

        if (catalinaBase == null || catalinaBase.isBlank()) {
            throw new IOException("CATALINA_BASE is not available.");
        }

        File directory =
                new File(catalinaBase, "qlkh-data/avatars");

        if (!directory.exists() && !directory.mkdirs()) {
            throw new IOException(
                    "Cannot create persistent avatar directory."
            );
        }

        return directory.getCanonicalFile();
    }

    public static File resolve(String fileName) throws IOException {
        if (fileName == null
                || fileName.isBlank()
                || fileName.contains("/")
                || fileName.contains("\\")
                || fileName.contains("..")) {
            throw new IOException("Invalid avatar file name.");
        }

        return new File(
                getDirectory(),
                fileName
        ).getCanonicalFile();
    }
}
