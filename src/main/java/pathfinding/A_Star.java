package pathfinding;

import com.google.common.collect.ImmutableList;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.PriorityQueue;
import java.util.stream.Collectors;

import static java.lang.Float.compare;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class A_Star {

  public static void main(String[] args) {

    //   X 0       1       2       3       4
    // Y
    // 0   a - 1 - b - 1 - c - 2 - f - 1 - g
    //     |       |
    //     1       5
    //     |       |
    // 1   d - 4 - e

    final Node a = new Node("a", 0, 0);
    final Node b = new Node("b", 1, 0);
    final Node c = new Node("c", 2, 0);
    final Node d = new Node("d", 0, 1);
    final Node e = new Node("e", 1, 1);
    final Node f = new Node("f", 3, 0);
    final Node g = new Node("g", 4, 0);

    System.out.println("distance(a, e) = " + Node.distance(a, e));
    System.out.println("distance(b, e) = " + Node.distance(b, e));
    System.out.println("distance(c, e) = " + Node.distance(c, e));
    System.out.println("distance(d, e) = " + Node.distance(d, e));
    System.out.println("distance(e, e) = " + Node.distance(e, e));
    System.out.println("distance(f, e) = " + Node.distance(f, e));
    System.out.println("distance(g, e) = " + Node.distance(g, e));

    a.connect(b, 1);
    a.connect(d, 1);
    b.connect(c, 2);
    b.connect(e, 5);
    d.connect(e, 4);
    c.connect(f, 2);
    f.connect(g, 1);

    // a -> e
    assertThat(search(a, e), is(new Path(5, a, d, e)));
    assertThat(f.visited, is(false));
    assertThat(g.visited, is(false));
  }

  /**
   * O(|E|)
   */
  private static Path search(final Node origin, final Node destination) {
    final PriorityQueue<Node> queue = new PriorityQueue<>((a, b) -> compare(a.cost(destination), b.cost(destination)));
    queue.add(origin);

    while (!queue.isEmpty()) {
      final Node node = queue.poll();

      // Mark this node as visited
      node.visited = true;

      // Are we there yet?
      if (node == destination) {
        return path(destination);
      }

      // Update costs for all unvisited neighbors and add them to queue
      node.neighbors.forEach((neighbor, distance) -> {
        if (neighbor.visited) {
          return;
        }
        final int newCost = node.cost + distance;
        if (neighbor.prev == null || neighbor.cost > newCost) {
          neighbor.prev = node;
          neighbor.cost = newCost;
        }
        queue.add(neighbor);
      });
    }

    // No path found. Graph is partitioned.
    return null;
  }

  private static Path path(final Node destination) {
    final Deque<Node> path = new ArrayDeque<>();
    Node n = destination;
    while (n != null) {
      path.addFirst(n);
      n = n.prev;
    }
    return new Path(destination.cost, path);
  }

  private static class Cost {

    private final Node prev;
    private final int cost;

    public Cost(final Node prev, final int cost) {
      this.prev = prev;
      this.cost = cost;
    }
  }

  private static class Path {

    private final int cost;
    private final List<Node> path;

    public Path(final int cost, final Collection<Node> path) {
      this.cost = cost;
      this.path = ImmutableList.copyOf(path);
    }

    public Path(final int cost, final Node... path) {
      this.cost = cost;
      this.path = ImmutableList.copyOf(path);
    }

    @Override
    public boolean equals(final Object o) {
      if (this == o) { return true; }
      if (o == null || getClass() != o.getClass()) { return false; }
      final Path path1 = (Path) o;
      return cost == path1.cost &&
             Objects.equals(path, path1.path);
    }

    @Override
    public int hashCode() {
      return Objects.hash(cost, path);
    }

    @Override
    public String toString() {
      return "Path{" +
             "cost=" + cost +
             ", path=" + path +
             '}';
    }
  }

  private static class Node {

    private final String name;
    private final int x;
    private final int y;
    private final Map<Node, Integer> neighbors = new HashMap<>();

    // Traversal state
    private Node prev;
    private int cost;
    private boolean visited;

    public Node(final String name, final int x, final int y) {
      this.name = name;
      this.x = x;
      this.y = y;
    }

    public void connect(final Node node, final int cost) {
      neighbors.put(node, cost);
    }

    @Override
    public String toString() {
      return "Node{" + name + ", " +
             "neighbors = (" + neighbors.entrySet().stream()
                 .map(e -> e.getValue() + ":" + e.getKey().name)
                 .collect(Collectors.joining(", ")) +
             ")}";
    }

    public void reset() {
      cost = 0;
      prev = null;
      visited = false;
    }

    public float cost(final Node destination) {
      return cost + distance(this, destination);
    }

    private static float distance(final Node a, final Node b) {
      return (float) Math.sqrt(Math.pow(b.x - a.x, 2) + Math.pow(b.y - a.y, 2));
    }
  }
}
