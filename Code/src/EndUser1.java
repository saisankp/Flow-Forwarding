import java.net.DatagramPacket;
import java.net.DatagramSocket;

import tcdIO.Terminal;

public class EndUser1 extends Node {
	private Terminal terminal;
	
	EndUser1(Terminal terminal) {
		this.terminal = terminal;
		try {
			socket = new DatagramSocket(INFO.PORT_NUMBER_OF_USER_1);
			listener.go();
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		try {
			Terminal terminal = new Terminal("End-User-1");
			(new EndUser1(terminal)).start();
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}
	}
	
	public synchronized void start() throws Exception {
		while (true) {
			String command = terminal.readString("As end-user 1, you can either :\n " + "1. Send a message to end-user 2.\n" + "2. Wait for messages from end-user 2.\n");
			if (command.contains("1")) {
				sendMessageToEndUser2();
			}
			else if (command.contains("2")) {
				terminal.println("Waiting for a message from end-user 2...");
				this.wait();
			}
		}
	}
	
	public synchronized void onReceipt(DatagramPacket incomingPacket) {
		this.notify();
		byte[] incomingPacketAsByteArray = incomingPacket.getData();
		if(incomingPacketAsByteArray[0] == INFO.PACKET_OUT) {
			//Convert the incoming packet to a string, ignoring the first 3 bytes which is the header information.
			byte[] messageInBytes = new byte[incomingPacketAsByteArray.length];
			for(int i = 0; i+3 < incomingPacketAsByteArray.length; i++) {
				messageInBytes[i] = incomingPacketAsByteArray[i+3];
			}
			//Trim the message to get rid of any leading or trailing blank spaces.
			String message = new String(messageInBytes).trim();
			terminal.println("I just got a message from end-user 2: " + message + "\n");
		}
	}
	
	public void sendMessageToEndUser2() {
		String messageToSend = terminal.readString("What message would you like to send?: \n");
		PacketUtility messageToSendAsPacket = new PacketUtility();
		messageToSendAsPacket.packetOut(INFO.LOCALHOST, INFO.PORT_OF_SWITCH, 2, messageToSend);
		sendPacket(messageToSendAsPacket);
	}
}