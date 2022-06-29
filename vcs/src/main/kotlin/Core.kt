package svcs

import java.security.MessageDigest

import java.io.File
import java.lang.Error
import java.nio.file.Files
import java.nio.file.Path

fun ByteArray.toHex(): String = joinToString(separator = "") { eachByte -> "%02x".format(eachByte) }


data class Commit(val prevHash: String, val hash: String, val username: String, val message: String)


class Core {
    private val configFile = File("vcs", "config.txt")
    private val indexFile = File("vcs", "index.txt")
    private val logFile = File("vcs", "log.txt")

    init {
        Files.createDirectories(Path.of("./vcs"))
        if (!indexFile.exists()) {
            Files.createFile(Path.of("vcs", "index.txt"))
        }
        if (!configFile.exists()) {
            Files.createFile(Path.of("vcs", "config.txt"))
        }
        if (!logFile.exists()) {
            Files.createFile(Path.of("vcs", "log.txt"))
        }
    }

    fun getTrackedFiles(): List<String> {
        return indexFile.readLines()
    }

    fun addFile(filePath: String) {
        val newFile = File(filePath)
        if (!newFile.exists()) {
            throw Error("Can't find '$filePath'.")
        }
        indexFile.appendText(filePath + System.lineSeparator())
    }

    private fun writeConfig(data: Pair<String, String>) {
        configFile.writeText("${data.first}:${data.second}")
    }

    private fun readConfig(): Map<String, String> {
        val list = configFile.readLines().map {
            val (k, v) = it.split(":")
            Pair(k, v)
        }

        return list.associate { it }
    }

    fun getCommits(): List<Commit> {
        return logFile.readLines().map {
            val (prevHash, hash, username, message) = it.split("\t")
            Commit(prevHash, hash, username, message)
        }
    }

    fun commit(message: String) {
        val hashInstance = MessageDigest.getInstance("SHA-256")
        val prevHash = getCommits().lastOrNull()?.hash ?: "0"

        getTrackedFiles().forEach { filePath ->
            val file = File(filePath)
            hashInstance.update(file.readBytes())
        }
        val hash = hashInstance.digest().toHex()

        if (hash == prevHash) {
            throw Error("Nothing to commit.")
        }

        val commitFolder = File(Path.of("vcs", "commits", hash).toString())
        commitFolder.mkdir()
        getTrackedFiles().forEach { filePath ->
            val file = File(filePath)
            file.copyTo(File(commitFolder.path, file.name))
        }

        logFile.appendText("$prevHash\t$hash\t${readConfig()["username"]}\t$message\n")
    }

    fun checkout(commitId: String) {
        val path = Path.of("vcs", "commits", commitId).toString()
        if (!File(path).exists()) {
            throw Error("Commit does not exist.")
        }

        if (File(path).isDirectory) {
            File(path).listFiles()?.forEach {
                val newFile = File("./" + it.name)
                if(!newFile.exists()) {
                    newFile.createNewFile()
                }
                it.copyTo(newFile, true)
            }
        }
    }

    var username: String?
        get() {
            return this.readConfig()["username"]
        }
        set(value) {
            if (value != null) {
                this.writeConfig(Pair("username", value))
            }
        }
}
