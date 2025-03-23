package org.woltDeliveryFee.tasks


import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import org.gradle.internal.os.OperatingSystem
import org.gradle.api.tasks.OutputFile
import java.io.File


abstract class StartServerTask : DefaultTask() {

    @OutputFile
    val pidFile = project.layout.buildDirectory.file("server.pid").get().asFile

    @TaskAction
    fun start() {
        val isWindows = OperatingSystem.current().isWindows
        val command =
            if (isWindows) listOf("cmd", "/c", "start", "/b", "gradlew.bat", "run")
            else listOf("./gradlew", "run")

        val process =
            ProcessBuilder(command)
                .directory(project.rootDir)
                .redirectOutput(ProcessBuilder.Redirect.INHERIT)
                .redirectError(ProcessBuilder.Redirect.INHERIT)
                .start()

        pidFile.parentFile.mkdirs()

        val pid = process.pid()
        pidFile.writeText(pid.toString())

        println("Server started (PID: $pid)")
        Thread.sleep(10000)
    }
}
