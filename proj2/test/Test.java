package test;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

public class Test {
    public static void testMap() {
        var m1 = new HashMap<String, Integer>();
        var m2 = new HashMap<String, Integer>();
        m1.put("1", 2);
        m1.put("2", 1);
        m2.put("1", 3);
        m2.putAll(m1);
        m2.forEach((k, v) -> System.out.printf("%s %d\n", k, v));
    }

    public static void testDate() {
        var dateFormat = new SimpleDateFormat("EEE MMM d HH:mm:ss yyyy Z", Locale.US);
        System.out.println(dateFormat.format(new Date()));
    }

    public static void main(String[] args) {
        testDate();
    }
}
