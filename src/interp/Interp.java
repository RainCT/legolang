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
import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.io.PrintStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.WildcardFileFilter;

import parser.*;
import llcclib.DefaultValue;

public class Interp {

    private CustomTree mTree;
    private JCodeModel mCodeModel;
    private JDefinedClass mClass;

    private ScopeStack mScopeStack;
    private LinkedList<Integer> mLineNumberStack;
    private int mSignalId = 0;
    private int mSubScopeId = 0;
    private PortConfig mPortConfig;

    public Interp(CustomTree tree) {
        mTree = tree;
    }

    public void compile(File resultDir, PrintStream codeModelStream) throws Exception {
        mCodeModel = new JCodeModel();
        Type.init(mCodeModel);

        mScopeStack = new ScopeStack();
        mPortConfig = new PortConfig();

        mLineNumberStack = new LinkedList<Integer>();
        pushLineNumber(0);
        try {
            compileMain(mCodeModel, mTree);
        } catch (RuntimeException e) {
            String message = "Line " + getLineNumber() + ": " + e.getMessage();
            throw new RuntimeException(message, e);
        }

        // Create output directories
        File libraryDir = new File("./llcc-build-area/llcclib");
        libraryDir.mkdirs();

        mCodeModel.build(resultDir, codeModelStream);

        // Copy support files
        FileUtils.copyDirectory(
            new File("./src/llcclib/"),
            new File("./llcc-build-area/llcclib/"),
            new WildcardFileFilter("*.java"));
    }

    private Integer getLineNumber() {
        return mLineNumberStack.peek();
    }

    private void pushLineNumber() {
        mLineNumberStack.push(mLineNumberStack.peek());
    }

    private void pushLineNumber(int lineNumber) {
        mLineNumberStack.push(lineNumber);
    }

    private void popLineNumber() {
        mLineNumberStack.pop();
    }

    private void setLineNumber(CustomTree tree) {
        popLineNumber();
        mLineNumberStack.push(tree.getLine());
    }

    private void compileMain(JCodeModel codeModel, CustomTree tree) throws Exception {
        assert tree != null && tree.getChild(0).getType() == LegoLangLexer.DEFAULTS;

        CustomTree defaultsTree = tree.getChild(0);
        CustomTree globalsTree = tree.getChild(1);
        CustomTree mainTree = tree.getChild(2);

        pushLineNumber();
        for (int i = 0; i < defaultsTree.getChildCount(); ++i) {
            CustomTree child = defaultsTree.getChild(i);
            assert child.getType() == LegoLangLexer.DEFAULT;
            setLineNumber(child);

            String sensor = child.getChild(0).getText();
            String port = child.getChild(1).getText();
            
            mPortConfig.setDefault(sensor, port);
        }
        popLineNumber();
        
        mClass = codeModel._class("Main");
        
        pushLineNumber();
        for (int i = 0 ; i < globalsTree.getChildCount() ; ++i) {
            CustomTree child = globalsTree.getChild(i);
            assert child.getType() == LegoLangLexer.GLOBAL;
            setLineNumber(child);
            
            assert child.getChild(0).getType() == LegoLangLexer.DECL;
            CustomTree decl = child.getChild(0);
            Type type = Type.fromTree(decl.getChild(0));
            String name = decl.getChild(1).getText();
            JVar variable;
            
            try {
                if (decl.getChildCount() == 3) { // We have an assignment
                    TypedExpression te = compileExpression(decl.getChild(2));
                    te = checkType(te, type);
                    variable = mClass.field(JMod.PRIVATE | JMod.STATIC,
                        type.toJava(), name, te.getExpr());
                }
                else variable = mClass.field(JMod.PRIVATE | JMod.STATIC,
                    type.toJava(), name, compileDefaultAssignment(type));
            } catch (IllegalArgumentException e) {
                throw new RuntimeException("Multiple definitions of global variable: " + name);
            }
            mScopeStack.addGlobalVariable(name, variable);
        }
        popLineNumber();
        
        JMethod mainMethod = mClass.method(JMod.PUBLIC | JMod.STATIC, void.class, "main");
        mainMethod.param(String[].class, "args");
        JBlock mainBlock = mainMethod.body();
        mScopeStack.enterFunction("__main__");
        mapFunctions(mClass, mainTree);
        compileFunctions(mClass, mainTree);
        compileBlock(mClass, mainBlock, mainTree);
    }
    
    JExpression compileDefaultAssignment(Type type) {
        if (type.isType(Type.BOOL)) return JExpr.FALSE;
        if (type.isArray()) return JExpr._new(type.toJava());
        if (type.isType(Type.INT)) return JExpr.lit(0L);
        if (type.isType(Type.FLOAT)) return JExpr.lit(0F);
        if (type.isType(Type.STRING)) return JExpr.lit("");

        // don't assert: sensor, motor and button don't have default
        return null;
    }

