package pl.edu.mimuw.client.services;

import org.springframework.stereotype.Service;
import pl.edu.mimuw.cloudatlas.signer.api.ISignerAPI;
import pl.edu.mimuw.cloudatlas.signing.outputs.SignedInstallation;
import pl.edu.mimuw.cloudatlas.signing.outputs.SignedUninstallation;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

@Service
public class SignerService implements ISignerAPI {
	private ISignerAPI signerAPI;

	public SignerService() throws RemoteException, NotBoundException {
		// TODO - config hostname
		Registry registry = LocateRegistry.getRegistry();
		signerAPI = (ISignerAPI) registry.lookup("SignerAPI");
	}

	@Override
	public SignedInstallation installQuery(String name, String query) throws RemoteException {
		return this.signerAPI.installQuery(name, query);
	}

	@Override
	public SignedUninstallation uninstallQuery(String name) throws RemoteException {
		return this.signerAPI.uninstallQuery(name);
	}
}
