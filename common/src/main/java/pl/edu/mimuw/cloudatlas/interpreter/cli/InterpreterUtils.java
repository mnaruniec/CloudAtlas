package pl.edu.mimuw.cloudatlas.interpreter.cli;

import pl.edu.mimuw.cloudatlas.interpreter.Interpreter;
import pl.edu.mimuw.cloudatlas.interpreter.InterpreterException;
import pl.edu.mimuw.cloudatlas.interpreter.QueryResult;
import pl.edu.mimuw.cloudatlas.interpreter.query.Absyn.Program;
import pl.edu.mimuw.cloudatlas.interpreter.query.Yylex;
import pl.edu.mimuw.cloudatlas.interpreter.query.parser;
import pl.edu.mimuw.cloudatlas.model.PathName;
import pl.edu.mimuw.cloudatlas.model.TypePrimitive;
import pl.edu.mimuw.cloudatlas.model.Value;
import pl.edu.mimuw.cloudatlas.model.ValueBoolean;
import pl.edu.mimuw.cloudatlas.model.ValueContact;
import pl.edu.mimuw.cloudatlas.model.ValueDouble;
import pl.edu.mimuw.cloudatlas.model.ValueDuration;
import pl.edu.mimuw.cloudatlas.model.ValueInt;
import pl.edu.mimuw.cloudatlas.model.ValueList;
import pl.edu.mimuw.cloudatlas.model.ValueSet;
import pl.edu.mimuw.cloudatlas.model.ValueString;
import pl.edu.mimuw.cloudatlas.model.ValueTime;
import pl.edu.mimuw.cloudatlas.model.ZMI;

import java.io.ByteArrayInputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

public class InterpreterUtils {
	private InterpreterUtils() {
	}

	private static PathName getPathName(ZMI zmi) {
		String name = ((ValueString) zmi.getAttributes().get(ZMI.NAME_ATTR)).getValue();
		return zmi.getFather() == null ? PathName.ROOT : getPathName(zmi.getFather()).levelDown(name);
	}

	public static void executeQueries(ZMI zmi, String query, boolean printPartialResults, String queryName)
			throws Exception {
		if (!zmi.getSons().isEmpty()) {
			for (ZMI son : zmi.getSons())
				executeQueries(son, query, printPartialResults, queryName);
			Interpreter interpreter = new Interpreter(zmi);
			Yylex lex = new Yylex(new ByteArrayInputStream(query.getBytes()));
			try {
				Program program = (new parser(lex)).pProgram();
				List<QueryResult> result = interpreter.interpretProgram(program);
				PathName zone = getPathName(zmi);
				for (QueryResult r : result) {
					if (printPartialResults) {
						System.out.println(zone + ": " + r);
					}
					zmi.getAttributes().addOrChange(r.getName(), r.getValue());
				}
			} catch (InterpreterException exception) {
				if (queryName != null) {
					System.out.println("Interpreter exception occurred: " + exception.getMessage());
				}
			}
		}
	}

	public static void printHierarchy(ZMI zmi) {
		System.out.println(zmi + "\n");
		for (ZMI son: zmi.getSons()) {
			printHierarchy(son);
		}
	}

	private static ValueContact createContact(String path, byte ip1, byte ip2, byte ip3, byte ip4)
			throws UnknownHostException {
		return new ValueContact(new PathName(path), InetAddress.getByAddress(new byte[]{
				ip1, ip2, ip3, ip4
		}));
	}

