import java.net.*;
import java.io.*;
import java.util.*;



public class Resolver {
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		boolean debug = false;

		if(args.length<2) {
			System.out.println("Usage: java SendDNS nameserver domain_name/ip_address");
			System.exit(1);
		}
		
		String OGnameserver = args[0];
		String OGhostname = args[1];
		String nameserver = OGnameserver;
		String hostname = OGhostname;
		String b [] = new String[3];
		while(true){
			b = SendDNS.iterative(nameserver, hostname);
			if (b[2].equals("true")){
				System.out.println(OGhostname + " resolves to " + b[1]);
				break;
			}
			else{
				nameserver = b[0];
			}
		}
		//System.out.printf("Sending DNS Query (%s) to server %s\n",hostname,nameserver);

		

	}
}
