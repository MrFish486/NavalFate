import java.net.*;
import java.io.IOException;

public class client {
	public static void main (String args[]) {
		try {
			DatagramSocket socket = new DatagramSocket(9010);
			byte[] data = new byte[64];
			System.out.printf("udp:%s:%d%n", InetAddress.getLocalHost().getHostAddress(), 9010);
			DatagramPacket rcv = new DatagramPacket(data, data.length);
			while (true) {
				socket.receive(rcv);
				String stringdata = new String(rcv.getData(), 0, rcv.getLength());
				System.out.print(String.format("From %s : %s", rcv.getAddress().toString(), stringdata));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
