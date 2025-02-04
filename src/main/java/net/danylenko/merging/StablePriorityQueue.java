package net.danylenko.merging;

import java.util.*;


class StablePriorityQueue<T extends Comparable> extends AbstractQueue<T> {

    private final PriorityQueue<PQRecord> priorityQueue;

    private Long counter = 0L;

    public StablePriorityQueue() {
        priorityQueue = new PriorityQueue<>(Comparator.comparing((PQRecord t) -> t.getValue())
                .thenComparingLong(value -> ((PQRecord) value).getOrder()));
    }

    @Override
    public Iterator<T> iterator() {
        Iterator<PQRecord> iterator = priorityQueue.iterator();
        return new Iterator<>() {


            @Override
            public boolean hasNext() {
                return iterator.hasNext();
            }

            @Override
            public T next() {
                return (T) iterator.next().value;
            }

            public void remove() {
                iterator.remove();
            }
        };
    }

    @Override
    public int size() {
        return priorityQueue.size();
    }

    @Override
    public boolean add(T t) {
        return priorityQueue.add(new PQRecord(t, counter++));
    }

    @Override
    public boolean offer(T t) {
        return add(t);
    }

    @Override
    public T poll() {
        return (T) priorityQueue.poll().value;
    }

    @Override
    public T element() {
        return null;
    }

    @Override
    public T peek() {
        return (T) priorityQueue.poll().value;
    }

    @Override
    public boolean isEmpty() {
        return priorityQueue.isEmpty();
    }

    private static class PQRecord<T extends Comparable<? super T>> {

        final T value;
        final long order;

        public PQRecord(final T value, final long order) {
            this.value = value;
            this.order = order;
        }

        public T getValue() {
            return value;
        }

        public long getOrder() {
            return order;
        }
    }
}
