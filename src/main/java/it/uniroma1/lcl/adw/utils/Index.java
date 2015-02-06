package it.uniroma1.lcl.adw.utils;


/**
 * A utility class for holding a primitive key-value pair of an {@code int} and
 * {@code float}.  {@link Index} instances are sortable where the index with the
 * largest value will come first.
 *  
 * @author jurgens
 *
 */
class Index implements Comparable<Index> {

    public final int key;
    public final float value;

    public Index(int key, float value) {
        this.key = key;
        this.value = value;
    }

    public int compareTo(Index i) {
        return -Float.compare(value, i.value);
    }
}
