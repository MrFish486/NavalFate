import java.net.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.*;

public class server {
	public static void main (String[] args) throws SocketException, IOException {
		Pattern a = Pattern.compile("placemine-[0-9]{1,}-[0-9]{1,}-(moored|drifting)");
		Pattern b = Pattern.compile("placeship-[0-9]{1,}-[0-9]{1,}");
		game main = new game(20, 20);
		String version = "1.0";
		byte[] rcvdata = new byte[256];
		System.out.println("Server is listening");
		DatagramPacket rcv    = new DatagramPacket(rcvdata, rcvdata.length);
		String request, reply;
		DatagramSocket socket = new DatagramSocket(Integer.parseInt(args[0]));
		while (true) {
			socket.receive(rcv);
			request = new String(rcv.getData(), 0, rcv.getLength());
			try {
				request = request.split("\n")[0]; // for netcat test purposes
			} catch (Exception e) {}
			System.out.println(String.format("From %s:%d : '%s'", rcv.getAddress().toString(), rcv.getPort(), request));
			if (request.equals("version?")) {
				reply = version;
			} else if (request.equals("status?")) {
				reply = main.toString();
			} else if (a.matcher(request).matches()) {
				try {
					boolean p = main.placemine(Integer.parseInt(request.split("-")[1]), Integer.parseInt(request.split("-")[2]), request.split("-")[3]);
					if (p) {
						reply = "1";
					} else {
						reply = "0";
					}
				} catch (Exception e) {
					reply = "!1";
				}
			} else if (b.matcher(request).matches()) {
				try {
					boolean p = main.placeship(Integer.parseInt(request.split("-")[1]), Integer.parseInt(request.split("-")[2]));
					if (p) {
						reply = "1";
					} else {
						reply = "0";
					}
				} catch (Exception e) {
					reply = "!1";
				}
			} else {
				reply = "!0";
			}
			DatagramPacket send = new DatagramPacket(reply.getBytes(), reply.getBytes().length, rcv.getAddress(), rcv.getPort());
			socket.send(send);
		}
	}
	public static void send (String data, InetAddress recipient, int port) throws UnknownHostException, SocketException, IOException {
		byte[] buffer  = data.getBytes();
		DatagramPacket packet  = new DatagramPacket(buffer, buffer.length, recipient, port);
		DatagramSocket socket  = new DatagramSocket();
		socket.send(packet);
	}
}

class game {
	int               height;
	int               width;
	ArrayList<ship>   ships;
	ArrayList<mine>   mines;
	public game (int height, int width) {
		this.height  = height;
		this.width   = width;
		this.ships   = new ArrayList<ship>();
		this.mines   = new ArrayList<mine>();
	}
	public boolean isOccupied (int x, int y) {
		for (int i = 0; i < this.ships.size(); i ++) {
			if (this.ships.get(i).x == x && this.ships.get(i).y == y) {
				return true;
			}
		}
		for (int i = 0; i < this.mines.size(); i ++) {
			if (this.mines.get(i).x == x && this.mines.get(i).y == y) {
				return true;
			}
		}
		return false;
	}
	public int occupations (int x, int y) {
		int occupations = 0;
		for (int i = 0; i < this.ships.size(); i ++) {
			if (this.ships.get(i).x == x && this.ships.get(i).y == y) {
				occupations ++;
			}
		}
		for (int i = 0; i < this.mines.size(); i ++) {
			if (this.mines.get(i).x == x && this.mines.get(i).y == y) {
				occupations ++;
			}
		}
		return occupations;
	}
	public boolean findInconsistencies () {
		for (int x = 0; x < this.width; x ++) {
			for (int y = 0; y < this.height; y ++) {
				if (this.occupations(x, y) > 1) {
					return true;
				}
			}
		}
		return true;
	}
	public boolean placemine (int x, int y, String type) {
		if (!this.isOccupied(x, y)) {
			if (type.equals("moored") || type.equals("drifting")) {
				this.mines.add(new mine(x, y, type));
				return true;
			}
		}
		return false;
	}
	public boolean placeship (int x, int y) {
		if (!this.isOccupied(x, y)) {
			this.ships.add(new ship(x, y));
			return true;
		}
		return false;
	}
	public void tick () {
		for (int i = 0; i < this.mines.size(); i ++) {
			mines.get(i).drift();
		}
		for (int i = 0; i < this.ships.size(); i ++) {
			for (int ii = 0; ii < this.mines.size(); ii ++) {
				if (this.mines.get(ii).x == this.ships.get(i).x && this.mines.get(ii).y == this.ships.get(i).y) {
					ships.remove(i);
					mines.remove(ii);
				}
			}
			for (int ii = 0; ii < this.ships.size(); ii ++) {
				if (this.ships.get(ii).x == this.ships.get(i).x && this.ships.get(ii).y == this.ships.get(i).y && i != ii) {
					ships.remove(i);
					ships.remove(ii);
				}
			}
		}
	}
	public void moveship (int x, int y, int tx, int ty) {
		for (int i = 0; i < this.ships.size(); i ++) {
			if (this.ships.get(i).x == x && this.ships.get(i).y == y) {
				this.ships.get(i).x = tx;
				this.ships.get(i).y = ty;
			}
		}
	}
	public String toString () {
		String ret = "";
		Boolean charfound;
		for (int x = 0; x < this.width; x ++) {
			for (int y = 0; y < this.height; y ++) {
				charfound = false;
				for (int i = 0; i < this.mines.size(); i ++) {
					if (this.mines.get(i).x == x && this.mines.get(i).y == y && !charfound) {
						ret += this.mines.get(i).rep();
						charfound = true;
					}
				}
				for (int i = 0; i < this.ships.size(); i ++) {
					if (this.ships.get(i).x == x && this.ships.get(i).y == y && !charfound) {
						ret += 's';
						charfound = true;
					}
				}
				if (!charfound) {
					ret += '~';
				}
			}
			ret += '\n';
		}
		ret += '\n';
		ret += String.format("%d mine(s), %d ship(s)", this.mines.size(), this.ships.size());
		return ret;
	}
}
class ship {
	int x;
	int y;
	public ship (int x, int y) {
		this.x = x;
		this.y = y;
	}
	public String toString () {
		return String.format("ship at x %d, y %d", this.x, this.y);
	}
}
class mine {
	int    x;
	int    y;
	String type;
	public mine (int x, int y, String type) {
		this.x    = x;
		this.y    = y;
		this.type = type;
	}
	public String toString () {
		return String.format("%s mine at x %d, y %d", this.type, this.x, this.y);
	}
	public void drift () {
		if (this.type.equals("drifting")) {
			double choice = Math.random();
			if (choice > 0 && choice < 0.25) {
				this.x ++;
			} else if (choice > 0.25 && choice < 0.5) {
				this.x --;
			} else if (choice > 0.5 && choice < 0.75) {
				this.y ++;
			} else if (choice > 0.75 && choice < 1) {
				this.y --;
			}
		}
	}
	public char rep () {
		if (this.type.equals("moored")) {
			return 'm';
		} else if (this.type.equals("drifting")) {
			return 'd';
		} else {
			return '?';
		}
	}
}
