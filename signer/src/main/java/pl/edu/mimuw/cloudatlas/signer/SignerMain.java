package pl.edu.mimuw.cloudatlas.signer;

import pl.edu.mimuw.cloudatlas.signer.api.ISignerAPI;
import pl.edu.mimuw.cloudatlas.signing.KeyReader;

import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

public class SignerMain {
	public static void main(String[] args) throws IOException, NoSuchPaddingException,
			NoSuchAlgorithmException, InvalidKeyException, InvalidKeySpecException {
		if (args.length < 2) {
			System.out.println("usage: signer <privateKeyFile> <usedIP>");
			System.exit(1);
		}
		String privateKeyFile = args[0];
		System.setProperty("java.rmi.server.hostname", args[1]);

		if (System.getSecurityManager() == null) {
			System.setSecurityManager(new SecurityManager());
		}

		Signer signer = new Signer(KeyReader.readPrivate(privateKeyFile));

		ISignerAPI stub =
				(ISignerAPI) UnicastRemoteObject.exportObject(signer, 0);
		Registry registry = LocateRegistry.getRegistry();
		registry.rebind("SignerAPI", stub);
		System.out.println("SignerAPI bound in RMI registry.");
	}
}
