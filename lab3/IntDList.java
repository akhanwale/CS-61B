

/**
 * Scheme-like pairs that can be used to form a list of integers.
 *
 * @author P. N. Hilfinger; updated by Vivant Sakore (1/29/2020)
 */
public class IntDList {

    /**
     * First and last nodes of list.
     */
    protected DNode _front, _back;
    protected int size = 0;

    /**
     * An empty list.
     */
    public IntDList() {
        _front = _back = null;
    }

    /**
     * @param values the ints to be placed in the IntDList.
     */
    public IntDList(Integer... values) {
        _front = _back = null;
        for (int val : values) {
            insertBack(val);
        }
    }

    /**
     * @return The first value in this list.
     * Throws a NullPointerException if the list is empty.
     */
    public int getFront() {
        return _front._val;
    }

    /**
     * @return The last value in this list.
     * Throws a NullPointerException if the list is empty.
     */
    public int getBack() {
        return _back._val;
    }

    /**
     * @return The number of elements in this list.
     */
    public int size() {
        return size;
    }

    /**
     * @param i index of element to return,
     *          where i = 0 returns the first element,
     *          i = 1 returns the second element,
     *          i = -1 returns the last element,
     *          i = -2 returns the second to last element, and so on.
     *          You can assume i will always be a valid index, i.e 0 <= i < size for positive indices
     *          and -size <= i <= -1 for negative indices
     * @return The integer value at index i
     */
    public int get(int i) {
        if (i < 0) {
            DNode pointer = _back;
            i += 1;
            while (i != 0) {
                pointer = pointer._prev;
                i += 1;
            }
            return pointer._val;
        } else if (i == 0) {
            return _front._val;
        } else {
            DNode pointer = _front;
            while (i != 0) {
                pointer = pointer._next;
                i -= 1;
            }
            return pointer._val;
        }
    }

    /**
     * @param d value to be inserted in the front
     */
    public void insertFront(int d) {
        if (size == 0) {
            _front = _back = new DNode(d);
            size += 1;
        } else {
            DNode newFront = new DNode(d);
            _front._prev = newFront;
            newFront._next = _front;
            _front = newFront;
            size += 1;
        }
    }

    /**
     * @param d value to be inserted in the back
     */
    public void insertBack(int d) {
        if (size == 0) {
            _front = _back = new DNode(d);
            size += 1;
        } else {
            DNode newBack = new DNode(d);
            _back._next = newBack;
            newBack._prev = _back;
            _back = newBack;
            size += 1;
        }
    }

    /**
     * @param d     value to be inserted
     * @param index index at which the value should be inserted
     *              where index = 0 inserts at the front,
     *              index = 1 inserts at the second position,
     *              index = -1 inserts at the back,
     *              index = -2 inserts at the second to last position, and so on.
     *              You can assume index will always be a valid index,
     *              i.e 0 <= index <= size for positive indices (including insertions at front and back)
     *              and -(size+1) <= index <= -1 for negative indices (including insertions at front and back).
     */
    public void insertAtIndex(int d, int index) {
        if ((index == -1) || (index == size)) {
            insertBack(d);
            System.out.println(toString());
        } else if ((index == 0) || (index == -(size + 1))) {
            insertFront(d);
            System.out.println(toString());
        } else if (index < 0) {
            DNode pointer = _back;
            index += 1;
            while (index != 0) {
                pointer = pointer._prev;
                index += 1;
            }
            System.out.println(pointer._val);
            DNode insert = new DNode(d);
            insert._next = pointer._next;
            insert._prev = pointer;
            pointer._next._prev = insert;
            pointer._next = insert;
            size += 1;
            System.out.println(toString());
        } else {
            DNode pointer = _front;
            while (index != 0) {
                pointer = pointer._next;
                index -= 1;
            }
            DNode insert = new DNode(d);
            insert._prev = pointer._prev;
            insert._next = pointer;
            pointer._prev._next = insert;
            pointer._prev = insert;
            size += 1;
            System.out.println(toString());
        }
    }

    /**
     * Removes the first item in the IntDList and returns it.
     *
     * @return the item that was deleted
     */
    public int deleteFront() {
        DNode deleted = _front;
        _front = _front._next;
        size -= 1;
        return deleted._val;
    }

    /**
     * Removes the last item in the IntDList and returns it.
     *
     * @return the item that was deleted
     */
    public int deleteBack() {
        DNode deleted = _back;
        _back = _back._prev;
        size -= 1;
        return deleted._val;
    }

    /**
     * @param index index of element to be deleted,
     *          where index = 0 returns the first element,
     *          index = 1 will delete the second element,
     *          index = -1 will delete the last element,
     *          index = -2 will delete the second to last element, and so on.
     *          You can assume index will always be a valid index,
     *              i.e 0 <= index < size for positive indices (including deletions at front and back)
     *              and -size <= index <= -1 for negative indices (including deletions at front and back).
     * @return the item that was deleted
     */
    public int deleteAtIndex(int index) {
        return index;
    }

    /**
     * @return a string representation of the IntDList in the form
     * [] (empty list) or [1, 2], etc.
     * Hint:
     * String a = "a";
     * a += "b";
     * System.out.println(a); //prints ab
     */
    public String toString() {
        if (size == 0) {
            return "[]";
        } else if (size == 1) {
            return "[" + Integer.toString(_front._val) + "]";
        } else {
            String out = "[";
            DNode pointer = _front;
            out += Integer.toString(pointer._val);
            pointer = pointer._next;
            while (pointer != _back) {
                out += ", ";
                out += Integer.toString(pointer._val);
                pointer = pointer._next;
            }
            out += ", " + Integer.toString(_back._val) + "]";
            return out;
        }
    }

    /**
     * DNode is a "static nested class", because we're only using it inside
     * IntDList, so there's no need to put it outside (and "pollute the
     * namespace" with it. This is also referred to as encapsulation.
     * Look it up for more information!
     */
    static class DNode {
        /** Previous DNode. */
        protected DNode _prev;
        /** Next DNode. */
        protected DNode _next;
        /** Value contained in DNode. */
        protected int _val;

        /**
         * @param val the int to be placed in DNode.
         */
        protected DNode(int val) {
            this(null, val, null);
        }

        /**
         * @param prev previous DNode.
         * @param val  value to be stored in DNode.
         * @param next next DNode.
         */
        protected DNode(DNode prev, int val, DNode next) {
            _prev = prev;
            _val = val;
            _next = next;
        }
    }

}
