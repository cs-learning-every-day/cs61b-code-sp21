package IntList;

import org.junit.Test;

import static org.junit.Assert.*;

public class SquarePrimesTest {

    /**
     * Here is a test for isPrime method. Try running it.
     * It passes, but the starter code implementation of isPrime
     * is broken. Write your own JUnit Test to try to uncover the bug!
     */
    @Test
    public void testSquarePrimesSimple() {
        IntList lst = IntList.of(14, 15, 16, 17, 18);
        boolean changed = IntListExercises.squarePrimes(lst);
        assertEquals("14 -> 15 -> 16 -> 289 -> 18", lst.toString());
        assertTrue(changed);
    }

    @Test
    public void testSquarePrimesNull() {
        boolean changed = IntListExercises.squarePrimes(null);
        assertFalse(changed);
    }

    @Test
    public void testSquarePrimesAllPrimes() {
        IntList lst = IntList.of(2, 3, 5, 7, 11);
        boolean changed = IntListExercises.squarePrimes(lst);
        assertEquals("4 -> 9 -> 25 -> 49 -> 121", lst.toString());
        assertTrue(changed);
    }

    @Test
    public void testSquarePrimesNoPrime() {
        IntList lst = IntList.of(4, 6);
        boolean changed = IntListExercises.squarePrimes(lst);
        assertEquals("4 -> 6", lst.toString());
        assertFalse(changed);
    }
}
