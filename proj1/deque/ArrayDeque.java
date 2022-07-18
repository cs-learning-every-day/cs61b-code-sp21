package deque;

import java.util.Iterator;

/**
 * @author huayang (sunhuayangak47@gmail.com)
 */
public class ArrayDeque<T> implements Deque<T>, Iterable<T> {
    private static final int DEFAULT_CAPACITY = 8;

    private int size;
    private T[] items;
    private int nextFirst;
    private int nextLast;

    public ArrayDeque() {
        size = 0;
        items = (T[]) new Object[DEFAULT_CAPACITY];
        nextFirst = DEFAULT_CAPACITY / 2;
        nextLast = nextFirst + 1;
    }


    private int convertIdx(int idx) {
        return (idx + items.length) % items.length;
    }

    private void resize(int capacity) {
        T[] newItems = (T[]) new Object[capacity];
        int newNextFirst = capacity / 2;
        int newNextLast = newNextFirst + 1;
        for (var ele : this) {
            newItems[newNextLast] = ele;
            newNextLast = (newNextLast + 1) % capacity;
        }
        nextFirst = newNextFirst;
        nextLast = newNextLast;
        items = newItems;
    }

    @Override
    public void addFirst(T item) {
        if (size == items.length) {
            resize(items.length * 2);
        }
        items[nextFirst] = item;
        nextFirst = convertIdx(nextFirst - 1);
        size++;
    }

    @Override
    public void addLast(T item) {
        if (size == items.length) {
            resize(items.length * 2);
        }
        items[nextLast] = item;
        nextLast = convertIdx(nextLast + 1);
        size++;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public T removeFirst() {
        if (isEmpty()) {
            return null;
        }
        if (size < items.length / 4 && size > 4) {
            resize(items.length / 4);
        }
        int idx = convertIdx(nextFirst + 1);
        T val = items[idx];
        items[idx] = null;
        nextFirst = idx;
        size--;
        return val;
    }

    @Override
    public T removeLast() {
        if (isEmpty()) {
            return null;
        }
        if (size < items.length / 4 && size > 4) {
            resize(items.length / 4);
        }

        int idx = convertIdx(nextLast - 1);
        T val = items[idx];
        items[idx] = null;
        nextLast = idx;
        size--;
        return val;
    }

    @Override
    public T get(int index) {
        if (index < 0 || index >= size) {
            return null;
        }
        return items[convertIdx(nextFirst + 1 + index)];
    }

    @Override
    public void printDeque() {
        for (var ele : this) {
            System.out.print(ele + " ");
        }
        System.out.println();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null) {
            return false;
        }

        if (o instanceof LinkedListDeque) {
            return o.equals(this);
        }

        if (getClass() != o.getClass()) {
            return false;
        }

        ArrayDeque<T> that = (ArrayDeque<T>) o;

        if (size != that.size
                || that.items.length != this.items.length) {
            return false;
        }

        for (int i = 0; i < size; i++) {
            if (!this.items[i].equals(that.items[i])) {
                return false;
            }
        }
        return true;
    }


    @Override
    public Iterator<T> iterator() {
        return new ArrayDequeIterator();
    }

    private class ArrayDequeIterator implements Iterator<T> {
        private int index = 0;

        @Override
        public boolean hasNext() {
            return index < size;
        }

        @Override
        public T next() {
            T val = items[convertIdx(nextFirst + 1 + index)];
            index++;
            return val;
        }
    }
}
