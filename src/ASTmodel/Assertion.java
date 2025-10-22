package ASTmodel;

// ========== Assertion.java ==========
public class Assertion {
    public enum Type {
        STATUS_EQUALS,
        HEADER_EQUALS,
        HEADER_CONTAINS,
        BODY_CONTAINS
    }

    private Type type;
    private String key;      // for header assertions
    private String expected; // String value or status code as string

    private Assertion(Type type, String key, String expected) {
        this.type = type;
        this.key = key;
        this.expected = expected;
    }

    public static Assertion statusEquals(int status) {
        return new Assertion(Type.STATUS_EQUALS, null, String.valueOf(status));
    }

    public static Assertion headerEquals(String headerName, String value) {
        return new Assertion(Type.HEADER_EQUALS, headerName, value);
    }

    public static Assertion headerContains(String headerName, String substring) {
        return new Assertion(Type.HEADER_CONTAINS, headerName, substring);
    }

    public static Assertion bodyContains(String substring) {
        return new Assertion(Type.BODY_CONTAINS, null, substring);
    }

    public Type getType() {
        return type;
    }

    public String getKey() {
        return key;
    }

    public String getExpected() {
        return expected;
    }

    public int getExpectedStatus() {
        return Integer.parseInt(expected);
    }
}
