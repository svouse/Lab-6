
import java.net.*;
import java.io.*;
import java.util.*;

public class SendDNS {

	static final short DNSPORT = 53;
	public static ArrayList<String> savings = new ArrayList<String>();
	public static String sOG;
	//public static String hostOG;
	public static void usage() {
		System.out.println("Usage: java SendDNS nameserver domain_name/ip_address");
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

	public static String[][] iterative(String nameserver, String hostname, String f, int recursive, int reverse){
		String back[] = new String[3];
		//ArrayList <ArrayList<String[]>> ret = new ArrayList<ArrayList<String[]>>();
		
		//ret.add(resolve);
		
		//System.out.printf("Sending DNS Query (%s) to server %s\n\n",hostname,nameserver);
		byte[] serveraddr = null;
		try {
			serveraddr = parseInetAddress(nameserver);
		}
		catch(NumberFormatException e){
			System.out.printf("Invalid nameserver %s\n",nameserver);
			System.exit(1);
		}

		DNS result = null;
		try {
			result = sendRequest(serveraddr, hostname, recursive, reverse);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		int saveNum = 0;
		if(f.equals("-v")){
			System.out.println("Resolving " +  hostname + " using server " + nameserver);
			System.out.println("Response:\n");
			System.out.println(result);
		}
		String s = result.toString();
		String[] parts = s.split("\n");
		//back[0] = server
		//back[1] = hostname
		//back[2] = done or not, or if alias
		String[][] resolve = new String[parts.length][3];
		String[][] ns = new String[parts.length][3];
		resolve[parts.length-1][0]="emptyR";
		ns[parts.length-1][0]="emptyNS";
		int useR = 0;
		int useNS = 0;
		String save = "";
		int pointer = 0;
		int point = 0;
		for(int i = 0; i<parts.length; i++){
			String[] p = parts[i].split(" ");
			/**
			try{
				System.out.println("H" + resolve[1][2]);
			}
			catch(Exception e){} 
			*/
			if(p[0].equals("A") && p[1].equals(hostname) && p[2].equals("null")){
				resolve[i][0] = null;
				resolve[i][1] = null;
				resolve[i][2] = null;
			}
			else if(p[0].equals("A") && p[1].equals(hostname) && !p[2].equals("null")){
				back[0]= p[2];
				back[1]= p[1];
				back[2]= "true";
				useR = 1;
				
				/**
				if(i+1<parts.length){
					String[] check = parts[i+1].split(" ");
					if(check[0].equals("A") && check[1].equals(hostname) && !check[2].equals("null")){
						back[2]= check[2];
					}
				}
				*/
				
				resolve[i][0] = back[0];
				resolve[i][1] = back[1];
				resolve[i][2] = back[2];
				//System.out.println(back[2]);
				
			}
			else if(p[0].equals("A") ){
				if(point == 0){
					savings.clear();
					point = 1;
				}
				//&& (Character.isDigit(c1) && Character.isDigit(c2))
				if(pointer ==1){
					back[0] = nameserver; //new server we want to try
					back[1] = p[1];
					back[2] = "false";
					hostname = sOG;
					nameserver = p[2];
				}
				else{
					back[0] = p[2]; //new server we want to try
					back[1] = p[1];
					back[2] = "false";
				}
				savings.add(p[1]);
				resolve[i][0] = back[0];
				resolve[i][1] = back[1];
				resolve[i][2] = back[2];
				useR = 1;
				//hostname = p[1];
				
			}
			else if(p[0].equals("NS")){
				//String[] n = new String[2];
				back[0] = nameserver;
				back[1] = p[1]; //hostname
				back[2] = "NS";
				ns[i][0] = back[0];
				ns[i][1] = back[1];
				ns[i][2] = back[2];
				useNS = 1;
			}
			else if(p[0].equals("CNAME")){
				hostname = p[2];
				//nameserver = p[0];
				back[0] = nameserver;
				back[1] = p[2];
				back[2] = "CNAME";
				sOG = p[2];
				resolve[i][0] = back[0];
				resolve[i][1] = back[1];
				resolve[i][2] = back[2];
			}
			else if(p[0].equals("PTR")){
				pointer = 1;
				if(p[1].equals(hostname) && p[2].equals("null")){
					resolve[i][0] = null;
					resolve[i][1] = null;
					resolve[i][2] = null;
					//hostname = save;
					continue;
				}
				back[0]= p[2]; //server address
				back[1]= p[1]; //hostname we want to return that it resolves to
				back[2]= "PTR";
				sOG = p[1];
				hostname = p[1];
				resolve[i][0] = back[0];
				resolve[i][1] = back[1];
				resolve[i][2] = back[2];
			}
			else if(p[0].equals("SOA")){
				back[0] = nameserver;
				back[1] = p[2];
				back[2] = "SOA";
				resolve[i][0] = back[0];
				resolve[i][1] = back[1];
				resolve[i][2] = back[2];
				useR = 1;
			}
			
			/**
			else if (saveNum == 0){
				save[0] = nameserver;
				save[1] = p[1]; //hostname
				save[2] = "false";
				saveNum = 1;
				resolve.add(save);
			}
			*/

		}
		/**
		for(int i = 0; i<resolve.length;i++){
			System.out.println("Here " +resolve[i][0]);
			System.out.println("Here " +resolve[i][1]);
			System.out.println("Here " +resolve[i][2]);
		}*/
		//System.out.println("list " + savings);
		if(useR == 1){
			//System.out.println("Should end " + resolve.get(0)[2]);
			return resolve;
		}
		else if(useNS == 1 && useR!=1){
			String[][] l = new String[savings.size()][3];
			for(int i = 0; i<savings.size();i++){
				l[i][0] = "NS";
				l[i][1] = savings.get(i);
				l[i][2] = savings.get(i);
			}
			return l;
		}
		return resolve;
	}

	//byte b [] = new byte[1000];
	//b = result.toString();
	//[] b = new String[Integer.parseInt(result.toString())];
	//System.out.println("Bytes " + b);


	public static DNS sendRequest(byte[] serveraddr, String hostname, int recursive, int reverse) throws IOException {
		/* create a datagram socket to send messages */
		DatagramSocket dSocket = null;
		try {
			dSocket = new DatagramSocket();
			dSocket.setSoTimeout(100);
		}
		catch(IOException e){
			System.err.println(e);
			System.exit(1);
		}

		// using a constant name server address for now.

		InetAddress serverAddress = null;
		/* get inet address of name server */
		boolean timedout = false;
		try {
			serverAddress = InetAddress.getByAddress(serveraddr);
		}
		catch(UnknownHostException e){
			System.err.println(e);
			System.exit(1);
		}

		/* set up buffers */
		String line;
		byte[] inBuffer = new byte[1000];

		DatagramPacket outPacket = new DatagramPacket(new byte[1],1,serverAddress,DNSPORT);
		DatagramPacket inPacket = new DatagramPacket(inBuffer,1000);

		// construct the query message in a byte array
		byte[] query = new byte[1500];
		int querylen=DNS.constructQuery(query,1500,hostname, recursive, reverse);

		// construct a DNS object from the byte array
		DNS dnsMessage = new DNS(query);
		//System.out.println("Sending query:"+dnsMessage+" to "+serverAddress);

		// send the byte array as a datagram
		outPacket.setData(query,0,querylen);
		for(int i=0; i<200; i++)
			dSocket.send(outPacket);

		// await the response 
		try {
			dSocket.receive(inPacket);
		} catch(SocketTimeoutException e) {
			timedout = true;
		}
		if (timedout) { 
			sendRequest(serveraddr, hostname, recursive, reverse);
		}

		byte[] answerbuf = inPacket.getData();

		DNS response = new DNS(answerbuf);
		return response;
	}
}
