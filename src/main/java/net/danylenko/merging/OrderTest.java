package net.danylenko.merging;

import java.util.*;
import java.util.function.*;
import java.util.stream.*;

public class OrderTest {
    public static void main(String... args) {
        List<String> list1 = List.of(new String("Heinz"), new String("Viktor"));
        List<String> list2 = List.of(new String("Heinz"), new String("Viktor"));
        List<String> list3 = List.of(new String("Heinz"), new String("Viktor"));
        List<String> list4 = List.of(new String("Heinz"), new String("Viktor"));
        List<String> list5 = List.of(new String("Heinz"), new String("Viktor"));

        test(MergingSortedSpliterator::new, list1, list2, list3, list4, list5);
        test(MergingSortedSpliteratorPQ::new, list1, list2, list3, list4, list5);
    }

    @SafeVarargs
    private static void test(Function<List<Stream<String>>, Spliterator<String>> merger, List<String>... lists) {
        Spliterator<String> spliterator = merger.apply(Stream.of(lists).map(List::stream).map(Stream::sorted).toList());
        System.out.println("Testing: " + spliterator.getClass().getSimpleName());
        Stream<String> stream = StreamSupport.stream(spliterator, false);
        stream.forEach(s -> System.out.println(s + " " + System.identityHashCode(s)));

        System.out.println("Expected:");
        for (int j = 0; j < lists[0].size(); j++) {
            for (int i = 0; i < lists.length; i++) {
                String s = lists[i].get(j);
                System.out.println(s + " " + System.identityHashCode(s));
            }
        }
        System.out.println();
    }
}

