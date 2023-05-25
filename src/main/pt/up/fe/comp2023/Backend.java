package pt.up.fe.comp2023;

import org.specs.comp.ollir.*;
import pt.up.fe.comp.jmm.jasmin.JasminBackend;
import pt.up.fe.comp.jmm.jasmin.JasminResult;
import pt.up.fe.comp.jmm.ollir.OllirResult;
import pt.up.fe.comp2023.backend.StackSize;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

public class Backend implements JasminBackend {
    private String superClass = "java/lang/Object";
    private ClassUnit ollirClass = null;
    private final StringBuilder jasminCode = new StringBuilder();
    private int labelCounter = 0;

    @Override
    public JasminResult toJasmin(OllirResult ollirResult) {
        this.ollirClass = ollirResult.getOllirClass();

        buildClass();
        buildSuper();
        buildFields();
        buildMethods();

        return new JasminResult(jasminCode.toString());
    }

    private void buildClass() {
        jasminCode.append(".class ")
            .append(accessModifierToString(ollirClass.getClassAccessModifier()))
            .append(" ")
            .append(ollirClass.getClassName())
            .append("\n");
    }

    private void buildSuper() {
        this.superClass = ollirClass.getSuperClass() == null ? "java/lang/Object" : fullClassName(ollirClass.getSuperClass());
        jasminCode.append(".super ")
            .append(this.superClass)
            .append("\n");
    }

    private void buildFields() {
        for (Field field : ollirClass.getFields()) {
            buildField(field);
        }
    }

    private void buildField(Field field) {
        jasminCode.append(".field ")
            .append(accessModifierToString(field.getFieldAccessModifier()))
            .append(" '")
            .append(field.getFieldName())
            .append("' ")
            .append(typeToString(field.getFieldType()))
            .append("\n");
    }

    private void buildMethods() {
        for (Method method : ollirClass.getMethods()) {
            if (method.isConstructMethod()) buildConstructor();
            else buildMethod(method);
        }
    }

    private void buildMethod(Method method) {
        StringBuilder methodCode = new StringBuilder();
        methodCode.append(".method ");

        methodCode.append(accessModifierToString(method.getMethodAccessModifier()))
            .append(" ");

        if (method.isStaticMethod()) methodCode.append("static ");
        if (method.isFinalMethod()) methodCode.append("final ");

        methodCode.append(method.getMethodName()).append("(");

        for (Element elem : method.getParams()) {
            methodCode.append(typeToString(elem.getType()));
        }

        methodCode.append(")")
            .append(typeToString(method.getReturnType()))
            .append("\n");

        methodCode.append("\t.limit stack 99\n")
            .append("\t.limit locals 99\n");

        StackSize stackSize = new StackSize();
        AtomicInteger nLocalVars = new AtomicInteger(0);
        Map<String, Integer> localVars = new HashMap<>();
        for (Element elem : method.getParams()) {
            nLocalVars.set(nLocalVars.intValue() + 1);
            localVars.put(((Operand) elem).getName(), nLocalVars.intValue());
        }
        for (Instruction instruction : method.getInstructions()) {
            for (String label : method.getLabels(instruction)) {
                methodCode.append(label)
                        .append(":\n");
            }
            buildInstruction(methodCode, instruction, nLocalVars, localVars, stackSize, true);
        }

        // If method does not contain return instruction,
        // manually add it, returning void.
        if (method.getInstructions().size() == 0 ||  method.getInstructions().get(method.getInstructions().size() - 1).getInstType() != InstructionType.RETURN) {
            methodCode.append("\treturn\n");
        }
        methodCode.append(".end method\n");

        jasminCode.append(methodCode.toString().replace("limit locals 99", "limit locals " + (nLocalVars.intValue() + 1))
                .replace("limit stack 99", "limit stack " + stackSize.getMaxSize()));
    }

    private void buildConstructor() {
        jasminCode.append(".method ").append(accessModifierToString(ollirClass.getClassAccessModifier())).append(" <init>()V\n")
            .append("\taload_0\n")
            .append("\tinvokespecial ").append(this.superClass).append("/<init>()V\n")
            .append("\treturn\n")
            .append(".end method\n");
    }

