package pt.up.fe.comp2023;

import pt.up.fe.comp.jmm.analysis.JmmSemanticsResult;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp2023.optimization.ConstFoldingVisitor;
import pt.up.fe.comp2023.optimization.ConstValueVisitor;

import java.util.ArrayList;
import java.util.Map;


public class Optimizer {
    public JmmSemanticsResult optimize(JmmSemanticsResult ast, Map<String, String> config){
        if (config.get("optimize").equals("true")) {
            return new JmmSemanticsResult(constantsOptimization(ast.getRootNode(), ast.getSymbolTable()), ast.getSymbolTable(), new ArrayList<>(),config);
        }
        return ast;
    }

    private JmmNode constantsOptimization(JmmNode root, SymbolTable symbolTable) {
        ConstValueVisitor constValueVisitor = new ConstValueVisitor(symbolTable);
        ConstFoldingVisitor constFoldingVisitor = new ConstFoldingVisitor();
        do {
            constFoldingVisitor.visit(root);
            constValueVisitor.visit(root);
            System.out.println("loop");
        } while (constValueVisitor.wasOptimized() || constFoldingVisitor.wasOptimized());

        return root;
    }

}
