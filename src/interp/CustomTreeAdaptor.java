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

import org.antlr.runtime.*;
import org.antlr.runtime.tree.*;

import parser.*;

public class CustomTreeAdaptor extends CommonTreeAdaptor {

    public Object create(Token payload) {
        return new CustomTree(payload);
    }

    public Object dupNode(Object t) {
        if (t == null) return null;
        return create(((CustomTree) t).token);
    }

    public Object errorNode(TokenStream input, Token start, Token stop,
            RecognitionException e) {
        return null;
    }

}
