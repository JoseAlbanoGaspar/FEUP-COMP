package pt.up.fe.comp2023;


import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.ast.AJmmVisitor;

public class OllirVisitor extends AJmmVisitor<String, String> {
    private String ollirString;
    private SymbolTable symbolTable;

    public OllirVisitor(SymbolTable symbolTable){
        this.symbolTable = symbolTable;
        ollirString = "";
    }
    @Override
    protected void buildVisitor() {

    }

    public String getOllirCode() {
        return ollirString;
    }
}
