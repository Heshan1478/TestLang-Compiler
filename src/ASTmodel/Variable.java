package ASTmodel;

import java.util.*;

public class Variable {
    private String name;
    private Object value; // String or Integer

    public Variable(String name, Object value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public Object getValue() {
        return value;
    }

    public String getValueAsString() {
        return value.toString();
    }
}