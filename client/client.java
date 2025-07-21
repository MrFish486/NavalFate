import java.net.*;
import java.io.IOException;
import java.util.regex.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.BufferedReader;
import java.io.InputStreamReader;

public class client {
	public static void main (String args[]) throws IOException {
		try {
			String        out, in;
			String        version = "0.1";
			int           port = 0;
			byte[]        rcv = new byte[2048];
			InetAddress address = InetAddress.getByName("0.0.0.0");;
			Pattern a = Pattern.compile("ship new [a-zA-Z0-9]{1,} [0-9]{1,} [0-9]{1,}");
			Pattern b = Pattern.compile("ship [a-zA-Z0-9]{1,} move [0-9]{1,} [0-9]{1,}( --speed=[0-9]{1,})?");
 			Pattern c = Pattern.compile("ship [a-zA-Z0-9]{1,} shoot [0-9]{1,} [0-9]{1,}");
			Pattern d = Pattern.compile("mine set [0-9]{1,} [0-9]{1,} --moored");
			Pattern e = Pattern.compile("mine set [0-9]{1,} [0-9]{1,} --drifting");
			Pattern f = Pattern.compile("mine remove [0-9]{1,} [0-9]{1,}");
			Pattern g = Pattern.compile("(--version|-v)");
			Pattern h = Pattern.compile("info [0-9]{1,} [0-9]{1,}");
			Pattern i = Pattern.compile("(-s|status)");
			Pattern j = Pattern.compile("(-t|tick)");
			try {
				File handle = new File(System.getProperty("user.home") + "/.navalfate");
				Scanner read  = new Scanner(handle);
				String line;
				while (read.hasNextLine()) {
					line = read.nextLine();
					try {
						if (line.split(" ").length != 2) {
							System.out.println("An error occurred while parsing the configuration file. The file was located and read successfully, but it can not be recognized by this program. (a)");
							return;
						}
						if (line.split(" ")[0].equals("ip")) {
							address = InetAddress.getByName(line.split(" ")[1]);
						} else if (line.split(" ")[0].equals("port")) {
							port = Integer.parseInt(line.split(" ")[1]);
						} else {
							System.out.println("An error occurred while parsing the configuration file. The file was located and read successfully, but it can not be recognized by this program. (b)");
							return;
						}
					} catch (Exception eee) {
						System.out.println("An error occurred while parsing the configuration file. The file was located and read successfully, but it can not be recognized by this program. (c)");
						return;
					}
				}
				read.close();
			} catch (FileNotFoundException ee) {
				PrintWriter handle = new PrintWriter(System.getProperty("user.home") + "/.navalfate", "UTF-8");
				BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
				System.out.println("The configuration file is missing. This is likely because this program is being run for the first time.");
				System.out.println("Please input the ip address of the server.");
				handle.println("ip " + input.readLine());
				System.out.println("Please input the port that the server is running on.");
				handle.println("port " + input.readLine());
				handle.close();
				System.out.println("The configuration file has been successfully written. This program will now stop.");
				return;
			}
			
			// Parse arguments and set outgoing
			String joinedargs = String.join(" ", args);
			if (a.matcher(joinedargs).matches()) {
				out = String.format("placeship-%s-%s-%s", args[3], args[4], args[2]);
			} else if (b.matcher(joinedargs).matches()) {
				out = String.format("moveship-%s-%s-%s", args[1], args[3], args[4]);
			} else if (c.matcher(joinedargs).matches()) {
				out = String.format("shoot-%s-%s-%s", args[1], args[3], args[4]);
			} else if (d.matcher(joinedargs).matches()) {
				out = String.format("placemine-%s-%s-moored", args[2], args[3]);
			} else if (e.matcher(joinedargs).matches()) {
				out = String.format("placemine-%s-%s-drifting", args[2], args[3]);
			} else if (f.matcher(joinedargs).matches()) {
				out = String.format("removemine-%s-%s", args[2], args[3]);
			} else if (g.matcher(joinedargs).matches()) {
				out = "version?";
			} else if (h.matcher(joinedargs).matches()) {
				out = String.format("info-%s-%s", args[1], args[2]);
			} else if (i.matcher(joinedargs).matches()) {
				out = "status?";
			} else if (j.matcher(joinedargs).matches()) {
				out = "tick";
			} else {
				System.out.println("Naval Fate.\nUsage:\n\tnaval_fate ship new <name> <x> <y>\n\tnaval_fate ship <name> move <x> <y> [--speed=<kn>]\n\tnaval_fate ship <name> shoot <x> <y>\n\tnaval_fate mine (set | remove) <x> <y> [--moored | --drifting]\n\tnaval_fate info <x> <y>\n\tnaval_fate (-s | status)\n\tnaval_fate (-t | tick)\n\tnaval_fate (-h | --help)\n\tnaval_fate (--version | -v)");
				return;
			}
			//
			DatagramSocket socket = new DatagramSocket();
			DatagramPacket packet = new DatagramPacket(out.getBytes(), out.getBytes().length, address, port);
			socket.send(packet);
			DatagramPacket rcvpacket = new DatagramPacket(rcv, rcv.length);
			socket.receive(rcvpacket);
			in = new String(rcvpacket.getData(), 0, rcvpacket.getLength());
			// Act based on what the server returned
			if (in.equals("!0") || in.equals("!1") || in.equals("!2")) {
				System.out.println("The server encountered an error. Consider checking that your client is up-to-date.");
				return;
			}
			if (a.matcher(joinedargs).matches()) {
				if (in.equals("1")) {
					System.out.println("Ship was successfully placed.");
				} else {
					System.out.println("Failed to place ship.");
				}
			} else if (b.matcher(joinedargs).matches()) {
				if (in.equals("1")) {
					System.out.println("Ship was successfully moved.");
				} else {
					System.out.println("Failed to place move.");
				}
			} else if (c.matcher(joinedargs).matches()) {
				if (in.equals("1")) {
					System.out.println("The ship hit either another ship or a mine.");
				} else {
					System.out.println("The ship missed or doesn't exist.");
				}
			} else if (d.matcher(joinedargs).matches()) {
				if (in.equals("1")) {
					System.out.println("Mine was successfully placed.");
				} else {
					System.out.println("Failed to place mine.");
				}
			} else if (e.matcher(joinedargs).matches()) {
				if (in.equals("1")) {
					System.out.println("Mine was successfully placed.");
				} else {
					System.out.println("Failed to place mine.");
				}
			} else if (f.matcher(joinedargs).matches()) {
				if (in.equals("1")) {
					System.out.println("Mine was successfully removed.");
				} else {
					System.out.println("Failed to place removed.");
				}
			} else if (g.matcher(joinedargs).matches()) {
				System.out.println(String.format("Client version : %s, server version : %s", version, in));
			} else if (h.matcher(joinedargs).matches()) {
				System.out.println(in);
			} else if (i.matcher(joinedargs).matches()) {
				System.out.println(in);
			} else if (j.matcher(joinedargs).matches()) {
				if (in.equals("1")) {
					System.out.println("Successfully ticked.");
				} else {
					System.out.println("Failed to tick.");
				}
			} else {
				System.out.println(String.format("Unknown response from server : '%s'", in));
			}
		} catch (IOException ee) {
			ee.printStackTrace();
		}
	}
}
