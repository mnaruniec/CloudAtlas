package pl.edu.mimuw.cloudatlas.agent.data;

import pl.edu.mimuw.cloudatlas.interpreter.cli.InterpreterUtils;
import pl.edu.mimuw.cloudatlas.model.PathName;
import pl.edu.mimuw.cloudatlas.model.ValueContact;
import pl.edu.mimuw.cloudatlas.model.ZMI;

import java.net.UnknownHostException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

class DataModel {
	ZMI root;

	public DataModel() {
		// TODO - remove
		{
			try {
				root = InterpreterUtils.createLabTestHierarchy();
				if (root != null) {
					generateIndex(root, new PathName(""));
				}
			} catch (ParseException | UnknownHostException e) {
				e.printStackTrace();
			}
		}
	}

	Map<String, ZMI> zmiIndex = new HashMap<>();

	Set<ValueContact> fallbackContacts = new HashSet<>();

	// TODO - remove?
	private void generateIndex(ZMI zmi, PathName pathName) {
		zmiIndex.put(pathName.getName(), zmi);
		for (ZMI son: zmi.getSons()) {
			generateIndex(son, pathName.levelDown(son.getName()));
		}
	}
}
