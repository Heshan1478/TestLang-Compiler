import java_cup.runtime.*;
import java.io.*;
import java.util.*;
import ASTmodel.*;

public class Main {
    public static void main(String[] args) {
        // Check command line arguments
        if (args.length < 1) {
            System.err.println("Usage: java Main <input.test>");
            System.err.println("Example: java Main example.test");
            System.exit(1);
        }

        String inputFile = args[0];

        try {
            System.out.println("Reading file: " + inputFile);

            // Create lexer and parser
            FileReader fileReader = new FileReader(inputFile);
            Lexer lexer = new Lexer(fileReader);
            Parser parser = new Parser(lexer);

            // Parse the file
            System.out.println("Starting parse...");
            Symbol result = parser.parse();
            System.out.println("✓ Parse completed successfully!\n");

            // Extract parsed items
            @SuppressWarnings("unchecked")
            List<Object> items = (List<Object>) result.value;

            if (items == null || items.isEmpty()) {
                System.err.println("Warning: No items parsed from file");
                return;
            }

            System.out.println("Found " + items.size() + " top-level item(s):\n");

            // Organize parsed items
            Config config = null;
            Map<String, Variable> variables = new LinkedHashMap<>();
            List<TestCase> testCases = new ArrayList<>();

            // Process each item
            for (Object item : items) {
                if (item instanceof Config) {
                    config = (Config) item;
                    System.out.println("✓ Config block:");

                    if (config.getBaseUrl() != null) {
                        System.out.println("    base_url = \"" + config.getBaseUrl() + "\"");
                    }

                    if (!config.getHeaders().isEmpty()) {
                        for (Map.Entry<String, String> header : config.getHeaders().entrySet()) {
                            System.out.println("    header \"" + header.getKey() +
                                    "\" = \"" + header.getValue() + "\"");
                        }
                    }
                    System.out.println();

                } else if (item instanceof Variable) {
                    Variable var = (Variable) item;
                    variables.put(var.getName(), var);
                    System.out.println("✓ Variable: " + var.getName() +
                            " = " + formatValue(var.getValue()));

                } else if (item instanceof TestCase) {
                    TestCase test = (TestCase) item;
                    testCases.add(test);
                    System.out.println("✓ Test: " + test.getName());
                    System.out.println("    Requests: " + test.getRequests().size());
                    System.out.println("    Assertions: " + test.getAssertions().size());

                    // Show details
                    for (Request req : test.getRequests()) {
                        System.out.println("      - " + req.getMethod() + " " + req.getPath());
                    }
                    for (Assertion assertion : test.getAssertions()) {
                        System.out.println("      - expect " + formatAssertion(assertion));
                    }
                    System.out.println();
                }
            }

            // Summary
            System.out.println("═══════════════════════════════════════");
            System.out.println("PARSING SUMMARY:");
            System.out.println("═══════════════════════════════════════");
            System.out.println("Config:    " + (config != null ? "✓ Present" : "✗ Not found"));
            System.out.println("Variables: " + variables.size());
            System.out.println("Tests:     " + testCases.size());
            System.out.println("═══════════════════════════════════════\n");

            // Semantic validation
            System.out.println("Validating...");
            SemanticChecker checker = new SemanticChecker();
            boolean isValid = checker.check(config, variables, testCases);

            if (!isValid) {
                System.err.println("✗ Validation failed. Please fix the errors above.\n");
                System.exit(1);
            }

            System.out.println("✓ Validation passed!\n");

            // Generate JUnit tests if we have test cases
            if (!testCases.isEmpty()) {
                System.out.println("═══════════════════════════════════════");
                System.out.println("GENERATING JUNIT TESTS:");
                System.out.println("═══════════════════════════════════════\n");

                CodeGenerator generator = new CodeGenerator(config, variables, testCases);
                String generatedCode = generator.generate();

                // Write to file
                try {
                    FileWriter writer = new FileWriter("GeneratedTests.java");
                    writer.write(generatedCode);
                    writer.close();

                    System.out.println("Parsing completed successfully.");
                    System.out.println("Generated: src/GeneratedTests.java");

                } catch (IOException e) {
                    System.err.println("✗ Error writing GeneratedTests.java: " + e.getMessage());
                    System.exit(1);
                }
            } else {
                System.out.println("⚠ Skipping code generation (no valid test cases)\n");
            }

        } catch (FileNotFoundException e) {
            System.err.println("Error: File not found: " + inputFile);
            System.err.println("Make sure the file exists and the path is correct.");
            System.exit(1);

        } catch (Exception e) {
            System.err.println("\n✗ ERROR during parsing:");
            System.err.println("─────────────────────────────────────");
            e.printStackTrace();
            System.err.println("─────────────────────────────────────");
            System.exit(1);
        }
    }

    // Helper method to format values for display
    private static String formatValue(Object value) {
        if (value instanceof String) {
            return "\"" + value + "\"";
        }
        return value.toString();
    }

    // Helper method to format assertion for display
    private static String formatAssertion(Assertion assertion) {
        switch (assertion.getType()) {
            case STATUS_EQUALS:
                return "status = " + assertion.getExpected();
            case HEADER_EQUALS:
                return "header \"" + assertion.getKey() + "\" = \"" +
                        assertion.getExpected() + "\"";
            case HEADER_CONTAINS:
                return "header \"" + assertion.getKey() + "\" contains \"" +
                        assertion.getExpected() + "\"";
            case BODY_CONTAINS:
                return "body contains \"" + assertion.getExpected() + "\"";
            default:
                return assertion.toString();
        }
    }
}