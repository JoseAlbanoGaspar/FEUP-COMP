package pt.up.fe.comp2023;

import org.specs.comp.ollir.*;
import pt.up.fe.comp.jmm.jasmin.JasminBackend;
import pt.up.fe.comp.jmm.jasmin.JasminResult;
import pt.up.fe.comp.jmm.ollir.OllirResult;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class Backend implements JasminBackend {

    @Override
    public JasminResult toJasmin(OllirResult ollirResult) {
        ClassUnit ollirClass = ollirResult.getOllirClass();

        StringBuilder jasminCode = new StringBuilder();

        buildClass(jasminCode, ollirClass);
        buildSuper(jasminCode, ollirClass.getSuperClass());
        buildFields(jasminCode, ollirClass.getFields());

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

    private void buildSuper(StringBuilder code, String superClass) {
        String superName = superClass == null ? "java/lang/Object" : superClass;
        code.append(".super ")
            .append(superName)
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
