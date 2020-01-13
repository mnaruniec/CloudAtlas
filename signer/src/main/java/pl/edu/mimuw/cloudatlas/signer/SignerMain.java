package pl.edu.mimuw.cloudatlas.signer;

import java.rmi.RemoteException;

public class SignerMain {
	public static void main(String[] args) throws RemoteException {
		System.out.println("Hello signer!");
//
//		IAgentAPI api;
//		try {
//			Registry registry = LocateRegistry.getRegistry();
//			api = (IAgentAPI) registry.lookup("AgentAPI");
//		} catch (Exception e) {
//			System.out.println("Could not retrieve AgentAPI object: " + e.getMessage());
//			System.exit(1);
//			return;
//		}
//
//		System.out.println("nic: " + api.getFallbackContacts());
//
//		Set<ValueContact> contacts = new HashSet<>();
//		try {
//			contacts.add(new ValueContact(new PathName("/dupa/1"), InetAddress.getByName("127.0.0.1")));
//			contacts.add(new ValueContact(new PathName("/dupa/2"), InetAddress.getByName("192.168.0.1")));
//		} catch (UnknownHostException e) {
//			e.printStackTrace();
//		}
//
//		api.setFallbackContacts(contacts);
//
//		System.out.println("cos: " + api.getFallbackContacts());
	}
}
