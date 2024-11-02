import java.io.File

sealed class InterpreterValue {
    data class StringValue(val value: String) : InterpreterValue()
    data class ArrayValue(val value: List<String>) : InterpreterValue()
}

val variables = mutableMapOf<String, InterpreterValue>()

fun lexer(code: String): List<String> {
    val assignmentPattern = Regex("""(\w+)\s*=\s*"([^"]*)"""")
    val printPattern = Regex("""print\("([^"]*)"\)|print\((\w+)\)""")
    val arrayDeclarationPattern = Regex("""(\w+)\s*=\s*\[\s*((?:"[^"]*"|'[^']*'|\w+)(?:\s*,\s*(?:"[^"]*"|'[^']*'|\w+))*)\s*\]""")
    val arrayAccessPattern = Regex("""(\w+)\s*=\s*(\w+)\[(\d+)\]""")
    val standaloneArrayAccessPattern = Regex("""(\w+)\[(\d+)\]""")
    
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

    // Match array declaration
    arrayDeclarationPattern.matchEntire(code)?.let {
        val (varName, elements) = it.destructured
        val array = elements.split(",").map { element ->
            element.trim().let { e ->
                when {
                    e.startsWith("\"") && e.endsWith("\"") -> e.substring(1, e.length - 1)
                    e.startsWith("'") && e.endsWith("'") -> e.substring(1, e.length - 1)
                    else -> e
                }
            }
        }
        return listOf("declare_array", varName, array.joinToString(","))
    }

    // Match array access with assignment
    arrayAccessPattern.matchEntire(code)?.let {
        val (varName, arrayName, index) = it.destructured
        return listOf("access_array", varName, arrayName, index)
    }

    // Match standalone array access
    standaloneArrayAccessPattern.matchEntire(code)?.let {
        val (arrayName, index) = it.destructured
        return listOf("standalone_array_access", arrayName, index)
    }
    
    throw IllegalArgumentException("Syntax error: Could not parse the line '$code'")
}

fun interpreter(tokens: List<String>) {
    when (tokens[0]) {
        "assign" -> {
            val (varName, varValue) = tokens.drop(1)
            variables[varName] = InterpreterValue.StringValue(varValue)
        }
        "print" -> {
            val text = tokens[1]
            println(text)
        }
        "print_var" -> {
            val varName = tokens[1]
            when (val value = variables[varName]) {
                is InterpreterValue.StringValue -> println(value.value)
                is InterpreterValue.ArrayValue -> println(value.value.joinToString(", "))
                null -> println("Error: Variable '$varName' is not defined.")
            }
        }
        "declare_array" -> {
            val (varName, elementsString) = tokens.drop(1)
            val elements = elementsString.split(",").map { it.trim() }
            variables[varName] = InterpreterValue.ArrayValue(elements)
        }
        "access_array" -> {
            val (varName, arrayName, index) = tokens.drop(1)
            when (val array = variables[arrayName]) {
                is InterpreterValue.ArrayValue -> {
                    val indexInt = index.toIntOrNull()
                    if (indexInt != null && indexInt >= 0 && indexInt < array.value.size) {
                        variables[varName] = InterpreterValue.StringValue(array.value[indexInt])
                    } else {
                        println("Index out of bounds: $index")
                    }
                }
                else -> println("Variable '$arrayName' is not an array")
            }
        }
        "standalone_array_access" -> {
            val (arrayName, index) = tokens.drop(1)
            when (val array = variables[arrayName]) {
                is InterpreterValue.ArrayValue -> {
                    val indexInt = index.toIntOrNull()
                    if (indexInt != null && indexInt >= 0 && indexInt < array.value.size) {
                        println(array.value[indexInt])
                    } else {
                        println("Index out of bounds: $index")
                    }
                }
                else -> println("Variable '$arrayName' is not an array")
            }
        }
    }
}

fun runFile(filename: String) {
    if (!filename.endsWith(".endoskeleton")) {
        println("Error: File must have a .endoskeleton extension.")
        return
    }

    val code = File(filename).readText().trim()
    val statements = code.split(";").map { it.trim() }.filter { it.isNotEmpty() }

    for (statement in statements) {
        try {
            val tokens = lexer(statement)
            interpreter(tokens)
        } catch (e: IllegalArgumentException) {
            println("Error processing statement: '$statement'")
            println("Error message: ${e.message}")
            return
        }
    }
}

fun main() {
    runFile("test.endoskeleton")
}