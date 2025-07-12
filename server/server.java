import java.net.*;
import java.io.IOException;

public class server {
	public static void main (String[] args) throws UnknownHostException, SocketException, IOException {
		byte[] buffer  = "Hello".getBytes();
		InetAddress    address = InetAddress.getByName("127.0.0.1");
		DatagramPacket packet  = new DatagramPacket(
			buffer, buffer.length, address, 9010
		);
		DatagramSocket socket  = new DatagramSocket();
		socket.send(packet);
	}
}

class game {
	int      height;
	int      width;
	player[] players;
	int[][]  map;
	public game (int height, int width, player[] players) {
		this.height  = height;
		this.width   = width;
		this.players = players;
		this.map     = new int[height][width];
	}
}

class player {
	InetAddress address;
	String      name;
	ship[]      ships;
	public player (InetAddress address, String name, ship[] ships) {
		this.address = address;
		this.name    = name;
		this.ships   = ships;
	}
}

class ship {
	public ship (
}
