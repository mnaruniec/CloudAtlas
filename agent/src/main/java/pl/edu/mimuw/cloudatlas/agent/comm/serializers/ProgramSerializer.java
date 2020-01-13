package pl.edu.mimuw.cloudatlas.agent.comm.serializers;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import pl.edu.mimuw.cloudatlas.interpreter.query.Absyn.Program;
import pl.edu.mimuw.cloudatlas.interpreter.query.PrettyPrinter;
import pl.edu.mimuw.cloudatlas.interpreter.query.Yylex;
import pl.edu.mimuw.cloudatlas.interpreter.query.parser;

import java.io.ByteArrayInputStream;

public class ProgramSerializer extends Serializer<Program> {
	@Override
	public void write(Kryo kryo, Output output, Program object) {
		kryo.writeObject(output, PrettyPrinter.print(object));
	}

	@Override
	public Program read(Kryo kryo, Input input, Class<? extends Program> type) {
		try {
			String string = input.readString();
			Yylex lex = new Yylex(new ByteArrayInputStream(string.getBytes()));
			return (new parser(lex)).pProgram();
		} catch (Exception e) {
			throw new IllegalArgumentException(e);
		}
	}
}
