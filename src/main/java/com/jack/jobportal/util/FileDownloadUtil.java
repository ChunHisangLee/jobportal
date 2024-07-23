package com.jack.jobportal.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

public class FileDownloadUtil {
    private static final Logger logger = LoggerFactory.getLogger(FileDownloadUtil.class);

    public Resource getFileAsResource(String downloadDir, String fileName) throws IOException {
        Path directoryPath = Paths.get(downloadDir);

        try (var paths = Files.list(directoryPath)) {
            Optional<Path> foundFile = paths.filter(file -> file.getFileName().toString().startsWith(fileName))
                    .findFirst();

            if (foundFile.isPresent()) {
                return new UrlResource(foundFile.get().toUri());
            }
        } catch (IOException e) {
            logger.error("Failed to get file as resource", e);
        }

        return null;
    }
}