    private void buildInstruction(StringBuilder methodCode, Instruction instruction, AtomicInteger nLocalVars, Map<String, Integer> localVars, StackSize stackSize, Boolean pop) {
        instruction.show();
        switch (instruction.getInstType()) {
            case ASSIGN -> buildAssignInstruction(methodCode, (AssignInstruction) instruction, nLocalVars, localVars, stackSize);
            case CALL -> buildCallInstruction(methodCode, (CallInstruction) instruction, localVars, stackSize, pop);
            case GOTO -> buildGotoInstruction(methodCode, (GotoInstruction) instruction);
            case NOPER -> buildNoperInstruction(methodCode, (SingleOpInstruction) instruction, localVars, stackSize);
            case BRANCH -> buildBranchInstruction(methodCode, (CondBranchInstruction) instruction, nLocalVars, localVars, stackSize);
            case RETURN -> buildReturnInstruction(methodCode, (ReturnInstruction) instruction, localVars, stackSize);
            case GETFIELD -> buildGetFieldInstruction(methodCode, (GetFieldInstruction) instruction, localVars, stackSize);
            case PUTFIELD -> buildPutFieldInstruction(methodCode, (PutFieldInstruction) instruction, localVars, stackSize);
            case UNARYOPER -> buildUnaryOperInstruction(methodCode, (UnaryOpInstruction) instruction, localVars, stackSize);
            case BINARYOPER -> buildBinaryOperInstruction(methodCode, (BinaryOpInstruction) instruction, localVars, stackSize);
        }
    }