    private void compileFunctions(JDefinedClass klass, CustomTree tree) {
        assert tree != null && tree.getType() == LegoLangLexer.LIST_INSTR;
        pushLineNumber();
        for (int i = 0 ; i < tree.getChildCount() ; ++i) {
            CustomTree child = tree.getChild(i);
            setLineNumber(child);
            if (child.getType() == LegoLangLexer.DEF) {
                compileFunction(child);
            } else if (child.getType() == LegoLangLexer.ON_SIGNAL) {
                String signal = child.getChild(0).getText();
                
                
                //Check if already using and prompt a warning
                if (child.getChildCount() >= 3) {
                    checkType(new TypedExpression(Type.fromTree(child.getChild(2)), JExpr._null()), Type.SENSOR);
                    if (signal.equals("onTouch")) mPortConfig.addUsed("touch", child.getChild(2).getText(), getLineNumber());
                    else if (signal.startsWith("onRange")) {
                        if (signal.length() > 7) {
                            try { Float.parseFloat(signal.substring(7, signal.length())); }
                            catch (NumberFormatException e) {throw new RuntimeException("Invalid range value. Expecting a number.");}
                        }
                        mPortConfig.addUsed("ultrasonic", child.getChild(2).getText(), getLineNumber());
                    }
                }
                //end
                
                String name = "__" + signal + "__" + ++mSignalId;

                JDefinedClass signalClass = null;
                try {
                    signalClass = klass._class(JMod.PUBLIC | JMod.STATIC, name);
                }
                catch (JClassAlreadyExistsException e) {
                    assert false;
                }
                signalClass.method(JMod.PUBLIC, void.class, "__run__").param(Type.INT.toJava(), "signal_data");;
                signalClass._implements(mCodeModel.directClass(
                    "llcclib.LegoFunctions.SignalCallback"));

                compileFunction(signalClass, name, (CustomTree) null,
                    child.getChild(1), Type.VOID);
                child.setSignalData(signal, signalClass);
            }
        }
        popLineNumber();
    }

    private void compileFunction(CustomTree tree) {
        assert tree != null && tree.getType() == LegoLangLexer.DEF;
        
        String funcName = tree.getChild(0).getText();
        CustomTree params = tree.getChild(1);
        CustomTree body = tree.getChild(2);

        JDefinedClass fClass = mScopeStack.getFunction(funcName);

        Type returnType;
        if (tree.getChildCount() == 4)
            returnType = Type.fromTree(tree.getChild(3));
        else
            returnType = Type.VOID;

        compileFunction(fClass, funcName, params, body, returnType);
    }

    // Also used for signal bodies
    private void compileFunction(JDefinedClass fClass, String funcName,
            CustomTree params, CustomTree body, Type returnType) {
        assert body != null && body.getType() == LegoLangLexer.LIST_INSTR;
        mScopeStack.enterFunction(funcName);
        
        JMethod function = (JMethod) fClass.methods().toArray()[0];

        // Add function parameters to scope
        JVar[] functionParameters = function.listParams();
        for (int i = 0; i < functionParameters.length; ++i) {
            mScopeStack.addVariable(functionParameters[i].name(), functionParameters[i]);
        }

        JBlock functionBody = function.body();

        // Initialize default parameter values
        if (params != null) {
            pushLineNumber();
            for (int i = 0; i < params.getChildCount(); ++i) {
                CustomTree child = params.getChild(i);
                setLineNumber(child);
                assert child.getType() == LegoLangLexer.PARAM;
                assert child.getChild(0).getType() == LegoLangLexer.DECL;
                if (child.getChild(0).getChildCount() == 3) {
                    JVar parameter = functionParameters[i];
                    TypedExpression defaultValue = compileExpression(child.getChild(0).getChild(2));
                    // Default values can only be literals, so no array clone required here
                    defaultValue = checkType(defaultValue, Type.fromJava(parameter.type()));
                    JBlock defaultBlock = functionBody._if(parameter.eq(JExpr._null()))._then();
                    defaultBlock.assign(parameter, defaultValue.getExpr());
                }
            }
            popLineNumber();
        }
        
        mapFunctions(fClass, body);
        compileFunctions(fClass, body);
        compileBlock(fClass, functionBody, body);
        
        if (!returnType.isType(Type.VOID))
            functionBody._return(JExpr._null()); // suppress stupid Java errors
        
        mScopeStack.leaveFunction();
    }

    private void compileBlock(JDefinedClass scopeClass, JBlock codeBlock, CustomTree tree) {
        assert tree != null && tree.getType() == LegoLangLexer.LIST_INSTR;
        pushLineNumber();
        for (int i = 0; i < tree.getChildCount(); ++i) {
            CustomTree child = tree.getChild(i);
            setLineNumber(child);
            compileInstruction(scopeClass, codeBlock, child);
        }
        popLineNumber();
    }

    /*
     * Create a sub-scope (eg. for an IF or a WHILE statement). Code in
     * the first level is added directly to `codeBlock', whereas any
     * functions defined in it are delegated to a new class (created just
     * for this sub-scope).
     * */
    private void compileSubScope(JDefinedClass scopeClass, JBlock codeBlock, CustomTree tree) {
        assert tree != null && tree.getType() == LegoLangLexer.LIST_INSTR;

        JDefinedClass subClass = null;

        try {
            String name = "__scope__" + ++mSubScopeId;
            subClass = scopeClass._class(JMod.PUBLIC | JMod.STATIC, name);
            codeBlock.directStatement("// created scope " + name);
        }
        catch (JClassAlreadyExistsException e) {
            assert false;
        }

        mapFunctions(subClass, tree);
        compileFunctions(subClass, tree);
        compileBlock(subClass, codeBlock, tree);
    }

