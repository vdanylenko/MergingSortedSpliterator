package net.danylenko.merging;

import org.openjdk.jmh.annotations.*;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@State(Scope.Thread)
// @Warmup(iterations = 2, time = 1)
// @Measurement(iterations = 2, time = 1)
 @Fork(1)
public class PerformanceTestJMH {
    private static final int SIZE = 1_500_000;
    List<Stream<Integer>> streams;

    private static Stream<Integer> test(Function<List<Stream<Integer>>,
            Stream<Integer>> merger, List<Stream<Integer>> streams) {
        return merger.apply(streams);
    }

    private static List<Stream<Integer>> makeStreams() {
        return Stream.generate(() -> generateSorted(SIZE))
                .limit(100).collect(Collectors.toList());
    }

    private static Stream<Integer> generateSorted(int size) {
        return IntStream.range(0, size).boxed();
    }

    @Setup(Level.Invocation)
    public void setup() {
        streams = makeStreams();
    }

    @Benchmark
    @BenchmarkMode({Mode.Throughput})
    @OutputTimeUnit(TimeUnit.SECONDS)
    public List<Integer> flatMapTest() {
        return test(s -> s.stream()
                .flatMap(Function.identity())
                .sorted(), streams).toList();
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.SECONDS)
    public List<Integer> flatMapParallelTest() {
        return test(s -> s.stream()
                .flatMap(Function.identity())
                .parallel()
                .sorted(), streams).toList();
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.SECONDS)
    public List<Integer>  mergeTest() {
        return test(s -> StreamSupport.stream(
                new MergingSortedSpliterator<>(s), false
        ), streams).toList();
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.SECONDS)
    public List<Integer> mergeTestPQ() {
        return test(s -> StreamSupport.stream(
                new MergingSortedSpliteratorPQ<>(s), false
        ), streams).toList();
    }
}

/*

Benchmark                               Mode  Cnt   Score   Error  Units
PerformanceTestJMH.flatMapParallelTest  avgt    5   5.774 ± 0.692   s/op
PerformanceTestJMH.flatMapTest          avgt    5  12.205 ± 4.085   s/op
PerformanceTestJMH.mergeTest            avgt    5  26.256 ± 0.601   s/op
PerformanceTestJMH.mergeTestPQ          avgt    5   9.910 ± 0.407   s/op

 */