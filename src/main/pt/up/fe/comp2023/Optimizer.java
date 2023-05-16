package pt.up.fe.comp2023;

import pt.up.fe.comp.jmm.analysis.JmmSemanticsResult;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp2023.optimization.ASToptimization;

import java.util.ArrayList;


public class Optimizer {
    public JmmSemanticsResult optimize(JmmSemanticsResult ast){
        if (ast.getConfig().getOrDefault("optimize", "false").equals("true")) {
            return new JmmSemanticsResult(astOptimization(ast.getRootNode(), ast.getSymbolTable()), ast.getSymbolTable(), new ArrayList<>(),ast.getConfig());
        }
        /*if (!ast.getConfig().getOrDefault("registerAllocation", "-1").equals("-1")) {
            return new JmmSemanticsResult(astOptimization(ast.getRootNode(), ast.getSymbolTable()), ast.getSymbolTable(), new ArrayList<>(),ast.getConfig());
        }*/
        return ast;
    }

    private JmmNode astOptimization(JmmNode root, SymbolTable symbolTable) {
        ASToptimization astOptimizerVisitor = new ASToptimization(symbolTable);
        do {
            astOptimizerVisitor.visit(root);
        } while (astOptimizerVisitor.wasOptimized());

        return root;
    }

}
