package net.danylenko.merging;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.*;

public class SortedStreamOfSortedStreams {
    private static final int SIZE = 5;

    public static void main(String... args) {
        List<List<Integer>> sortedLists = List.of(
                generateSortedRandom(SIZE),
                generateSortedRandom(SIZE),
                generateSortedRandom(SIZE),
                generateSortedRandom(SIZE)
        );

        List<Stream<Integer>> streams1 = sortedLists.stream()
                .map(Collection::stream)
                .map(Stream::sorted)
                .toList();

        List<Stream<Integer>> streams2 = sortedLists.stream()
                .map(Collection::stream)
                .map(Stream::sorted)
                .toList();

        List<Integer> list = StreamSupport.stream(
                new MergingSortedSpliterator<>(streams1), false
        ).toList();

        List<Integer> listPQ = StreamSupport.stream(
                new MergingSortedSpliteratorPQ<>(streams2), false
        ).toList();

        for (int i = 0; i < list.size(); i++) {
            System.out.println(list.get(i) + " " + listPQ.get(i));
        }
    }


    private static List<Integer> generateSortedRandom(int size) {
        return ThreadLocalRandom.current().ints(size, 0, size * 4)
                .parallel()
                .boxed()
                .toList();
    }
}