import java.util.List;
import java.util.concurrent.*;

public class FutureGang {
  private final ExecutorService executorService = Executors.newSingleThreadExecutor();

  abstract static class Assertion {
    private final CompletableFuture<String> future = new CompletableFuture<>();

    public CompletableFuture<String> collectResult() {
      return future;
    }

    void complete(String res) {
      future.complete(res);
    }
  }

  class AssertionImpl extends Assertion {
    private final long millis;

    public AssertionImpl(long millis) {
      log("Creating " + millis);
      this.millis = millis;
    }

    public void execute() {
      log("Executing " + millis);
      try {
        log(millis + " sleeping");
        Thread.sleep(millis);
        log(millis + " done");
        complete("yes");
      } catch (Exception e) {
        log(millis + " exception");
        complete("no");
      }
    }
  }

  private void log(String message) {
    System.out.println(
      String.format(
        "%s -> %s",
        String.valueOf(System.currentTimeMillis()).substring(8),
        message
      )
    );
  }

  private CompletableFuture<List<String>> wrappedExecutor(
    List<AssertionImpl> assertions
  ) {
    CompletableFuture<List<String>> wrapFuture = new CompletableFuture<>();

    for (var assertion : assertions) {
      executorService.submit(assertion::execute);
    }

    return wrapFuture;
  }

  public void run() {
    var assertions = List.of(
      new AssertionImpl(1000),
      new AssertionImpl(1001),
      new AssertionImpl(1002),
      new AssertionImpl(1003),
      new AssertionImpl(1004),
      new AssertionImpl(1005)
    );

    try {
      wrappedExecutor(assertions).get(3, TimeUnit.SECONDS);
    } catch (Exception e) {
      log("wrapper timed out!");
      e.printStackTrace();
    }

    final List<Runnable> unfinished = executorService.shutdownNow();
    System.out.println("unfinished " + unfinished);

    assertions
      .stream()
      .map(Assertion::collectResult)
      .forEach(
        f -> {
          f.cancel(true);
        }
      );
  }
}
