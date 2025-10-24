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
- [Backend Setup (Optional)](#backend-setup-optional)
- [Language Syntax](#language-syntax)
- [Examples](#examples)
- [Error Handling](#error-handling)
- [Testing the Compiler](#testing-the-compiler)

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
- **Triple-quoted multiline strings** for cleaner JSON bodies (bonus feature)
- **Range-based status assertions** for flexible status code checking (bonus feature)
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
├── invalid2.test              # Example: type error (status must be integer)
├── invalid3.test              # Example: undefined variable reference
├── invalid4.test              # Example: lexical error (identifier starts with digit)
├── README.md                  # This documentation file
└── TestLangBackend/           # Optional Spring Boot backend for testing
    ├── pom.xml
    └── src/main/java/com/testlang/
        ├── TestLangBackendApp.java
        └── ApiController.java
```

---

## Prerequisites

To build and run this compiler, you need:

- Java Development Kit (JDK) version 11 or higher
- JFlex 1.9.1 (included in lib/)
- Java CUP 0.11b (included in lib/)
- JUnit Platform Console Standalone 1.9.3 (included in lib/)

All required JAR files are included in the lib/ directory.

For the optional backend:
- Java 17 or higher
- Maven (bundled with IntelliJ IDEA)

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

**Without Backend:** Tests will fail with connection errors or 404 responses. This demonstrates that the generated code is functional and correctly attempts HTTP requests.

**With Backend Running:** Tests will successfully execute and pass, demonstrating a complete end-to-end API testing workflow.

See [Backend Setup](#backend-setup-optional) for instructions on running the Spring Boot backend.

---

## Backend Setup (Optional)

A simple Spring Boot backend is provided to demonstrate the generated tests against real API endpoints.

### Backend Structure

```
TestLangBackend/
├── pom.xml
└── src/main/java/com/testlang/
    ├── TestLangBackendApp.java
    └── ApiController.java
```

### Starting the Backend

**Option 1: Using IntelliJ IDEA**
1. Open the `TestLangBackend` project in IntelliJ
2. Right-click `TestLangBackendApp.java`
3. Select "Run 'TestLangBackendApp.main()'"
4. Backend will start on http://localhost:8080

**Option 2: Using Maven Command Line**
```bash
cd TestLangBackend
mvn spring-boot:run
```

The console will display:
```
Backend running at http://localhost:8080
Available endpoints:
   POST http://localhost:8080/api/login
   GET  http://localhost:8080/api/users/42
```

### Available Endpoints

The backend implements two REST endpoints that match the example.test specifications:

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/login` | User authentication endpoint |
| GET | `/api/users/{id}` | Retrieve user information |

#### POST /api/login

Validates user credentials and returns an authentication token.

**Request:**
```json
{
  "username": "admin",
  "password": "1234"
}
```

**Response (200 OK):**
```json
{
  "token": "abc123xyz789",
  "success": true,
  "username": "admin"
}
```

**Response (401 Unauthorized) - Invalid credentials:**
```json
{
  "error": "Invalid credentials"
}
```

#### GET /api/users/{id}

Retrieves user information by user ID.

**Example:** `GET /api/users/42`

**Response (200 OK):**
```json
{
  "id": 42,
  "name": "Chathura Perera",
  "role": "USER",
  "active": true
}
```

### Testing with Postman

You can test the backend endpoints using Postman:

1. Download Postman from https://www.postman.com/downloads/
2. Create a new request
3. Test each endpoint:

**Test POST /api/login:**
- Method: POST
- URL: `http://localhost:8080/api/login`
- Headers: `Content-Type: application/json`
- Body (raw JSON):
  ```json
  {
    "username": "admin",
    "password": "1234"
  }
  ```

**Test GET /api/users/42:**
- Method: GET
- URL: `http://localhost:8080/api/users/42`

### Running Tests Against Backend

The backend must be started separately before running the generated tests.

With the backend running, your generated tests will successfully make HTTP requests:

```bash
# 1. Generate tests from DSL
java -cp "lib/java-cup-runtime-11b-20160615.jar;src" Main example.test

# 2. Compile generated tests
javac -cp "lib/junit-platform-console-standalone-1.9.3.jar;src" src/GeneratedTests.java

# 3. Run tests (all should pass!)
java -jar lib/junit-platform-console-standalone-1.9.3.jar --class-path src --select-class GeneratedTests
```

**Expected Output (Backend Running):**
```
Thanks for using JUnit! Support its development at https://junit.org/sponsoring

Test run finished after 492 ms
[         2 containers found      ]
[         2 containers started    ]
[         2 containers successful ]
[         2 tests found           ]
[         2 tests started         ]
[         2 tests successful      ]
[         0 tests failed          ]
```

**Backend Console Output:**
```
POST /api/login - username: admin
GET /api/users/42
```

**Expected Output (Backend Not Running):**
```
Test run finished after 123 ms
[         2 tests found           ]
[         2 tests started         ]
[         0 tests successful      ]
[         2 tests failed          ]

Failures:
  Connection refused: localhost:8080
```

**Note:** If the backend is not running, tests will fail with connection errors. This is expected behavior and demonstrates that the generated code correctly attempts to connect to the configured endpoints.

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

### Triple-Quoted Multiline Strings (Bonus Feature)

For cleaner, more readable JSON bodies, you can use triple-quoted strings that span multiple lines without escaping quotes:

**Traditional syntax (still supported):**
```
body = "{ \"username\": \"admin\", \"password\": \"1234\" }";
```

**Multiline syntax (bonus feature):**
```
body = """
{
  "username": "admin",
  "password": "1234"
}
""";
```

Benefits:
- No need to escape double quotes
- Natural JSON formatting
- Improved readability
- Variable substitution still works: `"username": "$user"`

### Assertions

Each test must include at least two assertions. Supported assertion types:

**Status code equality:**
```
expect status = 200;
```

**Status code range (bonus feature):**
```
expect status in 200..299;    // Any 2xx success code
expect status in 400..499;    // Any 4xx client error
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
    body = """
    {
      "username": "$username",
      "password": "1234"
    }
    """;
  };
  expect status in 200..299;
}
```

At code generation time, variable references are replaced with their declared values.

---

## Examples

### Complete Working Example

Here is a complete example demonstrating all language features including bonus features (example.test):

```
config {
  base_url = "http://localhost:8080";
  header "Content-Type" = "application/json";
}

let user = "admin";
let id = 42;

test Login {
  POST "/api/login" {
    body = """
    {
      "username": "$user",
      "password": "1234"
    }
    """;
  };
  expect status in 200..299;
  expect header "Content-Type" contains "json";
  expect body contains "token";
}

test GetUser {
  GET "/api/users/$id";
  expect status = 200;
  expect body contains "\"id\":42";
}
```

This example shows:
- Configuration with base URL and default header
- Variable declarations (string and integer)
- POST request with **multiline body** (bonus feature) and variable substitution
- GET request with variable in path
- Multiple assertion types including **range-based status check** (bonus feature)
- Traditional status equality assertion

---

## Error Handling

The compiler provides detailed error messages for both syntax and semantic errors across all phases: lexical analysis, parsing, and semantic validation.

### Lexical Errors

Lexical errors occur during token recognition when invalid character sequences are encountered.

**Example: Identifier starting with a digit**

Input (invalid4.test):
```
config {
  base_url = "http://localhost:8080";
}

let 2user = "admin";

test TestIdentifier {
  GET "/api/test";
  expect status = 200;
  expect body contains "ok";
}
```

Error output:
```
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
LEXICAL ERROR:
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
Location: Line 6, Column 5
Invalid identifier: '2user'

Error: Identifier cannot start with a digit
Hint: Variable names must start with a letter or underscore
      Valid examples: a2, user1, _temp
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
```

### Syntax Errors

Syntax errors occur when the input does not conform to the grammar. The parser reports the location and provides helpful hints.

**Example 1: Missing semicolon after GET request**

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
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
SYNTAX ERROR:
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
Location: Line 8, Column 3
Unexpected token: EXPECT

Hint: Did you forget a semicolon after the previous statement?
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
```

**Example 2: Status must be integer, not string**

Input (invalid2.test):
```
config {
  base_url = "http://localhost:8080";
}

test StatusAsString {
  GET "/api/test";
  expect status = "200";
  expect body contains "ok";
}
```

Error output:
```
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
SEMANTIC ERROR:
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
Location: Status assertion
Error: Status code must be an integer, not a string

Found: "200"
Expected: 200 (without quotes)

Correct usage:
  expect status = 200;      // Correct
  expect status = "200";    // Wrong!
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
```

### Semantic Errors

Semantic errors occur when the code is syntactically correct but violates language rules.

**Example: Undefined variable reference**

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
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
SEMANTIC ERRORS:
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
Test 'UndefinedVariable', path: Undefined variable '$id'
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
```

---

## Testing the Compiler

To verify the compiler works correctly, test it with both valid and invalid inputs.

### Test Valid Input

```bash
java -cp "lib/java-cup-runtime-11b-20160615.jar;src" Main example.test
```

Expected: Successful parsing, validation, and code generation with bonus features (multiline strings, range assertions).

### Test Invalid Inputs

```bash
# Syntax error - missing semicolon
java -cp "lib/java-cup-runtime-11b-20160615.jar;src" Main invalid1.test

# Type error - status must be integer
java -cp "lib/java-cup-runtime-11b-20160615.jar;src" Main invalid2.test

# Semantic error - undefined variable
java -cp "lib/java-cup-runtime-11b-20160615.jar;src" Main invalid3.test

# Lexical error - identifier starts with digit
java -cp "lib/java-cup-runtime-11b-20160615.jar;src" Main invalid4.test
```

Each should produce clear, helpful error messages without crashing, demonstrating comprehensive error handling across all compiler phases.

---

## Conclusion

This compiler demonstrates a complete implementation of a domain-specific language for API testing. The project showcases lexical analysis, parsing, semantic validation, and code generation techniques.

**Bonus features implemented:**
- Triple-quoted multiline strings for cleaner JSON body definitions
- Range-based status code assertions for flexible HTTP response validation

The optional Spring Boot backend (TestLangBackend) provides a complete end-to-end demonstration, allowing the generated tests to execute against real HTTP endpoints and validate responses. The backend runs separately and implements the POST /api/login and GET /api/users/{id} endpoints used in the example tests.

---

**Date:** October 2025  
**Course:** SE2052 Programming Paradigms  
**Institution:** Sri Lanka Institute of Information Technology (SLIIT)