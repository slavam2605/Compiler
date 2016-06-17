package moklev.parsing;

/**
 * @author Моклев Вячеслав
 */
public class CppParser implements Parser {
    private String string;
    private int pos;

    public CppParser(String string) {
        this(string, 0);
    }

    public CppParser(String string, int pos) {
        this.string = string;
        this.pos = pos;
    }

    @Override
    public String getString() {
        return string;
    }

    @Override
    public void setString(String string) {
        this.string = string;
        pos = 0;
    }

    @Override
    public int getPosition() {
        return pos;
    }

    @Override
    public void setPosition(int pos) {
        if (string == null || pos < 0 || pos > string.length()) {
            throw new IndexOutOfBoundsException("pos is not in range of source string");
        }
        this.pos = pos;
    }

    private static final String[] types = {"int", "char"};

    private Type parseType() {
        int initPos = pos;
        for (String type : types) {
            if (!Parsers.parseSequence(this, type)) {
                pos = initPos;
                continue;
            }
            int pointerLevel = 0;
            while (Parsers.parseSequence(this, "*")) {
                pointerLevel++;
            }
            StringBuilder sb = new StringBuilder(type);
            for (int k = 0; k < pointerLevel; k++) {
                sb.append('*');
            }
            return new Type(sb.toString());
        }
        return null;
    }

    private ArgsList parseArgsList() {
        int initPos = pos;
        ArgsList argsList = new ArgsList();
        Type type = parseType();
        String lexem = Parsers.parseLexem(this);
        if (type == null || lexem == null) {
            pos = initPos;
            return argsList;
        }
        argsList.add(type, lexem);
        while (true) {
            initPos = pos;
            boolean flag = Parsers.parseSequence(this, ",");
            type = parseType();
            lexem = Parsers.parseLexem(this);
            if (!flag || type == null || lexem == null) {
                pos = initPos;
                return argsList;
            }
            argsList.add(type, lexem);
        }
    }

    private Function parseFunction() {
        int initPos = pos;
        Type type = parseType();
        String lexem = Parsers.parseLexem(this);
        boolean leftBracket = Parsers.parseSequence(this, "(");
        ArgsList argsList = parseArgsList();
        boolean rightBracket = Parsers.parseSequence(this, ")");
        BlockCode blockCode = parseBlockCode();
        if (type == null || lexem == null || !leftBracket
                || argsList == null || !rightBracket || blockCode == null ) {
            pos = initPos;
            return null;
        }
        return new Function(type, argsList, blockCode, lexem);
    }

    private FunctionDefinition parseFunctionDefinition() {
        int initPos = pos;
        Type type = parseType();
        String lexem = Parsers.parseLexem(this);
        boolean leftBracket = Parsers.parseSequence(this, "(");
        ArgsList argsList = parseArgsList();
        boolean rightBracket = Parsers.parseSequence(this, ")");
        boolean semicolon = Parsers.parseSequence(this, ";");
        if (type == null || lexem == null || !leftBracket
                || argsList == null || !rightBracket || !semicolon ) {
            pos = initPos;
            return null;
        }
        return new FunctionDefinition(type, argsList, lexem);
    }

    private VarDefinition parseVarDefinition() {
        int initPos = pos;
        Type type = parseType();
        String lexem = Parsers.parseLexem(this);
        if (type == null || lexem == null) {
            pos = initPos;
            return null;
        }
        return new VarDefinition(type, lexem);
    }

    private ExprList parseExprList() {
        int initPos = pos;
        ExprList exprList = new ExprList();
        Expression expression = parseExpression();
        if (expression == null) {
            pos = initPos;
            return exprList;
        }
        exprList.add(expression);
        while (true) {
            initPos = pos;
            boolean comma = Parsers.parseSequence(this, ",");
            if (comma) {
                expression = parseExpression();
            }
            if (!comma || expression == null) {
                pos = initPos;
                return exprList;
            }
            exprList.add(expression);
        }
    }

