package interp;

import parser.*;
import com.sun.codemodel.*;
import java.util.LinkedList;
import java.util.HashMap;
import llcclib.DefaultValue;

public class ScopeStack {

    private LinkedList<Scope> mScopes; // last scope is the main scope
    private HashMap<String, JMethod> legoFunctions;
    Scope global;

    public ScopeStack() {
        mScopes = new LinkedList<Scope>();
        
        legoFunctions = new HashMap<String, JMethod>();        
        global = new Scope();
        setLegoLibMap();
    }

    public void enterFunction(String funcName) {
        Scope s = new Scope();
        s.setFunctionName(funcName);
        mScopes.push(s);
    }

    public void leaveFunction() {
        assert mScopes.size() > 1;
        mScopes.pop();
    }

    public void enterSubScope() {
    	enterFunction(getCurrentFunctionName());
    }

    public void leaveSubScope() {
	    leaveFunction();
    }

    public boolean inMainScope() {
        return mScopes.size() < 2;
    }

    public void addFunction(String name, JDefinedClass function) {
        if (legoFunctions.get(name) != null) throw new RuntimeException("Function \""+name+"\" is reserved in LegoLang. Please use another name for your function.");
        Scope s = mScopes.peek();
        s.addFunction(name, function);
    }

    public JDefinedClass getFunction(String name){
        JDefinedClass function = mScopes.peek().getFunction(name);
        if (function == null) {
          for (int i = 1 ; i < mScopes.size() ; ++i) {
              function = mScopes.get(i).getFunction(name);
              if (function != null) return function;
          }
          throw new RuntimeException("Undeclared function: " + name);
        }
        return function;
    }

    public void addVariable(String name, JVar variable) {
        try {
            getVariable(name);
        } catch (RuntimeException e) {
            // Good, the variable doesn't exist yet, so we can
            // declare it now
            Scope s = mScopes.peek();
            s.addVariable(name, variable);
            return;
        }
        // Variable already exists - throw exception!
        throw new RuntimeException(
            "Multiple definitions of variable: " + name);
    }
    
    public void addGlobalVariable(String name, JVar variable) {
        if (global.getVariable(name) != null)
            throw new RuntimeException(
                "Multiple definitions of global variable: " + name);
        global.addVariable(name, variable);
    }

    public JVar getVariable(String name) {
        JVar variable = (mScopes.isEmpty())? global.getVariable(name) : mScopes.peek().getVariable(name);
        if (variable == null) {
            String actfunc = getCurrentFunctionName();
            for (int i = 1; i < mScopes.size() && actfunc == mScopes.get(i).getFunctionName(); ++i) {
                variable = mScopes.get(i).getVariable(name);
                if (variable != null) return variable;
            }
            variable = global.getVariable(name);
            if (variable != null) return variable;
            throw new RuntimeException("Undeclared variable: " + name);
        }
        return variable;
    }

    public String getCurrentFunctionName() {
        return mScopes.peek().getFunctionName();
    }
    
    public JMethod getLegoFunction(String function) {
        return legoFunctions.get(function);
    }
    
