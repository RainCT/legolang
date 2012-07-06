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

import org.antlr.runtime.tree.*;
import org.antlr.runtime.Token;

import parser.*;

import com.sun.codemodel.*;

public class CustomTree extends CommonTree {

    private String mSignalName;
    private JClass mSignalCallback;

    public CustomTree(Token t) {
        super(t);
    }

    public CustomTree getChild(int i) {
        return (CustomTree) super.getChild(i);
    }

    // For ON_SIGNAL:

    public void setSignalData(String name, JClass callback) {
        assert getType() == LegoLangLexer.ON_SIGNAL;
        mSignalName = name;
        mSignalCallback = callback;
    }

    public String getSignalName() {
        assert getType() == LegoLangLexer.ON_SIGNAL;
        return mSignalName;
    }

    public JClass getSignalCallback() {
        assert getType() == LegoLangLexer.ON_SIGNAL;
        return mSignalCallback;
    }

}
