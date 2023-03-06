package pt.up.fe.comp2023;

import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.PreorderJmmVisitor;

import java.util.List;

public class SymbolTableVisitor extends PreorderJmmVisitor {
    protected String imports;

    protected String className;

    protected String superList;

    protected List<Symbol> Fields;

    protected List<String> methods;

    protected Type type;

    protected List<Symbol> parameters;

    protected List<Symbol> LocalVariables;
    @Override
    protected void buildVisitor() {
        addVisit("Program", this::dealWithProgram);
        addVisit("Assignment", this::dealWithAssignment);
        addVisit("Integer", this::dealWithInteger);
        addVisit("Identifier", this::dealWithIdentifier);
        addVisit("ExprStmt", this::dealWithExprStmt);
        addVisit("BinaryOp", this::dealWithBinaryOp);
        addVisit("Parenthesis", this::dealWithParenthesis);
    }
}
