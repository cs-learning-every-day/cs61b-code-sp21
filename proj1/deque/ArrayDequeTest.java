package deque;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * @author huayang (sunhuayangak47@gmail.com)
 */
public class ArrayDequeTest {

    @Test
    public void testResize() {
        var ad = new ArrayDeque<Integer>();
        for (int i = 0; i < 100; i++) {
            ad.addLast(i);
        }
        ad.printDeque();

        assertTrue(ad.size() == 100);
        assertTrue(ad.get(50) == 50);


        for (int i = 0; i < 9900; i++) {
            ad.addLast(i);
        }

        assertTrue(ad.size() == 10000);

        for (int i = 0; i < 9999; i++) {
            ad.removeFirst();
        }

        assertTrue(ad.size() == 1);
    }

    @Test
    public void testBasicAll() {
        var ad = new ArrayDeque<Integer>();

        assertTrue(ad.isEmpty());

        ad.addFirst(0);

        assertTrue(ad.size() == 1);
        assertTrue(ad.get(0) == 0);

        ad.addLast(1);
        assertTrue(ad.get(1) == 1);
        assertTrue(ad.size() == 2);

        ad.addFirst(-1);
        ad.addLast(2);

        ad.printDeque();

        assertTrue(ad.removeFirst() == -1);
        assertTrue(ad.removeLast() == 2);
        assertTrue(ad.size() == 2);
    }
}
