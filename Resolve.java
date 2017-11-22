import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

/*
 * there's one bit in the header that says it will request a recursive query
 * you have to set that bit to make it a recursive query
 */

public class Resolve {

	public static final short DNSPORT = 53;
	public static int verbose = 0;
	public static int recursive = 0;
	public static String OGhostname = "";
	public static String OGserveraddress = "";
	public static ArrayList<String> roots = new ArrayList<String>();
	public static ArrayList<String> fin = new ArrayList<String>();
	
	public static String [] servers = {
			"198.41.0.4",
			"192.228.79.201",
			"192.33.4.12",
			"128.8.10.90",
			"192.203.230.10",
			"192.5.5.241",
			"192.112.36.4",
			"128.63.2.53",
			"192.36.148.17",
			"192.58.128.30",
			"193.0.14.129",
			"199.7.83.42",
	"202.12.27.33"};

	public static void usage() {
		System.out.println("Usage: java Sender <> || <-v> <hostname>");
		System.out.println("Usage: java Sender (<-v> || <-v -n>|| <-v -r>) <server-ip-address> <hostname>");
		System.exit(1);
	}

	public static byte[] parseInetAddress(String inetAddr) throws NumberFormatException {
		String[] sp = inetAddr.split("\\.");
		byte[] addr = new byte[4];
		if(sp.length!=4){
			throw new NumberFormatException();
		}
		for(int i=0; i<4; i++){
			int x = Integer.parseInt(sp[i]);
			if(x<0 || x>255)
				throw new NumberFormatException();
			addr[i] = (byte) x;
		}
		return addr;
	}

	public static void main(String[] args) throws IOException{
		boolean debug = false;
		for(int i = 0; i<servers.length; i++){
			//System.out.println(servers[i]);
			roots.add(servers[i]);
		}
		String nameserver = "";
		String hostname = ""; 	
		if(args.length < 1 || args.length > 4) {
			usage();
		}
		else if(args.length == 1) {
			hostname = args[0];
			nameserver = roots.get(0);
			
		}
		else if (args.length == 2) {
			if (args[0].equals("-v")) {
				verbose = 1;
			}
			nameserver = roots.get(0);
			hostname = args[1];
		}
		else if (args.length == 3) {
			if (args[0].equals("-r")) {
				recursive = 1;
				nameserver = args[1];
				hostname = args[2];
			}
			else{
				//specified -n
				nameserver = args[1];
				hostname = args[2];
			}
			
		}
		else if (args.length == 4) {
			nameserver = args[2];
			hostname = args[3];
			if (args[0].equals("-v")) {
				verbose = 1;
			}
			if (args[1].equals("-r")) {
				recursive = 1;
			}
			else{
				recursive = 0;
			}
		}
		
		//choose between recursion or not
		if (recursive != 1){
			OGserveraddress = nameserver;
			OGhostname = hostname;
			iterative(nameserver, hostname, roots);
		}
		else {
			OGserveraddress = nameserver;
			OGhostname = hostname;
			byte[] serveraddr = null;
			try {
				serveraddr = parseInetAddress(nameserver);
			}
			catch(NumberFormatException e){
				System.out.printf("Invalid nameserver %s\n", nameserver);
				System.exit(1);
			}
			if (verbose==1) System.out.printf("Sending DNS Query (%s) to server %s\n", hostname, nameserver);
			DNS result = sendRequest(serveraddr, hostname);
			//ArrayList<String> fin = new ArrayList<String>();
			if (verbose==1) System.out.println(result);
			
			for (DNS.ResourceRecord rec: result.rrlist) {
				if (rec.type == 5 && rec.value != null) {
					fin.add(hostname + " is and alias for " + rec.value);
					hostname = rec.value;
				}
				else if ((rec.type == 1) && (rec.value != null) && rec.name.equals(hostname)){
					fin.add(OGhostname + " resolved to " + rec.value);
					
				}
			}
			//print all statements
			for(String s:fin){
				System.out.println(s);
			}
		}
	}

