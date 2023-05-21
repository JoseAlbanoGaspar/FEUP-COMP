package pt.up.fe.comp2023.optimization.registerAllocation;

import org.specs.comp.ollir.*;

import java.util.*;

public class LivenessAnalysis {
    protected HashMap<Instruction, LivenessSets> sets;
    private Method method;

    public LivenessAnalysis(Method method) {
        this.sets = new HashMap<>();
        this.method = method;
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
        System.out.println("|||||LIVENESS FOR METHOD " + this.method.getMethodName() + " |||||||||||||||");
        for (Map.Entry<Instruction, LivenessSets> entry : sets.entrySet()) {
            System.out.println("-----Entry Set-----");
            System.out.println(entry.getKey());
            System.out.println("Use: " + entry.getValue().getUse());
            System.out.println("Def: " + entry.getValue().getDef());
            System.out.println("Ins: " + entry.getValue().getIn());
            System.out.println("Out: " + entry.getValue().getOut());
            System.out.println("-------------------");
        }
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
            if (!in_aux.equals(sets.get(i).getIn()) || !out_aux.equals(sets.get(i).getOut())){
                changed = true;
            }
        } while (changed);
    }
    public void execute() {
        for (Instruction i : method.getInstructions()) {
            computeUsesDefs(i);
        }
        //print();
        computeInOut();

        print();
    }

    private void computeUsesDefs(Instruction inst) {
        switch (inst.getInstType()) {
            case ASSIGN: {
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
                break;
            }
            case CALL: {
                break;
            }
            case GOTO: {
                break;
            }
            case BRANCH: {
                break;
            }
            case RETURN: {
                sets.get(inst).getUse().addAll(computeUses(inst));
                System.out.println("----RETURN SET------");
                System.out.println(sets.get(inst).getUse());
                System.out.println("--------------------");
                break;
            }
            case GETFIELD: {
                break;
            }
            case PUTFIELD: {
                break;
            }
            case UNARYOPER: {
                break;
            }
            case BINARYOPER: {
                sets.get(inst).getUse().addAll(computeUses(inst));
                System.out.println("----BINOP SET------");
                System.out.println(sets.get(inst).getUse());
                System.out.println("--------------------");
                break;
            }

            default: {
                break;
            }
        }

    }

    private Set<String> computeUses(Instruction inst) {
        Set<String> uses = new HashSet<>();
        switch (inst.getInstType()) {
            case CALL: {
                break;
            }
            case GOTO: {
                break;
            }
            case NOPER:  {
                SingleOpInstruction singleOpInstruction = (SingleOpInstruction) inst;
                System.out.println("----NOPER------");
                System.out.println(singleOpInstruction.getSingleOperand());
                System.out.println("---------------");
                String var = getVar(singleOpInstruction.getSingleOperand());
                if (var != null) {
                    uses.add(var);
                }
                return uses;
            }
            case BRANCH: {
                break;
            }
            case RETURN: {
                ReturnInstruction returnInstruction = (ReturnInstruction) inst;
                System.out.println("-----RETURN-------");
                System.out.println(returnInstruction.getOperand());
                System.out.println("------------------");
                String var = getVar(returnInstruction.getOperand());
                if (var != null) {
                    uses.add(var);
                }
                return uses;
            }
            case GETFIELD: {
                break;
            }
            case PUTFIELD: {
                break;
            }
            case UNARYOPER: {
                break;
            }
            case BINARYOPER: {
                BinaryOpInstruction biInst = (BinaryOpInstruction) inst;
                String var = getVar(biInst.getLeftOperand());
                String var2 = getVar(biInst.getRightOperand());
                System.out.println("-----BINOP-------");
                System.out.println(biInst.getLeftOperand());
                System.out.println(biInst.getRightOperand());
                System.out.println("------------------");
                if (var != null) {
                    uses.add(var);
                }
                if (var2 != null) {
                    uses.add(var2);
                }
                return uses;
            }

            default: {
                break;
            }
        }
        return uses;
    }

    public String getVar(Element element) {
        String[] split = element.toString().split("\\.");
        if (element.toString().contains("Operand"))
            return split[0].substring(9);
        return null;
    }

}
