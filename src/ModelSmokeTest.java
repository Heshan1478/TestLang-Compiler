import ASTmodel.*;

public class ModelSmokeTest {
    public static void main(String[] args) {
        Config c = new Config();
        c.setBaseUrl("http://localhost:8080");
        c.addHeader("Content-Type", "application/json");

        Variable v = new Variable("user", "admin");

        Request r = new Request("GET", "/api/users/$id");
        r.addHeader("Accept", "application/json");

        Assertion a = Assertion.statusEquals(200);

        TestCase t = new TestCase("GetUser");
        t.addRequest(r);
        t.addAssertion(a);

        System.out.println("Config → " + c);
        System.out.println("Variable → " + v);
        System.out.println("TestCase → " + t);
    }
}
