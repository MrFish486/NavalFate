import java.net.*;
import java.io.IOException;
import java.util.ArrayList;

public class server {
	public static void main (String[] args) {
		
	}
	public static void send (String data, InetAddress recipient, long port) throws UnknownHostException, SocketException, IOException {
		byte[] buffer  = data.getBytes();
		DatagramPacket packet  = new DatagramPacket(buffer, buffer.length, recipient, port);
		DatagramSocket socket  = new DatagramSocket();
		socket.send(packet);
	}
}

class InconsistentException extends Exception {
	public InconsistentException () {}
	public InconsistentException (String mesg) {
		super(mesg);
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
	public void findInconsistencies () throws InconsistentException {
		for (int x = 0; x < this.width; x ++) {
			for (int y = 0; y < this.height; y ++) {
				if (this.occupations(x, y) > 1) {
					throw new InconsistentException(String.format("Multiple items found at x %d, y %d.", x, y);
				}
			}
		}
	}
	public void placemine (int x, int y, String type) {
		if (!this.isOccupied(x, y)) {
			if (type == "moored" || type == "drifting") {
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
class InvalidMineException extends Exception {
	public InvalidMineException () {}
	public InvalidMineException (String mesg) {
		super(mesg);
	}
}
class mine {
	int    x;
	int    y;
	String type;
	public mine (int x, int y, String type) {
		if (type != "moored" && type != "drifting") {
			throw new InvalidMineException("Mine must be of type moored or drifting");
		}
		this.x    = x;
		this.y    = y;
		this.type = type;
	}
	public String toString () {
		return String.format("%s mine at x %d, y %d", this.type, this.x, this.y);
	}
	public void drift () {
		if (this.type == "drifting") {
			float choice = Math.random();
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
}
