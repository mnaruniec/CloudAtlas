package pl.edu.mimuw.cloudatlas.agent.data;

import pl.edu.mimuw.cloudatlas.interpreter.cli.InterpreterUtils;
import pl.edu.mimuw.cloudatlas.model.ValueContact;
import pl.edu.mimuw.cloudatlas.model.ZMI;

import java.net.UnknownHostException;
import java.text.ParseException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

class DataModel {
	ZMI root;

	// TODO - remove
	{
		try {
			root = InterpreterUtils.createLabTestHierarchy();
		} catch (ParseException e) {
			e.printStackTrace();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
	}

	Map<String, ZMI> zmiIndex;

	Set<ValueContact> fallbackContacts = new HashSet<>();
}