    private void mapFunctions(JDefinedClass klass, CustomTree tree) {
        assert tree != null && tree.getType() == LegoLangLexer.LIST_INSTR;

        pushLineNumber();
        for (int i = 0; i < tree.getChildCount(); ++i) {
            CustomTree child = tree.getChild(i);
            setLineNumber(child);
            if (child.getType() == LegoLangLexer.DEF) {
                String name = child.getChild(0).getText();
                JMethod function;
                JDefinedClass funclass;
                try {
                    funclass = klass._class(JMod.PUBLIC | JMod.STATIC, name);
                 }
                catch (JClassAlreadyExistsException e) {
                    throw new RuntimeException("Multiple definitions of function " + name);
                }
                if (child.getChildCount() == 4) { // The function has a return type
                    Type t = Type.fromTree(child.getChild(3));
                    function = funclass.method(JMod.PUBLIC | JMod.STATIC, t.toJava(), "__run__");
                }
                else function = funclass.method(JMod.PUBLIC | JMod.STATIC, void.class, "__run__");
                compileFunctionParams(function, child.getChild(1));
                mScopeStack.addFunction(name, funclass);
            }
        }
        popLineNumber();
    }

    private void compileFunctionParams(JMethod function, CustomTree tree) {
        assert tree != null && tree.getType() == LegoLangLexer.PARAMS;

        pushLineNumber();
        for (int i = 0; i < tree.getChildCount(); ++i) {
            CustomTree child = tree.getChild(i);
            setLineNumber(child);
            assert child.getType() == LegoLangLexer.PARAM;
            compileFunctionParam(function, child.getChild(0));
        }
        popLineNumber();
    }

    private void compileFunctionParam(JMethod function, CustomTree decl) {
        assert decl != null && decl.getType() == LegoLangLexer.DECL;
        
        Type type = Type.fromTree(decl.getChild(0));
        String name = decl.getChild(1).getText();
        JVar param = function.param(type.toJava(), name);

        // Look for a default value
        if (decl.getChildCount() == 3) {
            JAnnotationUse annotation = param.annotate(DefaultValue.class);
            CustomTree value = decl.getChild(2);
            if (value.getType() != LegoLangLexer.LITERAL)
                throw new RuntimeException("Expected a literal as default value.");
            annotation.param("value", value.getChild(0).getText());
        }
    }

    private TypedExpression checkType(TypedExpression tExpr, Type type) {
        if (tExpr.getType().isType(type))
            return tExpr;

        if (type.isType(Type.BOOL)) {
            if (tExpr.getType().isNumeric()) {
                JExpression expr = tExpr.getExpr().ne(JExpr.lit(0));
                return new TypedExpression(Type.BOOL, expr);
            }
        } else if (type.isType(Type.INT)) {
            if (tExpr.getType().isNumeric()) {
                JInvocation invoc = JExpr._new(Type.INT.toJava());
                JInvocation round = mCodeModel.directClass("Math").staticInvoke("round");
                round.arg(JExpr.cast(mCodeModel.DOUBLE, tExpr.getExpr()));
                invoc.arg(round);
                return new TypedExpression(Type.INT, invoc);
            } else if (tExpr.getType().isType(Type.BOOL)) {
                JExpression cast = JOp.cond(tExpr.getExpr().eq(JExpr.TRUE),
                    JExpr.lit(1L), JExpr.lit(0L));
                return new TypedExpression(Type.INT, cast);
            }
        } else if (type.isType(Type.FLOAT)) {
            if (tExpr.getType().isNumeric()) {
                JInvocation cast = JExpr._new(mCodeModel.directClass("Long"));
                cast.arg(tExpr.getExpr());
                cast = cast.invoke("floatValue");
                return new TypedExpression(Type.FLOAT, cast);
            }
        } else if (type.isType(Type.STRING)) {
            return new TypedExpression(Type.STRING, tExpr.getExpr());
            //return new TypedExpression(Type.STRING,
            //    tExpr.getExpr().invoke("toString"));
        }

        throw new RuntimeException(
            "Invalid expression type: expected " + type +
            ", got " + tExpr.getType());
    }

    private void assertNumeric(TypedExpression tExpr, String op) {
        if (!tExpr.getType().isNumeric()) {
            throw new RuntimeException(
                "Invalid expression type: " + tExpr.getType() +
                ", expected numeric type for '" + op + "' operator.");
        }
    }

    private void assertNumeric(JVar variable, String op) {
        assertNumeric(new TypedExpression(variable), op);
    }

