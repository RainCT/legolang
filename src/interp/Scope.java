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
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import parser.*;

public class Scope {

    private HashMap<String,JDefinedClass> functionMap;
    private HashMap<String,JVar> variableMap;
    private String mFunctionName;

	public Scope() {
        functionMap = new HashMap<String, JDefinedClass>();
        variableMap = new HashMap<String, JVar>();
	}

    private static boolean isReservedKeyword(String name) {
        List<String> keywords = Arrays.asList(
            // Write any keywords reserved for future use here
            // (or used by Java and not mangled by us)
            "try", "catch"
        );
        return keywords.contains(name);
    }

    public void addFunction(String name, JDefinedClass function) {
        if (functionMap.containsKey(name)) {
            throw new RuntimeException("Multiple definitions of function " + name);
        }
        if (isReservedKeyword(name)) {
            throw new RuntimeException("Invalid name for a function: " + name);
        }

        functionMap.put(name, function);
    }

    public JDefinedClass getFunction(String name) {
        JDefinedClass function = functionMap.get(name);
        return function;
    }

    public void addVariable(String name, JVar variable) {
        if (variableMap.containsKey(name)) {
            throw new RuntimeException("Multiple definitions of variable " + name);
        }
        if (isReservedKeyword(name)) {
            throw new RuntimeException("Invalid variable name: " + name);
        }

        variableMap.put(name, variable);
    }

    public JVar getVariable(String name) {
        JVar variable = variableMap.get(name);
        return variable;
    }

    public void setFunctionName(String s) {
	    mFunctionName = s;
    }

    public String getFunctionName() {
	    return mFunctionName;
    }

}
