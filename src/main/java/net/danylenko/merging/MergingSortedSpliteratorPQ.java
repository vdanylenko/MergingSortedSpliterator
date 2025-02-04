package net.danylenko.merging;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class MergingSortedSpliteratorPQ<T> implements Spliterator<T> {

    private final int characteristics;
    private final Comparator<? super T> comparator;
    private final StablePriorityQueue<Pair<T>> iterationQueue;
    private final long initialEstimatedSize;

    public MergingSortedSpliteratorPQ(Collection<Stream<T>> streams) {

        List<Spliterator<T>> spliterators = streams.stream()
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


        initialEstimatedSize = spliterators.stream()
                .mapToLong(Spliterator::estimateSize)
                .reduce((ch1, ch2) -> {
                    long result;
                    if ((result = ch1 + ch2) < 0) result = Long.MAX_VALUE;
                    return result;
                })
                .orElse(0L);

        // setting up iterators
        iterationQueue = spliterators.stream()
                .map(Spliterators::iterator)
                .filter(Iterator::hasNext)
                .map(iterator -> new Pair<>(iterator.next(), iterator,
                        Objects.requireNonNullElse(comparator,
                                (Comparator<? super T>) Comparator.naturalOrder())))
                .collect(Collectors.<Pair<T>, StablePriorityQueue<Pair<T>>>toCollection(()
                        -> new StablePriorityQueue<>()));
    }

    @Override
    public boolean tryAdvance(Consumer<? super T> action) {
        Objects.requireNonNull(action, "action==null");
        if (iterationQueue.isEmpty()) return false;

        var smallestElement = iterationQueue.poll();

        action.accept(smallestElement.value());

        if (smallestElement.itr().hasNext()) {
            var next = smallestElement.itr.next();
            iterationQueue.add(new Pair<>(next, smallestElement.itr(), comparator));
        }
        return true;
    }

    @Override
    public Spliterator<T> trySplit() {
        // never split - parallel not supported
        return null;
    }

    public long estimateSize() {
        return initialEstimatedSize;
    }

    public int characteristics() {
        return characteristics;
    }

    public Comparator<? super T> getComparator() {
        return comparator;
    }

    private static class Pair<T> implements Comparable<Pair<T>> {

        final T value;

        final Iterator<T> itr;

        Comparator<? super T> comparator;

        public Pair(T value, Iterator<T> itr, Comparator<? super T> comparator) {
            this.comparator = comparator;
            this.value = value;
            this.itr = itr;
        }

        @Override
        public int compareTo(Pair<T> o) {
            return comparator.compare(this.value, o.value);
        }

        public T value() {
            return value;
        }

        public Iterator<T> itr() {
            return itr;
        }
    }
}
