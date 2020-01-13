package pl.edu.mimuw.cloudatlas.signer;

import pl.edu.mimuw.cloudatlas.interpreter.query.Absyn.AliasedSelItemC;
import pl.edu.mimuw.cloudatlas.interpreter.query.Absyn.Program;
import pl.edu.mimuw.cloudatlas.interpreter.query.Absyn.ProgramC;
import pl.edu.mimuw.cloudatlas.interpreter.query.Absyn.SelItem;
import pl.edu.mimuw.cloudatlas.interpreter.query.Absyn.SelItemC;
import pl.edu.mimuw.cloudatlas.interpreter.query.Absyn.Statement;
import pl.edu.mimuw.cloudatlas.interpreter.query.Absyn.StatementC;
import pl.edu.mimuw.cloudatlas.model.Attribute;

import java.util.List;

public class IdentGetter implements
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