    private Factor parseFactor() {
        int initPos = pos;
        boolean doubleQuote = Parsers.parseSequence(this, "\"");
        String s = Parsers.parseUntil(this, '"');
        if (doubleQuote && s != null) {
            return new Factor(s, true);
        }
        pos = initPos;
        boolean quote = Parsers.parseSequence(this, "'");
        char c = Parsers.parseChar(this);
        char q = Parsers.parseChar(this);
        if (quote && q == '\'') {
            return new Factor(c);
        }
        pos = initPos;
        String lexem = Parsers.parseLexem(this);
        boolean leftBracket = Parsers.parseSequence(this, "(");
        ExprList exprList = null;
        if (lexem != null && leftBracket) {
            exprList = parseExprList();
        }
        boolean rightBracket = Parsers.parseSequence(this, ")");
        if (lexem != null && leftBracket && exprList != null && rightBracket) {
            return new Factor(exprList, lexem);
        }
        pos = initPos;
        lexem = Parsers.parseLexem(this);
        if (lexem != null) {
            return new Factor(lexem);
        }
        pos = initPos;
        String number = Parsers.parseInteger(this);
        if (number != null) {
            return new Factor(number);
        }
        pos = initPos;
        leftBracket = Parsers.parseSequence(this, "(");
        Expression expression = null;
        if (leftBracket) {
            expression = parseExpression();
        }
        rightBracket = Parsers.parseSequence(this, ")");
        if (!leftBracket || expression == null || !rightBracket) {
            pos = initPos;
            return null;
        }
        return new Factor(expression);
    }

    private Term parseTerm() {
        int initPos = pos;
        Term term = null;
        if (Parsers.parseSequence(this, "*")) {
            term = parseTerm();
        }
        if (term != null) {
            return new Term(term);
        }
        pos = initPos;
        term = null;
        if (Parsers.parseSequence(this, "&")) {
            term = parseTerm();
        }
        if (term != null) {
            return new Term(term, Term.ENREFERENCE);
        }
        pos = initPos;
        term = null;
        if (Parsers.parseSequence(this, "!")) {
            term = parseTerm();
        }
        if (term != null) {
            return new Term(term, Term.NOT);
        }
        pos = initPos;
        boolean leftBracket = Parsers.parseSequence(this, "(");
        Type type = parseType();
        boolean rightBracket = Parsers.parseSequence(this, ")");
        term = null;
        if (leftBracket && type != null && rightBracket) {
            term = parseTerm();
        }
        if (term != null) {
            return new Term(type, term);
        }
        // <Factor> [ '[' <Expression> ']' ]*
        pos = initPos;
        Factor factor = parseFactor();
        if (factor == null) {
            pos = initPos;
            return null;
        }
        term = new Term(factor);
        while (true) {
            initPos = pos;
            leftBracket = Parsers.parseSequence(this, "[");
            Expression expression = null;
            if (leftBracket) {
                expression = parseExpression();
            }
            rightBracket = Parsers.parseSequence(this, "]");
            if (expression == null || !rightBracket) {
                pos = initPos;
                return term;
            }
            term = new Term(term, expression);
        }
    }

    private MultExpression parseMultExpression() {
        int initPos = pos;
        Term term = parseTerm();
        if (term == null) {
            pos = initPos;
            return null;
        }
        MultExpression multExpression = new MultExpression(term);
        while (true) {
            initPos = pos;
            int op;
            if (Parsers.parseSequence(this, "*")) {
                op = MultExpression.MUL_OP;
            } else if (Parsers.parseSequence(this, "/")) {
                op = MultExpression.DIV_OP;
            } else if (Parsers.parseSequence(this, "%")) {
                op = MultExpression.MOD_OP;
            } else {
                pos = initPos;
                return multExpression;
            }
            term = parseTerm();
            if (term == null) {
                pos = initPos;
                return multExpression;
            }
            multExpression.add(term, op);
        }
    }

