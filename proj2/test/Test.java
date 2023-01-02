package test;

import java.util.HashMap;

public class Test {
    public static void main(String[] args) {
        var m1 = new HashMap<String, Integer>();
        var m2 = new HashMap<String, Integer>();
        m1.put("1", 2);
        m1.put("2", 1);
        m2.put("1", 3);
        m2.putAll(m1);
        m2.forEach((k, v) -> System.out.printf("%s %d\n", k, v));
    }
}
