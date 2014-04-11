package com.antwerkz.fengshui;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;

import static java.nio.file.StandardCopyOption.COPY_ATTRIBUTES;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

class SynchronizingFileVisitor implements FileVisitor<Path> {

    private final File source;
    private final File target;

    SynchronizingFileVisitor(final File source, final File target) {
        this.source = source;
        this.target = target;
    }

    @Override
    public FileVisitResult preVisitDirectory(final Path dir, final BasicFileAttributes attrs) throws IOException {
        return dir.getFileName().toString().equals(".git")
               ? FileVisitResult.SKIP_SUBTREE
               : FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) throws IOException {
        String relative = file.toString().substring(source.toString().length() + 1);
        File destination = new File(target, relative);
        if (!destination.exists()
            || Files.getLastModifiedTime(destination.toPath()).toMillis() < Files.getLastModifiedTime(file).toMillis()) {
            destination.getParentFile().mkdirs();
            Files.copy(file, destination.toPath(), REPLACE_EXISTING, COPY_ATTRIBUTES);
        }
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFileFailed(final Path file, final IOException exc) throws IOException {
        throw new UnsupportedOperationException("Not implemented yet!");
    }

    @Override
    public FileVisitResult postVisitDirectory(final Path dir, final IOException exc) throws IOException {
        return FileVisitResult.CONTINUE;
    }
}
