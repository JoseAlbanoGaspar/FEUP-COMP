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
        addVisit("ImportDeclaration", this::dealWithImportDeclaration);
        addVisit("ClassDeclaration", this::dealWithClassDeclaration);
        addVisit("VarDeclaration", this::dealWithVarDeclaration);
        addVisit("MainMethod", this::dealWithMainMethod);
        addVisit("Method", this::dealWithMethod);
        addVisit("IntType", this::dealWithIntType);
        addVisit("BoolType", this::dealWithBoolType);
        addVisit("IDType", this::dealWithIDType);
        addVisit("BlockCode", this::dealWithBlockCode);
        addVisit("If",this::dealWithIf);
        addVisit("While", this::dealWithWhile);
        addVisit("StatementExpression", this::dealWithStatementExpression);
        addVisit("Assignment", this::dealWithAssignment);
        addVisit("Array", this::dealWithArray);
        addVisit("Negation", this::dealWithNegation);
        addVisit("Parenthesis", this::dealWithParenthesis);
        addVisit("Multiplicative", this::dealWithMultiplicative);
        addVisit("Additive", this::dealWithAdditive);
        addVisit("Compare", this::dealWithCompare);
        addVisit("LogicalAnd", this::dealWithLogicalAnd);
        addVisit("SquareBrackets", this::dealWithSquareBrackets);
        addVisit("Length", this::dealWithLength);
        addVisit("FunctionCall", this::dealWithFunctionCall);
        addVisit("NewArray", this::dealWithNewArray);
        addVisit("NewClass", this::dealWithNewClass);
        addVisit("Integer", this::dealWithInteger);
        addVisit("BoolTrue", this::dealWithBoolTrue);
        addVisit("BoolFalse", this::dealWithBoolFalse);
        addVisit("Identifier", this::dealWithIdentifier);
        addVisit("This", this::dealWithThis);
    }

    private Object dealWithThis(Object o, Object o1) {
    }

    private Object dealWithIdentifier(Object o, Object o1) {
    }

    private Object dealWithBoolFalse(Object o, Object o1) {
    }

    private Object dealWithBoolTrue(Object o, Object o1) {
    }

    private Object dealWithInteger(Object o, Object o1) {
    }

    private Object dealWithNewClass(Object o, Object o1) {
    }

    private Object dealWithNewArray(Object o, Object o1) {
    }

    private Object dealWithFunctionCall(Object o, Object o1) {
    }

    private Object dealWithLength(Object o, Object o1) {
    }

    private Object dealWithSquareBrackets(Object o, Object o1) {
    }

    private Object dealWithLogicalAnd(Object o, Object o1) {
    }

    private Object dealWithCompare(Object o, Object o1) {
    }

    private Object dealWithAdditive(Object o, Object o1) {
    }

    private Object dealWithMultiplicative(Object o, Object o1) {
    }

    private Object dealWithParenthesis(Object o, Object o1) {
    }

    private Object dealWithNegation(Object o, Object o1) {
    }

    private Object dealWithArray(Object o, Object o1) {
    }

    private Object dealWithAssignment(Object o, Object o1) {
    }

    private Object dealWithStatementExpression(Object o, Object o1) {
    }

    private Object dealWithWhile(Object o, Object o1) {
    }

    private Object dealWithIf(Object o, Object o1) {
    }

    private Object dealWithBlockCode(Object o, Object o1) {
    }

    private Object dealWithIDType(Object o, Object o1) {
    }

    private Object dealWithBoolType(Object o, Object o1) {
    }

    private Object dealWithIntType(Object o, Object o1) {
    }

    private Object dealWithMethod(Object o, Object o1) {
    }

    private Object dealWithMainMethod(Object o, Object o1) {
    }

    private Object dealWithVarDeclaration(Object o, Object o1) {
    }

    private Object dealWithClassDeclaration(Object o, Object o1) {
    }

    private Object dealWithImportDeclaration(Object o, Object o1) {
    }

    private Object dealWithProgram(Object o, Object o1) {
    }
}