    private void compileAssignment(JBlock codeBlock, CustomTree tree) {
        TypedExpression leftHand;

        // Get the left hand of the assignment
        if (tree.getType() == LegoLangLexer.ASSIGN) {
            // We don't need read access to the variable, so if it's an array
            // it doesn't require an index (x[] is used for appending).
            leftHand = compileVariableReference(tree.getChild(0));
        } else {
            leftHand = compileExpression(tree.getChild(0));

            // Check type
            boolean isConcat = leftHand.getType().isType(Type.STRING) &&
                tree.getType() == LegoLangLexer.PLUSEQ;
            if (!leftHand.getType().isNumeric() && !isConcat) {
                throw new RuntimeException(
                    "Unsupported type for arithmetic assignment.");
            }
        }

        TypedExpression rightHand = compileExpression(tree.getChild(1));
        if (tree.getChild(0).getType() == LegoLangLexer.ARRAY) {
            // Writing to an array index
            rightHand = checkType(rightHand, leftHand.getType().getArrayType());
        } else {
            // Writing to a basic type or replacing a whole array
            rightHand = checkType(rightHand, leftHand.getType());
        }

        switch (tree.getType()) {
            case LegoLangLexer.ASSIGN:
                break;
            case LegoLangLexer.PLUSEQ:
                rightHand.setExpr(leftHand.getExpr().plus(rightHand.getExpr()));
                break;
            case LegoLangLexer.MINUSEQ:
                assertNumeric(rightHand, "-=");
                rightHand.setExpr(leftHand.getExpr().minus(rightHand.getExpr()));
                break;
            case LegoLangLexer.MULEQ:
                assertNumeric(rightHand, "*=");
                rightHand.setExpr(leftHand.getExpr().mul(rightHand.getExpr()));
                break;
            case LegoLangLexer.DIVEQ:
                assertNumeric(rightHand, "/=");
                rightHand.setExpr(leftHand.getExpr().div(rightHand.getExpr()));
                break;
            case LegoLangLexer.MODEQ:
                assertNumeric(rightHand, "%=");
                rightHand.setExpr(leftHand.getExpr().mod(rightHand.getExpr()));
                break;
            default:
                assert false;    
        }

        // Swallow copy for array to array assignments
        if (rightHand.getType().isArray()) {
            rightHand = new TypedExpression(rightHand.getType(),
                rightHand.getExpr().invoke("clone"));
        }

        if (tree.getChild(0).getType() == LegoLangLexer.ARRAY) {
            CustomTree leftHandTree = tree.getChild(0);
            JVar arrayVar = mScopeStack.getVariable(leftHandTree.getChild(0).getText());
            TypedExpression array = new TypedExpression(arrayVar);

            if (tree.getChild(0).getChildCount() < 2) {
                // Append to array
                JInvocation assign = codeBlock.invoke(array.getExpr(), "appendElement");
                assign.arg(rightHand.getExpr());
            } else {
                // Replace array element
                TypedExpression index = compileExpression(leftHandTree.getChild(1));
                index = checkType(index, Type.INT);

                JInvocation assign = codeBlock.invoke(array.getExpr(), "setElement");
                assign.arg(index.getExpr());
                assign.arg(rightHand.getExpr());
            }
        } else {
            assert leftHand.getExpr() instanceof JAssignmentTarget;
            codeBlock.assign((JAssignmentTarget) leftHand.getExpr(), rightHand.getExpr());
        }
    }

