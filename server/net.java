import java.net.*;
import java.io.IOException;

public class net {
	public static void send (String message, InetAddress recipient, long port) throws UnknownHostException, SocketException, IOException {
		byte[] buffer = data.getBytes();
		DatagramPacket packet = new DatagramPacket(buffer, buffer.length, recipient, port);
		DatagramSocket socket = new DatagramSocket();
		soket.send(packet);
	}
	public static void reply (long localport, String reply, String localdata
}
