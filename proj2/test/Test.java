package test;

import gitlet.Repository;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.PriorityQueue;

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

    public static void testRelativePath() throws IOException {
        var newFile = new File("./gitlet");
        System.out.println(newFile.isDirectory());
        System.out.println(newFile.getName());
        System.out.println(newFile.getPath());
        System.out.println(newFile.getAbsolutePath());
        System.out.println(newFile.getCanonicalPath());
        System.out.println(Repository.CWD.getCanonicalPath());
        System.out.println(newFile.getCanonicalPath().
                replace(Repository.CWD.getCanonicalPath(), ""));
    }

    public static void testPriorityQueue() {
        var q = new PriorityQueue<String>();
        q.add("a.txt");
        q.add("wug.txt");
        q.add("wug2.txt");
        for (String a : q) {
            System.out.println(a);
        }
    }

    public static void main(String[] args) throws IOException {
        testPriorityQueue();
    }
}
