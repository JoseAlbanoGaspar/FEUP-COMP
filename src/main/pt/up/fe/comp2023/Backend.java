package pt.up.fe.comp2023;

import org.specs.comp.ollir.*;
import pt.up.fe.comp.jmm.jasmin.JasminBackend;
import pt.up.fe.comp.jmm.jasmin.JasminResult;
import pt.up.fe.comp.jmm.ollir.OllirResult;

public class Backend implements JasminBackend {
    private String superClass = "java/lang/Object";
    private ClassUnit ollirClass = null;
    private final StringBuilder jasminCode = new StringBuilder();

    @Override
    public JasminResult toJasmin(OllirResult ollirResult) {
        this.ollirClass = ollirResult.getOllirClass();

        buildClass();
        buildSuper();
        buildFields();
        buildMethods();

        System.out.println(jasminCode);
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

        if (method.isStaticMethod()) jasminCode.append("static ");
        if (method.isFinalMethod()) jasminCode.append("final ");

        jasminCode.append(accessModifierToString(method.getMethodAccessModifier()))
            .append(" ").append(method.getMethodName()).append("(");

        for (Element elem : method.getParams()) {
            jasminCode.append(typeToString(elem.getType()));
        }

        jasminCode.append(")")
            .append(typeToString(method.getReturnType()))
            .append("\n");

        jasminCode.append("\t.limit stack 99\n")
            .append("\t.limit locals 99\n");

        for (Instruction instruction : method.getInstructions()) {
            buildInstruction(instruction);
        }

        // If method does not contain return instruction,
        // manually add it.
        if (method.getInstructions().get(method.getInstructions().size() - 1).getInstType() != InstructionType.RETURN) {
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

    private void buildInstruction(Instruction instruction) {
        switch (instruction.getInstType()) {
            case ASSIGN:
                buildAssignInstruction((AssignInstruction) instruction);
                break;
            case CALL:
                buildCallInstruction((CallInstruction) instruction);
                break;
            case GOTO:
                buildGotoInstruction((GotoInstruction) instruction);
                break;
            case NOPER:
                buildNoperInstruction();
                break;
            case BRANCH:
                buildBranchInstruction();
                break;
            case RETURN:
                buildReturnInstruction((ReturnInstruction) instruction);
                break;
            case GETFIELD:
                buildGetFieldInstruction((GetFieldInstruction) instruction);
                break;
            case PUTFIELD:
                buildPutFieldInstruction((PutFieldInstruction) instruction);
                break;
            case UNARYOPER:
                buildUnaryOperInstruction((UnaryOpInstruction) instruction);
                break;
            case BINARYOPER:
                buildBinaryOperInstruction((BinaryOpInstruction) instruction);
                break;
        }
    }

    private void buildAssignInstruction(AssignInstruction instruction) {}
    private void buildCallInstruction(CallInstruction instruction) {}
    private void buildGotoInstruction(GotoInstruction instruction) {}
    private void buildNoperInstruction() {}
    private void buildBranchInstruction() {}
    private void buildReturnInstruction(ReturnInstruction instruction) {}
    private void buildGetFieldInstruction(GetFieldInstruction instruction) {}
    private void buildPutFieldInstruction(PutFieldInstruction instruction) {}
    private void buildUnaryOperInstruction(UnaryOpInstruction instruction) {}
    private void buildBinaryOperInstruction(BinaryOpInstruction instruction) {}

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
