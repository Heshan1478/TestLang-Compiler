package ASTmodel;

import java.util.*;

// ========== TestCase.java ==========
public class TestCase {
    private String name;
    private List<Request> requests = new ArrayList<>();
    private List<Assertion> assertions = new ArrayList<>();

    public TestCase(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void addRequest(Request req) {
        requests.add(req);
    }

    public void addAssertion(Assertion assertion) {
        assertions.add(assertion);
    }

    public List<Request> getRequests() {
        return requests;
    }

    public List<Assertion> getAssertions() {
        return assertions;
    }
}
