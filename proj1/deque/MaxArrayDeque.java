package deque;

import java.util.Comparator;

/**
 * @author huayang (sunhuayangak47@gmail.com)
 */
public class MaxArrayDeque<T> extends ArrayDeque<T> {
    private Comparator<T> comparator;

    public MaxArrayDeque(Comparator<T> c) {
        this.comparator = c;
    }

    public T max() {
        return max(this.comparator);
    }

    public T max(Comparator<T> c) {
        if (isEmpty()) {
            return null;
        }
        T res = get(0);
        for (T ele : this) {
            if (c.compare(ele, res) > 0) {
                res = ele;
            }
        }
        return res;
    }


}
