package pt.up.fe.comp2023;

import org.specs.comp.ollir.*;
import pt.up.fe.comp.jmm.jasmin.JasminBackend;
import pt.up.fe.comp.jmm.jasmin.JasminResult;
import pt.up.fe.comp.jmm.ollir.OllirResult;

import java.lang.reflect.Array;

public class Backend implements JasminBackend {

    @Override
    public JasminResult toJasmin(OllirResult ollirResult) {
        ClassUnit ollirClass = ollirResult.getOllirClass();

        StringBuilder jasminCode = new StringBuilder();

        // class directive
        jasminCode.append(".class ").append(accessModifierToString(ollirClass.getClassAccessModifier())).append(" ").append(ollirClass.getClassName());
        jasminCode.append("\n");

        // super directive
        String superClass = ollirClass.getSuperClass() == null ? "java/lang/Object" : ollirClass.getSuperClass();
        jasminCode.append(".super ").append(superClass);
        jasminCode.append("\n");

        // fields directives
        for (Field field : ollirClass.getFields()) {
            jasminCode.append(".field ").append(accessModifierToString(field.getFieldAccessModifier()));
            jasminCode.append(" '").append(field.getFieldName()).append("' ");
            jasminCode.append(typeToString(field.getFieldType()));
            jasminCode.append("\n");
        }

        System.out.println(jasminCode);
        return new JasminResult(jasminCode.toString());
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
