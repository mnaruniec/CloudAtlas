package pl.edu.mimuw.cloudatlas.signing.outputs.payloads;

import pl.edu.mimuw.cloudatlas.interpreter.query.Absyn.Program;
import pl.edu.mimuw.cloudatlas.model.Attribute;

public final class InstallationPayload extends SignedObjectPayload {
	private Program query;

	private InstallationPayload() {}

	protected InstallationPayload(long timestamp, Attribute name, Program query) {
		super(timestamp, name);
		this.query = query;
	}

	public Program getQuery() {
		return query;
	}
}
