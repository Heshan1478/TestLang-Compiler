import ASTmodel.*;
import java.util.*;

public class SemanticChecker {
    private List<String> errors = new ArrayList<>();

    public boolean check(Config config, Map<String, Variable> variables, List<TestCase> testCases) {
        // Check for duplicate variable names (already handled by Map, but good practice)

        // Check each test case
        for (TestCase test : testCases) {
            checkTestCase(test, variables);
        }

        // Print all errors
        if (!errors.isEmpty()) {

            System.err.println("SEMANTIC ERRORS:");
            for (String error : errors) {
                System.err.println("✗ " + error);
            }

            return false;
        }

        return true;
    }

    private void checkTestCase(TestCase test, Map<String, Variable> variables) {
        // Check: at least 1 request
        if (test.getRequests().isEmpty()) {
            errors.add("Test '" + test.getName() + "': Must have at least 1 request");
        }

        // Check: at least 2 assertions
        if (test.getAssertions().size() < 2) {
            errors.add("Test '" + test.getName() + "': Must have at least 2 assertions (found " +
                    test.getAssertions().size() + ")");
        }

        // Check: undefined variables in requests
        for (Request req : test.getRequests()) {
            checkUndefinedVariables(req.getPath(), variables, "Test '" + test.getName() + "', path");
            if (req.getBody() != null) {
                checkUndefinedVariables(req.getBody(), variables, "Test '" + test.getName() + "', body");
            }
        }
    }

    private void checkUndefinedVariables(String text, Map<String, Variable> variables, String location) {
        if (text == null) return;

        // Find all $varname references
        int pos = 0;
        while ((pos = text.indexOf('$', pos)) != -1) {
            int end = pos + 1;
            while (end < text.length() &&
                    (Character.isLetterOrDigit(text.charAt(end)) || text.charAt(end) == '_')) {
                end++;
            }

            if (end > pos + 1) {
                String varName = text.substring(pos + 1, end);
                if (!variables.containsKey(varName)) {
                    errors.add(location + ": Undefined variable '$" + varName + "'");
                }
            }
            pos = end;
        }
    }
}