package deque;

import java.util.Iterator;

/**
 * @author huayang (sunhuayangak47@gmail.com)
 */
public interface Deque<T> extends Iterable<T> {
    void addFirst(T item);

    void addLast(T item);

    boolean isEmpty();

    int size();

    T removeFirst();

    T removeLast();

    T get(int index);


    @Override
    Iterator<T> iterator();

    default void printDeque() {
        for (var elem : this) {
            System.out.print(elem + " ");
        }
    }
}
