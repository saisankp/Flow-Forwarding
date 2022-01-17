import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

import tcdIO.Terminal;

public class Switch extends Node {
	private Terminal terminal;
	private String[][] flowTable;
	private DatagramPacket switchBuffer;
	private int switchNumber;

	Switch(Terminal terminal, int switchNumber) {
		this.terminal = terminal;
		this.switchNumber = switchNumber;
		try {
			socket = new DatagramSocket(INFO.PORT_OF_SWITCH);
			listener.go();
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		try {
			Terminal terminal = new Terminal("Switch");
			int switchNumber = Integer.parseInt(terminal.readString("What switch number am I?: "));
			(new Switch(terminal, switchNumber)).start();
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}
	}

	public synchronized void start() throws Exception {
		//Send a 'hello' packet to the controller since the switch has started running.
		PacketUtility packet = new PacketUtility();
		packet.helloPacket(INFO.CONTROLLER_HOST ,this.switchNumber);
		sendPacket(packet);
		while (true) {
			terminal.println("Waiting for messages to forward.");
			this.wait();
		}
	}

	public void flowTableModification(byte[] packetData) {
		byte[] incomingPacketWithoutHeader = new byte[packetData.length-3];
		for(int i = 0; i < incomingPacketWithoutHeader.length; i++) {
			incomingPacketWithoutHeader[i] = packetData[i+3];
		}
		String messageFromIncomingPacket = new String(incomingPacketWithoutHeader).replaceAll("\0", "");
		byte[] messageFromPacket = messageFromIncomingPacket.getBytes();
		String tableAsString = "";
		try {
			tableAsString = new String(messageFromPacket, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		String[] tableRowsAsStringArray = tableAsString.split(",");
		flowTable = new String[tableRowsAsStringArray.length/3][3];
		int currentRow = 0;
		for(int i = 0; i < tableRowsAsStringArray.length; i++) {
			if(i != 0) {
				if(i%3 == 0) {
					currentRow++;
				}
			}
			if(flowTable == null || flowTable.length == 0) {
				break;
			}
			flowTable[currentRow][i%3] = tableRowsAsStringArray[i];
		}

	}

	public void forwardPacketInSwitchBuffer() throws UnknownHostException {
		try {
			boolean finished = false;
			byte[] packetContent = this.switchBuffer.getData();
			for(int i = 0; i < this.flowTable.length && !finished; i++) {
				String compare = "";
				//If the localhost is 127.0.0.1 and flowtable[i][1] is Switch 3 net 3 (R2 ~ 172.20.66.4)
				if(this.switchBuffer.getAddress().getHostAddress().equals(INFO.LOCALHOST_IP) && flowTable[i][1].equals(INFO.USER_2_ADDRESS) && this.switchBuffer.getSocketAddress().toString().contains("49600")) {
					this.switchBuffer.setSocketAddress(new InetSocketAddress("127.0.0.1", 53521));
				}
				//Hence it's user 1 is the end destination.
				else if(packetContent[1] == 1) {
					compare = INFO.USER_1_ADDRESS;
					if(flowTable[i][2].equals("localhost") || flowTable[i][2].equals("127.0.0.1") ) {
						this.switchBuffer.setSocketAddress(new InetSocketAddress(flowTable[i][2], INFO.PORT_NUMBER_OF_USER_1));
					}
				}
				//Hence it's user 2 is the end destination;
				else if(packetContent[1] == 2) {
					compare = INFO.USER_2_ADDRESS;
					if(flowTable[i][2].equals("localhost") || flowTable[i][2].equals("127.0.0.1") ) {
						this.switchBuffer.setSocketAddress(new InetSocketAddress(flowTable[i][2], INFO.PORT_NUMBER_OF_USER_2));
					}
				}
				if(compare.equals(flowTable[i][0]) && this.switchBuffer.getAddress().getHostAddress().equals(flowTable[i][1])) {
					this.switchBuffer.setSocketAddress(new InetSocketAddress(flowTable[i][2], INFO.PORT_OF_SWITCH));
					finished = true;
				}
			}
			socket.send(this.switchBuffer);
			terminal.println("I just forwarded the switchBuffer to the next node to get the packets to it's destination.");
			this.switchBuffer = null;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public synchronized void onReceipt(DatagramPacket incomingPacket) {
		byte[] incomingPacketAsByteArray = incomingPacket.getData();
		//The hello message we sent has been acknowledged.
		if(incomingPacketAsByteArray[0] == INFO.HELLO) {
			terminal.println("I just got a 'hello' packet.");
		}
		//If we must send a packet out.
		else if (incomingPacketAsByteArray[0] == INFO.PACKET_OUT) {
			terminal.println("I just got a 'packet_out' packet.");
			if(switchBuffer == null) {
				switchBuffer = incomingPacket;
			}
			//Create a 'packet_in' packet and send it to the controller.
			PacketUtility packetIn = new PacketUtility();
			packetIn.packetIn(INFO.CONTROLLER_HOST, incomingPacketAsByteArray[1]);
			sendPacket(packetIn);
			terminal.println("I just sent a 'packet_in' packet to the Controller");
		}
		else if (incomingPacketAsByteArray[0] == INFO.FLOW_MOD) {
			terminal.println("I just got a 'flowMod' packet from the Controller");
			flowTableModification(incomingPacketAsByteArray);
			try {
				forwardPacketInSwitchBuffer();
			} catch (UnknownHostException e) {
				e.printStackTrace();
			}
		}
		else {
			terminal.println("An unknown packet has been sent.");
		}
	}
}