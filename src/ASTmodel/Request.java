package ASTmodel;

import java.util.*;

// ========== Request.java ==========
public class Request {
    private String method; // GET, POST, PUT, DELETE
    private String path;
    private Map<String, String> headers = new HashMap<>();
    private String body;

    public Request(String method, String path) {
        this.method = method;
        this.path = path;
    }

    public String getMethod() {
        return method;
    }

    public String getPath() {
        return path;
    }

    public void addHeader(String key, String value) {
        headers.put(key, value);
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getBody() {
        return body;
    }

    // Helper class for parser
    public static class HeaderEntry {
        public boolean isBody;
        public String key;
        public String value;

        public HeaderEntry(boolean isBody, String key, String value) {
            this.isBody = isBody;
            this.key = key;
            this.value = value;
        }
    }
}
