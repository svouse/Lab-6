import java.net.*;
import java.io.*;
import java.util.*;



public class Resolve {

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
		int reverse = 0;
		/**if(args.length<2) {
			System.out.println("Usage: java SendDNS nameserver domain_name/ip_address");
			System.exit(1);
		}*/
		int r = 0;
		if(args.length==1){
			char cA = args[0].charAt(0);
			char cB = args[0].charAt(1);
			if((Character.isDigit(cA) && Character.isDigit(cB))){
				//System.out.println("IP");
				OGhostname = args[0];
				OGnameserver = servers[1];
				reverse = 1;
			}
			else{
				OGhostname = args[0];
				OGnameserver = servers[1];
				//flag = "-v";
			}
			//recursive = "-n";
			//System.out.println(OGhostname + " " + OGnameserver);
		}
		if(args.length==2){
			if(args[0].equals("-v")){
				flag = "-v";
			}
			char cA = args[1].charAt(0);
			char cB = args[1].charAt(1);
			if((Character.isDigit(cA) && Character.isDigit(cB))){
				//System.out.println("IP");
				OGhostname = args[1];
				OGnameserver = servers[1];
			}
			else{
				OGhostname = args[1];
				OGnameserver = servers[1];
				//flag = "-v";
			}
		}
		if(args.length==3){
			recursive = args[0]; //-n
			OGnameserver = args[1];
			OGhostname = args[2];
		}
		if(args.length==4){
			flag = args[0]; //-v
			recursive = args[1]; // - n or -r
			OGnameserver = args[2];
			OGhostname = args[3];
		}
		if(recursive.equals("-r")){
			r = 1;
		}
		else{
			r = 0;
		}

		ArrayList<String> alias = new ArrayList<String>();

		String nameserver = OGnameserver;
		String hostname = OGhostname;
		String[][] b;
		
		ArrayList <String> fin = new ArrayList<String>();
		
		ArrayList <String> nsHosts = new ArrayList<String>();
		int done = 0;
		while(true){
			//flag = "-v";
			//System.out.println("here " + hostname + " " + nameserver);
			if(done ==1) break;
			b = SendDNS.iterative(nameserver, hostname, flag, r, reverse);
			//System.out.println("Check " + b[0][2]);
			for(int i = 0; i<b.length;i++){
				//char c1 = b.get(i)[2].charAt(0);
				//char c2 = b.get(i)[2].charAt(1);
				if(b[i][0]==null || b[i][1]==null || b[i][2]==null) continue;
				if(b[i][2].equals("SOA")){
					fin.add("Host " + OGhostname + " not found");
					done = 1;
					//System.out.println("Host " + OGhostname + " not found");
					break;
				}
				else if(b[i][2].equals("CNAME")){
					fin.add(hostname + " is an alias of " + b[i][1]);
					hostname = b[i][1];
					nameserver = b[i][0];
					//nameserver = OGnameserver;
					if(r==0) {
						nameserver = OGnameserver;
						break;
					}
				}
				else if(b[i][2].equals("true")){
					if(b[i][1].equals(OGhostname))
					fin.add(hostname + " resolves to " + b[i][0]);
					done = 1;
					
					//System.out.println("here");
					
				}
				else if(b[i][2].equals("false")){
					if(done ==1) break;
					nameserver = b[i][0];
					//hostname = b[i][1];
					break;
				}
				else if(b[i][0].equals("NS")){
					if(done==1) break;
					nameserver = OGnameserver;
					hostname = b[0][1];
				}
				else if(b[i][2].equals("PTR")){
					hostname = b[i][1];
				}
			}
			/**
			char c1 = b[2].charAt(0);
			char c2 = b[2].charAt(1);
			if(b[2].equals("SOA")){
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

		} */
		//System.out.printf("Sending DNS Query (%s) to server %s\n",hostname,nameserver);
		}
		for(int i = 0; i<fin.size();i++){
			System.out.println(fin.get(i));
		}
	}

}