	public static String iterative(String nameserver, String hostname, ArrayList<String> rootservers) throws IOException{
		byte[] serveraddr = null;
		try {
			serveraddr = parseInetAddress(nameserver);
		}
		catch(NumberFormatException e){
			System.out.printf("Invalid nameserver %s\n",nameserver);
			System.exit(1);
		}
		if (verbose == 1)
			System.out.printf("Sending DNS Query (%s) to server %s\n", hostname, nameserver);

		String originalHost = hostname;
		boolean found = false;
		DNS result = sendRequest(serveraddr, hostname);
		//hold records of name servers and aliases, also NS names which are needed when no A's come through
		ArrayList<DNS.ResourceRecord> NS = new ArrayList<DNS.ResourceRecord>();	
		ArrayList<DNS.ResourceRecord> aServer = new ArrayList<DNS.ResourceRecord>();
		ArrayList<String> nsNames = new ArrayList<String>();
		ArrayList<String> cNames = new ArrayList<String>();
		String foundIP = "";
		while (true){
			if (result != null) {
				if (verbose == 1) {
					System.out.print("Response:\n");
					System.out.println(result);
				}
				NS.clear();
				aServer.clear();
				for (DNS.ResourceRecord rec: result.rrlist) {
					if (rec.type == 1 && rec.name.equals(hostname) && rec.value != null) {
						//System.out.println("here " + rec.value);
						foundIP = rec.value;
						found = true;
					}
					else if (rec.type == 1 && rec.value != null) {
						//adding record of name server NS
						NS.add(rec);
					}
					//hit an alias, CNAME
					else if (rec.type == 5 && rec.value != null) {
						fin.add(hostname + " is an alias for " + rec.value);
						//making list of A ... ...
						aServer.add(rec);
						//add alias
						cNames.add(rec.value);
						//change hostname to alias
						hostname = rec.value;
					}
					else if (rec.type == 2 && rec.value != null){
						nsNames.add(rec.value);
					}
					// reverse, has PTR
					else if (rec.type == 12 && rec.value != null){
						
						fin.add(hostname + " resolves to " + rec.value);
						for(String s: fin) System.out.println(s);
						return rec.value;
					}
					else if (rec.type == 6) {
						fin.add(hostname + " could not be resolved");
						for(String s: fin) System.out.println(s);
						System.exit(0);
					}
				}
				if (found) break;
				//no name servers or a server, prob just a PTR or null
				if (NS.isEmpty() && aServer.isEmpty()) {
					if (!nsNames.isEmpty()) {
						while (foundIP.isEmpty()) {
							String newNS = "";
							//checking all nameservers we have
							while (newNS.isEmpty()) { 
								if (nsNames.isEmpty()) { 
									break;
								}
								newNS = iterative(rootservers.get(0), nsNames.get(0), rootservers);
								if (newNS == null){
									break;
								}
								nsNames.remove(0);
							}
							if (!newNS.isEmpty()){
								foundIP = iterative(newNS, hostname, rootservers);
								if (foundIP == null){
									break;
								}
							}
							if (foundIP == null || newNS == null)
								break;
						}
						return foundIP;
					}
					else if (!cNames.isEmpty()) {
						System.out.println(originalHost + " could not be resolved.");
					}
					return "";
				}

				serveraddr = null;
				try {
					//use a new root
					if (!aServer.isEmpty()) {
						serveraddr = parseInetAddress(rootservers.get(0));
					}
					//try name servers
					else {
						serveraddr = parseInetAddress(NS.get(0).value);		
						NS.remove(0);
					}
				}
				catch(NumberFormatException e) {
					System.out.printf("Invalid nameserver %s\n",nameserver);
					System.exit(1);
				}

				result = sendRequest(serveraddr, hostname);
			}
			else {
				if (!NS.isEmpty()) {
					try {
						serveraddr = parseInetAddress(NS.get(0).value);
						NS.remove(0);
						result = sendRequest(serveraddr, hostname);
					}
					catch (NumberFormatException e) {
						System.out.printf("Invalid nameserver %s\n",nameserver);
						System.exit(1);
					}	
				} else {
					System.out.println("No more servers");
					return "";	
				}
			}
		} // end while loop

		if (found && (OGhostname.equals(originalHost))) {
			
			if (!cNames.isEmpty()){
				for(String s: fin) System.out.println(s);
			}
			System.out.println(originalHost + " resolved to " + foundIP);
		}
		return foundIP;
	}


	public static DNS sendRequest(byte[] serveraddr, String hostname) throws IOException {
		/* create a datagram socket to send messages */
		DatagramSocket dSocket = null;
		try {
			dSocket = new DatagramSocket();
		}
		catch(IOException e) {
			System.err.println(e);
			System.exit(1);
		}

		// using a constant name server address for now.
		InetAddress serverAddress = null;
		/* get inet address of name server */
		try {
			serverAddress = InetAddress.getByAddress(serveraddr);
		}
		catch (UnknownHostException e) {
			System.err.println(e);
			System.exit(1);
		}

		/* set up buffers */
		String line;
		byte[] inBuffer = new byte[1000];

		DatagramPacket outPacket = new DatagramPacket(new byte[1], 1, serverAddress, DNSPORT);
		DatagramPacket inPacket = new DatagramPacket(inBuffer, 1000);


		boolean recurse = false;
		if (recursive == 1) {
			recurse = true;
		}

		// construct the query message in a byte array
		byte[] query = new byte[1500];
		int querylen = DNS.constructQuery(query, 1500, hostname, recurse);

		// construct a DNS object from the byte array
		DNS dnsMessage = new DNS(query);
		if (verbose == 1)
			System.out.println("Sending query: "+ dnsMessage.rrlist.get(0).name + " to "+ serverAddress);

		// send the byte array as a datagram
		outPacket.setData(query,0,querylen);
		for (int i = 0; i < 200; i++)
			dSocket.send(outPacket);

		// await the response
		// TODO: Have it time out.
		boolean timedout = false;
		dSocket.setSoTimeout(100);
		try {
			dSocket.receive(inPacket);
		}
		catch (SocketTimeoutException e) {
			timedout = true;
		}

		if (timedout) {
			byte[] answerbuf = inPacket.getData();
			DNS response = new DNS(answerbuf);
			return response;
		}
		else {
			byte[] answerbuf = inPacket.getData();
			DNS response = new DNS(answerbuf);
			return response;
		}
	}
}
