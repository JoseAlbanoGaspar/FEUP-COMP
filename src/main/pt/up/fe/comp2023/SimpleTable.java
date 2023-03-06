package pt.up.fe.comp2023;

import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.parser.JmmParserResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SimpleTable implements SymbolTable {
    protected List<String> imports;
    protected String className;
    protected String _super;
    protected List<Symbol> fields;
    protected List<String> methods;
    protected Map<String, Type> returnTypes;
    protected Map<String, List<Symbol>> parameters;
    protected Map<String, List<Symbol>> localVariables;
    public SimpleTable(List<String> imports, String className, String _super,
                       List<Symbol> fields, List<String> methods, Map<String, Type> returnTypes,
                       Map<String, List<Symbol>> parameters, Map<String, List<Symbol>> localVariables) {

        this.imports = imports;
        this.className = className;
        this._super = _super;
        this.fields = fields;
        this.methods = methods;
        this.returnTypes = returnTypes;
        this.parameters = parameters;
        this.localVariables = localVariables;
    }

    @Override
    public List<String> getImports() {
        return imports;
    }

    @Override
    public String getClassName() {
        return className;
    }

    @Override
    public String getSuper() {
        return _super;
    }

    @Override
    public List<Symbol> getFields() {
        return fields;
    }

    @Override
    public List<String> getMethods() {
        return methods;
    }

    @Override
    public Type getReturnType(String s) {
        return returnTypes.get(s);
    }

    @Override
    public List<Symbol> getParameters(String s) {
        return parameters.get(s);
    }

    @Override
    public List<Symbol> getLocalVariables(String s) {
        return localVariables.get(s);
    }
}
