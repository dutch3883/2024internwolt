package org.woltDeliveryFee.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.internal.os.OperatingSystem
import java.io.File

abstract class StopServerTask : DefaultTask() {

    @InputFile
    val pidFile = project.layout.buildDirectory.file("server.pid").get().asFile

    @TaskAction
    fun stop() {
        if (!pidFile.exists()) {
            println("Server PID file not found. Is the server running?")
            return
        }

        val pid = pidFile.readText().trim()
        val isWindows = OperatingSystem.current().isWindows

        val command =
            if (isWindows) listOf("taskkill", "/PID", pid, "/F") else listOf("kill", "-9", pid)

        val process = ProcessBuilder(command).start()

        process.waitFor()

        if (process.exitValue() == 0) {
            println("Server (PID: $pid) stopped successfully.")
            pidFile.delete()
        } else {
            println("Failed to stop server with PID $pid.")
        }
    }
}
