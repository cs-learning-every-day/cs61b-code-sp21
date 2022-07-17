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
        AListNoResizing<Integer> correct = new AListNoResizing<>();
        BuggyAList<Integer> broken = new BuggyAList<>();

        int N = 5000;
        for (int i = 0; i < N; i += 1) {
            int operationNumber = StdRandom.uniform(0, 4);
            if (operationNumber == 0) {
                // addLast
                int randVal = StdRandom.uniform(0, 100);
                correct.addLast(randVal);
                broken.addLast(randVal);
                System.out.println("addLast(" + randVal + ")");
            } else if (operationNumber == 1) {
                if (correct.size() > 0 && broken.size() > 0) {
                    int v1 = correct.getLast();
                    int v2 = broken.getLast();
                    assertEquals(v1, v2);
                    System.out.println("getLast(" + v1 + ")");
                }
            } else if (operationNumber == 2) {
                if (correct.size() > 0 && broken.size() > 0) {
                    int v1 = correct.removeLast();
                    int v2 = broken.removeLast();
                    assertEquals(v1, v2);
                    assertEquals(correct.size(), broken.size());
                    System.out.println("removeLast(" + v1 + ")");
                }
            }
        }
    }
}