	public static ZMI createLabTestHierarchy() throws ParseException, UnknownHostException {
		ZMI root = new ZMI();

		ValueContact violet07Contact = createContact("/uw/violet07", (byte) 10, (byte) 1, (byte) 1, (byte) 10);
		ValueContact khaki13Contact = createContact("/uw/khaki13", (byte) 10, (byte) 1, (byte) 1, (byte) 38);
		ValueContact khaki31Contact = createContact("/uw/khaki31", (byte) 10, (byte) 1, (byte) 1, (byte) 39);
		ValueContact whatever01Contact = createContact("/uw/whatever01", (byte) 82, (byte) 111, (byte) 52, (byte) 56);
		ValueContact whatever02Contact = createContact("/uw/whatever02", (byte) 82, (byte) 111, (byte) 52, (byte) 57);

		List<Value> list;

		root.getAttributes().add("level", new ValueInt(0L));
		root.getAttributes().add("name", new ValueString(null));
		root.getAttributes().add("owner", new ValueString("/uw/violet07"));
		root.getAttributes().add("timestamp", new ValueTime("2012/11/09 20:10:17.342"));
		root.getAttributes().add("contacts", new ValueSet(TypePrimitive.CONTACT));
		root.getAttributes().add("cardinality", new ValueInt(0L));

		ZMI uw = new ZMI(root);
		root.addSon(uw);
		uw.getAttributes().add("level", new ValueInt(1L));
		uw.getAttributes().add("name", new ValueString("uw"));
		uw.getAttributes().add("owner", new ValueString("/uw/violet07"));
		uw.getAttributes().add("timestamp", new ValueTime("2012/11/09 20:8:13.123"));
		uw.getAttributes().add("contacts", new ValueSet(TypePrimitive.CONTACT));
		uw.getAttributes().add("cardinality", new ValueInt(0L));

		ZMI pjwstk = new ZMI(root);
		root.addSon(pjwstk);
		pjwstk.getAttributes().add("level", new ValueInt(1L));
		pjwstk.getAttributes().add("name", new ValueString("pjwstk"));
		pjwstk.getAttributes().add("owner", new ValueString("/pjwstk/whatever01"));
		pjwstk.getAttributes().add("timestamp", new ValueTime("2012/11/09 20:8:13.123"));
		pjwstk.getAttributes().add("contacts", new ValueSet(TypePrimitive.CONTACT));
		pjwstk.getAttributes().add("cardinality", new ValueInt(0L));

		ZMI violet07 = new ZMI(uw);
		uw.addSon(violet07);
		violet07.getAttributes().add("level", new ValueInt(2L));
		violet07.getAttributes().add("name", new ValueString("violet07"));
		violet07.getAttributes().add("owner", new ValueString("/uw/violet07"));
		violet07.getAttributes().add("timestamp", new ValueTime("2012/11/09 18:00:00.000"));
		list = Arrays.asList(new Value[]{
				khaki31Contact, whatever01Contact
		});
		violet07.getAttributes().add("contacts", new ValueSet(new HashSet<Value>(list), TypePrimitive.CONTACT));
		violet07.getAttributes().add("cardinality", new ValueInt(1L));
		list = Arrays.asList(new Value[]{
				violet07Contact,
		});
		violet07.getAttributes().add("members", new ValueSet(new HashSet<Value>(list), TypePrimitive.CONTACT));
		violet07.getAttributes().add("creation", new ValueTime("2011/11/09 20:8:13.123"));
		violet07.getAttributes().add("cpu_usage", new ValueDouble(0.9));
		violet07.getAttributes().add("num_cores", new ValueInt(3L));
		violet07.getAttributes().add("has_ups", new ValueBoolean(null));
		list = Arrays.asList(new Value[]{
				new ValueString("tola"), new ValueString("tosia"),
		});
		violet07.getAttributes().add("some_names", new ValueList(list, TypePrimitive.STRING));
		violet07.getAttributes().add("expiry", new ValueDuration(13L, 12L, 0L, 0L, 0L));

		ZMI khaki31 = new ZMI(uw);
		uw.addSon(khaki31);
		khaki31.getAttributes().add("level", new ValueInt(2L));
		khaki31.getAttributes().add("name", new ValueString("khaki31"));
		khaki31.getAttributes().add("owner", new ValueString("/uw/khaki31"));
		khaki31.getAttributes().add("timestamp", new ValueTime("2012/11/09 20:03:00.000"));
		list = Arrays.asList(new Value[]{
				violet07Contact, whatever02Contact,
		});
		khaki31.getAttributes().add("contacts", new ValueSet(new HashSet<Value>(list), TypePrimitive.CONTACT));
		khaki31.getAttributes().add("cardinality", new ValueInt(1L));
		list = Arrays.asList(new Value[]{
				khaki31Contact
		});
		khaki31.getAttributes().add("members", new ValueSet(new HashSet<Value>(list), TypePrimitive.CONTACT));
		khaki31.getAttributes().add("creation", new ValueTime("2011/11/09 20:12:13.123"));
		khaki31.getAttributes().add("cpu_usage", new ValueDouble(null));
		khaki31.getAttributes().add("num_cores", new ValueInt(3L));
		khaki31.getAttributes().add("has_ups", new ValueBoolean(false));
		list = Arrays.asList(new Value[]{
				new ValueString("agatka"), new ValueString("beatka"), new ValueString("celina"),
		});
		khaki31.getAttributes().add("some_names", new ValueList(list, TypePrimitive.STRING));
		khaki31.getAttributes().add("expiry", new ValueDuration(-13L, -11L, 0L, 0L, 0L));

		ZMI khaki13 = new ZMI(uw);
		uw.addSon(khaki13);
		khaki13.getAttributes().add("level", new ValueInt(2L));
		khaki13.getAttributes().add("name", new ValueString("khaki13"));
		khaki13.getAttributes().add("owner", new ValueString("/uw/khaki13"));
		khaki13.getAttributes().add("timestamp", new ValueTime("2012/11/09 21:03:00.000"));
		list = Collections.emptyList();
		khaki13.getAttributes().add("contacts", new ValueSet(new HashSet<Value>(list), TypePrimitive.CONTACT));
		khaki13.getAttributes().add("cardinality", new ValueInt(1L));
		list = Arrays.asList(new Value[]{
				khaki13Contact,
		});
		khaki13.getAttributes().add("members", new ValueSet(new HashSet<Value>(list), TypePrimitive.CONTACT));
		khaki13.getAttributes().add("creation", new ValueTime((Long) null));
		khaki13.getAttributes().add("cpu_usage", new ValueDouble(0.1));
		khaki13.getAttributes().add("num_cores", new ValueInt(null));
		khaki13.getAttributes().add("has_ups", new ValueBoolean(true));
		list = Collections.emptyList();
		khaki13.getAttributes().add("some_names", new ValueList(list, TypePrimitive.STRING));
		khaki13.getAttributes().add("expiry", new ValueDuration((Long) null));

		ZMI whatever01 = new ZMI(pjwstk);
		pjwstk.addSon(whatever01);
		whatever01.getAttributes().add("level", new ValueInt(2L));
		whatever01.getAttributes().add("name", new ValueString("whatever01"));
		whatever01.getAttributes().add("owner", new ValueString("/uw/whatever01"));
		whatever01.getAttributes().add("timestamp", new ValueTime("2012/11/09 21:12:00.000"));
		list = Arrays.asList(new Value[]{
				violet07Contact, whatever02Contact,
		});
		whatever01.getAttributes().add("contacts", new ValueSet(new HashSet<Value>(list), TypePrimitive.CONTACT));
		whatever01.getAttributes().add("cardinality", new ValueInt(1L));
		list = Arrays.asList(new Value[]{
				whatever01Contact,
		});
		whatever01.getAttributes().add("members", new ValueSet(new HashSet<Value>(list), TypePrimitive.CONTACT));
		whatever01.getAttributes().add("creation", new ValueTime("2012/10/18 07:03:00.000"));
		whatever01.getAttributes().add("cpu_usage", new ValueDouble(0.1));
		whatever01.getAttributes().add("num_cores", new ValueInt(7L));
		list = Arrays.asList(new Value[]{
				new ValueString("rewrite")
		});
		whatever01.getAttributes().add("php_modules", new ValueList(list, TypePrimitive.STRING));

		ZMI whatever02 = new ZMI(pjwstk);
		pjwstk.addSon(whatever02);
		whatever02.getAttributes().add("level", new ValueInt(2L));
		whatever02.getAttributes().add("name", new ValueString("whatever02"));
		whatever02.getAttributes().add("owner", new ValueString("/uw/whatever02"));
		whatever02.getAttributes().add("timestamp", new ValueTime("2012/11/09 21:13:00.000"));
		list = Arrays.asList(new Value[]{
				khaki31Contact, whatever01Contact,
		});
		whatever02.getAttributes().add("contacts", new ValueSet(new HashSet<Value>(list), TypePrimitive.CONTACT));
		whatever02.getAttributes().add("cardinality", new ValueInt(1L));
		list = Arrays.asList(new Value[]{
				whatever02Contact,
		});
		whatever02.getAttributes().add("members", new ValueSet(new HashSet<Value>(list), TypePrimitive.CONTACT));
		whatever02.getAttributes().add("creation", new ValueTime("2012/10/18 07:04:00.000"));
		whatever02.getAttributes().add("cpu_usage", new ValueDouble(0.4));
		whatever02.getAttributes().add("num_cores", new ValueInt(13L));
		list = Arrays.asList(new Value[]{
				new ValueString("odbc")
		});
		whatever02.getAttributes().add("php_modules", new ValueList(list, TypePrimitive.STRING));

		return root;
	}
	public static ZMI createAssignmentTestHierarchy() throws ParseException, UnknownHostException {
		ZMI root = new ZMI();

		ValueContact violet07Contact = createContact("/uw/violet07", (byte) 10, (byte) 1, (byte) 1, (byte) 10);
		ValueContact khaki13Contact = createContact("/uw/khaki13", (byte) 10, (byte) 1, (byte) 1, (byte) 38);
		ValueContact khaki31Contact = createContact("/uw/khaki31", (byte) 10, (byte) 1, (byte) 1, (byte) 39);
		ValueContact whatever01Contact = createContact("/uw/whatever01", (byte) 82, (byte) 111, (byte) 52, (byte) 56);
		ValueContact whatever02Contact = createContact("/uw/whatever02", (byte) 82, (byte) 111, (byte) 52, (byte) 57);

		ValueContact UW1Contact = violet07Contact;
		ValueContact UW2AContact = khaki31Contact;
		ValueContact UW3BContact = khaki13Contact;
		ValueContact PJ1Contact = whatever01Contact;
		ValueContact PJ2Contact = whatever02Contact;

		ValueContact UW1AContact = createContact("/uw/violet08", (byte) 10, (byte) 1, (byte) 1, (byte) 11);
		ValueContact UW1BContact = createContact("/uw/violet09", (byte) 10, (byte) 1, (byte) 1, (byte) 12);
		ValueContact UW1CContact = createContact("/uw/violet10", (byte) 10, (byte) 1, (byte) 1, (byte) 13);
		ValueContact UW3AContact = createContact("/uw/khaki12", (byte) 10, (byte) 1, (byte) 1, (byte) 37);

		List<Value> list;

		root.getAttributes().add("level", new ValueInt(0L));
		root.getAttributes().add("name", new ValueString(null));

		ZMI uw = new ZMI(root);
		root.addSon(uw);
		uw.getAttributes().add("level", new ValueInt(1L));
		uw.getAttributes().add("name", new ValueString("uw"));

		ZMI pjwstk = new ZMI(root);
		root.addSon(pjwstk);
		pjwstk.getAttributes().add("level", new ValueInt(1L));
		pjwstk.getAttributes().add("name", new ValueString("pjwstk"));

		ZMI violet07 = new ZMI(uw);
		uw.addSon(violet07);
		violet07.getAttributes().add("level", new ValueInt(2L));
		violet07.getAttributes().add("name", new ValueString("violet07"));
		violet07.getAttributes().add("owner", new ValueString("/uw/violet07"));
		violet07.getAttributes().add("timestamp", new ValueTime("2012/11/09 18:00:00.000"));
		list = Arrays.asList(new Value[]{
				UW1AContact, UW1BContact, UW1CContact
		});
		violet07.getAttributes().add("contacts", new ValueSet(new HashSet<Value>(list), TypePrimitive.CONTACT));
		violet07.getAttributes().add("cardinality", new ValueInt(1L));
		list = Arrays.asList(new Value[]{
				UW1Contact,
		});
		violet07.getAttributes().add("members", new ValueSet(new HashSet<Value>(list), TypePrimitive.CONTACT));
		violet07.getAttributes().add("creation", new ValueTime("2011/11/09 20:8:13.123"));
		violet07.getAttributes().add("cpu_usage", new ValueDouble(0.9));
		violet07.getAttributes().add("num_cores", new ValueInt(3L));
		violet07.getAttributes().add("has_ups", new ValueBoolean(null));
		list = Arrays.asList(new Value[]{
				new ValueString("tola"), new ValueString("tosia"),
		});
		violet07.getAttributes().add("some_names", new ValueList(list, TypePrimitive.STRING));
		violet07.getAttributes().add("expiry", new ValueDuration(13L, 12L, 0L, 0L, 0L));

		ZMI khaki31 = new ZMI(uw);
		uw.addSon(khaki31);
		khaki31.getAttributes().add("level", new ValueInt(2L));
		khaki31.getAttributes().add("name", new ValueString("khaki31"));
		khaki31.getAttributes().add("owner", new ValueString("/uw/khaki31"));
		khaki31.getAttributes().add("timestamp", new ValueTime("2012/11/09 20:03:00.000"));
		list = Arrays.asList(new Value[]{
				UW2AContact
		});
		khaki31.getAttributes().add("contacts", new ValueSet(new HashSet<Value>(list), TypePrimitive.CONTACT));
		khaki31.getAttributes().add("cardinality", new ValueInt(1L));
		khaki31.getAttributes().add("members", new ValueSet(new HashSet<Value>(list), TypePrimitive.CONTACT));
		khaki31.getAttributes().add("creation", new ValueTime("2011/11/09 20:12:13.123"));
		khaki31.getAttributes().add("cpu_usage", new ValueDouble(null));
		khaki31.getAttributes().add("num_cores", new ValueInt(3L));
		khaki31.getAttributes().add("has_ups", new ValueBoolean(false));
		list = Arrays.asList(new Value[]{
				new ValueString("agatka"), new ValueString("beatka"), new ValueString("celina"),
		});
		khaki31.getAttributes().add("some_names", new ValueList(list, TypePrimitive.STRING));
		khaki31.getAttributes().add("expiry", new ValueDuration(-13L, -11L, 0L, 0L, 0L));

		ZMI khaki13 = new ZMI(uw);
		uw.addSon(khaki13);
		khaki13.getAttributes().add("level", new ValueInt(2L));
		khaki13.getAttributes().add("name", new ValueString("khaki13"));
		khaki13.getAttributes().add("owner", new ValueString("/uw/khaki13"));
		khaki13.getAttributes().add("timestamp", new ValueTime("2012/11/09 21:03:00.000"));
		list = Arrays.asList(new Value[]{
				UW3BContact, UW3AContact
		});
		khaki13.getAttributes().add("contacts", new ValueSet(new HashSet<Value>(list), TypePrimitive.CONTACT));
		khaki13.getAttributes().add("cardinality", new ValueInt(1L));
		list = Arrays.asList(new Value[]{
				UW3BContact,
		});
		khaki13.getAttributes().add("members", new ValueSet(new HashSet<Value>(list), TypePrimitive.CONTACT));
		khaki13.getAttributes().add("creation", new ValueTime((Long) null));
		khaki13.getAttributes().add("cpu_usage", new ValueDouble(0.1));
		khaki13.getAttributes().add("num_cores", new ValueInt(null));
		khaki13.getAttributes().add("num_processes", new ValueInt(107L));
		khaki13.getAttributes().add("has_ups", new ValueBoolean(true));
		list = Collections.emptyList();
		khaki13.getAttributes().add("some_names", new ValueList(list, TypePrimitive.STRING));
		khaki13.getAttributes().add("expiry", new ValueDuration((Long) null));

		ZMI whatever01 = new ZMI(pjwstk);
		pjwstk.addSon(whatever01);
		whatever01.getAttributes().add("level", new ValueInt(2L));
		whatever01.getAttributes().add("name", new ValueString("whatever01"));
		whatever01.getAttributes().add("owner", new ValueString("/pjwstk/whatever01"));
		whatever01.getAttributes().add("timestamp", new ValueTime("2012/11/09 21:12:00.000"));
		list = Arrays.asList(new Value[]{
				PJ1Contact
		});
		whatever01.getAttributes().add("contacts", new ValueSet(new HashSet<Value>(list), TypePrimitive.CONTACT));
		whatever01.getAttributes().add("cardinality", new ValueInt(1L));
		whatever01.getAttributes().add("members", new ValueSet(new HashSet<Value>(list), TypePrimitive.CONTACT));
		whatever01.getAttributes().add("creation", new ValueTime("2012/10/18 07:03:00.000"));
		whatever01.getAttributes().add("cpu_usage", new ValueDouble(0.1));
		whatever01.getAttributes().add("num_cores", new ValueInt(7L));
		whatever01.getAttributes().add("num_processes", new ValueInt(215L));
		list = Arrays.asList(new Value[]{
				new ValueString("rewrite")
		});
		whatever01.getAttributes().add("php_modules", new ValueList(list, TypePrimitive.STRING));

		ZMI whatever02 = new ZMI(pjwstk);
		pjwstk.addSon(whatever02);
		whatever02.getAttributes().add("level", new ValueInt(2L));
		whatever02.getAttributes().add("name", new ValueString("whatever02"));
		whatever02.getAttributes().add("owner", new ValueString("/pjwstk/whatever02"));
		whatever02.getAttributes().add("timestamp", new ValueTime("2012/11/09 21:13:00.000"));
		list = Arrays.asList(new Value[]{
				PJ2Contact
		});
		whatever02.getAttributes().add("contacts", new ValueSet(new HashSet<Value>(list), TypePrimitive.CONTACT));
		whatever02.getAttributes().add("cardinality", new ValueInt(1L));
		whatever02.getAttributes().add("members", new ValueSet(new HashSet<Value>(list), TypePrimitive.CONTACT));
		whatever02.getAttributes().add("creation", new ValueTime("2012/10/18 07:04:00.000"));
		whatever02.getAttributes().add("cpu_usage", new ValueDouble(0.4));
		whatever02.getAttributes().add("num_processes", new ValueInt(222L));
		whatever02.getAttributes().add("num_cores", new ValueInt(13L));
		list = Arrays.asList(new Value[]{
				new ValueString("odbc")
		});
		whatever02.getAttributes().add("php_modules", new ValueList(list, TypePrimitive.STRING));

		return root;
	}
}