    /*
     * Note: `scopeClass' is only to be used for subscope creation
     * */
    private void compileInstruction(JDefinedClass scopeClass, JBlock codeBlock,
            CustomTree tree) {
        assert tree != null;

        switch (tree.getType()) {
            case LegoLangLexer.ASSIGN:
            case LegoLangLexer.PLUSEQ: 
            case LegoLangLexer.MINUSEQ:
            case LegoLangLexer.MULEQ:
            case LegoLangLexer.DIVEQ:
            case LegoLangLexer.MODEQ:
                compileAssignment(codeBlock, tree);
                return;
            case LegoLangLexer.DECL: {
                Type type = Type.fromTree(tree.getChild(0));
                String name = tree.getChild(1).getText();
                JVar variable;
                if (tree.getChildCount() == 3) { // We got an assignment!
                    TypedExpression te = compileExpression(tree.getChild(2));
                    
                    // Swallow copy for array to array assignments
                    if (te.getType().isArray()) {
                        te = new TypedExpression(te.getType(), te.getExpr().invoke("clone"));
                    }
                    te = checkType(te, type);

                    variable = codeBlock.decl(type.toJava(), name, te.getExpr());
                }
                else variable = codeBlock.decl(type.toJava(), name, compileDefaultAssignment(type));
                mScopeStack.addVariable(name, variable);
                return;
            }
            case LegoLangLexer.POSTINCR: // ++x and x++ statements (not expressions!) are the same
            case LegoLangLexer.PREINCR: {
                String variableName = tree.getChild(0).getText();
                JVar variable = mScopeStack.getVariable(variableName);
                assertNumeric(variable, "↑");
                codeBlock.directStatement("++"+variableName+";");
                return;
            }
            case LegoLangLexer.POSTDECR: // --x and x-- statements (not expressions!) are the same
            case LegoLangLexer.PREDECR: {
                String variableName = tree.getChild(0).getText();
                JVar variable = mScopeStack.getVariable(variableName);
                assertNumeric(variable, "↓");
                codeBlock.directStatement("--"+variableName+";");
                return;
            }
            case LegoLangLexer.FUNCALL: {
                String name = tree.getChild(0).getText();
                
                JMethod func = mScopeStack.getLegoFunction(name);
                JInvocation invocation;
                if (func != null) {
                    invocation = mCodeModel.directClass(
                        "llcclib.LegoFunctions").staticInvoke(name);
                    codeBlock.add(invocation);
                }
                else {
                    JDefinedClass fclass = mScopeStack.getFunction(name);
                    invocation = codeBlock.staticInvoke(fclass, "__run__");
                    func = (JMethod)fclass.methods().toArray()[0];
                }
                compileFunctionCallArgs(name, invocation, func, tree.getChild(1));
                return;
            }
            case LegoLangLexer.IF: {
                mScopeStack.enterSubScope();
                TypedExpression tExpr = compileExpression(tree.getChild(0));
                tExpr = checkType(tExpr, Type.BOOL);
                JConditional conditional = codeBlock._if(tExpr.getExpr());
                compileSubScope(scopeClass, conditional._then(), tree.getChild(1));
                mScopeStack.leaveSubScope();
                if (tree.getChild(2) != null) {
                    // There's an else
                    mScopeStack.enterSubScope();
                    compileSubScope(scopeClass, conditional._else(), tree.getChild(2));
                    mScopeStack.leaveSubScope();
                }
                return;
            }
            case LegoLangLexer.PRINT: {
                if (tree.getChildCount() == 1) {
                    JClass system = mCodeModel.ref(java.lang.System.class);
                    JFieldRef systemOut = system.staticRef("out");
                    JInvocation invocation = systemOut.invoke("println");
                    codeBlock.add(invocation);
                    TypedExpression tExpr = compileExpression(tree.getChild(0));
                    if (tExpr.getType().isType(Type.VOID))
                        throw new RuntimeException("Can't print void expression.");
                    invocation.arg(tExpr.getExpr());
                } else {
                    JClass lcd = mCodeModel.directClass("lejos.nxt.LCD");
                    JInvocation invocation = codeBlock.staticInvoke(lcd, "drawString");
                    JExpression expr = compileExpression(tree.getChild(0)).getExpr();
                    invocation.arg(expr);
                    TypedExpression xy = compileExpression(tree.getChild(1).getChild(0));
                    checkType(xy, Type.INT);
                    if (tree.getChild(1).getChildCount() == 2) {
                        invocation.arg(xy.getExpr().invoke("intValue"));
                        TypedExpression y = compileExpression(tree.getChild(1).getChild(1));
                        checkType(y, Type.INT);
                        invocation.arg(y.getExpr().invoke("intValue"));
                    }
                    else {
                        invocation.arg(JExpr.lit(0));
                        invocation.arg(xy.getExpr().invoke("intValue"));
                    }
                }
                return;
            }
            case LegoLangLexer.DELETE: {
                CustomTree target = tree.getChild(0);
                JVar array = getVariableFromTree(target);
                
                if (!Type.fromJava(array.type()).isArray())
                    throw new RuntimeException("Delete used with non-array variable.");

                if (target.getType() == LegoLangLexer.ARRAY) {
                    // Delete array element
                    TypedExpression index = compileExpression(target.getChild(1));
                    index = checkType(index, Type.INT);

                    JInvocation assign = codeBlock.invoke(array, "deleteElement");
                    assign.arg(index.getExpr());
                } else {
                    // Delete whole array
                    JInvocation emptyArray = JExpr._new(array.type());
                    codeBlock.assign(array, emptyArray);
                }
                return;
            }
            case LegoLangLexer.WHILE: {
                mScopeStack.enterSubScope();
                TypedExpression tExp = compileExpression(tree.getChild(0));
                tExp = checkType(tExp, Type.BOOL);
                JWhileLoop wloop = codeBlock._while(tExp.getExpr());
                compileSubScope(scopeClass, wloop.body(), tree.getChild(1));
                mScopeStack.leaveSubScope();
                return;
            }
            case LegoLangLexer.FOREACH: {
                mScopeStack.enterSubScope();

                JVar array = getVariableFromTree(tree.getChild(1));
                if (!Type.fromJava(array.type()).isArray())
                    throw new RuntimeException("foreach statement expected " +
                        "an array, got " + Type.fromJava(array.type()) + ".");

                JForLoop floop = codeBlock._for();
                String indvar = "__i__" + (mSubScopeId+1); // induction variable name
                JVar i_for = floop.init(Type.INT.toJava(), indvar, JExpr.lit(0L)); // __i__ X= 0
                floop.test(i_for.lt(array.invoke("getLength")));
                floop.update(i_for.incr());

                String name = tree.getChild(0).getText(); // user variable
                JBlock forBody = floop.body();
                JVar variable = forBody.decl(
                    Type.fromJava(array.type()).getArrayType().toJava(), name,
                    array.invoke("getElement").arg(i_for));
                mScopeStack.addVariable(name, variable);
                
                compileSubScope(scopeClass, forBody, tree.getChild(2));

                mScopeStack.leaveSubScope();
                return;
            }
            case LegoLangLexer.FOR: {
                mScopeStack.enterSubScope();

                // We need a new Java scope to hold the induction variable
                JBlock subBlock = codeBlock.block();

                TypedExpression start = compileExpression(tree.getChild(1));
                start = checkType(start, Type.INT);
                TypedExpression end = compileExpression(tree.getChild(2));
                end = checkType(end, Type.INT);
                
                String indName = tree.getChild(0).getText(); // induction variable name
                String tmp1name = "__tmp1__"+(mSubScopeId+1);
                String tmp2name = "__tmp2__"+(mSubScopeId+1);
                JVar tmp1var = subBlock.decl(start.getType().toJava(), tmp1name, start.getExpr());
                JVar tmp2var = subBlock.decl(end.getType().toJava(), tmp2name,
                    JOp.cond(start.getExpr().lt(end.getExpr()),
                        end.getExpr().plus(JExpr.lit(1L)),
                        end.getExpr().minus(JExpr.lit(1L))));
                
                JVar indVar = subBlock.decl(Type.INT.toJava(), indName, tmp1var); // ind = start
                TypedExpression ind = new TypedExpression(Type.INT, indVar);
                TypedExpression tmp2 = new TypedExpression(Type.INT, tmp2var);
                JWhileLoop wloop = subBlock._while(ind.getBasicExpr().ne(tmp2.getBasicExpr()));
                mScopeStack.addVariable(indName, indVar);
                
                // Increment/decrement induction variable
                JBlock whileBody = wloop.body();
                compileSubScope(scopeClass, whileBody, tree.getChild(3));
                whileBody.assign(indVar, JOp.cond(tmp1var.lt(tmp2var),
                    indVar.plus(JExpr.lit(1L)), indVar.minus(JExpr.lit(1L))));
                
                mScopeStack.leaveSubScope();
                return;
            }
            case LegoLangLexer.BREAK: {
                JBlock tautologyBody = codeBlock._if(JExpr.TRUE.eq(JExpr.TRUE))._then();
                tautologyBody._break();
                return;
            }
            case LegoLangLexer.RETURN: {
                if (mScopeStack.inMainScope())
                    throw new RuntimeException("Unexpected return in global scope.");
                TypedExpression tExp;
                if (tree.getChildCount() == 1)
                    tExp = compileExpression(tree.getChild(0));
                else
                    tExp = TypedExpression.VOID;
                JDefinedClass fclass = mScopeStack.getFunction(
                    mScopeStack.getCurrentFunctionName());
                Object[] meths =  fclass.methods().toArray();
                JType funcType = ((JMethod) meths[0]).type();
                tExp = checkType(tExp, Type.fromJava(funcType));
                
                JBlock tautologyBody = codeBlock._if(JExpr.TRUE.eq(JExpr.TRUE))._then();
                tautologyBody._return(tExp.getExpr());
                return;
            }
            case LegoLangLexer.ON_SIGNAL: {
                String signal = tree.getSignalName();
                JClass cbClass = tree.getSignalCallback();

                JInvocation invocation = mCodeModel.directClass(
                        "llcclib.LegoFunctions.SignalManager").staticInvoke("registerCallback");
                invocation.arg(JExpr.lit(signal));
                invocation.arg(JExpr._new(cbClass));
                if (tree.getChildCount() == 3) {
                    checkType(new TypedExpression(Type.fromTree(tree.getChild(2)), JExpr._null()), Type.SENSOR);
                    invocation.arg(JExpr._new(Type.SENSOR.toJava()).arg(tree.getChild(2).getText()));
                }
                codeBlock.add(invocation);

                return;
            }
            case LegoLangLexer.PASS:
            case LegoLangLexer.DEF: {
                // Do nothing
                return;
            }
            default: {
                System.err.println("Unexpected instruction: " + tree);
                assert false;
                return;
            }
        }
    }

