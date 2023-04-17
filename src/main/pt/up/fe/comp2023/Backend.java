package pt.up.fe.comp2023;

import org.specs.comp.ollir.*;
import pt.up.fe.comp.jmm.jasmin.JasminBackend;
import pt.up.fe.comp.jmm.jasmin.JasminResult;
import pt.up.fe.comp.jmm.ollir.OllirResult;

import java.util.ArrayList;

public class Backend implements JasminBackend {
    private String superClass = "java/lang/Object";
    private ClassUnit ollirClass = null;

    @Override
    public JasminResult toJasmin(OllirResult ollirResult) {
        this.ollirClass = ollirResult.getOllirClass();

        StringBuilder jasminCode = new StringBuilder();

        buildClass(jasminCode);
        buildSuper(jasminCode);
        buildFields(jasminCode);
        buildMethods(jasminCode);

        System.out.println(jasminCode);
        return new JasminResult(jasminCode.toString());
    }

    private void buildClass(StringBuilder code) {
        code.append(".class ")
            .append(accessModifierToString(ollirClass.getClassAccessModifier()))
            .append(" ")
            .append(ollirClass.getClassName())
            .append("\n");
    }

    private void buildSuper(StringBuilder code) {
        this.superClass = superClass == null ? "java/lang/Object" : fullClassName(superClass);
        code.append(".super ")
            .append(this.superClass)
            .append("\n");
    }

    private void buildFields(StringBuilder code) {
        for (Field field : ollirClass.getFields()) {
            buildField(code, field);
        }
    }

    private void buildField(StringBuilder code, Field field) {
        code.append(".field ")
            .append(accessModifierToString(field.getFieldAccessModifier()))
            .append(" '")
            .append(field.getFieldName())
            .append("' ")
            .append(typeToString(field.getFieldType()))
            .append("\n");
    }

    private void buildMethods(StringBuilder code) {
        for (Method method : ollirClass.getMethods()) {
            if (method.isConstructMethod()) buildConstructor(code);
            else buildMethod(code, method);
        }
    }

    private void buildMethod(StringBuilder code, Method method) {
        code.append(".method ");

        if (method.isStaticMethod()) code.append("static ");
        if (method.isFinalMethod()) code.append("final ");

        code.append(accessModifierToString(method.getMethodAccessModifier()))
            .append(" ").append(method.getMethodName()).append("(");

        for (Element elem : method.getParams()) {
            code.append(typeToString(elem.getType()));
        }

        code.append(")")
            .append(typeToString(method.getReturnType()))
            .append("\n");

        code.append("\t.limit stack 99\n")
            .append("\t.limit locals 99\n");

        for (Instruction instruction : method.getInstructions()) {
            buildInstruction(code, instruction);
        }

        // If method does not contain return instruction,
        // manually add it.
        if (method.getInstructions().get(method.getInstructions().size() - 1).getInstType() != InstructionType.RETURN) {
            code.append("\treturn\n");
        }
        code.append(".end method\n");
    }

    private void buildConstructor(StringBuilder code) {
        code.append(".method ").append(accessModifierToString(ollirClass.getClassAccessModifier())).append(" <init>()V\n")
            .append("\taload_0\n")
            .append("\tinvokespecial ").append(this.superClass).append("/<init>()V\n")
            .append("\treturn\n")
            .append(".end method\n");
    }

    private void buildInstruction(StringBuilder code, Instruction instruction) {
        switch (instruction.getInstType()) {
            case ASSIGN:
                buildAssignInstruction(code, (AssignInstruction) instruction);
                break;
            case CALL:
                buildCallInstruction(code, (CallInstruction) instruction);
                break;
            case GOTO:
                buildGotoInstruction(code, (GotoInstruction) instruction);
                break;
            case NOPER:
                buildNoperInstruction(code);
                break;
            case BRANCH:
                buildBranchInstruction(code);
                break;
            case RETURN:
                buildReturnInstruction(code, (ReturnInstruction) instruction);
                break;
            case GETFIELD:
                buildGetFieldInstruction(code, (GetFieldInstruction) instruction);
                break;
            case PUTFIELD:
                buildPutFieldInstruction(code, (PutFieldInstruction) instruction);
                break;
            case UNARYOPER:
                buildUnaryOperInstruction(code, (UnaryOpInstruction) instruction);
                break;
            case BINARYOPER:
                buildBinaryOperInstruction(code, (BinaryOpInstruction) instruction);
                break;
        }
    }

    private void buildAssignInstruction(StringBuilder code, AssignInstruction instruction) {}
    private void buildCallInstruction(StringBuilder code, CallInstruction instruction) {}
    private void buildGotoInstruction(StringBuilder code, GotoInstruction instruction) {}
    private void buildNoperInstruction(StringBuilder code) {}
    private void buildBranchInstruction(StringBuilder code) {}
    private void buildReturnInstruction(StringBuilder code, ReturnInstruction instruction) {}
    private void buildGetFieldInstruction(StringBuilder code, GetFieldInstruction instruction) {}
    private void buildPutFieldInstruction(StringBuilder code, PutFieldInstruction instruction) {}
    private void buildUnaryOperInstruction(StringBuilder code, UnaryOpInstruction instruction) {}
    private void buildBinaryOperInstruction(StringBuilder code, BinaryOpInstruction instruction) {}

    private String fullClassName(String className) {
        for (String imp : ollirClass.getImports()) {
            if (imp.endsWith(className)) return imp.replace('.', '/');
        }
        return className;
    }

    private String accessModifierToString(AccessModifiers accessModifiers) {
        return accessModifiers == AccessModifiers.DEFAULT ? "public" : accessModifiers.toString().toLowerCase();
    }

    private String typeToString(Type type) {
        switch (type.getTypeOfElement()) {
            case INT32:
                return "I";
            case BOOLEAN:
                return "Z";
            case ARRAYREF:
                return "[" + typeToString(((ArrayType)type).getElementType());
            case CLASS: case OBJECTREF:
                return "L" + ((ClassType)type).getName() + ";";
            case THIS:
                return "L" + ollirClass.getClassName();
            case STRING:
                return "Ljava/lang/String;";
            case VOID:
                return "V";
            default: // Should be unreachable
                throw new IllegalArgumentException("Unknown type: " + type.getTypeOfElement());
        }
    }

}
