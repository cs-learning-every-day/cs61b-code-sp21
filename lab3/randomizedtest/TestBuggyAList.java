package randomizedtest;

import edu.princeton.cs.algs4.StdRandom;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by hug.
 */
public class TestBuggyAList {
    // YOUR TESTS HERE

    @Test
    public void testThreeAddThreeRemove() {
        var bl = new BuggyAList<>();
        var al = new AListNoResizing<>();
        for (int i = 4; i <= 6; i++) {
            bl.addLast(i);
            al.addLast(i);
        }
        for (int i = 0; i < 3; i++) {
            assertEquals(al.get(i), bl.get(i));
        }
        al.removeLast();
        bl.removeLast();
        for (int i = 0; i < 2; i++) {
            assertEquals(al.get(i), bl.get(i));
        }
    }

    @Test
    public void randomizedTest() {
        var L = new BuggyAList<>();

        int N = 500;
        for (int i = 0; i < N; i += 1) {
            int operationNumber = StdRandom.uniform(0, 3);
            if (operationNumber == 0) {
                // addLast
                int randVal = StdRandom.uniform(0, 100);
                L.addLast(randVal);
                System.out.println("addLast(" + randVal + ")");
            } else if (operationNumber == 1) {
                // size
                int size = L.size();
                System.out.println("size: " + size);
            } else if (operationNumber == 2) {
                L.removeLast();
                System.out.println("removeLast()");
            }
        }
    }
}
