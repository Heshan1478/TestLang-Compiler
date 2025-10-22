package ASTmodel;

import java.util.*;

// ========== Config.java ==========
public class Config {
    private String baseUrl;
    private Map<String, String> headers = new HashMap<>();

    public void setBaseUrl(String url) {
        this.baseUrl = url;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void addHeader(String key, String value) {
        headers.put(key, value);
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    // Helper class for parser
    public static class HeaderEntry {
        public boolean isBaseUrl;
        public String key;
        public String value;

        public HeaderEntry(boolean isBaseUrl, String key, String value) {
            this.isBaseUrl = isBaseUrl;
            this.key = key;
            this.value = value;
        }
    }
}