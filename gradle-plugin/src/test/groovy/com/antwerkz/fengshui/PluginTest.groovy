package com.antwerkz.fengshui
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.testng.annotations.BeforeMethod
import org.testng.annotations.Test

import static org.testng.Assert.assertTrue

public class PluginTest {
    private Project project
    private File buildDir

    @BeforeMethod
    public createProject() {
        project = ProjectBuilder.builder().build()
        buildDir = project.getBuildDir()
        project.apply plugin: 'fengshui'
    }

    @Test
    public void defaults() {
        project.task("sync", type: SyncFilesTask) {
            gitUrl = new File("../core/build/remote.git").toURI().toURL().toString()
        }

        project.tasks.sync.sync()
        assertTrue(new File("fengshui", "file1").exists())
        assertTrue(new File(buildDir, "fengshui/mirror/file1").exists())
    }

    @Test
    public void overrides() {
        def synced = new File(buildDir, "synced")

        project.task("sync", type: SyncFilesTask) {
            gitUrl = new File("../core/build/remote.git").toURI().toURL().toString()
            settingsMirror = "$project.buildDir/overrides"
            settingsTarget = synced.getAbsolutePath()
        }

        project.tasks.sync.sync()
        assertTrue(synced.exists())
        assertTrue(new File(synced, "file1").exists())
    }
}
