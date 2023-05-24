package pt.up.fe.comp2023;

import pt.up.fe.comp.jmm.analysis.JmmSemanticsResult;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp2023.optimization.ASToptimization;
import pt.up.fe.comp2023.optimization.FoldingVisitor;
import pt.up.fe.comp2023.optimization.WhileInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;


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
        WhileInfo whileInfo = new WhileInfo();
        whileInfo.visit(root);
        List<Set<String>> whileData = whileInfo.getWhileData();
        List<List<Set<String>>> ifData = whileInfo.getIfData();
        ASToptimization astOptimizerVisitor = new ASToptimization(symbolTable, whileData, ifData);
        FoldingVisitor foldingVisitor = new FoldingVisitor();
        do {
            foldingVisitor.visit(root);
            astOptimizerVisitor.visit(root);
        } while (astOptimizerVisitor.wasOptimized() || foldingVisitor.wasOptimized());

        return root;
    }

}