    private void buildAssignInstruction(StringBuilder methodCode, AssignInstruction instruction, AtomicInteger currVars, Map<String, Integer> localVars, StackSize stackSize) {
        Integer variable = localVars.get(((Operand) instruction.getDest()).getName());
        if (variable == null) { // variable not previously used
            currVars.set(currVars.intValue() + 1);
            localVars.put(((Operand) instruction.getDest()).getName(), currVars.intValue());
        }

        // checking for iinc
        if (checkIinc(instruction)) {
            methodCode.append("\tiinc ")
                    .append(variable)
                    .append(" ")
                    .append(getIinc(instruction))
                    .append("\n");
            return;
        }

        // execute right side of assignment,
        // this way the resulting value should
        // be at the top of the stack
        buildInstruction(methodCode, instruction.getRhs(), currVars, localVars, stackSize, false);

        // store top of the stack in the local variable
        buildStore(methodCode, typePrefix(instruction.getDest().getType()), localVars, ((Operand) instruction.getDest()).getName());
        stackSize.decreaseSize(1);
    }
    private void buildCallInstruction(StringBuilder methodCode, CallInstruction instruction, Map<String, Integer> localVariables, StackSize stackSize, Boolean pop) {
        switch (instruction.getInvocationType()) {
            case NEW -> {
                methodCode.append("\tnew ")
                        .append(fullClassName((Operand) instruction.getFirstArg())).append("\n");
                stackSize.increaseSize(1);
            }
            case invokespecial -> {
                buildLoad(methodCode, instruction.getFirstArg(), localVariables);
                stackSize.increaseSize(1);
                int decreaseSize = 1;
                for (Element elem : instruction.getListOfOperands()) {
                    buildLoad(methodCode, elem, localVariables);
                    stackSize.increaseSize(1);
                    decreaseSize++;
                }
                methodCode.append("\tinvokespecial ")
                        .append(fullClassName(((Operand) instruction.getFirstArg()))).append("/")
                        .append(((LiteralElement) instruction.getSecondArg()).getLiteral().replace("\"", ""));
                methodCode.append("(");
                for (Element element : instruction.getListOfOperands()) {
                    methodCode.append(typeToString(element.getType()));
                }
                methodCode.append(")")
                        .append(typeToString(instruction.getReturnType()))
                        .append("\n");
                stackSize.decreaseSize(decreaseSize);
                stackSize.increaseSize(1);

                // deal with pop
                if (pop && instruction.getReturnType().getTypeOfElement() != ElementType.VOID) {
                    methodCode.append("\tpop\n");
                    stackSize.decreaseSize(1);
                }
            }
            case invokevirtual -> {
                buildLoad(methodCode, instruction.getFirstArg(), localVariables);
                stackSize.increaseSize(1);
                int decreaseSize = 1;
                for (Element elem : instruction.getListOfOperands()) {
                    buildLoad(methodCode, elem, localVariables);
                    stackSize.increaseSize(1);
                    decreaseSize++;
                }
                methodCode.append("\tinvokevirtual ")
                        .append(fullClassName(((Operand) instruction.getFirstArg()))).append("/")
                        .append(((LiteralElement) instruction.getSecondArg()).getLiteral().replace("\"", ""));
                methodCode.append("(");
                for (Element element : instruction.getListOfOperands()) {
                    methodCode.append(typeToString(element.getType()));
                }
                methodCode.append(")")
                        .append(typeToString(instruction.getReturnType()))
                        .append("\n");
                stackSize.decreaseSize(decreaseSize);
                stackSize.increaseSize(1);

                // deal with pop
                if (pop && instruction.getReturnType().getTypeOfElement() != ElementType.VOID) {
                    methodCode.append("\tpop\n");
                    stackSize.decreaseSize(1);
                }
            }
            case invokestatic -> {
                int decreaseSize = 0;
                for (Element elem : instruction.getListOfOperands()) {
                    buildLoad(methodCode, elem, localVariables);
                    stackSize.increaseSize(1);
                    decreaseSize++;
                }
                methodCode.append("\tinvokestatic ")
                        .append(fullClassName(((Operand) instruction.getFirstArg()))).append("/")
                        .append(((LiteralElement) instruction.getSecondArg()).getLiteral().replace("\"", ""));
                methodCode.append("(");
                for (Element element : instruction.getListOfOperands()) {
                    methodCode.append(typeToString(element.getType()));
                }
                methodCode.append(")")
                        .append(typeToString(instruction.getReturnType()))
                        .append("\n");
                stackSize.decreaseSize(decreaseSize);
                stackSize.increaseSize(1);

                // deal with pop
                if (pop && instruction.getReturnType().getTypeOfElement() != ElementType.VOID) {
                    methodCode.append("\tpop\n");
                    stackSize.decreaseSize(1);
                }
            }
            case invokeinterface -> {
                int decreaseSize = 0;
                for (Element elem : instruction.getListOfOperands()) {
                    buildLoad(methodCode, elem, localVariables);
                    stackSize.increaseSize(1);
                    decreaseSize++;
                }
                methodCode.append("\tinvokeinterface ")
                        .append(fullClassName(((Operand) instruction.getFirstArg()))).append("/")
                        .append(((LiteralElement) instruction.getSecondArg()).getLiteral().replace("\"", ""));
                methodCode.append("(");
                for (Element element : instruction.getListOfOperands()) {
                    methodCode.append(typeToString(element.getType()));
                }
                methodCode.append(")")
                        .append(typeToString(instruction.getReturnType()))
                        .append("\n");
                stackSize.decreaseSize(decreaseSize);
                stackSize.increaseSize(1);

                // deal with pop
                if (pop && instruction.getReturnType().getTypeOfElement() != ElementType.VOID) {
                    methodCode.append("\tpop\n");
                    stackSize.decreaseSize(1);
                }
            }
        }
    }
    private void buildGotoInstruction(StringBuilder methodCode, GotoInstruction instruction) {
        methodCode.append("\tgoto ")
                .append(instruction.getLabel())
                .append("\n");
    }
    private void buildNoperInstruction(StringBuilder methodCode, SingleOpInstruction instruction, Map<String, Integer> localVariables, StackSize stackSize) {
        buildLoad(methodCode, instruction.getSingleOperand(), localVariables);
        stackSize.increaseSize(1);
    }
    private void buildBranchInstruction(StringBuilder methodCode, CondBranchInstruction instruction, AtomicInteger currVars, Map<String, Integer> localVars, StackSize stackSize) {
        buildInstruction(methodCode, instruction.getCondition(), currVars, localVars, stackSize, false);
        methodCode.append("\tifne ")
                .append(instruction.getLabel())
                .append("\n");
    }
    private void buildReturnInstruction(StringBuilder methodCode, ReturnInstruction instruction, Map<String, Integer> localVariables, StackSize stackSize) {
        if (instruction.hasReturnValue()) {
            buildLoad(methodCode, instruction.getOperand(), localVariables);
            stackSize.increaseSize(1);
            methodCode.append("\t")
                    .append(typePrefix(instruction.getOperand().getType()));
        } else {
            methodCode.append("\t");
        }
        methodCode.append("return\n");
    }
    private void buildGetFieldInstruction(StringBuilder methodCode, GetFieldInstruction instruction, Map<String, Integer> localVariables, StackSize stackSize) {
        buildLoad(methodCode, instruction.getFirstOperand(), localVariables);
        stackSize.increaseSize(1);

        methodCode.append("\tgetfield ")
                .append(fullClassName(((Operand) instruction.getFirstOperand()).getName()))
                .append("/").append(((Operand) instruction.getSecondOperand()).getName()).append(" ")
                .append(typeToString(instruction.getSecondOperand().getType())).append("\n");
        stackSize.decreaseSize(1);
        stackSize.increaseSize(1);
    }
    private void buildPutFieldInstruction(StringBuilder methodCode, PutFieldInstruction instruction, Map<String, Integer> localVariables, StackSize stackSize) {
        buildLoad(methodCode, instruction.getFirstOperand(), localVariables);
        buildLoad(methodCode, instruction.getThirdOperand(), localVariables);
        stackSize.increaseSize(2);

        methodCode.append("\tputfield ")
                .append(fullClassName(((Operand) instruction.getFirstOperand()).getName()))
                .append("/").append(((Operand) instruction.getSecondOperand()).getName()).append(" ")
                .append(typeToString(instruction.getSecondOperand().getType())).append("\n");
        stackSize.decreaseSize(2);
    }
    private void buildUnaryOperInstruction(StringBuilder methodCode, UnaryOpInstruction instruction, Map<String, Integer> localVariables, StackSize stackSize) {
        if (instruction.getOperation().getOpType() == OperationType.NOTB) { // Only supported unary operation
            buildLoad(methodCode, instruction.getOperand(), localVariables);
            stackSize.increaseSize(1);
            // negate the boolean value by XORing it with 1
            methodCode.append("\ticonst_1\n")
                    .append("\tixor\n");
        } else {
            throw new IllegalArgumentException("Operation type " + instruction.getOperation().getOpType() + " is not supported for unary operation");
        }
    }
    private void buildBinaryOperInstruction(StringBuilder methodCode, BinaryOpInstruction instruction, Map<String, Integer> localVariables, StackSize stackSize) {
        buildLoad(methodCode, instruction.getLeftOperand(), localVariables);
        buildLoad(methodCode, instruction.getRightOperand(), localVariables);
        stackSize.increaseSize(2);
        switch (instruction.getOperation().getOpType()) {
            case MUL -> methodCode.append("\timul\n");
            case DIV -> methodCode.append("\tidiv\n");
            case ADD -> methodCode.append("\tiadd\n");
            case SUB -> methodCode.append("\tisub\n");
            case LTH -> {
                methodCode.append("\tif_icmplt true").append(labelCounter).append("\n")
                        .append("\ticonst_0\n")
                        .append("\tgoto end").append(labelCounter).append("\n")
                        .append("true").append(labelCounter).append(":\n")
                        .append("\ticonst_1\n")
                        .append("end").append(labelCounter).append(":\n");
                labelCounter++;
            }
            case ANDB -> methodCode.append("\tiand\n");
        }
        stackSize.decreaseSize(1);
    }
    private void buildLoad(StringBuilder methodCode, Element element, Map<String, Integer> localVariables) {
        if (element.isLiteral()) {
            int literalValue = Integer.parseInt(((LiteralElement) element).getLiteral());
            if (literalValue == -1) {
                methodCode.append("\ticonst_m1\n");
            } else if (literalValue >= 0 && literalValue <= 5) {
                methodCode.append("\ticonst_").append(literalValue).append("\n");
            } else if (literalValue >= -128 && literalValue <= 127) {
                methodCode.append("\tbipush ").append(literalValue).append("\n");
            } else if (literalValue >= -32768 && literalValue <= 32767) {
                methodCode.append("\tsipush ").append(literalValue).append("\n");
            } else {
                methodCode.append("\tldc ").append(literalValue).append("\n");
            }
        } else if (element.getType().getTypeOfElement() == ElementType.THIS ||
                (element.getType().getTypeOfElement() == ElementType.OBJECTREF &&
                ((Operand) element).getName().equals("this"))) {
            methodCode.append("\taload_0\n");
        } else {
            int varIndex = localVariables.get(((Operand) element).getName());
            if (varIndex >= 0 && varIndex <= 3) {
                methodCode.append("\t")
                        .append(typePrefix(element.getType()))
                        .append("load_").append(varIndex)
                        .append("\n");
            } else {
                methodCode.append("\t")
                        .append(typePrefix(element.getType()))
                        .append("load ").append(varIndex)
                        .append("\n");
            }
        }
    }


