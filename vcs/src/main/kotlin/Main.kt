package svcs

class CLI {
    private val core = Core()

    fun configCmd(args: Array<String>) {
        val newUsername: String? = if (args.size == 2) {
            args[1]
        } else {
            null
        }

        val currentUserName = core.username

        if (newUsername == null) {
            if (currentUserName == null) {
                println("Please, tell me who you are.")
            } else {
                println("The username is $currentUserName.")
            }
        } else {
            core.username = newUsername
            println("The username is $newUsername.")
        }
    }

    fun addCmd(args: Array<String>) {
        val filePath: String? = if (args.size == 2) {
            args[1]
        } else {
            null
        }

        val trackedFiles = core.getTrackedFiles()

        if (filePath == null) {
            if (trackedFiles.isEmpty()) {
                println("Add a file to the index.")
            } else {
                println("Tracked files:")
                trackedFiles.forEach { println(it) }
            }
        } else {
            try {
                core.addFile(filePath)
                println("The file '$filePath' is tracked.")
            } catch (e: Error) {
                println(e.message)
            }
        }
    }

    fun logCmd() {
        core.getCommits().reversed().also { list ->
            if (list.isEmpty()) {
                println("No commits yet.")
            } else {
                println("The last commits:")
                list.forEach { commit ->
                    println("commit ${commit.hash}")
                    println("Author: ${commit.username}")
                    println(commit.message + '\n')
                }
            }
        }
    }

    fun commitCmd(args: Array<String>) {
        if (args.size == 1) {
            println("Message was not passed.")
        } else {
            val message = args.slice(1 until args.size).joinToString(" ")
            try {
                core.commit(message)
                println("Changes are committed.")
            } catch (e: Error) {
                println(e.message)
            }

        }
    }

    fun checkoutCmd(args: Array<String>) {
        if(args.size == 1) {
            println("Commit id was not passed.")
            return
        }

        val commitId = args[1]

        try {
            core.checkout(commitId)
            println("Switched to commit $commitId.")
        } catch (e: Error) {
            println(e.message)
        }
    }

    fun helpCmd() {
        println(
            """
            These are SVCS commands:
            config     Get and set a username.
            add        Add a file to the index.
            log        Show commit logs.
            commit     Save changes.
            checkout   Restore a file.
        """.trimIndent()
        )
    }

    fun wrongCmd(cmd: String) {
        println("'$cmd' is not a SVCS command.")
    }
}

fun main(args: Array<String>) {
    val cli = CLI()
    if (args.isEmpty()) {
        cli.helpCmd()
        return
    }

    when (val cmd = args[0]) {
        "config" -> cli.configCmd(args)
        "add" -> cli.addCmd(args)
        "log" -> cli.logCmd()
        "commit" -> cli.commitCmd(args)
        "checkout" -> cli.checkoutCmd(args)
        "--help" -> cli.helpCmd()
        else -> cli.wrongCmd(cmd)
    }
}
