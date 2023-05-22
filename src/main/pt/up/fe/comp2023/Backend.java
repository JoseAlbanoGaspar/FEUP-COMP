package pt.up.fe.comp2023;

import org.specs.comp.ollir.*;
import pt.up.fe.comp.jmm.jasmin.JasminBackend;
import pt.up.fe.comp.jmm.jasmin.JasminResult;
import pt.up.fe.comp.jmm.ollir.OllirResult;

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
        this.superClass = superClass == null ? "java/lang/Object" : fullClassName(superClass);
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
        jasminCode.append(".method ");

        jasminCode.append(accessModifierToString(method.getMethodAccessModifier()))
            .append(" ");

        if (method.isStaticMethod()) jasminCode.append("static ");
        if (method.isFinalMethod()) jasminCode.append("final ");

        jasminCode.append(method.getMethodName()).append("(");

        for (Element elem : method.getParams()) {
            jasminCode.append(typeToString(elem.getType()));
        }

        jasminCode.append(")")
            .append(typeToString(method.getReturnType()))
            .append("\n");

        jasminCode.append("\t.limit stack 99\n")
            .append("\t.limit locals 99\n");

        AtomicInteger nLocalVars = new AtomicInteger(0);
        Map<String, Integer> localVars = new HashMap<>();
        for (Element elem : method.getParams()) {
            nLocalVars.set(nLocalVars.intValue() + 1);
            localVars.put(((Operand) elem).getName(), nLocalVars.intValue());
        }
        for (Instruction instruction : method.getInstructions()) {
            for (String label : method.getLabels(instruction)) {
                jasminCode.append(label)
                        .append(":\n");
            }
            buildInstruction(instruction, nLocalVars, localVars, true);
        }

        // If method does not contain return instruction,
        // manually add it, returning void.
        if (method.getInstructions().size() == 0 ||  method.getInstructions().get(method.getInstructions().size() - 1).getInstType() != InstructionType.RETURN) {
            jasminCode.append("\treturn\n");
        }
        jasminCode.append(".end method\n");
    }

    private void buildConstructor() {
        jasminCode.append(".method ").append(accessModifierToString(ollirClass.getClassAccessModifier())).append(" <init>()V\n")
            .append("\taload_0\n")
            .append("\tinvokespecial ").append(this.superClass).append("/<init>()V\n")
            .append("\treturn\n")
            .append(".end method\n");
    }

    private void buildInstruction(Instruction instruction, AtomicInteger nLocalVars, Map<String, Integer> localVars, Boolean pop) {
        instruction.show();
        switch (instruction.getInstType()) {
            case ASSIGN -> buildAssignInstruction((AssignInstruction) instruction, nLocalVars, localVars);
            case CALL -> buildCallInstruction((CallInstruction) instruction, localVars, pop);
            case GOTO -> buildGotoInstruction((GotoInstruction) instruction);
            case NOPER -> buildNoperInstruction((SingleOpInstruction) instruction, localVars);
            case BRANCH -> buildBranchInstruction((CondBranchInstruction) instruction, nLocalVars, localVars);
            case RETURN -> buildReturnInstruction((ReturnInstruction) instruction, localVars);
            case GETFIELD -> buildGetFieldInstruction((GetFieldInstruction) instruction, localVars);
            case PUTFIELD -> buildPutFieldInstruction((PutFieldInstruction) instruction, localVars);
            case UNARYOPER -> buildUnaryOperInstruction((UnaryOpInstruction) instruction, localVars);
            case BINARYOPER -> buildBinaryOperInstruction((BinaryOpInstruction) instruction, localVars);
        }
    }

    private void buildAssignInstruction(AssignInstruction instruction, AtomicInteger currVars, Map<String, Integer> localVars) {
        Integer variable = localVars.get(((Operand) instruction.getDest()).getName());
        if (variable == null) { // variable not previously used
            currVars.set(currVars.intValue() + 1);
            localVars.put(((Operand) instruction.getDest()).getName(), currVars.intValue());
        }
        // execute right side of assignment,
        // this way the resulting value should
        // be at the top of the stack
        buildInstruction(instruction.getRhs(), currVars, localVars, false);

        // store top of the stack in the local variable
        buildStore(typePrefix(instruction.getDest().getType()), localVars, ((Operand) instruction.getDest()).getName());
    }
    private void buildCallInstruction(CallInstruction instruction, Map<String, Integer> localVariables, Boolean pop) {
        switch (instruction.getInvocationType()) {
            case NEW -> jasminCode.append("\tnew ")
                    .append(fullClassName((Operand) instruction.getFirstArg())).append("\n");
            case invokespecial -> {
                buildLoad(instruction.getFirstArg(), localVariables);
                for (Element elem : instruction.getListOfOperands()) {
                    buildLoad(elem, localVariables);
                }
                jasminCode.append("\tinvokespecial ")
                        .append(fullClassName(((Operand) instruction.getFirstArg()))).append("/")
                        .append(((LiteralElement) instruction.getSecondArg()).getLiteral().replace("\"", ""));
                jasminCode.append("(");
                for (Element element : instruction.getListOfOperands()) {
                    jasminCode.append(typeToString(element.getType()));
                }
                jasminCode.append(")")
                        .append(typeToString(instruction.getReturnType()))
                        .append("\n");

                // deal with pop
                if (pop && instruction.getReturnType().getTypeOfElement() != ElementType.VOID) {
                    jasminCode.append("\tpop\n");
                }
            }
            case invokevirtual -> {
                buildLoad(instruction.getFirstArg(), localVariables);
                for (Element elem : instruction.getListOfOperands()) {
                    buildLoad(elem, localVariables);
                }
                jasminCode.append("\tinvokevirtual ")
                        .append(fullClassName(((Operand) instruction.getFirstArg()))).append("/")
                        .append(((LiteralElement) instruction.getSecondArg()).getLiteral().replace("\"", ""));
                jasminCode.append("(");
                for (Element element : instruction.getListOfOperands()) {
                    jasminCode.append(typeToString(element.getType()));
                }
                jasminCode.append(")")
                        .append(typeToString(instruction.getReturnType()))
                        .append("\n");

                // deal with pop
                if (pop && instruction.getReturnType().getTypeOfElement() != ElementType.VOID) {
                    jasminCode.append("\tpop\n");
                }
            }
            case invokestatic -> {
                for (Element elem : instruction.getListOfOperands()) {
                    buildLoad(elem, localVariables);
                }
                jasminCode.append("\tinvokestatic ")
                        .append(fullClassName(((Operand) instruction.getFirstArg()))).append("/")
                        .append(((LiteralElement) instruction.getSecondArg()).getLiteral().replace("\"", ""));
                jasminCode.append("(");
                for (Element element : instruction.getListOfOperands()) {
                    jasminCode.append(typeToString(element.getType()));
                }
                jasminCode.append(")")
                        .append(typeToString(instruction.getReturnType()))
                        .append("\n");

                // deal with pop
                if (pop && instruction.getReturnType().getTypeOfElement() != ElementType.VOID) {
                    jasminCode.append("\tpop\n");
                }
            }
            case invokeinterface -> {
                for (Element elem : instruction.getListOfOperands()) {
                    buildLoad(elem, localVariables);
                }
                jasminCode.append("\tinvokeinterface ")
                        .append(fullClassName(((Operand) instruction.getFirstArg()))).append("/")
                        .append(((LiteralElement) instruction.getSecondArg()).getLiteral().replace("\"", ""));
                jasminCode.append("(");
                for (Element element : instruction.getListOfOperands()) {
                    jasminCode.append(typeToString(element.getType()));
                }
                jasminCode.append(")")
                        .append(typeToString(instruction.getReturnType()))
                        .append("\n");

                // deal with pop
                if (pop && instruction.getReturnType().getTypeOfElement() != ElementType.VOID) {
                    jasminCode.append("\tpop\n");
                }
            }
        }
    }
    private void buildGotoInstruction(GotoInstruction instruction) {
        jasminCode.append("\tgoto ")
                .append(instruction.getLabel())
                .append("\n");
    }
    private void buildNoperInstruction(SingleOpInstruction instruction, Map<String, Integer> localVariables) {
        buildLoad(instruction.getSingleOperand(), localVariables);
    }
    private void buildBranchInstruction(CondBranchInstruction instruction, AtomicInteger currVars, Map<String, Integer> localVars) {
        buildInstruction(instruction.getCondition(), currVars, localVars, false);
        jasminCode.append("\tifne ")
                .append(instruction.getLabel())
                .append("\n");
    }
    private void buildReturnInstruction(ReturnInstruction instruction, Map<String, Integer> localVariables) {
        if (instruction.hasReturnValue()) {
            buildLoad(instruction.getOperand(), localVariables);
            jasminCode.append("\t")
                    .append(typePrefix(instruction.getOperand().getType()));
        } else {
            jasminCode.append("\t");
        }
        jasminCode.append("return\n");
    }
    private void buildGetFieldInstruction(GetFieldInstruction instruction, Map<String, Integer> localVariables) {
        buildLoad(instruction.getFirstOperand(), localVariables);

        jasminCode.append("\tgetfield ")
                .append(fullClassName(((Operand) instruction.getFirstOperand()).getName()))
                .append("/").append(((Operand) instruction.getSecondOperand()).getName()).append(" ")
                .append(typeToString(instruction.getSecondOperand().getType())).append("\n");
    }
    private void buildPutFieldInstruction(PutFieldInstruction instruction, Map<String, Integer> localVariables) {
        buildLoad(instruction.getFirstOperand(), localVariables);
        buildLoad(instruction.getThirdOperand(), localVariables);

        jasminCode.append("\tputfield ")
                .append(fullClassName(((Operand) instruction.getFirstOperand()).getName()))
                .append("/").append(((Operand) instruction.getSecondOperand()).getName()).append(" ")
                .append(typeToString(instruction.getSecondOperand().getType())).append("\n");
    }
    private void buildUnaryOperInstruction(UnaryOpInstruction instruction, Map<String, Integer> localVariables) {
        if (instruction.getOperation().getOpType() == OperationType.NOTB) { // Only supported unary operation
            buildLoad(instruction.getOperand(), localVariables);
            // negate the boolean value by XORing it with 1
            jasminCode.append("\ticonst_1\n")
                    .append("\tixor\n");
        } else {
            throw new IllegalArgumentException("Operation type " + instruction.getOperation().getOpType() + " is not supported for unary operation");
        }
    }
    private void buildBinaryOperInstruction(BinaryOpInstruction instruction, Map<String, Integer> localVariables) {
        buildLoad(instruction.getLeftOperand(), localVariables);
        buildLoad(instruction.getRightOperand(), localVariables);
        switch (instruction.getOperation().getOpType()) {
            case MUL -> jasminCode.append("\timul\n");
            case DIV -> jasminCode.append("\tidiv\n");
            case ADD -> jasminCode.append("\tiadd\n");
            case SUB -> jasminCode.append("\tisub\n");
            case LTH -> {
                jasminCode.append("\tif_icmplt true").append(labelCounter).append("\n")
                        .append("\ticonst_0\n")
                        .append("\tgoto end").append(labelCounter).append("\n")
                        .append("true").append(labelCounter).append(":\n")
                        .append("\ticonst_1\n")
                        .append("end").append(labelCounter).append(":\n");
                labelCounter++;
            }
            case ANDB -> jasminCode.append("\tiand\n");
        }
    }
    private void buildLoad(Element element, Map<String, Integer> localVariables) {
        if (element.isLiteral()) {
            int literalValue = Integer.parseInt(((LiteralElement) element).getLiteral());
            if (literalValue == -1) {
                jasminCode.append("\ticonst_m1\n");
            } else if (literalValue >= 0 && literalValue <= 5) {
                jasminCode.append("\ticonst_").append(literalValue).append("\n");
            } else if (literalValue >= -128 && literalValue <= 127) {
                jasminCode.append("\tbipush ").append(literalValue).append("\n");
            } else if (literalValue >= -32768 && literalValue <= 32767) {
                jasminCode.append("\tsipush ").append(literalValue).append("\n");
            } else {
                jasminCode.append("\tldc ").append(literalValue).append("\n");
            }
        } else if (element.getType().getTypeOfElement() == ElementType.THIS) {
            jasminCode.append("\taload_0\n");
        } else {
            int varIndex = localVariables.get(((Operand) element).getName());
            if (varIndex >= 0 && varIndex <= 3) {
                jasminCode.append("\t")
                        .append(typePrefix(element.getType()))
                        .append("load_").append(varIndex)
                        .append("\n");
            } else {
                jasminCode.append("\t")
                        .append(typePrefix(element.getType()))
                        .append("load ").append(varIndex)
                        .append("\n");
            }
        }
    }


    private void buildStore(String prefix, Map<String, Integer> localVariables, String variableName) {
        int varIndex = localVariables.get(variableName);
        if (varIndex >= 0 && varIndex <= 3) {
            // use the low-cost instruction if the variable index is between 0 and 3
            jasminCode.append("\t").append(prefix).append("store_").append(varIndex).append("\n");
        } else {
            jasminCode.append("\t").append(prefix).append("store ").append(varIndex).append("\n");
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
}
