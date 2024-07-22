package com.jack.jobportal.util;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

public class FileDownloadUtil {
    public Resource getFileAsResource(String downloadDir, String fileName) throws IOException {
        Path path = Paths.get(downloadDir);

        Optional<Path> foundFile = Files.list(path)
                .filter(file -> file.getFileName()
                        .toString()
                        .startsWith(fileName))
                .findFirst();

        if (foundFile.isPresent()) {
            return new UrlResource(foundFile.get().toUri());
        }

        return null;
    }
}
