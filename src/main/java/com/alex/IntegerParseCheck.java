package com.alex;

import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

/**
 * Prevent to use Integer.parseInt
 */
public class IntegerParseCheck extends AbstractCheck {

    public static final String MSG_KEY = "integer.noParseInt";

    public int[] getDefaultTokens() {
        return getRequiredTokens();
    }

    public int[] getAcceptableTokens() {
        return new int[] {
                TokenTypes.METHOD_CALL
        };
    }

    public int[] getRequiredTokens() {
        return new int[] {
                TokenTypes.METHOD_CALL
        };
    }

    @Override
    public void visitToken(final DetailAST ast) {
        DetailAST dotNode = ast.getFirstChild();
        if (dotNode == null || dotNode.getType() != TokenTypes.DOT) {
            return;
        }

        DetailAST firstChild = dotNode.getFirstChild();
        DetailAST lastChild = dotNode.getLastChild();
        if (firstChild == null
                || firstChild.getType() != TokenTypes.IDENT
                || lastChild == null
                || lastChild.getType() != TokenTypes.IDENT
                ) {
            return;
        }

        String className = firstChild.getText();
        String methodName = lastChild.getText();
        if (("Integer".equals(className) && "parseInt".equals(methodName)))
        log(ast.getLineNo(),
                ast.getColumnNo(),
                MSG_KEY);
    }
}
