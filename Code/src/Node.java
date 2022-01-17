import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.concurrent.CountDownLatch;

public abstract class Node {
	static final int PACKETSIZE = 65536;
	DatagramSocket socket;
	Listener listener;
	CountDownLatch latch;
	
	Node() {
		latch = new CountDownLatch(1);
		listener = new Listener();
		listener.setDaemon(true);
		listener.start();
	}

	protected void sendPacket(PacketUtility packetUtility) {
		try {
			socket.send(packetUtility.getPacket());
		} catch (IOException e) {
			e.printStackTrace();
		}	
	}
	
	/* 
	 * This function is Abstract so it can be adjusted to different classes such as Switches,
	 * End-users or a Controller.Hence, different classes can behave differently when they receive packets.
	 */
	public abstract void onReceipt(DatagramPacket packet);
	/**
	 * Listens for incoming packets on a datagram socket and informs registered
	 * receivers about incoming packets.
	 */
	class Listener extends Thread {
		//Stating to the listener that the socket has been initialized.
		public void go() {
			latch.countDown();
		}

		//Listen for incoming packets and inform receivers.
		public void run() {
			try {
				latch.await();
				// Infinite loop that attempts to receive packets, and notify receivers.
				while (true) {
					DatagramPacket packet = new DatagramPacket(new byte[PACKETSIZE], PACKETSIZE);
					socket.receive(packet);
					onReceipt(packet);
				}
			} catch (Exception e) {
				if (!(e instanceof SocketException))
					e.printStackTrace();
			}
		}
	}
}