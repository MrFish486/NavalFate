import java.net.*;
import java.io.IOException;
import java.util.regex.*;

public class client {
	public static void main (String args[]) {
		try {
			String        out, in;
			int           port;
			byte[]        rcv = new byte[2048];
			IntetAdddress address;
			// Parse arguments and set outgoing
			DatagramSocket socket = new DatagramSocket();
			DatagramPacket packet = new DatagramPacket(out.getBytes(), out.getBytes().length, address, port);
			socket.send(packet);
			DatagramPacket rcvpacket = new DatagramPacket(rcv, rcv.length);
			socket.receive(rcv);
			in = new String(rcvpacket.getData(), 0, rcvpacket.getLength());
			// Act based on what the server returned
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
