package com.antwerkz.fengshui;

import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Repository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;

import static java.lang.String.format;
import static java.nio.file.StandardCopyOption.COPY_ATTRIBUTES;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

public class Syncer {
    private static final Logger LOG = LoggerFactory.getLogger(Syncer.class);
    private final String gitUrl;
    private final String tag;
    private final File settingsMirror;
    private File target = new File(".").getAbsoluteFile();

    public Syncer(final String gitUrl, final String tag, final File settingsMirror) {
        this.gitUrl = gitUrl;
        this.tag = tag;
        this.settingsMirror = settingsMirror;
    }

    public File getTarget() {
        return target;
    }

    public void setTarget(final File syncTarget) {
        this.target = syncTarget.getAbsoluteFile();
    }

    public void execute() throws IOException, GitAPIException {
        if (!settingsMirror.exists()) {
            LOG.info(format("Cloning %s in to %s", gitUrl, settingsMirror));
            Git git = new CloneCommand()
                          .setURI(gitUrl)
                          .setBranch(tag)
                          .setDirectory(settingsMirror)
                          .call();
        }
        Files.walkFileTree(settingsMirror.toPath(), new FileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(final Path dir, final BasicFileAttributes attrs) throws IOException {
                return dir.getFileName().toString().equals(".git")
                       ? FileVisitResult.SKIP_SUBTREE
                       : FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) throws IOException {
                String relative = file.toString().substring(settingsMirror.toString().length() + 1);
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
        });
    }
}
