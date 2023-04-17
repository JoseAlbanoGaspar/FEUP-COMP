package pt.up.fe.comp2023;

import org.specs.comp.ollir.*;
import pt.up.fe.comp.jmm.jasmin.JasminBackend;
import pt.up.fe.comp.jmm.jasmin.JasminResult;
import pt.up.fe.comp.jmm.ollir.OllirResult;

import java.util.ArrayList;

public class Backend implements JasminBackend {
    private String superClass = "java/lang/Object";

    @Override
    public JasminResult toJasmin(OllirResult ollirResult) {
        ClassUnit ollirClass = ollirResult.getOllirClass();

        StringBuilder jasminCode = new StringBuilder();

        buildClass(jasminCode, ollirClass);
        buildSuper(jasminCode, ollirClass.getSuperClass(), ollirClass);
        buildFields(jasminCode, ollirClass.getFields());
        buildMethods(jasminCode, ollirClass.getMethods(), ollirClass);

        System.out.println(jasminCode);
        return new JasminResult(jasminCode.toString());
    }

    private void buildClass(StringBuilder code, ClassUnit ollirClass) {
        code.append(".class ")
            .append(accessModifierToString(ollirClass.getClassAccessModifier()))
            .append(" ")
            .append(ollirClass.getClassName())
            .append("\n");
    }

    private void buildSuper(StringBuilder code, String superClass, ClassUnit ollirClass) {
        this.superClass = superClass == null ? "java/lang/Object" : fullClassName(ollirClass, superClass);
        code.append(".super ")
            .append(this.superClass)
            .append("\n");
    }

    private void buildFields(StringBuilder code, ArrayList<Field> fields) {
        for (Field field : fields) {
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

    private void buildMethods(StringBuilder code, ArrayList<Method> methods, ClassUnit ollirClass) {
        for (Method method : methods) {
            if (method.isConstructMethod()) buildConstructor(code, ollirClass);
            else buildMethod(code, method);
        }
    }

    private void buildMethod(StringBuilder code, Method method) {
        code.append(".method ").append(accessModifierToString(method.getMethodAccessModifier()))
            .append(" ").append(method.getMethodName()).append("(");

        for (Element elem : method.getParams()) {
            code.append(typeToString(elem.getType()));
        }

        code.append(")")
            .append(typeToString(method.getReturnType()))
            .append("\n");

        // TODO: Add body
        code.append("\t.limit stack 99\n")
            .append("\t.limit locals 99\n");

        code.append("\treturn\n")
            .append(".end method\n");
    }

    private void buildConstructor(StringBuilder code, ClassUnit ollirClass) {
        code.append(".method ").append(accessModifierToString(ollirClass.getClassAccessModifier())).append(" <init>()V\n")
            .append("\taload_0\n")
            .append("\tinvokespecial ").append(this.superClass).append("/<init>()V\n")
            .append("\treturn\n")
            .append(".end method\n");
    }

    private String fullClassName(ClassUnit ollirClass, String className) {
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
            case THIS: // ??
                return "Lthis;";
            case STRING:
                return "Ljava/lang/String;";
            case VOID:
                return "V";
            default: // Should be unreachable
                throw new IllegalArgumentException("Unknown type: " + type.getTypeOfElement());
        }
    }

}
