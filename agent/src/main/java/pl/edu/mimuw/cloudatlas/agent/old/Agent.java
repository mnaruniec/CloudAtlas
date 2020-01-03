/**
 * Copyright (c) 2014, University of Warsaw
 * All rights reserved.
 * <p>
 * Redistribution and use in source and binary forms, with or without modification, are permitted
 * provided that the following conditions are met:
 * <p>
 * 1. Redistributions of source code must retain the above copyright notice, this list of
 * conditions and the following disclaimer.
 * <p>
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of
 * conditions and the following disclaimer in the documentation and/or other materials provided
 * with the distribution.
 * <p>
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY
 * WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package pl.edu.mimuw.cloudatlas.agent.old;

import pl.edu.mimuw.cloudatlas.agent.old.exceptions.IllegalAttributeException;
import pl.edu.mimuw.cloudatlas.agent.old.exceptions.NoSuchZoneException;
import pl.edu.mimuw.cloudatlas.agent.old.exceptions.QueryParsingException;
import pl.edu.mimuw.cloudatlas.interpreter.Interpreter;
import pl.edu.mimuw.cloudatlas.interpreter.QueryResult;
import pl.edu.mimuw.cloudatlas.interpreter.query.Absyn.AliasedSelItemC;
import pl.edu.mimuw.cloudatlas.interpreter.query.Absyn.Program;
import pl.edu.mimuw.cloudatlas.interpreter.query.Absyn.ProgramC;
import pl.edu.mimuw.cloudatlas.interpreter.query.Absyn.SelItem;
import pl.edu.mimuw.cloudatlas.interpreter.query.Absyn.SelItemC;
import pl.edu.mimuw.cloudatlas.interpreter.query.Absyn.Statement;
import pl.edu.mimuw.cloudatlas.interpreter.query.Absyn.StatementC;
import pl.edu.mimuw.cloudatlas.interpreter.query.Yylex;
import pl.edu.mimuw.cloudatlas.interpreter.query.parser;
import pl.edu.mimuw.cloudatlas.model.Attribute;
import pl.edu.mimuw.cloudatlas.model.AttributesMap;
import pl.edu.mimuw.cloudatlas.model.PathName;
import pl.edu.mimuw.cloudatlas.model.TypePrimitive;
import pl.edu.mimuw.cloudatlas.model.Value;
import pl.edu.mimuw.cloudatlas.model.ValueContact;
import pl.edu.mimuw.cloudatlas.model.ValueQuery;
import pl.edu.mimuw.cloudatlas.model.ValueSet;
import pl.edu.mimuw.cloudatlas.model.ZMI;

import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class Agent {
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
		for (ZMI son : zmi.getSons()) {
			buildIndex(son, path.levelDown(son.getName()));
		}
		zmiIndex.put(path.toString(), zmi);
	}

	public Set<String> getStoredZones() {
		treeLock.readLock().lock();
		try {
			return new HashSet<String>(zmiIndex.keySet());
		} finally {
			treeLock.readLock().unlock();
		}
	}

	public Map<String, Value> getZoneAttributes(String zone, boolean excludeQueries) throws NoSuchZoneException {
		treeLock.readLock().lock();
		try {
			ZMI zmi = zmiIndex.get(zone);
			if (zmi == null) {
				throw new NoSuchZoneException("Zone '" + zone + "' not found.");
			}
			return zmi.getAttributes().toMap(excludeQueries);
		} finally {
			treeLock.readLock().unlock();
		}
	}

	public void upsertZoneAttributes(String zone, Map<String, Value> attributes) throws NoSuchZoneException, IllegalAttributeException {
		if (attributes.containsKey(ZMI.NAME_ATTR.getName())) {
			throw new IllegalAttributeException("Name attribute cannot be changed.");
		}

		AttributesMap attributesMap = new AttributesMap();
		for (Map.Entry<String, Value> entry : attributes.entrySet()) {
			if (entry.getValue().isInternal()) {
				throw new IllegalAttributeException(
						"Value of type " + entry.getValue().getType() + " for key '" + entry.getKey() + "' is not settable."
				);
			}
			if (entry.getKey().startsWith("&")) {
				throw new IllegalAttributeException(entry.getKey() + " is reserved and cannot be set.");
			}
			// internal Attribute constructor validates key
			attributesMap.add(entry.getKey(), entry.getValue());
		}

		treeLock.writeLock().lock();
		try {
			ZMI zmi = zmiIndex.get(zone);
			if (zmi == null) {
				throw new NoSuchZoneException("Zone '" + zone + "' not found.");
			}
			if (!zmi.getSons().isEmpty()) {
				throw new IllegalArgumentException("Zone '" + zone + "' is not a leaf zone.");
			}
			zmi.getAttributes().addOrChange(attributesMap);

			refreshAll(root);
		} finally {
			treeLock.writeLock().unlock();
		}
	}

	public void installQuery(String name, String query) throws IllegalAttributeException, QueryParsingException {
		Attribute attribute = new Attribute("&" + name);

		Program program;
		try {
			Yylex lex = new Yylex(new ByteArrayInputStream(query.getBytes()));
			program = (new parser(lex)).pProgram();
		} catch (Exception e) {
			throw new QueryParsingException(e.getMessage());
		}
		validateIdents(program);
		ValueQuery value = new ValueQuery(program);

		treeLock.writeLock().lock();
		try {
			installQuery(root, attribute, value);
			refreshAll(root);
		} finally {
			treeLock.writeLock().unlock();
		}
	}

	private void validateIdents(Program p) throws IllegalAttributeException {
		List<Attribute> attributes = new LinkedList<>();
		p.accept(new IdentGetter(), attributes);
		for (Attribute attribute : attributes) {
			if (Attribute.isQuery(attribute)) {
				throw new IllegalAttributeException(attribute.getName() + " is reserved and cannot be used as an alias.");
			}
			if (attribute.equals(ZMI.NAME_ATTR)) {
				throw new IllegalAttributeException("Name attribute cannot be overwritten.");
			}
		}
	}

	private static class IdentGetter implements
			Program.Visitor<Void, List<Attribute>>,
			Statement.Visitor<Void, List<Attribute>>,
			SelItem.Visitor<Void, List<Attribute>> {
		@Override
		public Void visit(ProgramC p, List<Attribute> arg) {
			for (Statement stmt : p.liststatement_) {
				stmt.accept(this, arg);
			}
			return null;
		}

		@Override
		public Void visit(StatementC p, List<Attribute> arg) {
			for (SelItem item : p.listselitem_) {
				item.accept(this, arg);
			}
			return null;
		}

		@Override
		public Void visit(SelItemC p, List<Attribute> arg) {
			throw new IllegalArgumentException("All items in top-level SELECT must be aliased.");
		}

		@Override
		public Void visit(AliasedSelItemC p, List<Attribute> arg) {
			arg.add(new Attribute(p.qident_));
			return null;
		}
	}

	private void installQuery(ZMI zmi, Attribute attribute, ValueQuery query) {
		List<ZMI> sons = zmi.getSons();
		if (!sons.isEmpty()) {
			for (ZMI son : sons) {
				installQuery(son, attribute, query);
			}
			zmi.getAttributes().addOrChange(attribute, query);
		}
	}

	private void refreshAll(ZMI zmi) {
		List<ZMI> sons = zmi.getSons();
		if (!sons.isEmpty()) {
			for (ZMI son : sons) {
				refreshAll(son);
			}
			Interpreter interpreter = new Interpreter(zmi);

			Map<Attribute, Program> queries = new HashMap<>();
			for (Map.Entry<Attribute, Value> entry : zmi.getAttributes()) {
				if (Attribute.isQuery(entry.getKey()) && entry.getValue() instanceof ValueQuery) {
					queries.put(entry.getKey(), ((ValueQuery) entry.getValue()).getValue());
				}
			}

			for (Map.Entry<Attribute, Program> entry : queries.entrySet()) {
				try {
					List<QueryResult> results = interpreter.interpretProgram(
							entry.getValue()
					);
					for (QueryResult result : results) {
						zmi.getAttributes().addOrChange(result.getName(), result.getValue());
					}
				} catch (Exception e) {
					System.out.println(
							"Exception when evaluating query "
									+ entry.getKey().getName()
									+ " in node " + zmi.getName() + ": "
									+ e.getMessage()
					);
				}
			}
		}
	}

	public void uninstallQuery(String name) {
		treeLock.writeLock().lock();
		try {
			uninstallQuery(root, "&" + name);
		} finally {
			treeLock.writeLock().unlock();
		}
	}

	private void uninstallQuery(ZMI zmi, String name) {
		Value query = zmi.getAttributes().getOrNull(name);
		if (query != null) {
			zmi.getAttributes().remove(name);
			for (ZMI son : zmi.getSons()) {
				uninstallQuery(son, name);
			}
		}
	}

	public void setFallbackContacts(Set<ValueContact> contacts) {
		this.fallbackContacts = new ValueSet((Set) contacts, TypePrimitive.CONTACT);
	}

	public Set<ValueContact> getFallbackContacts() {
		return (Set) fallbackContacts;
	}
}