    private void setLegoLibMap() {
        JCodeModel cm = new JCodeModel();
        JDefinedClass libclass = cm.anonymousClass(cm.directClass("llcclib.LegoFunctions")); 
        JClass anotClass = cm.ref("PortType");
        JAnnotationUse annotation;
        
        String name = "move";
        JMethod func = libclass.method(JMod.PUBLIC | JMod.STATIC, void.class, name);
        func.param(Type.INT.toJava(), "speed");
        func.param(Type.MOTOR.toJava(), "motor");
        legoFunctions.put(name, func);
        
        name = "stop";
        func = libclass.method(JMod.PUBLIC | JMod.STATIC, void.class, name);
        func.param(Type.MOTOR.toJava(), "motor");
        legoFunctions.put(name, func);
        
        name = "rotate";
        func = libclass.method(JMod.PUBLIC | JMod.STATIC, void.class, name);
        func.param(Type.INT.toJava(), "degrees");
        func.param(Type.MOTOR.toJava(), "motor");
        legoFunctions.put(name, func);

        name = "rotateAndWait";
        func = libclass.method(JMod.PUBLIC | JMod.STATIC, void.class, name);
        func.param(Type.INT.toJava(), "degrees");
        func.param(Type.MOTOR.toJava(), "motor");
        legoFunctions.put(name, func);
        
        name = "rangeScan";
        func = libclass.method(JMod.PUBLIC | JMod.STATIC, Long.class, name);
        func.param(Type.INT.toJava(), "distance");
        JVar param = func.param(Type.SENSOR.toJava(), "port");
        param.annotate(anotClass).param("ultrasonic", true);
        legoFunctions.put(name, func);
        
        name = "isTouching";
        func = libclass.method(JMod.PUBLIC | JMod.STATIC, Boolean.class, name);
        param = func.param(Type.SENSOR.toJava(), "port");
        param.annotate(anotClass).param("touch", true);
        legoFunctions.put(name, func);
        
        name = "setLamp";
        func = libclass.method(JMod.PUBLIC | JMod.STATIC, void.class, name);
        func.param(Type.COLOR.toJava(), "color");
        param = func.param(Type.SENSOR.toJava(), "port");
        param.annotate(anotClass).param("light", true);
        legoFunctions.put(name, func);
        
        name = "readColor";
        func = libclass.method(JMod.PUBLIC | JMod.STATIC, Type.COLOR.toJava(), name);
        param = func.param(Type.SENSOR.toJava(), "port");
        param.annotate(anotClass).param("light", true);
        legoFunctions.put(name, func);

        name = "calibrateWhite";
        func = libclass.method(JMod.PUBLIC | JMod.STATIC, void.class, name);
        param = func.param(Type.SENSOR.toJava(), "port");
        param.annotate(anotClass).param("light", true);
        legoFunctions.put(name, func);

        name = "calibrateBlack";
        func = libclass.method(JMod.PUBLIC | JMod.STATIC, void.class, name);
        param = func.param(Type.SENSOR.toJava(), "port");
        param.annotate(anotClass).param("light", true);
        legoFunctions.put(name, func);
        
        name = "waitForButton";
        func = libclass.method(JMod.PUBLIC | JMod.STATIC, void.class, name);
        legoFunctions.put(name, func);
        
        name = "delay";
        func = libclass.method(JMod.PUBLIC | JMod.STATIC, void.class, name);
        func.param(Type.INT.toJava(), "time");
        legoFunctions.put(name, func);

        name = "clear";
        func = libclass.method(JMod.PUBLIC | JMod.STATIC, void.class, name);
        legoFunctions.put(name, func);
        
        name = "getRed";
        func = libclass.method(JMod.PUBLIC | JMod.STATIC, Long.class, name);
        func.param(Type.COLOR.toJava(), "color");
        legoFunctions.put(name, func);
        
        name = "getGreen";
        func = libclass.method(JMod.PUBLIC | JMod.STATIC, Long.class, name);
        func.param(Type.COLOR.toJava(), "color");
        legoFunctions.put(name, func);
        
        name = "getBlue";
        func = libclass.method(JMod.PUBLIC | JMod.STATIC, Long.class, name);
        func.param(Type.COLOR.toJava(), "color");
        legoFunctions.put(name, func);

        name = "colorSimilarity";
        func = libclass.method(JMod.PUBLIC | JMod.STATIC, Float.class, name);
        func.param(Type.COLOR.toJava(), "color1");
        func.param(Type.COLOR.toJava(), "color2");
        legoFunctions.put(name, func);

        name = "isColor";
        func = libclass.method(JMod.PUBLIC | JMod.STATIC, Boolean.class, name);
        func.param(Type.COLOR.toJava(), "color1");
        func.param(Type.COLOR.toJava(), "color2");
        param = func.param(Type.FLOAT.toJava(), "tolerance");
        annotation = param.annotate(DefaultValue.class);
        annotation.param("value", "0.0");
        legoFunctions.put(name, func);
        
        name = "isPressed";
        func = libclass.method(JMod.PUBLIC | JMod.STATIC, Long.class, name);
        func.param(Type.BUTTON.toJava(), "button"); 
        legoFunctions.put(name, func);
        
        name = "waitForUpAndDown";
        func = libclass.method(JMod.PUBLIC | JMod.STATIC, Long.class, name);
        func.param(Type.BUTTON.toJava(), "button");
        legoFunctions.put(name, func);
        
        name = "waitForPress";
        func = libclass.method(JMod.PUBLIC | JMod.STATIC, Long.class, name);
        param = func.param(Type.BUTTON.toJava(), "button"); 
        annotation = param.annotate(DefaultValue.class);
        annotation.param("button", "0");
        legoFunctions.put(name, func);

        name = "twoBeeps";
        func = libclass.method(JMod.PUBLIC | JMod.STATIC, void.class, name);
        legoFunctions.put(name, func);

        name = "beepSequence";
        func = libclass.method(JMod.PUBLIC | JMod.STATIC, void.class, name);
        legoFunctions.put(name, func);

        name = "beepSequenceUp";
        func = libclass.method(JMod.PUBLIC | JMod.STATIC, void.class, name);
        legoFunctions.put(name, func);

        name = "setVolume";
        func = libclass.method(JMod.PUBLIC | JMod.STATIC, void.class, name);
        func.param(Type.INT.toJava(), "volume");
        legoFunctions.put(name, func);

        name = "getVolume";
        func = libclass.method(JMod.PUBLIC | JMod.STATIC, Long.class, name);
        legoFunctions.put(name, func);

        name = "playSample";
        func = libclass.method(JMod.PUBLIC | JMod.STATIC, void.class, name);
        func.param(Type.newArray(Type.INT).toJava(), "samples");
        func.param(Type.INT.toJava(), "freq");
        param = func.param(Type.INT.toJava(), "volume");
        annotation = param.annotate(DefaultValue.class);
        annotation.param("volume", "null");
        legoFunctions.put(name, func);

        name = "playTone";
        func = libclass.method(JMod.PUBLIC | JMod.STATIC, void.class, name);
        func.param(Type.INT.toJava(), "freq");
        func.param(Type.INT.toJava(), "duration");
        param = func.param(Type.INT.toJava(), "volume");
        annotation = param.annotate(DefaultValue.class);
        annotation.param("volume", "null");
        legoFunctions.put(name, func);

        name = "buzz";
        func = libclass.method(JMod.PUBLIC | JMod.STATIC, void.class, name);
        legoFunctions.put(name, func);

        name = "abort";
        func = libclass.method(JMod.PUBLIC | JMod.STATIC, void.class, name);
        legoFunctions.put(name, func);

        name = "getTimestamp";
        func = libclass.method(JMod.PUBLIC | JMod.STATIC, Long.class, name);
        legoFunctions.put(name, func);

        name = "getUptime";
        func = libclass.method(JMod.PUBLIC | JMod.STATIC, Long.class, name);
        legoFunctions.put(name, func);

        name = "getUptimeNano";
        func = libclass.method(JMod.PUBLIC | JMod.STATIC, Long.class, name);
        legoFunctions.put(name, func);

        name = "round";
        func = libclass.method(JMod.PUBLIC | JMod.STATIC, Float.class, name);
        func.param(Type.FLOAT.toJava(), "number");
        func.param(Type.INT.toJava(), "precision");
        legoFunctions.put(name, func);

        name = "pow";
        func = libclass.method(JMod.PUBLIC | JMod.STATIC, Float.class, name);
        func.param(Type.FLOAT.toJava(), "base");
        func.param(Type.FLOAT.toJava(), "exponent");
        legoFunctions.put(name, func);

        name = "random";
        func = libclass.method(JMod.PUBLIC | JMod.STATIC, Long.class, name);
        param = func.param(Type.INT.toJava(), "start"); 
        annotation = param.annotate(DefaultValue.class);
        annotation.param("start", "-1");//default value ignored
        param = func.param(Type.INT.toJava(), "end"); 
        annotation = param.annotate(DefaultValue.class);
        annotation.param("end", "-1");//default value ignored
        legoFunctions.put(name, func);
        
        name = "washMyDishes";
        func = libclass.method(JMod.PUBLIC | JMod.STATIC, void.class, name);
        func.param(Type.MOTOR.toJava(), "roda1");
        func.param(Type.MOTOR.toJava(), "roda2");
        param = func.param(Type.SENSOR.toJava(), "light");
        param.annotate(anotClass).param("light", true);
        legoFunctions.put(name, func);
        
        name = "stopLamp";
        func = libclass.method(JMod.PUBLIC | JMod.STATIC, void.class, name);
        param = func.param(Type.SENSOR.toJava(), "port");
        param.annotate(anotClass).param("light", true);
        legoFunctions.put(name, func);
        
    }

}
