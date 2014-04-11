package com.antwerkz.fengshui;

import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static java.lang.String.format;

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
        
        Files.walkFileTree(settingsMirror.toPath(), new SynchronizingFileVisitor(settingsMirror, target));
    }

}
