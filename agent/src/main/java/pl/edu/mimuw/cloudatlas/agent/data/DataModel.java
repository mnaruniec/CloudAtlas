package pl.edu.mimuw.cloudatlas.agent.data;

import pl.edu.mimuw.cloudatlas.model.ValueContact;
import pl.edu.mimuw.cloudatlas.model.ZMI;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

class DataModel {
	ZMI root;

	Map<String, ZMI> zmiIndex;

	Set<ValueContact> fallbackContacts = new HashSet<>();
}