    private void buildStore(StringBuilder methodCode, String prefix, Map<String, Integer> localVariables, String variableName) {
        int varIndex = localVariables.get(variableName);
        if (varIndex >= 0 && varIndex <= 3) {
            // use the low-cost instruction if the variable index is between 0 and 3
            methodCode.append("\t").append(prefix).append("store_").append(varIndex).append("\n");
        } else {
            methodCode.append("\t").append(prefix).append("store ").append(varIndex).append("\n");
        }
    }

    private String fullClassName(Operand operand) {
        if (Objects.equals(operand.getName(), "this")) ollirClass.getClassName();
        if (operand.getType().getTypeOfElement() == ElementType.CLASS) return fullClassName(operand.getName());
        return fullClassName(((ClassType) operand.getType()).getName());
    }

    private String fullClassName(String className) {
        if (Objects.equals(className, "this")) return ollirClass.getClassName();
        for (String imp : ollirClass.getImports()) {
            if (imp.endsWith(className)) return imp.replace('.', '/');
        }
        return className;
    }

    private String accessModifierToString(AccessModifiers accessModifiers) {
        return accessModifiers == AccessModifiers.DEFAULT ? "public" : accessModifiers.toString().toLowerCase();
    }

    private String typeToString(Type type) {
        return switch (type.getTypeOfElement()) {
            case INT32 -> "I";
            case BOOLEAN -> "Z";
            case ARRAYREF -> "[" + typeToString(((ArrayType) type).getElementType());
            case CLASS, OBJECTREF -> "L" + ((ClassType) type).getName() + ";";
            case THIS -> "L" + ollirClass.getClassName();
            case STRING -> "Ljava/lang/String;";
            case VOID -> "V";
        };
    }

