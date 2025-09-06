package dev.mark.server;

import java.io.*;
import java.util.zip.*;

public class jar2zip {
    public static void main(String[] args) {

        File jarFile = new File(""); // input jar (jar -> zip)
        File zipFile = new File(""); // output zip

        try (
                FileInputStream fis = new FileInputStream(jarFile);
                ZipInputStream jarStream = new ZipInputStream(fis);
                FileOutputStream fos = new FileOutputStream(zipFile);
                ZipOutputStream zipOut = new ZipOutputStream(fos)
        ) {
            ZipEntry entry;
            while ((entry = jarStream.getNextEntry()) != null) {
                if (entry.getName().startsWith("META-INF/")) {
                    continue;
                }

                ZipEntry newEntry = new ZipEntry(entry.getName());
                zipOut.putNextEntry(newEntry);

                byte[] buffer = new byte[1024];
                int len;
                while ((len = jarStream.read(buffer)) > 0) {
                    zipOut.write(buffer, 0, len);
                }

                zipOut.closeEntry();
                jarStream.closeEntry();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
