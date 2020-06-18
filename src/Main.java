public class Main {

  public static void main(String[] args) {
    var futureGang = new FutureGang();
    futureGang.run();
    try {
      Thread.sleep(100000);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }
}
