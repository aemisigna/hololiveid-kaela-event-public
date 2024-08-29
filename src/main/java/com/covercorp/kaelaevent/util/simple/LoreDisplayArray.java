package com.covercorp.kaelaevent.util.simple;

import org.bukkit.ChatColor;

import java.util.AbstractList;
import java.util.Arrays;

public class LoreDisplayArray<E> extends AbstractList<E> {
    private Object[] elementData;
    private int size;
    private static final Object[] METADATA;
    private static final int DEFAULT_CAPACITY = 10;

    public LoreDisplayArray() {
        this.elementData = LoreDisplayArray.METADATA;
    }

    public LoreDisplayArray(E... elements) {
        this.elementData = LoreDisplayArray.METADATA;
        for (E element : elements) {
            this.add(element);
        }
    }

    private void ensureCapacity(int minCapacity) {
        if (this.elementData == LoreDisplayArray.METADATA) {
            minCapacity = Math.max(DEFAULT_CAPACITY, minCapacity);
        }
        if (minCapacity - this.elementData.length > 0) {
            final int oldCapacity = this.elementData.length;
            int newCapacity = oldCapacity + (oldCapacity >> 1);
            if (newCapacity - minCapacity < 0) {
                newCapacity = minCapacity;
            }
            this.elementData = Arrays.copyOf(this.elementData, newCapacity);
        }
    }

    public void add(final String e, final ChatColor color) {
        final String[] split2;
        final String[] split = split2 = e.split("%n%");
        for (final String s : split2) {
            this.add(color + StringUtils.translate(s));
        }
    }

    public void addString(final String e) {
        final String[] split2;
        final String[] split = split2 = e.split("%n%");
        for (final String s : split2) {
            this.add(StringUtils.translate(s));
        }
    }

    @Override
    public boolean add(final Object e) {
        this.ensureCapacity(this.size + 1);
        this.elementData[this.size++] = e;
        return true;
    }

    @Override
    public E get(final int index) {
        this.rangeCheck(index);
        return (E)this.elementData[index];
    }

    private void rangeCheck(final int index) {
        if (index >= this.size) {
            throw new IndexOutOfBoundsException();
        }
    }

    @Override
    public int size() {
        return this.size;
    }

    static {
        METADATA = new Object[0];
    }
}