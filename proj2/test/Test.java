package test;

import gitlet.Commit;
import gitlet.Repository;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.*;

public class Test {
    public static void testMap() {
        var m1 = new HashMap<String, Integer>();
        var m2 = new HashMap<String, Integer>();
        m1.put("1", 2);
        m1.put("2", 1);

        m2.put("1", 3);
        m2.putAll(m1);

        m2.forEach((k, v) -> System.out.printf("%s %d\n", k, v));


        m1.put("1", 1);
        System.out.println(m1.equals(m2));
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

    /**
     * <- c3 <- c5
     * c1 <- c2
     * <- c4 <- c6
     */
    public static void testSplitPoint() {

        Commit c1 = new Commit("test1", new Date());
        Commit c2 = new Commit("test2", new Date());
        c2.addParent(c1);
        Commit c3 = new Commit("test3", new Date());
        c3.addParent(c2);
        Commit c4 = new Commit("test4", new Date());
        c4.addParent(c2);
        Commit c5 = new Commit("test5", new Date());
        c5.addParent(c3);
        Commit c6 = new Commit("test6", new Date());
        c6.addParent(c4);
        Commit c7 = new Commit("test7", new Date());
        c7.addParent(c6);

        Optional<Commit> splitPoint = Repository.getSplitPoint(c7, c5);
        Commit commit = splitPoint.orElseThrow();
        System.out.println(commit.getMessage());
    }

    static void writeContents(File file, Object... contents) {
        try {
            if (file.isDirectory()) {
                throw
                        new IllegalArgumentException("cannot overwrite directory");
            }
            BufferedOutputStream str =
                    new BufferedOutputStream(Files.newOutputStream(file.toPath()));
            for (Object obj : contents) {
                if (obj instanceof byte[]) {
                    str.write((byte[]) obj);
                } else {
                    str.write(((String) obj).getBytes(StandardCharsets.UTF_8));
                }
            }
            str.close();
        } catch (IOException | ClassCastException excp) {
            throw new IllegalArgumentException(excp.getMessage());
        }
    }

    public static void TestWriteFile() {
        File file = new File("./test.txt");
        writeContents(file, "test".getBytes());
        writeContents(file, System.lineSeparator() + "test2");
    }

    public static void TestPathConvert() {
        System.out.println("aa/123/.gitlet".replaceAll("//", File.separator));
    }

    public static void main(String[] args) throws IOException {
        TestPathConvert();
    }
}
