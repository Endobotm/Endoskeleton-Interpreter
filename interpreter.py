import os
import re


def lexer(code):
    token_pattern = r'print\("([^"]*)"\)'
    match = re.match(token_pattern, code)
    if match:
        return ["print", match.group(1)]
    else:
        raise ValueError("Syntax error: Could not parse the line.")


def parser(tokens):
    if tokens[0] == "print":
        return ("print", tokens[1])


def interpreter(parsed_code):
    command, text = parsed_code
    if command == "print":
        print(text)


def run_file(filename):
    if not filename.endswith(".endoskeleton"):
        print("Error: File must have a .endoskeleton extension.")
        return

    with open(filename, "r") as file:
        code = file.read()

    tokens = lexer(code)
    parsed_code = parser(tokens)
    interpreter(parsed_code)


run_file("test.endoskeleton")