    private TypedExpression compileFunctionCallArgs(String name,
        JInvocation invocation, JMethod function, CustomTree args)
    {
        assert args != null && args.getType() == LegoLangLexer.ARGS;
        
        JVar[] params = function.listParams();
        if (args.getChildCount() > params.length)
            throw new RuntimeException("Too many arguments calling " + name);
    
        if (params.length > 0) {
            for (int i = 0; i < args.getChildCount(); ++i) {
                CustomTree arg = args.getChild(i);
                
                // Check if the port is being used
                if (arg.getType() == LegoLangLexer.LITERAL
                    && arg.getChild(0).getType() == LegoLangLexer.SENSOR_PORT)
                {
                    if (!params[i].annotations().isEmpty()) {
                        JAnnotationUse annot = (JAnnotationUse) params[i].annotations().toArray()[0];
                        if (annot.getAnnotationClass().name().equals("PortType")) {
                            String sensorName =  annot.getAnnotationMembers().keySet().iterator().next();
                            String callPort = arg.getChild(0).getText(); // "Sx"
                            mPortConfig.addUsed(sensorName, callPort, getLineNumber());
                        }
                    }
                }
                
                TypedExpression tExpr = compileExpression(arg);
                tExpr = checkType(tExpr, Type.fromJava(params[i].type()));
                if (tExpr.getType().isArray()) {
                    // Pass arrays by value
                    invocation.arg(tExpr.getExpr().invoke("clone"));
                } else {
                    invocation.arg(tExpr.getExpr());
                }
            }
            if (args.getChildCount() != params.length) {
                if (params[args.getChildCount()].annotations().isEmpty())
                    throw new RuntimeException("Too few arguments calling " + name);
            }
            for (int i = args.getChildCount(); i < params.length; ++i) {
                JAnnotationUse annot = (JAnnotationUse) params[i].annotations().toArray()[0];
                if (annot.getAnnotationClass().name().equals("PortType")) {
                    // Which type of sensor does this variable want?
                    Set<String> sensorSet = annot.getAnnotationMembers().keySet();
                    assert sensorSet.size() == 1;
                    String sensor = sensorSet.iterator().next();

                    // What is the default port for that sensor?
                    JInvocation sensorPort = JExpr._new(Type.SENSOR.toJava());
                    sensorPort.arg(mPortConfig.getDefault(sensor));
                    invocation.arg(sensorPort);
                } else {
                    TypedExpression tExpr = new TypedExpression(
                        Type.fromJava(params[i].type()), JExpr._null());
                    invocation.arg(tExpr.getExpr());
                }
            }
        }

        return new TypedExpression(Type.fromJava(function.type()), invocation);
    }

    private JVar getVariableFromTree(CustomTree tree) {
        switch (tree.getType()) {
            case LegoLangLexer.ID: {
                String variableName = tree.getText();
                return mScopeStack.getVariable(variableName);
            }
            case LegoLangLexer.ARRAY: {
                String variableName = tree.getChild(0).getText();
                JVar array = mScopeStack.getVariable(variableName);
                if (!Type.fromJava(array.type()).isArray())
                    throw new RuntimeException("Indexing non-array variable.");
                return array;
            }
            default: {
                assert false;
                return null;
            }
        }
    }

    private TypedExpression compileVariableReference(CustomTree tree) {
        return new TypedExpression(getVariableFromTree(tree));
    }

