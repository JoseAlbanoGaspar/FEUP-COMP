package pt.up.fe.comp2023.optimization.registerAllocation;

import org.specs.comp.ollir.*;
import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;

import java.util.*;

public class LivenessAnalysis {
    protected HashMap<Instruction, LivenessSets> sets;
    private final Method method;
    private final SymbolTable symbolTable;

    public LivenessAnalysis(Method method, SymbolTable symbTable) {
        this.sets = new HashMap<>();
        this.method = method;
        this.symbolTable = symbTable;
        for (Instruction i : method.getInstructions()) {
            System.out.println(i);
            sets.put(i, new LivenessSets());
        }
    }

    public HashMap<Instruction, LivenessSets> getSets() {
        return sets;
    }

    private void print() {
        // print uses/defs for testing
        System.out.println("|||||  LIVENESS FOR METHOD " + this.method.getMethodName() + "  |||||");
        for (Map.Entry<Instruction, LivenessSets> entry : sets.entrySet()) {
            System.out.println("-----Entry Set-----");
            System.out.println(entry.getKey());
            System.out.println("Use: " + entry.getValue().getUse());
            System.out.println("Def: " + entry.getValue().getDef());
            System.out.println("Ins: " + entry.getValue().getIn());
            System.out.println("Out: " + entry.getValue().getOut());
            System.out.println("-------------------");
        }
        System.out.println("|||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||");
    }
    private void computeInOut() {
        boolean changed = false;
        do for (Instruction i : method.getInstructions()) {
            changed = false;
            Set<String> in_aux = sets.get(i).getIn();
            Set<String> out_aux = sets.get(i).getOut();
            // Compute IN set
            Set<String> difference = new HashSet<>(sets.get(i).getOut());
            difference.removeAll(sets.get(i).getDef());
            Set<String> union = new HashSet<>(sets.get(i).getUse());
            union.addAll(difference);
            sets.get(i).setIn(union);
            // Compute OUT set
            for (Node node : i.getSuccessors()) {
                if (node.getNodeType().equals(NodeType.INSTRUCTION)) {
                    final Instruction inst = (Instruction) node;
                    sets.get(i).getOut().addAll(sets.get(inst).getIn());
                }
            }
            if (!in_aux.equals(sets.get(i).getIn()) || !out_aux.equals(sets.get(i).getOut())) {
                changed = true;
            }
        } while (changed);
    }
    public void execute() {
        for (Instruction i : method.getInstructions()) {
            computeUsesDefs(i);
        }
        computeInOut();

        print();
    }

    private void computeUsesDefs(Instruction inst) {
        switch (inst.getInstType()) {
            case ASSIGN -> {
                AssignInstruction assignInst = (AssignInstruction) inst;
                System.out.println("----------");
                System.out.println(assignInst.getDest().toString());
                System.out.println("----------");
                sets.get(inst).getDef().add(getVar(assignInst.getDest()));
                // uses ...
                sets.get(inst).getUse().addAll(computeUses(assignInst.getRhs()));
                System.out.println("-----RHS--------");
                System.out.println(sets.get(inst).getUse());
                System.out.println("-----------------");
            }
            case CALL, UNARYOPER ->
                sets.get(inst).getUse().addAll(computeUses(inst));

            case BRANCH -> {
                CondBranchInstruction branch = (CondBranchInstruction) inst;
                sets.get(inst).getUse().addAll(computeUses(branch.getCondition()));
            }
            case RETURN -> {
                sets.get(inst).getUse().addAll(computeUses(inst));
                System.out.println("----RETURN SET------");
                System.out.println(sets.get(inst).getUse());
                System.out.println("--------------------");
            }
            case GETFIELD -> {
                sets.get(inst).getUse().addAll(computeUses(inst));
            }
            case PUTFIELD -> {
                sets.get(inst).getUse().addAll(computeUses(inst));
            }
            case BINARYOPER -> {
                sets.get(inst).getUse().addAll(computeUses(inst));
                System.out.println("----BINOP SET------");
                System.out.println(sets.get(inst).getUse());
                System.out.println("--------------------");
            }
            default -> {
            }
        }

    }
    private void addElement(Element element, Set<String> uses) {
        String var = getVar(element);
        if (var != null) {
            uses.add(var);
        }
    }
    private Set<String> computeUses(Instruction inst) {
        Set<String> uses = new HashSet<>();
        switch (inst.getInstType()) {
            case CALL -> {
                CallInstruction call = (CallInstruction) inst;
                String var = getVar(call.getFirstArg());
                if (var != null) {
                    uses.add(var);
                }
                for (Element e : call.getListOfOperands()) {
                    var = getVar(e);
                    if (var != null) {
                        uses.add(var);
                    }
                }
                return uses;

            }
            case NOPER -> {
                SingleOpInstruction singleOpInstruction = (SingleOpInstruction) inst;
                System.out.println("----NOPER------");
                System.out.println(singleOpInstruction.getSingleOperand());
                System.out.println("---------------");
                addElement(singleOpInstruction.getSingleOperand(), uses);

                return uses;
            }
            case BRANCH -> {
                CondBranchInstruction branch = (CondBranchInstruction) inst;
                return computeUses(branch.getCondition());
            }
            case RETURN -> {
                ReturnInstruction returnInstruction = (ReturnInstruction) inst;
                System.out.println("-----RETURN-------");
                System.out.println(returnInstruction.getOperand());
                System.out.println("------------------");
                addElement(returnInstruction.getOperand(),uses);
                return uses;
            }
            case GETFIELD -> {
                GetFieldInstruction getField = (GetFieldInstruction) inst;
                addElement(getField.getFirstOperand(), uses);
                return uses;
            }
            case PUTFIELD -> {
                PutFieldInstruction putField = (PutFieldInstruction) inst;
                addElement(putField.getFirstOperand(), uses);
                addElement(putField.getThirdOperand(), uses);
                return uses;
            }
            case UNARYOPER -> {
                SingleOpInstruction unaryOp = (SingleOpInstruction) inst;
                addElement(unaryOp.getSingleOperand(), uses);
                return uses;
            }
            case BINARYOPER -> {
                BinaryOpInstruction biInst = (BinaryOpInstruction) inst;
                System.out.println("-----BINOP-------");
                System.out.println(biInst.getLeftOperand());
                System.out.println(biInst.getRightOperand());
                System.out.println("------------------");
                addElement(biInst.getLeftOperand(), uses);
                addElement(biInst.getRightOperand(), uses);
                return uses;
            }
            default -> {
            }
        }
        return uses;
    }

    public String getVar(Element element) {
        if (element.toString().contains("Operand") && isLocal(element)) {
            return ( (Operand) element ).getName();
        }
        return null;
    }

    private boolean isLocal(Element element) {
        List<Symbol> vars = this.symbolTable.getLocalVariables(method.getMethodName());
        String name = ((Operand) element).getName();
        if (vars != null)
           for (Symbol s : vars)
               if (s.getName().equals( name )) return true;
        //check if it's temp register
        String firstChar = name.substring(0, 1);
        String remainingChars = name.substring(1);
        return firstChar.equals("t") && remainingChars.matches("-?\\d+");
    }

}
