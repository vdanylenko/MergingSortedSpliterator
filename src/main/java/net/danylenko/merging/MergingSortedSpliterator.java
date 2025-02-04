package net.danylenko.merging;

import java.util.*;
import java.util.function.*;
import java.util.stream.*;

/**
 * Original spliterator from
 * https://www.javaspecialists.eu/archive/Issue289-MergingSortedSpliterator.html
 *
 */
public class MergingSortedSpliterator<T> implements Spliterator<T> {
    private final List<Spliterator<T>> spliterators;
    private final List<Iterator<T>> iterators;
    private final int characteristics;
    private final Object[] nextItem;
    private static final Object START_OF_STREAM = new Object();
    private static final Object END_OF_STREAM = new Object();
    private final Comparator<? super T> comparator;

    public MergingSortedSpliterator(Collection<Stream<T>> streams) {
        spliterators = streams.stream()
                .map(Stream::spliterator)
                .collect(Collectors.toList());
        if (!spliterators.stream().allMatch(
                spliterator -> spliterator.hasCharacteristics(SORTED)))
            throw new IllegalArgumentException("Streams must be sorted");
        Comparator<? super T> comparator = spliterators.stream()
                .map(Spliterator::getComparator)
                .reduce(null, (a, b) -> {
                    if (Objects.equals(a, b)) return a;
                    else throw new IllegalArgumentException(
                            "Mismatching comparators " + a + " and " + b);
                });
        this.comparator = Objects.requireNonNullElse(comparator,
                (Comparator<? super T>) Comparator.naturalOrder());
        characteristics = spliterators.stream()
                .mapToInt(Spliterator::characteristics)
                .reduce((ch1, ch2) -> ch1 & ch2)
                .orElse(0) & ~DISTINCT; // Mask out DISTINCT

        // setting up iterators
        this.iterators = spliterators.stream()
                .map(Spliterators::iterator)
                .collect(Collectors.toList());
        nextItem = new Object[streams.size()];
        Arrays.fill(nextItem, START_OF_STREAM);

    }

    private Object fetchNext(Iterator<T> iterator) {
        return iterator.hasNext() ? iterator.next() : END_OF_STREAM;
    }

    @Override
    public boolean tryAdvance(Consumer<? super T> action) {
        Objects.requireNonNull(action, "action==null");
        if (nextItem.length == 0) return false;
        T smallest = null;
        int smallestIndex = -1;
        for (int i = 0; i < nextItem.length; i++) {
            Object o = nextItem[i];
            if (o == START_OF_STREAM)
                nextItem[i] = o = fetchNext(iterators.get(i));
            if (o != END_OF_STREAM) {
                T t = (T) o;
                if (smallest == null ||
                        comparator.compare(t, smallest) < 0) {
                    smallest = t;
                    smallestIndex = i;
                }
            }
        }

        // smallest might be null if the stream contains nulls
        if (smallestIndex == -1) return false;

        nextItem[smallestIndex] =
                fetchNext(iterators.get(smallestIndex));

        action.accept(smallest);
        return true;
    }

    @Override
    public Spliterator<T> trySplit() {
        // never split - parallel not supported
        return null;
    }

    public long estimateSize() {
        return spliterators.stream()
                .mapToLong(Spliterator::estimateSize)
                .reduce((ch1, ch2) -> {
                    long result;
                    if ((result = ch1 + ch2) < 0) result = Long.MAX_VALUE;
                    return result;
                })
                .orElse(0);
    }

    public int characteristics() {
        return characteristics;
    }

    public Comparator<? super T> getComparator() {
        return comparator;
    }
}