    private TypedExpression compileExpression(CustomTree tree) {
        assert tree != null;

        // Atoms
        switch (tree.getType()) {
            case LegoLangLexer.ID: {
                return compileVariableReference(tree);
            }
            case LegoLangLexer.SIGNAL_DATA: {
                return new TypedExpression(Type.INT, JExpr.direct(tree.getText()));
            }
            case LegoLangLexer.ARRAY: {
                if (tree.getChildCount() < 2) {
                    throw new RuntimeException("Array access without index!");
                }

                TypedExpression array = compileVariableReference(tree);
                Type arrayType = array.getType().getArrayType();

                TypedExpression index = compileExpression(tree.getChild(1));
                index = checkType(index, Type.INT);

                JInvocation access = array.getExpr().invoke("getElement");
                access.arg(index.getExpr());

                JExpression cast = JExpr.cast(arrayType.toJava(), access);
                return new TypedExpression(arrayType, cast);
            }
            case LegoLangLexer.LENGTH: {
                TypedExpression array = compileVariableReference(tree.getChild(0));
                JInvocation lengthExpr = array.getExpr().invoke("getLength");
                return new TypedExpression(Type.INT, lengthExpr);
            }
            case LegoLangLexer.LITERAL: {
                return compileLiteral(tree.getChild(0));
            }
            case LegoLangLexer.ARRAY_INIT: {
                Type type = Type.newArray(Type.fromTree(tree.getChild(0)));
                JInvocation array = JExpr._new(type.toJava());
                for (int i = 1; i < tree.getChildCount(); ++i) {
                    TypedExpression element = compileExpression(tree.getChild(i));
                    element = checkType(element, type.getArrayType());
                    array.arg(element.getExpr());
                }
                return new TypedExpression(type, array);
            }
            case LegoLangLexer.FUNCALL: {
                String name = tree.getChild(0).getText();

                JMethod func = mScopeStack.getLegoFunction(name);
                JInvocation invocation;
                if (func != null) {
                
                    invocation = mCodeModel.directClass(
                        "llcclib.LegoFunctions").staticInvoke(name);
                }
                else {
                    JDefinedClass fclass = mScopeStack.getFunction(name);
                    invocation = fclass.staticInvoke("__run__");
                    func = (JMethod) fclass.methods().toArray()[0];
                }
                return compileFunctionCallArgs(name, invocation, func, tree.getChild(1));
	        }
        }

        // Unary operators
        if (tree.getChildCount() == 1) {
            TypedExpression result = compileExpression(tree.getChild(0));

            switch (tree.getType()) {
                case LegoLangLexer.NOT:
                    result = checkType(result, Type.BOOL);
                    result.setExpr(result.getExpr().not());
                    break;
                case LegoLangLexer.MINUS:
                    result = checkType(result, Type.INT);
                    JInvocation invoc = JExpr._new(Type.INT.toJava());
                    invoc.arg(result.getExpr().minus());
                    result.setExpr(invoc);
                    break;
                case LegoLangLexer.PLUS:
                    result = checkType(result, Type.INT);
                    // nothing to do here...
                    break;
                case LegoLangLexer.PREINCR: {
                    String variableName = tree.getChild(0).getText();
                    JVar variable = mScopeStack.getVariable(variableName);
                    assertNumeric(variable, "↑");
                    JExpression expr = (variable.incr()).plus(JExpr.lit(1));
                    result = new TypedExpression(Type.fromJava(variable.type()),expr);
                    break;
                }
                case LegoLangLexer.PREDECR:{
                    String variableName = tree.getChild(0).getText();
                    JVar variable = mScopeStack.getVariable(variableName);
                    assertNumeric(variable, "↓");
                    JExpression expr = (variable.decr()).minus(JExpr.lit(1));
                    result = new TypedExpression(Type.fromJava(variable.type()),expr);
                    break;
                }
                case LegoLangLexer.POSTINCR: {
                    String variableName = tree.getChild(0).getText();
                    JVar variable = mScopeStack.getVariable(variableName);
                    assertNumeric(variable, "↑");
                    result.setExpr(result.getExpr().incr());
                    break;
                }
                case LegoLangLexer.POSTDECR: {
                    String variableName = tree.getChild(0).getText();
                    JVar variable = mScopeStack.getVariable(variableName);
                    assertNumeric(variable, "↓");
                    result.setExpr(result.getExpr().decr());
                    break;
                }
                default: {
                    assert false;
                }
            }

            return result;
        }

        // Binary operators
        assert (tree.getChildCount() == 2);

        JExpression expr = null;
        TypedExpression arg1 = compileExpression(tree.getChild(0));
        TypedExpression arg2 = compileExpression(tree.getChild(1));

        switch (tree.getType()) {
            // Relational operators
            case LegoLangLexer.EQ:
            case LegoLangLexer.NEQ: {
                boolean negated = tree.getType() == LegoLangLexer.NEQ;
                if (arg1.getType().isType(Type.VOID)) {
                    throw new RuntimeException("Can't compare void expressions.");
                } else {
                    arg2 = checkType(arg2, arg1.getType());
                    if (arg1.getType().isType(Type.STRING) || arg1.getType().isArray()) {
                        JInvocation equals = arg1.getExpr().invoke("equals");
                        equals.arg(arg2.getExpr());
                        expr = equals;
                    } else {
                        if (!arg1.getType().isType(Type.BOOL)) {
                            assertNumeric(arg1, (negated) ? "!=" : "==");
                            assertNumeric(arg2, (negated) ? "!=" : "==");
                        }
                        expr = arg1.getBasicExpr().eq(arg2.getBasicExpr());
                    }
                }
                if (negated)
                    expr = expr.not();
                break;
            }
            case LegoLangLexer.LT:
                assertNumeric(arg1, "<");
                assertNumeric(arg2, "<");
                expr = arg1.getBasicExpr().lt(arg2.getBasicExpr());
                break;
            case LegoLangLexer.LE:
                assertNumeric(arg1, "<=");
                assertNumeric(arg2, "<=");
                expr = arg1.getBasicExpr().lte(arg2.getBasicExpr());
                break;
            case LegoLangLexer.GT:
                assertNumeric(arg1, ">");
                assertNumeric(arg2, ">");
                expr = arg1.getBasicExpr().gt(arg2.getBasicExpr());
                break;
            case LegoLangLexer.GE:
                assertNumeric(arg1, ">=");
                assertNumeric(arg2, ">=");
                expr = arg1.getBasicExpr().gte(arg2.getBasicExpr());
                break;

            // Logic operators
            case LegoLangLexer.AND:
                arg1 = checkType(arg1, Type.BOOL);
                arg2 = checkType(arg2, Type.BOOL);
                expr = arg1.getExpr().cand(arg2.getExpr());
                break;
            case LegoLangLexer.OR:
                arg1 = checkType(arg1, Type.BOOL);
                arg2 = checkType(arg2, Type.BOOL);
                expr = arg1.getExpr().cor(arg2.getExpr());
                break;
        }

        if (expr != null)
            return new TypedExpression(Type.BOOL, expr);

        // Concatenation
        if (tree.getType() == LegoLangLexer.PLUS && (
            arg1.getType().isType(Type.STRING) ||
            arg2.getType().isType(Type.STRING)))
        {
            arg1 = checkType(arg1, Type.STRING);
            arg2 = checkType(arg2, Type.STRING);
            expr = arg1.getExpr().plus(arg2.getExpr());
            return new TypedExpression (Type.STRING, expr);
        }

        // FIXME: array addition?

        Type numericType = null;
        if (arg1.getType().isType(Type.FLOAT) || arg2.getType().isType(Type.FLOAT))
            numericType = Type.FLOAT;
        else if (arg1.getType().isType(Type.INT) || arg2.getType().isType(Type.INT))
            numericType = Type.INT;
        else
            numericType = Type.INT;

        arg1 = checkType(arg1, numericType);
        arg2 = checkType(arg2, numericType);

        // Arithmetic operators
        switch (tree.getType()) {
            case LegoLangLexer.PLUS:
                expr = arg1.getExpr().plus(arg2.getExpr());
                break;
            case LegoLangLexer.MINUS:
                expr = arg1.getExpr().minus(arg2.getExpr());
                break;
            case LegoLangLexer.MUL:
                expr = arg1.getExpr().mul(arg2.getExpr());
                break;
            case LegoLangLexer.DIV:
                expr = arg1.getExpr().div(arg2.getExpr());
                break;
            case LegoLangLexer.MOD:
                expr = arg1.getExpr().mod(arg2.getExpr());
                break;
            default:
                assert false;
        }

        JInvocation invoc = JExpr._new(numericType.toJava());
        invoc.arg(expr);
        return new TypedExpression(numericType, invoc);
    }

