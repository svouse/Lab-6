import java.net.*;
import java.io.*;
import java.util.*;



public class Resolver {

	public static void main(String[] args) {
		String [] servers = {
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
		// TODO Auto-generated method stub
		boolean debug = false;
		String flag = "";
		String recursive ="";
		String OGnameserver ="";
		String OGhostname = "";
		/**if(args.length<2) {
			System.out.println("Usage: java SendDNS nameserver domain_name/ip_address");
			System.exit(1);
		}*/
		if(args.length==1){
			char cA = args[0].charAt(0);
			char cB = args[0].charAt(1);
			if((Character.isDigit(cA) && Character.isDigit(cB))){
				System.out.println("IP");
				
			}
			else{
				OGhostname = args[0];
				OGnameserver = servers[1];
				//flag = "-v";
			}
			//System.out.println(OGhostname + " " + OGnameserver);
		}
		if(args.length==3){
			recursive = args[0]; //-n
			OGnameserver = args[1];
			OGhostname = args[2];
		}
		if(args.length==4){
			flag = args[0]; //-v
			recursive = args[1];
			OGnameserver = args[2];
			OGhostname = args[3];
		}
		if(recursive.equals("-r")){
			System.out.println("Recursive");
		}
		else if(recursive.equals("-n") || args.length==1){
			ArrayList<String> alias = new ArrayList<String>();
			
			String nameserver = OGnameserver;
			String hostname = OGhostname;
			String b [] = new String[3];
			while(true){
				b = SendDNS.iterative(nameserver, hostname, flag);
				char c1 = b[2].charAt(0);
				char c2 = b[2].charAt(1);
				if(b[2]=="SOA"){
					System.out.println("Host " + OGhostname + " not found");
					break;
				}
				else if((Character.isDigit(c1) && Character.isDigit(c2))){
					if(!alias.isEmpty()){
						for(int i = 0; i<alias.size();i++){
							System.out.println(alias.get(i));
						}
						System.out.println(hostname + " resolves to " + b[2]);
						System.out.println(hostname + " resolves to " + b[0]);
					}
					System.out.println(OGhostname + " resolves to " + b[2]);
					System.out.println(OGhostname + " resolves to " + b[0]);
					break;
				}
				else if (b[2].equals("true")){
					if(!alias.isEmpty()){
						for(int i = 0; i<alias.size();i++){
							System.out.println(alias.get(i));
						}
						System.out.println(hostname + " resolves to " + b[0]);
						break;
					}
					System.out.println(OGhostname + " resolves to " + b[0]);
					break;
				}
				else if(b[2].equals("CNAME")){
					alias.add(hostname + " is an alias of " + b[1]);
					nameserver = OGnameserver;
					hostname = b[1];
				}
				else{
					nameserver = b[0];
					hostname = b[1];
				}

			}
			//System.out.printf("Sending DNS Query (%s) to server %s\n",hostname,nameserver);

		}

	}
}
