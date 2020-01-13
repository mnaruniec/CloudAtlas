package pl.edu.mimuw.cloudatlas.interpreter;

import pl.edu.mimuw.cloudatlas.model.Attribute;
import pl.edu.mimuw.cloudatlas.model.ZMI;

public class ProtectedAttributesHelper {
	public static final String[] PROTECTED_ATTRIBUTES = {
			ZMI.NAME_ATTR.getName(),
			ZMI.LEVEL_ATTR.getName(),
			ZMI.OWNER_ATTR.getName(),
			ZMI.CARDINALITY_ATTR.getName(),
			ZMI.TIMESTAMP_ATTR.getName(),
			ZMI.CONTACTS_ATTR.getName()
	};

	private ProtectedAttributesHelper() {}

	public static boolean isProtected(Attribute attr) {
		for (String prot: PROTECTED_ATTRIBUTES) {
			if (attr.getName().equals(prot)) {
				return true;
			}
		}
		return false;
	}
}
