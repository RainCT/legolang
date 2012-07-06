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

public class TypedExpression {

    private Type mType;
    private JExpression mExpr;

    public static TypedExpression VOID = new TypedExpression(Type.VOID, null);

	public TypedExpression(Type type, JExpression expr) {
        mType = type;
        mExpr = expr;
	}

    public TypedExpression(JVar variable) {
        mType = Type.fromJava(variable.type());
        mExpr = variable;
    }

    public Type getType() {
        return mType;
    }

    public void setExpr(JExpression expr) {
        mExpr = expr;
    }

    public JExpression getExpr() {
        return mExpr;
    }

    public JExpression getBasicExpr() {
        if (mType.isType(Type.INT)) {
            return mExpr.invoke("longValue");
        } else if (mType.isType(Type.FLOAT)) {
            return mExpr.invoke("floatValue");
        } else if (mType.isType(Type.BOOL)) {
            return mExpr;
        }

        assert false;
        return null;
    }

}
