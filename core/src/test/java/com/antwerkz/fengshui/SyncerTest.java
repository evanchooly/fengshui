package com.antwerkz.fengshui;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.InitCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.dircache.DirCache;
import org.eclipse.jgit.revwalk.RevCommit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static java.lang.String.format;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

@Test
public class SyncerTest {
    private static final Logger LOG = LoggerFactory.getLogger(SyncerTest.class);
    private final File localDir = new File("build/local.git").getAbsoluteFile();
    private final File remoteDir = new File("build/remote.git").getAbsoluteFile();
    private final String gitUrl;
    private File syncTarget = new File("build/syncTarget").getAbsoluteFile();

    public SyncerTest() throws MalformedURLException {
        gitUrl = remoteDir.toURI().toURL().toString();
    }

    @BeforeClass
    public void createData() {
        deleteTree(localDir);
        deleteTree(remoteDir);
        deleteTree(syncTarget);
        initLocalGitRepo();
    }

    private void initLocalGitRepo() {
        try {
            Git repo = new InitCommand()
                           .setDirectory(remoteDir)
                           .call();
            writeFile("file1", "I'm file 1!");
            writeFile("file2", "I have a bit more content");
            new File(remoteDir, "subdir").mkdirs();
            writeFile("subdir/file3", "");
            DirCache cache = repo
                                 .add()
                                 .addFilepattern("file1")
                                 .addFilepattern("file2")
                                 .addFilepattern("subdir/file3")
                                 .call();
            RevCommit commit = repo.commit()
                                   .setMessage("adding initial test files")
                                   .call();
            assertTrue(commit.getId() != null);
        } catch (GitAPIException e) {
            throw new RuntimeException(e);
        }
    }

    private Path deleteTree(final File path) {
        try {
            return Files.walkFileTree(path.toPath(), new FileVisitor<Path>() {
                @Override
                public FileVisitResult preVisitDirectory(final Path dir, final BasicFileAttributes attrs) throws IOException {
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) throws IOException {
                    Files.delete(file);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFileFailed(final Path file, final IOException exc) throws IOException {
                    return FileVisitResult.TERMINATE;
                }

                @Override
                public FileVisitResult postVisitDirectory(final Path dir, final IOException exc) throws IOException {
                    Files.delete(dir);
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void writeFile(final String name, final String content) {
        File output = new File(remoteDir, name);
        try (PrintWriter writer = new PrintWriter(new FileWriter(output))) {
            writer.println(content);
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @Test
    public void testExecute() throws Exception {
        Syncer syncer = new Syncer(gitUrl, "master", localDir);
        syncer.setTarget(syncTarget);
        syncer.execute();
        List<Path> local = listFiles(localDir.toPath());
        List<Path> sync = listFiles(syncTarget.toPath());
        System.out.println("local = " + local);
        System.out.println("sync = " + sync);
        assertEquals(local.size(), sync.size());
        Iterator<Path> localIterator = local.iterator();
        Iterator<Path> syncIterator = sync.iterator();
        while(localIterator.hasNext()) {
            Path localNext = localIterator.next();
            Path syncNext = syncIterator.next();
            assertEquals(Files.readAllBytes(localNext), Files.readAllBytes(syncNext), 
                         format("%s and %s should have the same content", localNext, syncNext));
        }
    }

    private List<Path> listFiles(final Path dir) throws IOException {
        final List<Path> files = new ArrayList<>();
        Files.walkFileTree(dir, new FileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(final Path dir, final BasicFileAttributes attrs) throws IOException {
                return dir.getFileName().toString().equals(".git")
                                     ? FileVisitResult.SKIP_SUBTREE
                                     : FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) throws IOException {
                files.add(file);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFileFailed(final Path file, final IOException exc) throws IOException {
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(final Path dir, final IOException exc) throws IOException {
                return FileVisitResult.CONTINUE;
            }
        });
        
        return files;
    }
}
