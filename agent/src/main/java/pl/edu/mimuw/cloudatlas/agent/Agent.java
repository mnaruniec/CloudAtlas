/**
 * Copyright (c) 2014, University of Warsaw
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted
 * provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this list of
 * conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of
 * conditions and the following disclaimer in the documentation and/or other materials provided
 * with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY
 * WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package pl.edu.mimuw.cloudatlas.agent;

import pl.edu.mimuw.cloudatlas.agent.api.IAgentAPI;
import pl.edu.mimuw.cloudatlas.model.PathName;
import pl.edu.mimuw.cloudatlas.model.TypePrimitive;
import pl.edu.mimuw.cloudatlas.model.Value;
import pl.edu.mimuw.cloudatlas.model.ValueContact;
import pl.edu.mimuw.cloudatlas.model.ValueSet;
import pl.edu.mimuw.cloudatlas.model.ValueString;
import pl.edu.mimuw.cloudatlas.model.ZMI;

import java.rmi.RemoteException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class Agent implements IAgentAPI {
	private ZMI root;
	private ReadWriteLock treeLock = new ReentrantReadWriteLock();
	private Map<String, ZMI> zmiIndex = new HashMap<>();
	private ValueSet fallbackContacts = new ValueSet(TypePrimitive.CONTACT);

	public Agent(ZMI root) {
		if (root == null) {
			throw new IllegalArgumentException();
		}
		this.root = root;
		buildIndex(this.root, PathName.ROOT);
	}

	private void buildIndex(ZMI zmi, PathName path) {
		for (ZMI son: zmi.getSons()) {
			buildIndex(son, path.levelDown(son.getName()));
		}
		zmiIndex.put(path.toString(), zmi);
	}

	@Override
	public Set<String> getStoredZones() throws RemoteException {
		treeLock.readLock().lock();
		try {
			return new HashSet<String>(zmiIndex.keySet());
		} finally {
			treeLock.readLock().unlock();
		}
	}

	@Override
	public Map<String, Value> getZoneAttributes(String zone) throws RemoteException, NoSuchZoneException {
		treeLock.readLock().lock();
		try {
			ZMI zmi = zmiIndex.get(zone);
			if (zmi == null) {
				throw new NoSuchZoneException("Zone '" + zone + "' not found.");
			}
			return zmi.getAttributes().toMap();
		} finally {
			treeLock.readLock().unlock();
		}
	}

	@Override
	public void upsertZoneAttributes(String zone, Map<String, Value> attributes) throws RemoteException {

	}

	@Override
	public void installQuery(String name, String query) throws RemoteException {

	}

	@Override
	public void uninstallQuery(String name) throws RemoteException {

	}

	@Override
	public void setFallbackContacts(Set<ValueContact> contacts) throws RemoteException {
		this.fallbackContacts = new ValueSet((Set)contacts, TypePrimitive.CONTACT);
	}

	@Override
	public Set<ValueContact> getFallbackContacts() throws RemoteException {
		return (Set)fallbackContacts;
	}
}