    private PlusExpression parsePlusExpression() {
        int initPos = pos;
        MultExpression multExpression = parseMultExpression();
        if (multExpression == null) {
            pos = initPos;
            return null;
        }
        PlusExpression plusExpression = new PlusExpression(multExpression);
        while (true) {
            initPos = pos;
            boolean isPlus;
            if (Parsers.parseSequence(this, "+")) {
                isPlus = true;
            } else if (Parsers.parseSequence(this, "-")) {
                isPlus = false;
            } else {
                pos = initPos;
                return plusExpression;
            }
            multExpression = parseMultExpression();
            if (multExpression == null) {
                pos = initPos;
                return plusExpression;
            }
            plusExpression.add(multExpression, isPlus);
        }
    }

    private CompareExpression parseCompareExpression() {
        int initPos = pos;
        PlusExpression plusExpression = parsePlusExpression();
        if (plusExpression == null) {
            pos = initPos;
            return null;
        }
        CompareExpression compareExpression = new CompareExpression(plusExpression);
        while (true) {
            initPos = pos;
            int op;
            if (Parsers.parseSequence(this, ">=")) {
                op = CompareExpression.GRE_OP;
            } else if (Parsers.parseSequence(this, "<=")) {
                op = CompareExpression.LSE_OP;
            } else if (Parsers.parseSequence(this, ">")) {
                op = CompareExpression.GR_OP;
            } else if (Parsers.parseSequence(this, "<")) {
                op = CompareExpression.LS_OP;
            } else {
                pos = initPos;
                return compareExpression;
            }
            plusExpression = parsePlusExpression();
            if (plusExpression == null) {
                pos = initPos;
                return compareExpression;
            }
            compareExpression.add(plusExpression, op);
        }
    }

    private EqualExpression parseEqualExpression() {
        int initPos = pos;
        CompareExpression compareExpression = parseCompareExpression();
        if (compareExpression == null) {
            pos = initPos;
            return null;
        }
        EqualExpression equalExpression = new EqualExpression(compareExpression);
        while (true) {
            initPos = pos;
            int op;
            if (Parsers.parseSequence(this, "==")) {
                op = EqualExpression.EQ_OP;
            } else if (Parsers.parseSequence(this, "!=")) {
                op = EqualExpression.NEQ_OP;
            } else {
                pos = initPos;
                return equalExpression;
            }
            compareExpression = parseCompareExpression();
            if (compareExpression == null) {
                pos = initPos;
                return equalExpression;
            }
            equalExpression.add(compareExpression, op);
        }
    }

    private AndExpression parseAndExpression() {
        int initPos = pos;
        EqualExpression equalExpression = parseEqualExpression();
        if (equalExpression == null) {
            pos = initPos;
            return null;
        }
        AndExpression andExpression = new AndExpression(equalExpression);
        while (true) {
            initPos = pos;
            int op;
            if (Parsers.parseSequence(this, "&&")) {
                op = AndExpression.AND_OP;
            }  else {
                pos = initPos;
                return andExpression;
            }
            equalExpression = parseEqualExpression();
            if (equalExpression == null) {
                pos = initPos;
                return andExpression;
            }
            andExpression.add(equalExpression, op);
        }
    }

    private OrExpression parseOrExpression() {
        int initPos = pos;
        AndExpression andExpression = parseAndExpression();
        if (andExpression == null) {
            pos = initPos;
            return null;
        }
        OrExpression orExpression = new OrExpression(andExpression);
        while (true) {
            initPos = pos;
            int op;
            if (Parsers.parseSequence(this, "||")) {
                op = OrExpression.OR_OP;
            }  else {
                pos = initPos;
                return orExpression;
            }
            andExpression = parseAndExpression();
            if (andExpression == null) {
                pos = initPos;
                return orExpression;
            }
            orExpression.add(andExpression, op);
        }
    }

