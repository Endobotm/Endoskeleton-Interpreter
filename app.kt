import java.io.File

val variables = mutableMapOf<String, String>()

fun lexer(code: String): List<String> {
    val assignmentPattern = Regex("""(\w+)\s*=\s*"([^"]*)"""")
    val printPattern = Regex("""print\("([^"]*)"\)|print\((\w+)\)""")
    
    // Match assignment
    assignmentPattern.matchEntire(code)?.let {
        val (varName, varValue) = it.destructured
        return listOf("assign", varName, varValue)
    }
    
    // Match print statement
    printPattern.matchEntire(code)?.let {
        return if (it.groups[1] != null) {
            listOf("print", it.groups[1]!!.value)
        } else {
            listOf("print_var", it.groups[2]!!.value)
        }
    }
    
    throw IllegalArgumentException("Syntax error: Could not parse the line.")
}

fun interpreter(tokens: List<String>) {
    when (tokens[0]) {
        "assign" -> {
            val (varName, varValue) = tokens.drop(1)
            variables[varName] = varValue
        }
        "print" -> {
            val text = tokens[1]
            println(text)
        }
        "print_var" -> {
            val varName = tokens[1]
            println(variables[varName] ?: "Error: Variable '$varName' is not defined.")
        }
    }
}

// Main function to read the file and execute code
fun runFile(filename: String) {
    // Check for .endoskeleton extension
    if (!filename.endsWith(".endoskeleton")) {
        println("Error: File must have a .endoskeleton extension.")
        return
    }

    // Read file content and split by semicolons
    val code = File(filename).readText().trim()
    val statements = code.split(";").map { it.trim() }.filter { it.isNotEmpty() }

    // Process each statement
    for (statement in statements) {
        val tokens = lexer(statement)
        interpreter(tokens)
    }
}

fun main() {
    runFile("test.endoskeleton")
}
