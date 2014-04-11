package com.antwerkz.fengshui

import org.gradle.api.Plugin
import org.gradle.api.Project

public class FengShuiPlugin implements Plugin<Project> {
    @Override
    public void apply(final Project project) {
        project.extensions.create("fengshui", FengShuiPluginExtension)
    }
}

class FengShuiPluginExtension {
    String gitUrl
    String syncTarget
}