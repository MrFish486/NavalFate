// Inspired by the docopt example https://www.pypi.org/project/docopt/
import java.net.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.*;

public class server {
	public static void main (String[] args) throws SocketException, IOException {
		int port;
		String ownip;
		try {
			port = Integer.parseInt(args[0]);
		} catch (Exception e) {
			System.out.println("Usage : java server <local port>");
			return;
		}
		System.out.println("Loading...");
		Pattern a = Pattern.compile("placemine-[0-9]{1,}-[0-9]{1,}-(moored|drifting)");
		Pattern b = Pattern.compile("placeship-[0-9]{1,}-[0-9]{1,}-[a-zA-Z0-9]{1,}");
		Pattern c = Pattern.compile("removemine-[0-9]{1,}-[0-9]{1,}");
		Pattern d = Pattern.compile("info-[0-9]{1,}-[0-9]{1,}");
		Pattern ee = Pattern.compile("moveship-[a-zA-Z0-9]{1,}-[0-9]{1,}-[0-9]{1,}");
		Pattern f = Pattern.compile("shoot-[a-zA-Z0-9]{1,}-[0-9]{1,}-[0-9]{1,}");
		game main = new game(20, 20);

		String version = "0.1";
		String motd    = "Hello from the server!";

		byte[] rcvdata = new byte[256];
		System.out.println("\033[FFetching own ip...");
		ownip = InetAddress.getLocalHost().getHostAddress().toString();
		System.out.println(String.format("\033[FServer is listening. Use `nc -u %s %s` to connect with netcat. Consider configuring port forwarding.", ownip, args[0]));
		DatagramPacket rcv    = new DatagramPacket(rcvdata, rcvdata.length);
		String request, reply;
		DatagramSocket socket = new DatagramSocket(port);
		while (true) {
			socket.receive(rcv);
			request = new String(rcv.getData(), 0, rcv.getLength());
			try {
				request = request.replaceAll("^[\n\r]", "").replaceAll("[\n\r]$", "");
				//request = request.split("\n")[0]; // for netcat test purposes
			} catch (Exception e) {}
			System.out.println(String.format("From ip(%s), port(%d) : '%s'", rcv.getAddress().toString(), rcv.getPort(), request));
			if (request.equals("help")) {
				reply = "Refer to https://www.github.com/MrFish486/NavalFate for help";
			} else if (request.equals("version?")) {
				reply = version;
			} else if (request.equals("status?")) {
				reply = main.toString();
			} else if (request.equals("ip?")) {
				reply = ownip;
			} else if (a.matcher(request).matches()) {
				reply = "!2";
				try {
					boolean act = true;
					for (int i = 0; i < main.ships.size(); i ++) {
						if (main.ships.get(i).name.equals(request.split("-")[3])) {
							reply = "0";
							act = false;
						}
					}
					if (act) {
						boolean p = main.placemine(Integer.parseInt(request.split("-")[2]), Integer.parseInt(request.split("-")[1]), request.split("-")[3]);
						if (p) {
							reply = "1";
						} else {
							reply = "0";
						}
					}
				} catch (Exception e) {
					reply = "!1";
				}
			} else if (b.matcher(request).matches()) {
				reply = "!1";
				try {
					boolean p = main.placeship(Integer.parseInt(request.split("-")[2]), Integer.parseInt(request.split("-")[1]), request.split("-")[3]);

					if (p) {
						reply = "1";
					} else {
						reply = "0";
					}
				} catch (Exception e) {
					reply = "!1";
				}
			} else if (request.equals("tick")) {
				try {
					main.tick();
					reply = "1";
				} catch (Exception e) {
					reply = "0";
				}
			} else if (c.matcher(request).matches()) {
				reply = "!2";
				try {
					reply = "0";
					for (int i = 0; i < main.mines.size(); i ++) {
						if (main.mines.get(i).x == Integer.parseInt(request.split("-")[2]) && main.mines.get(i).y == Integer.parseInt(request.split("-")[1])) {
							main.mines.remove(i);
							reply = "1";
							break;
						}
					}
				} catch (Exception e) {
					reply = "!1";
				}
			} else if (d.matcher(request).matches()) {
				reply = "!2";
				try {
					reply = String.format("water at x %s, y %s", request.split("-")[1], request.split("-")[2]);
					boolean acted = false;
					for (int i = 0; i < main.mines.size(); i ++) {
						if (main.mines.get(i).x == Integer.parseInt(request.split("-")[2]) && main.mines.get(i).y == Integer.parseInt(request.split("-")[1]) && !acted) {
							reply = main.mines.get(i).toString();
							acted = true;
							break;
							
						}
					}
					for (int i = 0; i < main.ships.size(); i ++) {
						if (main.ships.get(i).x == Integer.parseInt(request.split("-")[2]) && main.ships.get(i).y == Integer.parseInt(request.split("-")[1]) && !acted) {
							reply = main.ships.get(i).toString();
							acted = true;
							break;
						}
					}
				} catch (Exception e) {
					reply = "!1";
				}
			} else if (ee.matcher(request).matches()) {
				reply = "!2";
				try {
					reply = "0";
					for (int i = 0; i < main.ships.size(); i ++) {
						if (main.ships.get(i).name.equals(request.split("-")[1])) {
							int xx = Integer.parseInt(request.split("-")[3]); // Must have atomic operations
							int yy = Integer.parseInt(request.split("-")[2]);
							main.ships.get(i).setpos(xx, yy);
							reply = "1";
						}
					}
				} catch (Exception e) {
					reply = "!1";
				}
			} else if (f.matcher(request).matches()) {
				reply = "!2";
				try {
					reply = "0";
					boolean acted = false;
					for (int i = 0; i < main.ships.size(); i ++) {
						if (main.ships.get(i).name.equals(request.split("-")[1])) {
							int xx = Integer.parseInt(request.split("-")[3]);
							int yy = Integer.parseInt(request.split("-")[2]);
							for (int ii = 0; ii < main.ships.size(); ii ++) {
								if (main.ships.get(ii).x == xx && main.ships.get(ii).y == yy && !acted) {
									main.ships.remove(ii);
									acted = true;
									reply = "1";
									break;
								}
							}
							for (int ii = 0; ii < main.mines.size(); ii ++) {
								if (main.mines.get(ii).x == xx && main.mines.get(ii).y == yy && !acted) {
									main.mines.remove(ii);
									acted = true;
									reply = "1";
									break;
								}
							}
						}
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
	public boolean placeship (int x, int y, String name) {
		if (!this.isOccupied(x, y)) {
			this.ships.add(new ship(x, y, name));
			return true;
		}
		return false;
	}
	public void tick () {
		for (int i = 0; i < this.mines.size(); i ++) {
			mines.get(i).drift(this.height, this.width);
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
						ret += 'S';
						charfound = true;
					}
				}
				if (!charfound) {
					ret += '~';
				}
				ret += ' ';
			}
			ret += "\n\n";
		}
		ret += String.format("%d mine(s), %d ship(s)\n", this.mines.size(), this.ships.size());
		ret += "M : moored (anchored) mine\nD : drifting mine\nS : ship\n~ : water\n";
		return ret;
	}
}
class ship {
	int x;
	int y;
	String name;
	public ship (int x, int y, String name) {
		this.x = x;
		this.y = y;
		this.name = name;
	}
	public String toString () {
		return String.format("ship \"%s\" at x %d, y %d", this.name, this.y, this.x);
	}
	public void setpos (int x, int y) {
		this.x = x;
		this.y = y;
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
		return String.format("%s mine at x %d, y %d", this.type, this.y, this.x);
	}
	public void drift (int h, int w) {
		if (this.type.equals("drifting")) {
			double choice = Math.random();
			if (choice > 0 && choice < 0.25) {
				if (this.x != w) {
					this.x ++;
				}
			} else if (choice > 0.25 && choice < 0.5) {
				if (this.x != 0) {
					this.x --;
				}
			} else if (choice > 0.5 && choice < 0.75) {
				if (this.y != h) {
					this.y ++;
				}
			} else if (choice > 0.75 && choice < 1) {
				if (this.y != 0) {
					this.y --;
				}
			}
		}
	}
	public char rep () {
		if (this.type.equals("moored")) {
			return 'M';
		} else if (this.type.equals("drifting")) {
			return 'D';
		} else {
			return '?';
		}
	}
}
