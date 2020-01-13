package pl.edu.mimuw.cloudatlas.serialization.serializers;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import pl.edu.mimuw.cloudatlas.interpreter.query.Absyn.ProgramC;
import pl.edu.mimuw.cloudatlas.interpreter.query.PrettyPrinter;
import pl.edu.mimuw.cloudatlas.interpreter.query.Yylex;
import pl.edu.mimuw.cloudatlas.interpreter.query.parser;

import java.io.ByteArrayInputStream;

public class ProgramCSerializer extends Serializer<ProgramC> {
	@Override
	public void write(Kryo kryo, Output output, ProgramC object) {
		kryo.writeObject(output, PrettyPrinter.print(object));
	}

	@Override
	public ProgramC read(Kryo kryo, Input input, Class<? extends ProgramC> type) {
		try {
			String string = input.readString();
			Yylex lex = new Yylex(new ByteArrayInputStream(string.getBytes()));
			return (ProgramC) (new parser(lex)).pProgram();
		} catch (Exception e) {
			throw new IllegalArgumentException(e);
		}
	}
}