    private TypedExpression compileLiteral(CustomTree tree) {
        assert tree != null;

        JExpression expr = null;
        switch (tree.getType()) {
            case LegoLangLexer.INT: {
                JInvocation invoc = JExpr._new(Type.INT.toJava());
                invoc.arg(JExpr.lit(Long.parseLong(tree.getText())));
                expr = invoc;
                break;
            }
            case LegoLangLexer.FLOAT: {
                JInvocation invoc = JExpr._new(Type.FLOAT.toJava());
                invoc.arg(JExpr.lit(Float.parseFloat(tree.getText())));
                expr = invoc;
                break;
            }
            case LegoLangLexer.STRING: {
                expr = JExpr.lit(tree.getText());
                break;
            }
            case LegoLangLexer.BOOL: {
                boolean value = tree.getText().equals("true");
                expr = JExpr.lit(value).eq(JExpr.TRUE);
                break;
            }
            case LegoLangLexer.COLOR: {
                String hex = tree.getText();
                if (hex.length() == 4) {
                    char[] extend = { '#',
                        hex.charAt(1), hex.charAt(1),
                        hex.charAt(2), hex.charAt(2),
                        hex.charAt(3), hex.charAt(3)
                    };
                    hex = new String(extend);
                }
                JInvocation color = JExpr._new(Type.COLOR.toJava());
                color.arg(JExpr.lit(Integer.parseInt(hex.substring(1, 3), 16)));
                color.arg(JExpr.lit(Integer.parseInt(hex.substring(3, 5), 16)));
                color.arg(JExpr.lit(Integer.parseInt(hex.substring(5, 7), 16)));
                expr = color;
                break;
            }
            case LegoLangLexer.BUTTON: {
                JInvocation bt = JExpr._new(Type.BUTTON.toJava());
                bt.arg(tree.getText());
                expr = bt;
                break;
            }
            case LegoLangLexer.SENSOR_PORT: {
                JInvocation sp = JExpr._new(Type.SENSOR.toJava());
                sp.arg(tree.getText());
                expr = sp;
                break;
            }
            case LegoLangLexer.MOTOR: {
                JInvocation mt = JExpr._new(Type.MOTOR.toJava());
                mt.arg(tree.getText());
                expr = mt;
                break;
            }
            
        }

        assert (expr != null);
        return new TypedExpression(Type.fromTree(tree), expr);
    }

}
