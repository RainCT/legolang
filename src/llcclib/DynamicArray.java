/**
 * Copyright © 2012 Siegfried-A. Gevatter Pujals <siegfried@gevatter.com>
 * Copyright © 2012 Gerard Canal Camprodon <grar.knal@gmail.com>
 *
 * Permission to use, copy, modify, and/or distribute this software for
 * any purpose with or without fee is hereby granted, provided that the
 * above copyright notice and this permission notice appear in all copies.
 *
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR
 * ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN ACTION
 * OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF OR IN
 * CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
*/

package llcclib;

import java.util.Vector;


public class DynamicArray<T> extends Vector<T> {

    public DynamicArray() {
        super();
    }

    public DynamicArray(T... elements) {
        super();
        ensureCapacity(elements.length);
        for (T element : elements) {
            addElement(element);
        }
    }

    public T getElement(Long i) {
        int index = getIndex(i);
        return (T) elementAt(index);
    }

    public void deleteElement(Long i) {
        int index = getIndex(i);
        removeElementAt(index);
    }

    public void setElement(Long i, T v) {
        int index = getIndex(i);
        setElementAt(v, index);
    }

    public void appendElement(T v) {
        addElement(v);
    }

    public Long getLength() {
        return new Long(size());
    }

    public boolean equals(Object o) {
        return false;
    }

    public boolean equals(DynamicArray<T> o) {
        if (!getLength().equals(o.getLength()))
            return false;

        for (long i = 0; i < getLength(); ++i) {
            if (!getElement(i).equals(o.getElement(i)))
                return false;
        }

        return true;
    }

    public DynamicArray<T> clone() {
        /*
        try {
            return (DynamicArray<T>) super.clone();
        } catch (CloneNotSupportedException e) {
            System.err.println("DynamicArray: clone not supported!");
        }
        */

        DynamicArray<T> copy = new DynamicArray<T>();
        copy.ensureCapacity(size());
        for (int i = 0; i < getLength(); ++i)
            copy.appendElement(elementAt(i));
        return copy;
    }

    private int getIndex(Long i) {
        if (i < Integer.MIN_VALUE || i > Integer.MAX_VALUE) {
            throw new RuntimeException("Array index exceeds integer size.");
        }

        // Support for negative indices
        Long index = (i >= 0) ? i : getLength() + i;

        // Check array bounds
        if (index < 0 || index >= getLength()) {
            throw new RuntimeException("Array index "+i+" is out of range.");
        }

        return (int) ((long) index);
    }

}
