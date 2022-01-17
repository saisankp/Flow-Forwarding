import java.net.DatagramPacket;
import java.net.DatagramSocket;

import tcdIO.Terminal;

public class Controller extends Node {
	private Terminal terminal;
	private final String[][] controllerFlowTable = { 
//                                               SEND MESSAGES FROM END-USER 1 TO END-USER 2
//          { Final Destination , Original start point,       Current Switch     ,         Component In     , 	      Component Out      }
			{INFO.USER_2_ADDRESS, INFO.USER_1_ADDRESS, INFO.USER_1_ADDRESS,        INFO.LOCALHOST_IP,          INFO.SWITCH_2_ADDRESS_NET1},
			{INFO.USER_2_ADDRESS, INFO.USER_1_ADDRESS, INFO.SWITCH_2_ADDRESS_NET1, INFO.USER_1_ADDRESS,        INFO.SWITCH_3_ADDRESS_NET2},
			{INFO.USER_2_ADDRESS, INFO.USER_1_ADDRESS, INFO.SWITCH_3_ADDRESS_NET2, INFO.SWITCH_2_ADDRESS_NET2, INFO.USER_2_ADDRESS},
			{INFO.USER_2_ADDRESS, INFO.USER_1_ADDRESS, INFO.USER_2_ADDRESS,        INFO.SWITCH_3_ADDRESS_NET3, INFO.LOCALHOST_IP},

//          									 SEND MESSAGES FROM END-USER 2 TO END-USER 1
//          { Final Destination , Original start point,       Current Switch     ,         Component In     , 	      Component Out      }
			{INFO.USER_1_ADDRESS, INFO.USER_2_ADDRESS, INFO.USER_2_ADDRESS,        INFO.LOCALHOST_IP,          INFO.SWITCH_3_ADDRESS_NET3}, 
			{INFO.USER_1_ADDRESS, INFO.USER_2_ADDRESS, INFO.SWITCH_3_ADDRESS_NET2, INFO.USER_2_ADDRESS,        INFO.SWITCH_2_ADDRESS_NET2},
			{INFO.USER_1_ADDRESS, INFO.USER_2_ADDRESS, INFO.SWITCH_2_ADDRESS_NET1, INFO.SWITCH_3_ADDRESS_NET2, INFO.USER_1_ADDRESS},
			{INFO.USER_1_ADDRESS, INFO.USER_2_ADDRESS, INFO.USER_1_ADDRESS,        INFO.SWITCH_2_ADDRESS_NET1, INFO.LOCALHOST_IP}
	};

	Controller(Terminal terminal) {
		try {
			this.terminal = terminal;
			socket = new DatagramSocket(INFO.PORT_NUMBER_OF_CONTROLLER);
			listener.go();
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		try {
			Terminal terminal = new Terminal("Controller");
			(new Controller(terminal)).start();
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}
	}

	public synchronized void start() throws Exception {
		terminal.println("This controller is on port: " + INFO.PORT_NUMBER_OF_CONTROLLER);
		while (true) {
			terminal.println("Waiting for contact");
			this.wait();
		}
	}

	//If the controller gets a "hello" packet from any of the switches, then it must respond back with a "hello" packet.
	//If the controller gets a "packet_in" packet, then it must respond back by updating the flow table of thw switch.
	public synchronized void onReceipt(DatagramPacket incomingPacket) {
		byte[] incomingPacketAsByteArray = incomingPacket.getData();
		//Get the switch number that this packet is coming from.
		String switchNumber = "";
		if(incomingPacket.getAddress().getHostAddress().equals(INFO.SWITCH_1_ADDRESS)) {
			switchNumber = "1";
		}
		else if(incomingPacket.getAddress().getHostAddress().equals(INFO.SWITCH_2_ADDRESS_NET1) || incomingPacket.getAddress().getHostAddress().equals(INFO.SWITCH_2_ADDRESS_NET2)) {
			switchNumber = "2";
		} else if(incomingPacket.getAddress().getHostAddress().equals(INFO.SWITCH_3_ADDRESS_NET2) || incomingPacket.getAddress().getHostAddress().equals(INFO.SWITCH_3_ADDRESS_NET3)) {
			switchNumber = "3";
		} else if(incomingPacket.getAddress().getHostAddress().equals(INFO.SWITCH_4_ADDRESS)) {
			switchNumber = "4";
		}
		
		if(incomingPacketAsByteArray[0] == INFO.HELLO) {
			terminal.println("I just got a 'hello' packet from switch number: " + switchNumber);
			//Send back a 'hello' packet!
			PacketUtility respondBackWithHello = new PacketUtility();
			respondBackWithHello.helloPacket(incomingPacket.getAddress().getHostAddress(), incomingPacketAsByteArray[1]);
			sendPacket(respondBackWithHello);
		}
		else if (incomingPacketAsByteArray[0] == INFO.PACKET_IN) {
			terminal.println("I just got a 'packet_in' packet from switch number: " + switchNumber);
			//Update the table and send it.
			PacketUtility updatedTable = new PacketUtility();
			updatedTable.flowMod(incomingPacket.getAddress().getHostAddress(), incomingPacket.getPort(),  this.controllerFlowTable);
			sendPacket(updatedTable);
			terminal.println("I just sent a 'flowMod' packet to switch number: " + switchNumber);
		}
	}
}