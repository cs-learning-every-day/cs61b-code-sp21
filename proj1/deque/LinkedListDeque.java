package deque;

import com.google.common.base.Objects;

import java.util.Iterator;

/**
 * @author huayang (sunhuayangak47@gmail.com)
 */
public class LinkedListDeque<T> implements Deque<T>, Iterable<T> {
    private static class Node<T> {
        private T value;
        private Node<T> next;
        private Node<T> prev;

        private Node() {
            next = this;
            prev = this;
        }

        private Node(T value, Node<T> prev, Node<T> next) {
            this.value = value;
            this.next = next;
            this.prev = prev;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }

            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            Node<T> node = (Node<T>) o;
            return Objects.equal(value, node.value);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(value, next, prev);
        }
    }

    private int size;
    private Node<T> sentinel;


    public LinkedListDeque() {
        size = 0;
        sentinel = new Node<>();
    }


    @Override
    public void addFirst(T item) {
        var head = sentinel.next;
        sentinel.next = new Node<>(item, sentinel, head);
        head.prev = sentinel.next;
        size++;
    }

    @Override
    public void addLast(T item) {
        var last = sentinel.prev;
        sentinel.prev = new Node<>(item, last, sentinel);
        last.next = sentinel.prev;
        size++;
    }


    @Override
    public int size() {
        return this.size;
    }

    @Override
    public T removeFirst() {
        if (size == 0) {
            return null;
        }

        Node<T> removedNode = sentinel.next;

        sentinel.next = removedNode.next;
        sentinel.next.prev = sentinel;

        size--;

        return removedNode.value;
    }

    @Override
    public T removeLast() {
        if (size == 0) {
            return null;
        }

        var removedNode = sentinel.prev;
        sentinel.prev = removedNode.prev;
        removedNode.prev.next = sentinel;

        size--;
        return removedNode.value;
    }

    @Override
    public T get(int index) {
        if (index < 0 || index >= size) {
            return null;
        }
        var cur = sentinel;
        while (index >= 0) {
            cur = cur.next;
            index--;
        }
        return cur.value;
    }

    @Override
    public void printDeque() {
        for (var ele : this) {
            System.out.print(ele + " ");
        }
        System.out.println();
    }

    private T helper(Node<T> node, int index) {
        if (index == 0) {
            return node.value;
        }
        return helper(node.next, index - 1);
    }

    public T getRecursive(int index) {
        if (index < 0 || index >= size) {
            return null;
        }
        return helper(sentinel.next, index);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null) {
            return false;
        }

        if (!(o instanceof Deque)) {
            return false;
        }

        Deque that = (Deque) o;
        if (that.size() != size) {
            return false;
        }

//        Node<T> x = sentinel.next;
//        for(var iter = that.iterator(); iter.hasNext();) {
//            Object ele = iter.next();
//            if (!x.value.equals(ele)) {
//                return false;
//            }
//            x = x.next;
//        }

        Node<T> x = sentinel.next;
        for (int i = 0; i < size; i++) {
            if (!x.value.equals(that.get(i))) {
                return false;
            }
            x = x.next;
        }
        return true;
    }

    @Override
    public Iterator<T> iterator() {
        return new LinkedListDequeIterator();
    }


    private class LinkedListDequeIterator implements Iterator<T> {
        private Node<T> cur = sentinel.next;

        @Override
        public boolean hasNext() {
            return cur != sentinel;
        }

        @Override
        public T next() {
            T val = cur.value;
            cur = cur.next;
            return val;
        }
    }
}
