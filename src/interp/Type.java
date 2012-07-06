/**
 * Copyright © 2012 Siegfried-A. Gevatter Pujals <siegfried@gevatter.com>
 * Copyright © 2012 Gerard Canal Camprodon <grar.knal@gmail.com>
 *
 * Permission to use, copy, modify, and/or distribute this software for
 * any purpose with or without fee is hereby granted, provided that the
 * above copyright notice and this permission notice appear in all copies.
 *
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR
 * ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN ACTION
 * OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF OR IN
 * CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
*/

package interp;

import com.sun.codemodel.*;

import parser.*;

public class Type {

    private enum Basic {
        BOOL,
        FLOAT,
        INT,
        STRING,
        COLOR,
        ARRAY,
        BUTTON,
        SENSOR,
        MOTOR,
        VOID
    }

    // Initialize simple types (ie. all but ARRAY)
    public static Type BOOL     = new Type(Basic.BOOL);
    public static Type FLOAT    = new Type(Basic.FLOAT);
    public static Type INT      = new Type(Basic.INT);
    public static Type STRING   = new Type(Basic.STRING);
    public static Type COLOR    = new Type(Basic.COLOR);
    public static Type BUTTON   = new Type(Basic.BUTTON);
    public static Type SENSOR   = new Type(Basic.SENSOR);
    public static Type MOTOR    = new Type(Basic.MOTOR);
    public static Type VOID     = new Type(Basic.VOID);

    private Basic mType;
    private Type mArrayType;

    private static JCodeModel mCodeModel;

    public static void init(JCodeModel codeModel) {
        mCodeModel = codeModel;
    }

    private Type(Type.Basic type) {
        mType = type;
    }

    public static Type newArray(Type type) {
        Type arrayType = new Type(Type.Basic.ARRAY);
        arrayType.mArrayType = type;
        return arrayType;
    }

    public static Type fromTree(CustomTree tree) {
        Type type = getTypeFromTree(tree.getType());

        if (type.isArray()) {
            type.mArrayType = fromTree(tree.getChild(0));
        }

        return type;
    }

    public static Type fromJava(JType javaType) {
        Type type = getTypeFromJava(javaType);

        if (type.isArray()) {
            JType innerType = ((JClass) javaType).getTypeParameters().get(0);
            type.mArrayType = fromJava(innerType);
        }

        return type;
    }

    private static Type getTypeFromTree(int type) {
        switch (type) {
            case LegoLangLexer.BOOL:
            case LegoLangLexer.T_BOOL:
                return new Type(Basic.BOOL);
            case LegoLangLexer.FLOAT:
            case LegoLangLexer.T_FLOAT:
                return new Type(Basic.FLOAT);
            case LegoLangLexer.INT:
            case LegoLangLexer.T_INT:
                return new Type(Basic.INT);
            case LegoLangLexer.STRING:
            case LegoLangLexer.T_STRING:
                return new Type(Basic.STRING);
            case LegoLangLexer.COLOR:
            case LegoLangLexer.T_COLOR:
                return new Type(Basic.COLOR);
            case LegoLangLexer.T_ARRAY:
                return new Type(Basic.ARRAY);
            case LegoLangLexer.SENSOR_PORT:
            case LegoLangLexer.T_SENSOR:
                return new Type(Basic.SENSOR);
            case LegoLangLexer.MOTOR:
            case LegoLangLexer.T_MOTOR:
                return new Type(Basic.MOTOR);
            case LegoLangLexer.BUTTON:
            case LegoLangLexer.T_BUTTON:
                return new Type(Basic.BUTTON);
            default:
                throw new RuntimeException("Invalid type: " + type);
        }
    }

    private static Type getTypeFromJava(JType type) {
        if (type.name().equals("Boolean"))
            return new Type(Basic.BOOL);
        if (type.name().equals("Float"))
            return new Type(Basic.FLOAT);
        if (type.name().equals("Long"))
            return new Type(Basic.INT);
        if (type.name().equals("String"))
            return new Type(Basic.STRING);
        if (type.name().equals("Color"))
            return new Type(Basic.COLOR);
        if (type.name().startsWith("DynamicArray<"))
            return new Type(Basic.ARRAY);
        if (type.name().equals("Motor"))
            return new Type(Basic.MOTOR);
        if (type.name().equals("Sensor"))
            return new Type(Basic.SENSOR);
        if (type.name().equals("Button"))
            return new Type(Basic.BUTTON);
        if (type.name().equals("void")) 
            return new Type(Basic.VOID);

        assert false; return null;
    }

    public JType toJava() {
        switch (mType) {
            case BOOL:      return mCodeModel._ref(Boolean.class);
            case FLOAT:     return mCodeModel._ref(Float.class);
            case INT:       return mCodeModel._ref(Long.class);
            case STRING:    return mCodeModel._ref(String.class);
            case COLOR:     return mCodeModel.directClass("llcclib.Color");
            case ARRAY:     return mCodeModel.directClass(
                                "llcclib.DynamicArray").narrow(
                                    getArrayType().toJava());
            case MOTOR:     return mCodeModel.directClass("llcclib.LegoTypes.Motor");
            case SENSOR:    return mCodeModel.directClass("llcclib.LegoTypes.Sensor");
            case BUTTON:    return mCodeModel.directClass("llcclib.LegoTypes.Button");
            case VOID:      return mCodeModel.VOID;
        }
        assert false; return null;
    }

    public boolean isType(Type... otherTypes) {
        for (Type otherType : otherTypes) {
            if (mType == otherType.mType) {
                if (!isArray() || mArrayType.isType(otherType.mArrayType))
                    return true;
            }
        }

        return false;
    }

    public boolean isNumeric() {
        return mType == Basic.INT || mType == Basic.FLOAT;
    }

    public boolean isArray() {
        return mType == Basic.ARRAY;
    }

    public Type getArrayType() {
        assert isArray();
        return mArrayType;
    }

    public String toString() {
        String name = mType.toString();
        if (isArray())
            name = mArrayType.toString() + "[]";
        return name;
    }

}