    private String typePrefix(Type type) {
        return switch (type.getTypeOfElement()) {
            case INT32, BOOLEAN -> "i";
            default -> "a";
        };
    }

    private boolean checkIinc(AssignInstruction instruction) {
        if (instruction.getRhs().getInstType() == InstructionType.BINARYOPER) {

            BinaryOpInstruction rhs = (BinaryOpInstruction) instruction.getRhs();

            // check if operation is either an add or sub
            if (rhs.getOperation().getOpType() != OperationType.ADD && rhs.getOperation().getOpType() != OperationType.SUB) return false;

            // check if rhs contains the dest variable and a literal
            if (!rhs.getLeftOperand().isLiteral() && ((Operand) rhs.getLeftOperand()).getName().equals(((Operand) instruction.getDest()).getName()))
                return rhs.getRightOperand().isLiteral() && (Integer.parseInt(((LiteralElement) rhs.getRightOperand()).getLiteral()) >> 8) == 0;
            else if (rhs.getOperation().getOpType() == OperationType.ADD && !rhs.getRightOperand().isLiteral() && ((Operand) rhs.getRightOperand()).getName().equals(((Operand) instruction.getDest()).getName()))
                return rhs.getLeftOperand().isLiteral() && (Integer.parseInt(((LiteralElement) rhs.getLeftOperand()).getLiteral()) >> 8) == 0;
            else return false;
        } else return false;
    }

    private String getIinc(AssignInstruction instruction) {
        BinaryOpInstruction rhs = (BinaryOpInstruction) instruction.getRhs();
        String sign = rhs.getOperation().getOpType() == OperationType.ADD ? "" : "-";
        if (!rhs.getLeftOperand().isLiteral() && ((Operand) rhs.getLeftOperand()).getName().equals(((Operand) instruction.getDest()).getName()))
            return sign + ((LiteralElement) rhs.getRightOperand()).getLiteral();
        else
            return sign + ((LiteralElement) rhs.getLeftOperand()).getLiteral();
    }
}
