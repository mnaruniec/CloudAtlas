package pl.edu.mimuw.cloudatlas.signer;

import pl.edu.mimuw.cloudatlas.interpreter.ProtectedAttributesHelper;
import pl.edu.mimuw.cloudatlas.interpreter.query.Absyn.Program;
import pl.edu.mimuw.cloudatlas.interpreter.query.Yylex;
import pl.edu.mimuw.cloudatlas.interpreter.query.parser;
import pl.edu.mimuw.cloudatlas.model.Attribute;
import pl.edu.mimuw.cloudatlas.signer.api.ISignerAPI;
import pl.edu.mimuw.cloudatlas.signing.PayloadSigner;
import pl.edu.mimuw.cloudatlas.signing.outputs.SignedInstallation;
import pl.edu.mimuw.cloudatlas.signing.outputs.SignedUninstallation;
import pl.edu.mimuw.cloudatlas.signing.outputs.payloads.InstallationPayload;
import pl.edu.mimuw.cloudatlas.signing.outputs.payloads.UninstallationPayload;

import javax.crypto.NoSuchPaddingException;
import java.io.ByteArrayInputStream;
import java.rmi.RemoteException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class Signer implements ISignerAPI {
	private final Map<Attribute, List<Attribute>> queryMap = new HashMap<>();
	private final Map<Attribute, Attribute> attributeMap = new HashMap<>();
	private final Object lock = queryMap;

	private PayloadSigner payloadSigner;

	private IdentGetter identGetter = new IdentGetter();

	public Signer(PrivateKey privateKey)
			throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException {
		this.payloadSigner = new PayloadSigner(privateKey);
	}

	@Override
	public SignedInstallation installQuery(String name, String query) {
		try {
			Attribute attr = new Attribute("&" + name);

			Yylex lex = new Yylex(new ByteArrayInputStream(query.getBytes()));
			Program program = (new parser(lex)).pProgram();

			List<Attribute> fields = new LinkedList<>();
			program.accept(identGetter, fields);

			for (Attribute field: fields) {
				if (Attribute.isQuery(field)) {
					throw new IllegalArgumentException(
							"Query attribute '" + field.getName() + "' is not a proper column name.");
				}
				if (ProtectedAttributesHelper.isProtected(field)) {
					throw new IllegalArgumentException(
							"Attribute '" + field.getName() + "' is protected.");
				}
			}

			synchronized (lock) {
				for (Attribute field: fields) {
					Attribute queryName = attributeMap.get(field);
					if (queryName != null && !queryName.equals(attr)) {
						throw new IllegalArgumentException("Attribute '" + field.getName()
								+ "' is already computed by query '" + queryName.getName() + "'.");
					}
				}

				SignedInstallation signed = signInstallation(attr, program);

				List<Attribute> oldFields = queryMap.get(attr);
				if (oldFields != null) {
					for (Attribute oldField: oldFields) {
						attributeMap.remove(oldField);
					}
				}

				for (Attribute field: fields) {
					attributeMap.put(field, attr);
				}

				queryMap.put(attr, fields);

				return signed;
			}
		} catch (Exception e) {
			System.out.println("Caught exception when installing query.");
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	private SignedInstallation signInstallation(Attribute name, Program query) {
		InstallationPayload payload = new InstallationPayload(new Date().getTime(), name, query);
		byte[] signature = payloadSigner.sign(payload);
		return new SignedInstallation(payload, signature);
	}

	@Override
	public SignedUninstallation uninstallQuery(String name) {
		try {
			synchronized (lock) {
				Attribute attr = new Attribute("&" + name);
				SignedUninstallation signed = signUninstallation(attr);

				List<Attribute> fields = queryMap.get(attr);
				if (fields != null) {
					queryMap.remove(attr);
					for (Attribute field: fields) {
						attributeMap.remove(field);
					}
				}

				return signed;
			}
		} catch (Exception e) {
			System.out.println("Caught exception when uninstalling query.");
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	private SignedUninstallation signUninstallation(Attribute name) {
		UninstallationPayload payload = new UninstallationPayload(new Date().getTime(), name);
		byte[] signature = payloadSigner.sign(payload);
		return new SignedUninstallation(payload, signature);
	}

	@Override
	public void ping() throws RemoteException {}
}
