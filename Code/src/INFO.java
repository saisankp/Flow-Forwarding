public class INFO {
	//Types of OpenFlow packets.
	static final int HELLO = 1;
	static final int PACKET_IN = 2;
	static final int PACKET_OUT = 3;
	static final int FLOW_MOD = 4;	
	
	//End-users in the network and it's associated number.
	static final String ENDUSER1 = "1";
	static final String ENDUSER2 = "2";
	
	//Host names involved in the network.
	static final String CONTROLLER_HOST = "Controller";
	static final String LOCALHOST = "localhost";
	
	//Ports of components on the network
	static final int PORT_OF_SWITCH = 51510;
	static final int PORT_NUMBER_OF_CONTROLLER = 49999;
	static final int PORT_NUMBER_OF_USER_1 = 49600;
	static final int PORT_NUMBER_OF_USER_2 = 53521;
	
	//Addresses of involved in the network.
	static final String LOCALHOST_IP = "127.0.0.1";

	//Network 1 (net1):
	static final String SWITCH_1_ADDRESS = "172.20.11.3";
	static final String USER_1_ADDRESS = "172.20.11.3";
	static final String SWITCH_2_ADDRESS_NET1 = "172.20.11.4";
	
	//Network 2 (net2):
	static final String SWITCH_2_ADDRESS_NET2 = "172.20.33.3";
	static final String SWITCH_3_ADDRESS_NET2 = "172.20.33.4";
	
	//Network 3 (net3):
	static final String SWITCH_3_ADDRESS_NET3 = "172.20.66.4";
	static final String SWITCH_4_ADDRESS = "172.20.66.3";
	static final String USER_2_ADDRESS = "172.20.66.3";
}