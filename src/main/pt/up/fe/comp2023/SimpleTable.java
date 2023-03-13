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
    public String toString(){
        StringBuilder bob = new StringBuilder();

        if (!imports.isEmpty()) {
            bob.append("IMPORTS:\n");
            for (String impt : imports) {
                bob.append("    -> ").append(impt).append("\n");
            }
        }

        bob.append("CLASS NAME: ").append(className).append("\n");
        if(_super != null)
            bob.append("SUPER CLASS NAME: ").append(_super).append("\n");

        if (!fields.isEmpty()) {
            bob.append("FIELDS:\n");
            for (Symbol f : fields) {
                bob.append("    -> ").append(f.print()).append("\n");
            }
        }

        if (!methods.isEmpty()) {
            bob.append("METHODS:\n");
            for (String mth : methods) {
                bob.append("    -> ").append(mth).append("\n");
                bob.append("        Returns: ").append(returnTypes.get(mth).print()).append("\n");
                if (!parameters.get(mth).isEmpty()) {
                    bob.append(("        Parameters:\n"));
                    for (Symbol par : parameters.get(mth)) {
                        bob.append("            -> ").append(par.print()).append("\n");
                    }
                }
                if (!localVariables.get(mth).isEmpty()) {
                    bob.append(("       Local Variables:\n"));
                    for (Symbol variable : localVariables.get(mth)) {
                        bob.append("            -> ").append(variable.print()).append("\n");
                    }
                }
            }
        }


        return bob.toString();
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
