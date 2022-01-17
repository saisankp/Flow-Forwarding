import java.net.DatagramPacket;
import java.net.InetSocketAddress;

public class PacketUtility {
	private DatagramPacket packet;
	
	PacketUtility(){}
	
	//Include message information/payload in an array of bytes.
	public void includeMessageInformation(byte[] packetContent, String message) {
		byte[] messageArray = message.getBytes();
		for(int i = 0; i+3 < packetContent.length && i < messageArray.length; i++) {
			packetContent[i+3] = messageArray[i];
		}
	}
	
	//Get the Datagram Packet.
	public DatagramPacket getPacket() {
		return this.packet;
	}
	
	//Create 'flowMod' packet by adding appropriate header and message information after getting neccessary data.
	public void flowMod(String address, int portNumber, String controllerFlowTable[][]) {
		//Now we must process the flow table from the controller before it is sent to the switch.
		String data = "";
		for(int i = 0; i < controllerFlowTable.length; i++) {
			if(controllerFlowTable[i][2].equals(address)) {
				data = data + controllerFlowTable[i][0] + "," + controllerFlowTable[i][3] + "," + controllerFlowTable[i][4] + ",";
			}
		}
		byte[] packetAsByteArray = new byte[data.length() + 3];
		includeHeaderInformation(packetAsByteArray, INFO.FLOW_MOD, INFO.PORT_NUMBER_OF_CONTROLLER, packetAsByteArray.length);
		includeMessageInformation(packetAsByteArray, data);
		InetSocketAddress dstAddress = new InetSocketAddress(address, portNumber);
		DatagramPacket packet = new DatagramPacket(packetAsByteArray, packetAsByteArray.length, dstAddress);
		this.packet = packet;
	}
	
	//Create 'packet_in' packet by adding appropriate header information.
	public void packetIn(String address, int finalDestination) {
		byte[] headerInformation = new byte[3];
		includeHeaderInformation(headerInformation, INFO.PACKET_IN, finalDestination, headerInformation.length);
		InetSocketAddress dstAddress = new InetSocketAddress(address, INFO.PORT_NUMBER_OF_CONTROLLER);
		DatagramPacket packet = new DatagramPacket(headerInformation, headerInformation.length, dstAddress);
		this.packet = packet;
	}
	
	//Create 'hello' packet by adding appropriate header information.
	public void helloPacket(String address, int finalDestination) {
		byte[] headerInformation = new byte[3];
		includeHeaderInformation(headerInformation, INFO.HELLO, finalDestination, headerInformation.length);
		InetSocketAddress dstAddress = new InetSocketAddress(address, INFO.PORT_NUMBER_OF_CONTROLLER);
		DatagramPacket finalPacket = new DatagramPacket(headerInformation, headerInformation.length, dstAddress);
		this.packet = finalPacket;
	}
	
	//Create 'packet_out' packet by adding appropriate header information.
	public void packetOut(String address, int port, int finalDestination, String message) {
		byte[] packetAsByteArray = new byte[message.length()+3];
		includeHeaderInformation(packetAsByteArray, INFO.PACKET_OUT, finalDestination, packetAsByteArray.length);
		includeMessageInformation(packetAsByteArray, message);
		InetSocketAddress dstAddress = new InetSocketAddress(address, port);
		DatagramPacket packet = new DatagramPacket(packetAsByteArray, packetAsByteArray.length, dstAddress);
		this.packet = packet;
	}
	
	//Include header information in an array of bytes.
	public void includeHeaderInformation(byte[] incomingPacket, int typeOfPacket, int finalEndDestination, int lengthOfPacket) {
		incomingPacket[0] = (byte) typeOfPacket;
		incomingPacket[1] = (byte) finalEndDestination;
		incomingPacket[2] = (byte) lengthOfPacket;
	}
}