    private Expression parseExpression() {
        int initPos = pos;
        Term term = parseTerm();
        boolean flag = Parsers.parseSequence(this, "=");
        Expression expression = null;
        if (term != null && flag) {
            expression = parseExpression();
        }
        if (expression != null) {
            return new Expression(term, expression);
        }
        pos = initPos;
        OrExpression orExpression = parseOrExpression();
        if (orExpression != null) {
            return new Expression(orExpression);
        }
        pos = initPos;
        return null;
    }

    private LineCode parseLineCode() {
        int initPos = pos;
        boolean flag = Parsers.parseSequence(this, "if");
        boolean leftBracket = Parsers.parseSequence(this, "(");
        Expression expression = parseExpression();
        boolean rightBracket = Parsers.parseSequence(this, ")");
        LineCode lineCode = null;
        if (flag && leftBracket && expression != null && rightBracket) {
            lineCode = parseLineCode();
        }
        if (lineCode != null) {
            return new LineCode(expression, lineCode, LineCode.IF_TYPE);
        }
        pos = initPos;
        BlockCode blockCode = parseBlockCode();
        if (blockCode != null) {
            return new LineCode(blockCode);
        }
        pos = initPos;
        String lexem = Parsers.parseLexem(this);
        if (lexem != null && lexem.equals("return")) {
            expression = parseExpression();
            boolean semicolon = Parsers.parseSequence(this, ";");
            if (semicolon && expression != null) {
                return new LineCode(expression, LineCode.RETURN_TYPE);
            }
        }
        pos = initPos;
        VarDefinition varDefinition = parseVarDefinition();
        boolean semicolon = Parsers.parseSequence(this, ";");
        if (varDefinition != null && semicolon) {
            return new LineCode(varDefinition);
        }
        pos = initPos;
        expression = parseExpression();
        semicolon = Parsers.parseSequence(this, ";");
        if (expression != null && semicolon) {
            return new LineCode(expression);
        }
        pos = initPos;
        flag = Parsers.parseSequence(this, "for");
        leftBracket = Parsers.parseSequence(this, "(");
        Expression init = null;
        if (flag && leftBracket) {
            init = parseExpression();
        }
        semicolon = Parsers.parseSequence(this, ";");
        Expression condition = null;
        if (init != null && semicolon) {
            condition = parseExpression();
        }
        boolean semicolon2 = Parsers.parseSequence(this, ";");
        Expression iteration = null;
        if (condition != null && semicolon2) {
            iteration = parseExpression();
        }
        rightBracket = Parsers.parseSequence(this, ")");
        lineCode = null;
        if (iteration != null && rightBracket) {
            lineCode = parseLineCode();
        }
        if (lineCode != null) {
            return new LineCode(init, condition, iteration, lineCode);
        }
        pos = initPos;
        return null;
    }

    private LinesCode parseLinesCode() {
        int initPos = pos;
        LinesCode linesCode = new LinesCode();
        while (true) {
            LineCode lineCode = parseLineCode();
            if (lineCode == null) {
                pos = initPos;
                return linesCode;
            }
            initPos = pos;
            linesCode.add(lineCode);
        }
    }

    private BlockCode parseBlockCode() {
        int initPos = pos;
        if (!Parsers.parseSequence(this, "{")) {
            pos = initPos;
            return null;
        }
        LinesCode linesCode = parseLinesCode();
        if (linesCode == null) {
            pos = initPos;
            return null;
        }
        if (!Parsers.parseSequence(this, "}")) {
            pos = initPos;
            return null;
        }
        return new BlockCode(linesCode);
    }

    private Program parseProgram() {
        int initPos;
        Program program = new Program();
        Function function;
        FunctionDefinition definition;
        while (true) {
            initPos = pos;
            function = parseFunction();
            definition = null;
            if (function == null) {
                definition = parseFunctionDefinition();
            }
            if (function == null && definition == null) {
                pos = initPos;
                return program;
            }
            if (function != null) {
                program.add(function);
            } else {
                program.add(definition);
            }
        }
    }

    @Override
    public Token parse() {
        Program program = parseProgram();
        Parsers.skipSpaces(this);
        if (pos != string.length()) {
            return null;
        } else {
            return program;
        }
    }
}