package deque;

import org.junit.Assert;
import org.junit.Test;

import java.util.Comparator;

/**
 * @author huayang (sunhuayangak47@gmail.com)
 */
public class MaxArrayDequeTest {

    @Test
    public void test() {
        var c = Comparator.comparingInt(String::length);
        var mas = new MaxArrayDeque<>(c);
        mas.addFirst("abc");
        mas.addFirst("abcde");

        Assert.assertEquals("abcde", mas.max());
        mas.addFirst("aaaaaa");
        Assert.assertEquals("aaaaaa", mas.max(c));
    }
}
