package pt.up.fe.comp2023;

import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.ast.PreorderJmmVisitor;

import java.util.*;

public class SymbolTableVisitor extends PreorderJmmVisitor<Void, Void> {
    protected List<String> imports = new ArrayList<String>();
    protected String className = "";
    protected String _super = null;
    protected List<Symbol> fields = new ArrayList<Symbol>();
    protected List<String> methods = new ArrayList<String>();
    protected Map<String, Type> returnTypes = new HashMap<String, Type>();
    protected Map<String, List<Symbol>> parameters = new HashMap<String, List<Symbol>>();
    protected Map<String, List<Symbol>> localVariables = new HashMap<String, List<Symbol>>();
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
        addVisit("Multiplicative", this::dealWithBinaryOp);
        addVisit("Compare", this::dealWithCompare);
        addVisit("LogicalAnd", this::dealWithLogicalAnd);
        addVisit("SquareBrackets", this::dealWithSquareBrackets);
        addVisit("Length", this::dealWithLength);
        addVisit("FunctionCall", this::dealWithFunctionCall);
        addVisit("NewArray", this::dealWithNewArray);
        addVisit("NewClass", this::dealWithNewClass);
        addVisit("Integer", this::dealWithInteger);
        addVisit("BoolLiteral", this::dealWithBool);
        addVisit("Identifier", this::dealWithIdentifier);
        addVisit("This", this::dealWithThis);
        addVisit("MethodArgs", this::dealWithMethodArgs);
    }



    public SimpleTable generateSymbolicTable() {
        return new SimpleTable(imports, className, _super, fields, methods, returnTypes, parameters, localVariables);
    }

    private Void dealWithThis(JmmNode jmmnode, Void _void) {
        return null;
    }

    private Void dealWithIdentifier(JmmNode node, Void _void) {
        return null;
    }

    private Void dealWithBool(JmmNode node, Void _void) {
        return null;
    }

    private Void dealWithInteger(JmmNode node, Void _void) {
        return null;
    }

    private Void dealWithNewClass(JmmNode node, Void _void) { return null; }

    private Void dealWithNewArray(JmmNode node, Void _void) {
        return null;
    }

    private Void dealWithFunctionCall(JmmNode node, Void _void) {
        return null;
    }

    private Void dealWithLength(JmmNode node, Void _void) {
        return null;
    }

    private Void dealWithSquareBrackets(JmmNode node, Void _void) {
        return null;
    }

    private Void dealWithLogicalAnd(JmmNode node, Void _void) {
        return null;
    }

    private Void dealWithCompare(JmmNode node, Void _void) {
        return null;
    }

    private Void dealWithBinaryOp(JmmNode node, Void _void) {
        return null;
    }

    private Void dealWithParenthesis(JmmNode node, Void _void) {
        return null;
    }

    private Void dealWithNegation(JmmNode node, Void _void) {
        return null;
    }

    private Void dealWithArray(JmmNode node, Void _void) {
        return null;
    }

    private Void dealWithAssignment(JmmNode node, Void _void) {
        return null;
    }

    private Void dealWithStatementExpression(JmmNode node, Void _void) {
        return null;
    }

    private Void dealWithWhile(JmmNode node, Void _void) {
        return null;
    }

    private Void dealWithIf(JmmNode node, Void _void) {
        return null;
    }

    private Void dealWithBlockCode(JmmNode node, Void _void) {
        return null;
    }

    private Void dealWithIDType(JmmNode node, Void _void) {
        return null;
    }

    private Void dealWithBoolType(JmmNode node, Void _void) {
        return null;
    }

    private Void dealWithIntType(JmmNode node, Void _void) {
        return null;
    }

    private Void dealWithMethod(JmmNode node, Void _void) {
        methods.add(node.get("name"));
        JmmNode retTypeNode = node.getChildren().get(0);


        System.out.println("------------");
        System.out.println(retTypeNode.getKind());
        System.out.println("------------");
        Type type = null;
        switch (retTypeNode.getKind()) {
            case "IDType":
                type = new Type(retTypeNode.get("typeName"), (boolean)retTypeNode.getObject("isArray"));
                break;
            case "IntType":
                type = new Type("int", (boolean)retTypeNode.getObject("isArray"));
                break;
            case "BoolType":
                type = new Type("boolean", (boolean)retTypeNode.getObject("isArray"));
                break;
        }
        System.out.println(type);
        System.out.println("---------");
        returnTypes.put(node.get("name"),type);
        /*add an empty array associated with this method in the arguments map*/
        parameters.put(node.get("name"), new ArrayList<>());
        localVariables.put(node.get("name"), new ArrayList<>());
        return null;
    }
    private Void dealWithMethodArgs(JmmNode node, Void unused) {
        /*System.out.println("----------------");
        System.out.println(node.get("types"));
        System.out.println(node.get("args"));
        System.out.println(node.getChildren());
        System.out.println("--------------");*/

        List<?> listArgs = objectToIterable(node.getObject("args"));
        if (listArgs.isEmpty()) return null;


        List<Symbol> args = new ArrayList<>();
        for (var i = 0; i < listArgs.size(); i++) {
            /*System.out.println(node.getChildren().get(i));
            System.out.println(listArgs.get(i));*/
            JmmNode typeNode = node.getChildren().get(i);
            Type type = null;
            switch (typeNode.getKind()) {
                case "IDType":
                    type = new Type(typeNode.get("typeName"), (boolean)typeNode.getObject("isArray"));
                    break;
                case "IntType":
                    type = new Type("int", (boolean)typeNode.getObject("isArray"));
                    break;
                case "BoolType":
                    type = new Type("boolean", (boolean)typeNode.getObject("isArray"));
                    break;
            }
            args.add(new Symbol(type, listArgs.get(i).toString()));
        }
        this.parameters.put(node.getJmmParent().get("name"), args);
        return null;
    }
    private Void dealWithMainMethod(JmmNode node, Void _void) {
        methods.add("main");
        returnTypes.put("main", new Type("void", false));
        /*add an empty array associated with this method in the arguments map*/
        localVariables.put("main", new ArrayList<>());

        String typeType = node.getChildren().get(0).getKind();
        Type type = null;
        switch (typeType) {
            case "IDType":
                type = new Type(node.getChildren().get(0).get("typeName"), (boolean)node.getChildren().get(0).getObject("isArray"));
                break;
            case "IntType":
                type = new Type("int", (boolean)node.getChildren().get(0).getObject("isArray"));
                break;
            case "BoolType":
                type = new Type("boolean", (boolean)node.getChildren().get(0).getObject("isArray"));
                break;
        }
        this.returnTypes.put("main", new Type("void", false));
        List<Symbol> args = new ArrayList<Symbol>(List.of(new Symbol(type, node.get("arg"))));
        parameters.put("main", args);
        localVariables.put("main", new ArrayList<>());
        return null;
    }

    private Void dealWithVarDeclaration(JmmNode node, Void _void) {
        String typeType = node.getJmmChild(0).getKind();
        Type type = null;
        switch (typeType) {
            case "IDType":
                type = new Type(node.getChildren().get(0).get("typeName"), (boolean)node.getChildren().get(0).getObject("isArray"));
                break;
            case "IntType":
                type = new Type("int", (boolean)node.getChildren().get(0).getObject("isArray"));
                break;
            case "BoolType":
                type = new Type("boolean", (boolean)node.getChildren().get(0).getObject("isArray"));
                break;
        }
        Symbol field = new Symbol(type, node.get("var"));
        /*System.out.println("-------------");
        System.out.println(node.getJmmParent().getKind());
        System.out.println("-------------");*/
        if(node.getJmmParent().getKind().equals("Method")) {
            this.localVariables.put(node.getJmmParent().get("name"),List.of(field));
        }
        else if(node.getJmmParent().getKind().equals("MainMethod")){
            this.localVariables.put("main",List.of(field));
        }
        else
            this.fields.add(field);
        return null;
    }

    private Void dealWithClassDeclaration(JmmNode node, Void _void) {
        this.className = node.get("name");
        if (node.hasAttribute("superName"))
            this._super = node.get("superName");

        return null;
    }


    private Void dealWithImportDeclaration(JmmNode node, Void _void) {
        List<?> list = objectToIterable(node.getObject("packageNames"));

        if (list.isEmpty()) return null;


        StringBuilder pkg = new StringBuilder();
        for (var imp : list) {
            pkg.append(imp);
            pkg.append(".");
        }

        imports.add(String.valueOf(pkg).substring(0, pkg.length() - 1));

        return null;
    }

    private Void dealWithProgram(JmmNode node, Void _void) {
        return null;
    }
    private List<?> objectToIterable(Object obj) {
        List<?> list = new ArrayList<>();
        if (obj.getClass().isArray()) {
            list = Arrays.asList((Object[])obj);
        } else if (obj instanceof Collection) {
            list = new ArrayList<>((Collection<?>)obj);
        }
        return list;
    }

}
