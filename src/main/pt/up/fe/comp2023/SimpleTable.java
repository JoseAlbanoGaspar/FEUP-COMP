package pt.up.fe.comp2023;

import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.parser.JmmParserResult;

import java.util.List;

public class SimpleTable implements SymbolTable {
    protected final List<String> imports;
    protected final String className;
    protected final String _super;
    protected final List<Symbol> fields;
    protected final List<String> methods;
    protected final Type type;
    protected final List<Symbol> parameters;
    protected final List<Symbol> localVariables;
    public SimpleTable(List<String> imports, String className, String _super,
                       List<Symbol> fields, List<String> methods, Type type,
                       List<Symbol> parameters, List<Symbol> localVariables) {

        this.imports = imports;
        this.className = className;
        this._super = _super;
        this.fields = fields;
        this.methods = methods;
        this.type = type;
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
        return type;
    }

    @Override
    public List<Symbol> getParameters(String s) {
        return parameters;
    }

    @Override
    public List<Symbol> getLocalVariables(String s) {
        return localVariables;
    }
}
