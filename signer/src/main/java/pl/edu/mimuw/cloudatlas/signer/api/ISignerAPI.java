package pl.edu.mimuw.cloudatlas.signer.api;

import pl.edu.mimuw.cloudatlas.signing.outputs.SignedInstallation;
import pl.edu.mimuw.cloudatlas.signing.outputs.SignedUninstallation;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ISignerAPI extends Remote {
	SignedInstallation installQuery(String name, String query) throws RemoteException;

	SignedUninstallation uninstallQuery(String name) throws RemoteException;

	void ping() throws RemoteException;
}
