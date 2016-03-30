package com.kon.orientdb.transform.service.impl;

import com.kon.orientdb.transform.exception.TransformationException;
import com.kon.orientdb.transform.service.DirectoryTraversalService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by kshevchuk on 10/5/2015.
 */
@Component
public class DirectoryTraverseServiceImpl implements DirectoryTraversalService {

    static final Logger logger = LogManager.getLogger();
    @Override
    public List<String> traverseDirectory(Path directory) throws TransformationException {
        List<String> filesList = new ArrayList<>();
        try {
            filesList.addAll(listFiles(directory));
        } catch (IOException e) {
            throw new TransformationException("Failed to traverse directory.", e);
        }

        return filesList;
    }

    private List<String> listFiles(Path directory) throws IOException {
        final List<String> filesList = new ArrayList<>();

        Files.walkFileTree(directory, new SimpleFileVisitor<Path>() {

            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                if (com.google.common.io.Files.getFileExtension(file.getFileName().toString()).equalsIgnoreCase("csv")) {
                    filesList.add(file.toString());
                }
                return FileVisitResult.CONTINUE;
            }


            @Override
            public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                return FileVisitResult.CONTINUE;
            }


            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                return FileVisitResult.CONTINUE;
            }

        });

        return filesList;
    }

}
