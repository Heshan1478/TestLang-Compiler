# TestLang++ - HTTP API Testing DSL

**SE2052 Programming Paradigms - Individual Assignment**  
A Domain-Specific Language (DSL) for HTTP API testing that compiles into executable JUnit 5 tests.

**Author:** E.H. Anjana  
**University:** Sri Lanka Institute of Information Technology (SLIIT)

---

## Table of Contents

- [Overview](#overview)
- [Features](#features)
- [Project Structure](#project-structure)
- [Prerequisites](#prerequisites)
- [Build Instructions](#build-instructions)
- [Usage](#usage)
- [Language Syntax](#language-syntax)
- [Examples](#examples)
- [Error Handling](#error-handling)
- [Testing the Compiler](#testing-the-compiler)
- [Assignment Requirements](#assignment-requirements)

---

## Overview

TestLang++ is a compiler that translates declarative HTTP API test specifications into Java JUnit 5 test code. The compiler consists of three main phases: lexical analysis using JFlex, parsing using Java CUP, and code generation that produces executable JUnit tests.

The generated tests use Java 11+ HttpClient for making HTTP requests and JUnit 5 assertions for validation. This approach allows testers to write API tests in a simple, readable DSL format rather than directly coding in Java.

---

## Features

This compiler supports the following features:

- HTTP request methods: GET, POST, PUT, DELETE
- Configuration block for base URL and default headers
- Variable declarations with string and integer types
- Variable substitution in request paths and bodies
- Request-specific headers and JSON body payloads
- Multiple assertion types: status codes, header validation, body content checking
- Comprehensive error detection with helpful error messages
- Clean code generation producing idiomatic JUnit 5 tests

---

## Project Structure

```
TestLangPP/
├── src/
│   ├── ASTmodel/              # Abstract Syntax Tree node classes
│   │   ├── Assertion.java     # Assertion AST node
│   │   ├── Config.java        # Configuration block AST node
│   │   ├── Request.java       # HTTP request AST node
│   │   ├── TestCase.java      # Test case AST node
│   │   └── Variable.java      # Variable declaration AST node
│   ├── lexer.flex             # JFlex lexer specification
│   ├── parser.cup             # Java CUP parser grammar
│   ├── Main.java              # Compiler main entry point
│   ├── CodeGenerator.java     # JUnit test code generator
│   ├── SemanticChecker.java   # Semantic validation logic
│   ├── TestLexer.java         # Utility for debugging lexer
│   ├── Lexer.java             # Generated lexer (created during build)
│   ├── Parser.java            # Generated parser (created during build)
│   └── sym.java               # Token symbol definitions (generated)
├── lib/
│   ├── jflex-1.9.1.jar
│   ├── java-cup-11b-20151001.jar
│   ├── java-cup-runtime-11b-20160615.jar
│   └── junit-platform-console-standalone-1.9.3.jar
├── example.test               # Valid test file demonstrating features
├── invalid1.test              # Example: syntax error (missing semicolon)
├── invalid2.test              # Example: semantic error (insufficient assertions)
├── invalid3.test              # Example: undefined variable reference
├── invalid4.test              # Example: type error (body not a string)
└── README.md                  # This documentation file
```

---

## Prerequisites

To build and run this compiler, you need:

- Java Development Kit (JDK) version 11 or higher
- JFlex 1.9.1 (included in lib/)
- Java CUP 0.11b (included in lib/)
- JUnit Platform Console Standalone 1.9.3 (included in lib/)

All required JAR files are included in the lib/ directory.

---

## Build Instructions

Follow these steps to build the compiler from source.

### Step 1: Generate the Lexer

Run JFlex to generate the lexical analyzer from the specification:

```bash
java -cp "lib/jflex-1.9.1.jar" jflex.Main src/lexer.flex
```

This creates `src/Lexer.java`.

### Step 2: Generate the Parser

Run Java CUP to generate the parser from the grammar specification:

```bash
java -cp "lib/java-cup-11b-20151001.jar;lib/java-cup-runtime-11b-20160615.jar" java_cup.Main -parser Parser -symbols sym src/parser.cup
```

This creates `Parser.java` and `sym.java` in the project root. Move these files to the `src/` directory if needed.

### Step 3: Compile All Source Files

Compile the entire compiler including generated files:

```bash
javac -cp "lib/java-cup-runtime-11b-20160615.jar;src" src/ASTmodel/*.java src/Lexer.java src/Parser.java src/sym.java src/Main.java src/CodeGenerator.java src/SemanticChecker.java
```

If you encounter any compilation errors, ensure all generated files are in the correct location.

---

## Usage

### Compiling a Test File

To compile a TestLang++ test file into JUnit tests:

```bash
java -cp "lib/java-cup-runtime-11b-20160615.jar;src" Main example.test
```

The compiler will:
1. Parse the input file and validate syntax
2. Perform semantic checking (variable references, assertion counts)
3. Generate `GeneratedTests.java` containing JUnit 5 test methods

### Compiling the Generated Tests

Once generated, compile the JUnit test file:

```bash
javac -cp "lib/junit-platform-console-standalone-1.9.3.jar;src" src/GeneratedTests.java
```

### Running the Generated Tests

Execute the compiled tests using JUnit Platform Console:

```bash
java -jar lib/junit-platform-console-standalone-1.9.3.jar --class-path src --select-class GeneratedTests
```

Note: The tests will attempt to connect to the configured backend URL (default: http://localhost:8080). If no backend is running, tests will fail with connection errors or 404 responses. This is expected behavior and demonstrates that the generated code is functional.

---

## Language Syntax

The TestLang++ language consists of three main sections: configuration, variable declarations, and test cases.

### Configuration Block

The optional configuration block specifies the base URL and default headers for all requests:

```
config {
  base_url = "http://localhost:8080";
  header "Content-Type" = "application/json";
  header "Authorization" = "Bearer token123";
}
```

If no base_url is specified, request paths must be absolute URLs. Default headers are applied to all requests but can be overridden per request.

### Variable Declarations

Variables can be declared with either string or integer values:

```
let user = "admin";
let id = 42;
let token = "abc123";
```

Variables are referenced in strings and paths using the dollar sign prefix: `$varname`.

### Test Cases

Each test case must have a unique name and contain at least one request and at least two assertions:

```
test TestName {
  METHOD "path" {
    header "Key" = "Value";
    body = "{ \"field\": \"value\" }";
  };
  expect status = 200;
  expect header "Content-Type" contains "json";
  expect body contains "success";
}
```

### HTTP Request Methods

The following request formats are supported:

**GET and DELETE (no request body):**
```
GET "/api/users";
DELETE "/api/users/42";
```

**POST and PUT (with optional request body and headers):**
```
POST "/api/login" {
  header "Content-Type" = "application/json";
  body = "{ \"username\": \"admin\" }";
};

PUT "/api/users/42" {
  body = "{ \"role\": \"ADMIN\" }";
};
```

Note: The semicolon after the closing brace is required.

### Assertions

Each test must include at least two assertions. Supported assertion types:

**Status code equality:**
```
expect status = 200;
```

**Header exact match:**
```
expect header "Content-Type" = "application/json";
```

**Header substring match:**
```
expect header "Content-Type" contains "json";
```

**Body substring match:**
```
expect body contains "\"success\": true";
```

### Variable Substitution

Variables can be used in request paths and body strings:

```
let id = 42;
let username = "admin";

test Example {
  GET "/api/users/$id";
  expect status = 200;
  
  POST "/api/login" {
    body = "{ \"username\": \"$username\" }";
  };
  expect status = 200;
}
```

At code generation time, variable references are replaced with their declared values.

---

## Examples

### Complete Working Example

Here is a complete example demonstrating all language features (example.test):

```
config {
  base_url = "http://localhost:8080";
  header "Content-Type" = "application/json";
}

let user = "admin";
let id = 42;

test Login {
  POST "/api/login" {
    body = "{ \"username\": \"$user\", \"password\": \"1234\" }";
  };
  expect status = 200;
  expect header "Content-Type" contains "json";
  expect body contains "\"token\":";
}

test GetUser {
  GET "/api/users/$id";
  expect status = 200;
  expect body contains "\"id\": 42";
}
```

This example shows:
- Configuration with base URL and default header
- Variable declarations (string and integer)
- POST request with body and variable substitution
- GET request with variable in path
- Multiple assertion types (status, header contains, body contains)

---

## Error Handling

The compiler provides detailed error messages for both syntax and semantic errors.

### Syntax Errors

Syntax errors occur when the input does not conform to the grammar. The parser reports the location and provides helpful hints.

**Example: Missing semicolon after GET request**

Input (invalid1.test):
```
config {
  base_url = "http://localhost:8080";
}

test MissingSemicolon {
  GET "/test"
  expect status = 200;
  expect body contains "ok";
}
```

Error output:
```
SYNTAX ERROR:
Location: Line 8, Column 3
Unexpected token: EXPECT
Hint: Did you forget a semicolon after the previous statement?
```

### Semantic Errors

Semantic errors occur when the code is syntactically correct but violates language rules.

**Example 1: Insufficient assertions**

Input (invalid2.test):
```
config {
  base_url = "http://localhost:8080";
}

test NotEnoughAssertions {
  GET "/api/test";
  expect status = 200;
}
```

Error output:
```
SEMANTIC ERRORS:
Test 'NotEnoughAssertions': Must have at least 2 assertions (found 1)
```

**Example 2: Undefined variable reference**

Input (invalid3.test):
```
config {
  base_url = "http://localhost:8080";
}

let user = "admin";

test UndefinedVariable {
  GET "/api/users/$id";
  expect status = 200;
  expect body contains "user";
}
```

Error output:
```
SEMANTIC ERRORS:
Test 'UndefinedVariable', path: Undefined variable '$id'
```

**Example 3: Type error in body**

Input (invalid4.test):
```
config {
  base_url = "http://localhost:8080";
}

test BodyNotString {
  POST "/api/test" {
    body = 123;
  };
  expect status = 200;
  expect body contains "ok";
}
```

Error output:
```
SYNTAX ERROR:
Location: Line 8, Column 12
Unexpected token: NUMBER
Hint: Check if you need quotes around this value.
```

---

## Testing the Compiler

To verify the compiler works correctly, test it with both valid and invalid inputs.

### Test Valid Input

```bash
java -cp "lib/java-cup-runtime-11b-20160615.jar;src" Main example.test
```

Expected: Successful parsing, validation, and code generation.

### Test Invalid Inputs

```bash
# Syntax error - missing semicolon
java -cp "lib/java-cup-runtime-11b-20160615.jar;src" Main invalid1.test

# Semantic error - not enough assertions
java -cp "lib/java-cup-runtime-11b-20160615.jar;src" Main invalid2.test

# Semantic error - undefined variable
java -cp "lib/java-cup-runtime-11b-20160615.jar;src" Main invalid3.test

# Type error - body must be string
java -cp "lib/java-cup-runtime-11b-20160615.jar;src" Main invalid4.test
```

Each should produce clear, helpful error messages without crashing.

---

## Assignment Requirements

This project fulfills all requirements specified in the SE2062 assignment:

**Language Design Fidelity (25 marks):**
- Implements all required constructs: config, variables, tests, requests, assertions
- Handles edge cases with appropriate error messages
- Well-documented syntax and semantics

**Scanner & Parser Quality (30 marks):**
- JFlex-based scanner with comprehensive token recognition
- Java CUP parser with proper grammar rules
- Meaningful error messages with line and column information
- Graceful error recovery where possible

**Code Generation (30 marks):**
- Generates compilable, idiomatic JUnit 5 code
- Uses Java 11+ HttpClient for HTTP requests
- Proper variable substitution implementation
- Clean code structure with reusable helper methods

**Demo & Examples (15 marks):**
- Working end-to-end pipeline from DSL to executable tests
- Multiple examples demonstrating language features
- Clear demonstration of error handling
- Complete documentation

---

## Conclusion

This compiler demonstrates a complete implementation of a domain-specific language for API testing. The project showcases lexical analysis, parsing, semantic validation, and code generation techniques learned in SE2052.



---

**Date:** October 2025  
**Course:** SE2052 Programming Paradigms  
**Institution:** Sri Lanka Institute of Information Technology (SLIIT)