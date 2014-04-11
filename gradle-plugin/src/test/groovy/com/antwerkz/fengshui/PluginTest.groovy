package com.antwerkz.fengshui
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.testng.annotations.BeforeMethod
import org.testng.annotations.Test

import static org.testng.Assert.assertTrue

public class PluginTest {
    private Project project

    @BeforeMethod
    public createProject() {
        project = ProjectBuilder.builder().build()
        this.project.apply plugin: 'fengshui'
        this.project.setBuildDir(new File("build").getAbsoluteFile())
    }

    @Test
    public void defaults() {
        project.task("sync", type: SyncFilesTask) {
            gitUrl = new File("../core/build/remote.git").toURI().toURL().toString()
        }

        project.tasks.sync.sync()
        assertTrue(new File("fengshui", "file1").exists())
        assertTrue(new File("build/fengshui/mirror", "file1").exists())
    }

    @Test
    public void overrides() {
        def synced = new File("build/synced")

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
