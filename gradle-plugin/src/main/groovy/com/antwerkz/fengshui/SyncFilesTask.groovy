package com.antwerkz.fengshui

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

class SyncFilesTask extends DefaultTask {

    def gitUrl
    def settingsTarget = "fengshui"
    def tag = "master"
    def settingsMirror = "${project.buildDir}/fengshui/mirror"
    
    @TaskAction
    def sync() {
        def sync = new Syncer(gitUrl, tag, new File(settingsMirror));
        sync.setTarget(new File(settingsTarget))
        println "Syncing from $settingsMirror to $settingsTarget using tag ${tag}"
        sync.execute()
    }
}
    