package org.liquigraph.core.rules;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;

class DeletionFileVisitor implements FileVisitor<Path> {

    @Override
    public FileVisitResult preVisitDirectory(Path dir,
                                             BasicFileAttributes attrs) throws IOException {
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFile(Path file,
                                     BasicFileAttributes attrs) throws IOException {

        Files.delete(file);
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFileFailed(Path file,
                                           IOException exception) throws IOException {
        throw exception;
    }

    @Override
    public FileVisitResult postVisitDirectory(Path dir,
                                              IOException exception) throws IOException {
        if (exception != null) {
            throw exception;
        }
        Files.delete(dir);
        return FileVisitResult.CONTINUE;
    }
}