package com.alex;

import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

import javax.sound.midi.Soundbank;

/**
 * Prevent calling BigDecimal.divide(...) without define ROUND_MODE
 */
public class BigDecimalCheck extends AbstractCheck {
    public static final String MSG_KEY = "bigDecimal.noRoundingMode";

    private final int SAFE_DEPTH = 200;

    public int[] getDefaultTokens() {
        return getRequiredTokens();
    }

    public int[] getAcceptableTokens() {
        return getRequiredTokens();
    }

    public int[] getRequiredTokens() {
        return new int[] {TokenTypes.VARIABLE_DEF, TokenTypes.PARAMETER_DEF};
    }

    @Override
    public void visitToken(final DetailAST ast) {
        if (!isDefineBigDecimal(ast)) {
            return;
        }
        final DetailAST variable = ast.findFirstToken(TokenTypes.IDENT);

        DetailAST beginAST = ast;
        if (ast.getType() == TokenTypes.PARAMETER_DEF) {
            beginAST = getBeginASTOfParameterDef(ast);
        }

        if (beginAST == null) {
            return;
        }

        checkDivide(beginAST, variable, 0);
    }

    /**
     * get the first DetailAST of method content
     * @param parameterDef
     * @return
     */
    private DetailAST getBeginASTOfParameterDef (DetailAST parameterDef) {
        DetailAST methodDef = parameterDef.getParent().getParent();
        if (methodDef.getType() != TokenTypes.METHOD_DEF) {
            return null;
        }

        DetailAST slist = methodDef.findFirstToken(TokenTypes.SLIST);
        if (slist == null) {
            return null;
        }
        return slist.getFirstChild();
    }

    /**
     * Determine if define a BigDecimal variable
     * @param ast
     * @return
     */
    private boolean isDefineBigDecimal(final DetailAST ast) {
        if (ast == null) {
            return false;
        }
        final DetailAST type = ast.findFirstToken(TokenTypes.TYPE);
        if (type == null) {
            return false;
        }
        final DetailAST typeId = type.findFirstToken(TokenTypes.IDENT);
        if (typeId == null) {
            return false;
        }
        return "BigDecimal".equals(typeId.getText());
    }

    /**
     * check all lines
     * @param beginAST
     * @param variable
     */
    private void checkDivide(final DetailAST beginAST, final DetailAST variable, int depth) {
        if (depth++ > SAFE_DEPTH) {
            return;
        }

        DetailAST ast = beginAST;
        do {
            if (ast.getType() != TokenTypes.METHOD_CALL && ast.getFirstChild() == null) {
                ast = ast.getNextSibling();
                continue;
            }

            if (ast.getType() == TokenTypes.METHOD_CALL
                    && isCallingDivide(ast, variable)
                    && isOnly1Param(ast)) {
                log(ast.getLineNo(),
                        ast.getColumnNo(),
                        MSG_KEY, variable.getText());
            }

            if (ast.getFirstChild() != null) {
                checkDivide(ast.getFirstChild(), variable, depth);
            }
            ast = ast.getNextSibling();
        } while (ast != null);
    }

    /**
     * ast type is METHOD_CALL
     * @param methodCall
     * @return
     */
    private boolean isCallingDivide(final DetailAST methodCall, final DetailAST variable) {
        if (methodCall == null || variable == null) {
            return false;
        }

        DetailAST dot = methodCall.findFirstToken(TokenTypes.DOT);
        if (dot == null) {
            return false;
        }

        DetailAST variableName = dot.getFirstChild();
        if (variableName == null
                || variableName.getType() != TokenTypes.IDENT
                || !variable.getText().equals(variableName.getText())) {
            return false;
        }

        DetailAST methodName = dot.getLastChild();
        if (methodName == null
                || methodName.getType() != TokenTypes.IDENT
                || !"divide".equals(methodName.getText())) {
            return false;
        }
        return true;
    }

    /**
     * Determine if this methodCall only has one param
     * @param methodCall
     * @return
     */
    private boolean isOnly1Param(final DetailAST methodCall) {
        if (methodCall == null) {
            return false;
        }

        DetailAST elist = methodCall.findFirstToken(TokenTypes.ELIST);
        if (elist == null) {
            return false;
        }

        DetailAST fistExpr = elist.getFirstChild();
        if (fistExpr == null) {
            return false;
        }

        return fistExpr.getNextSibling() == null;
    }
}
