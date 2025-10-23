package ASTmodel;

public class Assertion {
    public enum Type {
        STATUS_EQUALS,
        STATUS_IN_RANGE,
        HEADER_EQUALS,
        HEADER_CONTAINS,
        BODY_CONTAINS
    }

    private Type type;
    private String key;
    private String expected;
    private int rangeStart;
    private int rangeEnd;

    private Assertion(Type type, String key, String expected) {
        this.type = type;
        this.key = key;
        this.expected = expected;
    }

    private Assertion(Type type, int rangeStart, int rangeEnd) {
        this.type = type;
        this.rangeStart = rangeStart;
        this.rangeEnd = rangeEnd;
    }

    public static Assertion statusEquals(int status) {
        return new Assertion(Type.STATUS_EQUALS, null, String.valueOf(status));
    }

    public static Assertion statusInRange(int start, int end) {
        return new Assertion(Type.STATUS_IN_RANGE, start, end);
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

    public int getRangeStart() {
        return rangeStart;
    }

    public int getRangeEnd() {
        return rangeEnd;
    }
}