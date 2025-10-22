import ASTmodel.*;
import java.util.*;

public class CodeGenerator {
    private Config config;
    private Map<String, Variable> variables;
    private List<TestCase> testCases;

    public CodeGenerator(Config config, Map<String, Variable> variables, List<TestCase> testCases) {
        this.config = config;
        this.variables = variables;
        this.testCases = testCases;
    }

    public String generate() {
        StringBuilder sb = new StringBuilder();

        // Imports
        sb.append("import org.junit.jupiter.api.*;\n");
        sb.append("import static org.junit.jupiter.api.Assertions.*;\n");
        sb.append("import java.net.http.*;\n");
        sb.append("import java.net.*;\n");
        sb.append("import java.time.Duration;\n");
        sb.append("import java.nio.charset.StandardCharsets;\n");
        sb.append("import java.util.*;\n\n");

        // Class declaration
        sb.append("public class GeneratedTests {\n");

        // Static fields
        String baseUrl = (config != null && config.getBaseUrl() != null)
                ? config.getBaseUrl()
                : "http://localhost:8080";
        sb.append("    static String BASE = \"").append(baseUrl).append("\";\n");
        sb.append("    static Map<String, String> DEFAULT_HEADERS = new HashMap<>();\n");
        sb.append("    static HttpClient client;\n\n");

        // @BeforeAll setup
        sb.append("    @BeforeAll\n");
        sb.append("    static void setup() {\n");
        sb.append("        client = HttpClient.newBuilder()\n");
        sb.append("            .connectTimeout(Duration.ofSeconds(5))\n");
        sb.append("            .build();\n");

        // Add default headers from config
        if (config != null && !config.getHeaders().isEmpty()) {
            for (Map.Entry<String, String> header : config.getHeaders().entrySet()) {
                sb.append("        DEFAULT_HEADERS.put(\"")
                        .append(escapeJava(header.getKey()))
                        .append("\", \"")
                        .append(escapeJava(header.getValue()))
                        .append("\");\n");
            }
        }
        sb.append("    }\n\n");

        // Generate test methods
        for (TestCase test : testCases) {
            generateTestMethod(sb, test);
        }

        sb.append("}\n");

        return sb.toString();
    }

    private void generateTestMethod(StringBuilder sb, TestCase test) {
        sb.append("    @Test\n");
        sb.append("    void test_").append(test.getName()).append("() throws Exception {\n");

        // Generate each request and its assertions
        for (Request request : test.getRequests()) {
            generateRequest(sb, request, test.getAssertions());
        }

        sb.append("    }\n\n");
    }

    private void generateRequest(StringBuilder sb, Request request, List<Assertion> assertions) {
        String path = substituteVariables(request.getPath());
        String url = path.startsWith("/") ? "BASE + \"" + path + "\"" : "\"" + path + "\"";

        sb.append("        HttpRequest.Builder b = HttpRequest.newBuilder(URI.create(").append(url).append("))\n");
        sb.append("            .timeout(Duration.ofSeconds(10))\n");

        // HTTP method
        String method = request.getMethod();
        if ("GET".equals(method)) {
            sb.append("            .GET();\n");
        } else if ("DELETE".equals(method)) {
            sb.append("            .DELETE();\n");
        } else if ("POST".equals(method) || "PUT".equals(method)) {
            String body = request.getBody();
            if (body != null) {
                body = substituteVariables(body);
                sb.append("            .").append(method).append("(HttpRequest.BodyPublishers.ofString(\"")
                        .append(escapeJava(body)).append("\"));\n");
            } else {
                sb.append("            .").append(method).append("(HttpRequest.BodyPublishers.noBody());\n");
            }
        }

        // Add default headers
        sb.append("        for (var e : DEFAULT_HEADERS.entrySet()) {\n");
        sb.append("            b.header(e.getKey(), e.getValue());\n");
        sb.append("        }\n");

        // Add request-specific headers
        if (!request.getHeaders().isEmpty()) {
            for (Map.Entry<String, String> header : request.getHeaders().entrySet()) {
                String value = substituteVariables(header.getValue());
                sb.append("        b.header(\"")
                        .append(escapeJava(header.getKey()))
                        .append("\", \"")
                        .append(escapeJava(value))
                        .append("\");\n");
            }
        }

        // Send request
        sb.append("        HttpResponse<String> resp = client.send(b.build(), HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));\n\n");

        // Generate assertions for this request
        for (Assertion assertion : assertions) {
            generateAssertion(sb, assertion);
        }
    }

    private void generateAssertion(StringBuilder sb, Assertion assertion) {
        switch (assertion.getType()) {
            case STATUS_EQUALS:
                sb.append("        assertEquals(")
                        .append(assertion.getExpectedStatus())
                        .append(", resp.statusCode());\n");
                break;

            case HEADER_EQUALS:
                sb.append("        assertEquals(\"")
                        .append(escapeJava(assertion.getExpected()))
                        .append("\", resp.headers().firstValue(\"")
                        .append(escapeJava(assertion.getKey()))
                        .append("\").orElse(\"\"));\n");
                break;

            case HEADER_CONTAINS:
                sb.append("        assertTrue(resp.headers().firstValue(\"")
                        .append(escapeJava(assertion.getKey()))
                        .append("\").orElse(\"\").contains(\"")
                        .append(escapeJava(assertion.getExpected()))
                        .append("\"));\n");
                break;

            case BODY_CONTAINS:
                sb.append("        assertTrue(resp.body().contains(\"")
                        .append(escapeJava(assertion.getExpected()))
                        .append("\"));\n");
                break;
        }
    }

    // Variable substitution: $varname -> actual value
    private String substituteVariables(String text) {
        if (text == null || variables.isEmpty()) {
            return text;
        }

        String result = text;
        for (Map.Entry<String, Variable> entry : variables.entrySet()) {
            String varName = "$" + entry.getKey();
            String varValue = entry.getValue().getValueAsString();
            result = result.replace(varName, varValue);
        }
        return result;
    }

    // Escape special characters for Java strings
    private String escapeJava(String str) {
        if (str == null) return "";
        return str.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}