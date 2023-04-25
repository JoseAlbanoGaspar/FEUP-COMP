package pt.up.fe.comp2023;

import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.parser.JmmParserResult;
import pt.up.fe.comp2023.optimization.ConstFoldingVisitor;
import pt.up.fe.comp2023.optimization.ConstValueVisitor;

import java.util.ArrayList;
import java.util.Map;


public class Optimizer {
    public JmmParserResult optimize(JmmParserResult ast, Map<String, String> config){
        if (config.get("optimize").equals("true")) {
            ast = constantsOptimization(ast, config);
        }
        if (!config.get("registerAllocation").equals("-1")) {
            ast = registerAllocationOptimization(ast);
        }
        return ast;
    }

    private JmmParserResult constantsOptimization(JmmParserResult ast, Map<String, String> config) {
        ConstValueVisitor constValueVisitor = new ConstValueVisitor();
        ConstFoldingVisitor constFoldingVisitor = new ConstFoldingVisitor();
        JmmNode root = ast.getRootNode();
        while (constValueVisitor.wasOptimized() || constFoldingVisitor.wasOptimized()) {
            constValueVisitor.visit(root);
            root = constValueVisitor.getRootNode();
            constFoldingVisitor.visit(root);
            root = constFoldingVisitor.getRootNode();
        }
        return new JmmParserResult(root, new ArrayList<>(), config);
    }

    private JmmParserResult registerAllocationOptimization(JmmParserResult ast) {

        return ast;
    }


}
