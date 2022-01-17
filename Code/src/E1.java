public class E1 {
	public static void main(String[] args) {
		try {
			//Create two threads to setup the Docker container E1, containing User 1 and it's local switch (i.e. service).
			Thread one = new Thread(() -> Switch.main(args));
			Thread two = new Thread(() -> EndUser1.main(args));
			one.start();
			two.start();
			one.join();
			two.join();
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}
	}
}
