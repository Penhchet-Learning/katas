package heap;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;

import quicksort.Util;

import static com.google.common.primitives.Ints.asList;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

@State(Scope.Benchmark)
public class HeapSort {

  private static final int[] INPUT = ThreadLocalRandom.current().ints(10_000).toArray();
  private int[] input = INPUT.clone();

  @Setup(Level.Invocation)
  public void setup() {
    input = Util.IDENTICAL_17_1K.clone();
  }

  public static void main(final String... args) throws RunnerException {
    // Sanity checks
    assertThat(asList(heapSort(new int[]{0, 9, 1, 8, 2, 7, 3, 6, 4, 5})), contains(0, 1, 2, 3, 4, 5, 6, 7, 8, 9));
    assertThat(asList(heapSort(new int[]{0, 9, 1, 8, 2, 7, 3, 6, 4, 5}, 2, 8)), contains(0, 9, 1, 2, 3, 6, 7, 8, 4, 5));
    final int[] vs = ThreadLocalRandom.current().ints(10_000).toArray();
    final int[] vsSorted = vs.clone();
    Arrays.sort(vsSorted);
    assertThat(asList(heapSort(vs)), is(asList(vsSorted)));
    fuzz(1000, 1000);

    // Benchmark
    Options opt = new OptionsBuilder()
        .include(".*" + HeapSort.class.getSimpleName() + ".*")
        .forks(1)
        .warmupIterations(3)
        .measurementIterations(5)
        .build();
    new Runner(opt).run();
  }

  private static void fuzz(final int n, final int m) {
    for (int i = 0; i < n; i++) {
      final ThreadLocalRandom rand = ThreadLocalRandom.current();
      final int[] x = rand.ints(m).toArray();
      final int l = rand.nextInt(m / 2);
      final int u = rand.nextInt(l, m);
      final int[] expected = x.clone();
      Arrays.sort(expected, l, u);
      heapSort(x, l, u);
      assertThat(x, is(expected));
    }
  }

  @Benchmark
  public int[] benchHeapSort() {
    return heapSort(input);
  }

  @Benchmark
  public int[] benchArraysSort() {
    Arrays.sort(input);
    return input;
  }

  /**
   * In-place heap sort.
   *
   * Step 1. Transform the (unsorted) input into a max-heap in-place, iterating from left to right.
   * Step 2. Transform the heap into a sorted list, from right to left, by extracting the greatest element and
   * prepending it to the list, in-place.
   */
  public static int[] heapSort(final int[] x) {
    heapSort(x, 0, x.length);
    return x;
  }

  public static int[] heapSort(final int[] x, final int l, final int u) {
    // Transform input into a heap
    for (int i = l + 1; i < u; i++) {
      siftUp(x, l, i);
    }
    // Transform heap into a sorted array
    for (int i = u - 1; i > l; i--) {
      swap(x, l, i);
      siftDown(x, l, i);
    }
    return x;
  }

  private static void siftDown(final int[] heap, final int l, final int u) {
    // n -> n * 2 + 1
    //      n * 2 + 2

    int i = l;
    while (i < u) {
      final int cl = l + (i - l << 1) + 1;
      if (cl >= u) {
        break;
      }
      final int cr = cl + 1;
      final int c;
      if (cr >= u || heap[cl] >= heap[cr]) {
        c = cl;
      } else {
        c = cr;
      }
      if (heap[c] <= heap[i]) {
        break;
      }
      swap(heap, i, c);
      i = c;
      assert valid(heap, l, i + 1);
    }
  }

  private static boolean valid(final int[] vs, final int l, final int u) {
    for (int i = l + 1; i < u; i++) {
      final int parent = l + (i - l - 1 >> 1);
      if (vs[parent] < vs[i]) {
        return false;
      }
    }
    return true;
  }

  private static void siftUp(final int[] heap, final int l, int i) {
    // n -> (n - 1) / 2
    while (true) {
      final int parent = l + (i - l - 1 >> 1);
      if (parent < l) {
        break;
      }
      if (heap[parent] >= heap[i]) {
        break;
      }
      swap(heap, parent, i);
      i = parent;
    }
    assert valid(heap, l, i + 1);
  }

  private static void swap(final int[] heap, final int a, final int b) {
    final int t = heap[a];
    heap[a] = heap[b];
    heap[b] = t;
  }
}
