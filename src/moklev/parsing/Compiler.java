package moklev.parsing;

import com.sun.xml.internal.messaging.saaj.packaging.mime.util.ASCIIUtility;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Моклев Вячеслав
 */
public class Compiler {

    public static Scope scope = new Scope();
    public static int rbpOffset = 0;
    public static Map<String, Integer> labelNumber = new HashMap<>();
    public static Map<String, TypeInstance> returnType;
    public static Map<String, List<TypeInstance>> argsType;
    public static List<String> rodata = new ArrayList<>();

    private static String newLabel(String name) {
        Integer c = labelNumber.get(name);
        int p;
        if (c == null) {
            p = 0;
            labelNumber.put(name, 1);
        } else {
            p = c;
            labelNumber.put(name, c + 1);
        }
        return "." + name + p;
    }

    private static String newLabel() {
        return newLabel("label");
    }

    private static void deduceFactorType(Factor factor) {
        switch (factor.mode) {
            case Factor.FUNCTION:
                for (Token expr : factor.get(0).contents) {
                    deduceExpressionType((Expression) expr);
                }
                factor.typeInstance = returnType.get(factor.value);
                break;
            case Factor.EXPR:
                deduceExpressionType((Expression) factor.get(0));
                factor.typeInstance = ((TypedToken) factor.get(0)).typeInstance;
                break;
            case Factor.CHAR:
                factor.typeInstance = TypeInstance.CHAR;
                break;
            case Factor.STRING:
                factor.typeInstance = TypeInstance.PCHAR;
                break;
            case Factor.NUMBER:
                factor.typeInstance = TypeInstance.INT;
                break;
            case Factor.VAR:
                factor.typeInstance = scope.getType(factor.value);
                break;
            default:
                throw new TypificationException("Unknown mode: " + factor.mode);
        }
    }

    private static void deduceTermType(Term term) {
        switch (term.mode) {
            case Term.DEREFERENCE:
                deduceTermType((Term) term.get(0));
                term.typeInstance = ((TypedToken) term.get(0)).typeInstance.dereference();
                break;
            case Term.CAST:
                deduceTermType((Term) term.get(1));
                term.typeInstance = new TypeInstance(term.get(0).toString());
                break;
            case Term.FACTOR:
                deduceFactorType((Factor) term.get(0));
                term.typeInstance = ((TypedToken) term.get(0)).typeInstance;
                break;
            case Term.GET:
                deduceTermType((Term) term.get(0));
                deduceExpressionType((Expression) term.get(1));
                term.typeInstance = ((TypedToken) term.get(0)).typeInstance.dereference();
                break;
            case Term.NOT:
                deduceTermType((Term) term.get(0));
                term.typeInstance = ((TypedToken) term.get(0)).typeInstance;
                break;
            case Term.ENREFERENCE:
                deduceTermType((Term) term.get(0));
                term.typeInstance = ((TypedToken) term.get(0)).typeInstance.enreference();
                break;
            default:
                throw new TypificationException("Unknown mode: " + term.mode);
        }
    }

    private static void deduceMultExpressionType(MultExpression multExpression) {
        for (Token token: multExpression.contents) {
            deduceTermType((Term) token);
        }
        multExpression.typeInstance = ((TypedToken) multExpression.get(0)).typeInstance;
    }

    private static void deducePlusExpressionType(PlusExpression plusExpression) {
        for (Token token: plusExpression.contents) {
            deduceMultExpressionType((MultExpression) token);
        }
        plusExpression.typeInstance = ((TypedToken) plusExpression.get(0)).typeInstance;
    }

    private static void deduceCompareExpressionType(CompareExpression compareExpression) {
        for (Token token: compareExpression.contents) {
            deducePlusExpressionType((PlusExpression) token);
        }
        compareExpression.typeInstance = ((TypedToken) compareExpression.get(0)).typeInstance;
    }

    private static void deduceEqualExpressionType(EqualExpression equalExpression) {
        for (Token token: equalExpression.contents) {
            deduceCompareExpressionType((CompareExpression) token);
        }
        equalExpression.typeInstance = ((TypedToken) equalExpression.get(0)).typeInstance;
    }

    private static void deduceAndExpressionType(AndExpression andExpression) {
        for (Token token: andExpression.contents) {
            deduceEqualExpressionType((EqualExpression) token);
        }
        andExpression.typeInstance = ((TypedToken) andExpression.get(0)).typeInstance;
    }

    private static void deduceOrExpressionType(OrExpression orExpression) {
        for (Token token: orExpression.contents) {
            deduceAndExpressionType((AndExpression) token);
        }
        orExpression.typeInstance = ((TypedToken) orExpression.get(0)).typeInstance;
    }

    private static void deduceExpressionType(Expression expression) {
        switch (expression.mode) {
            case Expression.ASSIGN:
                deduceTermType((Term) expression.get(0));
                deduceExpressionType((Expression) expression.get(1));
                expression.typeInstance = ((TypedToken) expression.get(1)).typeInstance;
                break;
            case Expression.SIMPLE:
                deduceOrExpressionType((OrExpression) expression.get(0));
                expression.typeInstance = ((TypedToken) expression.get(0)).typeInstance;
                break;
            default:
                throw new TypificationException("Unknown mode: " + expression.mode);
        }
    }

    public static void compileFactorAddress(Factor factor, List<String> result) {
        switch (factor.mode) {
            case Factor.VAR:
                int offset = scope.get(factor.value);
                result.add("lea r9, [rbp + " + offset + "]");
                result.add("push r9");
                break;
            case Factor.EXPR:
                compileExpressionAddress((Expression) factor.get(0), result);
                break;
            default:
                throw new CompilationException("Assignable expression is not lvalue");
        }
    }

    public static void compileTermAddress(Term term, List<String> result) {
        switch (term.mode) {
            case Term.DEREFERENCE:
                compileTerm((Term) term.get(0), result);
                break;
            case Term.GET:
                compileTerm((Term) term.get(0), result);
                compileExpression((Expression) term.get(1), result);
                result.add("pop rax");
                result.add("pop r8");
                result.add("mov r9, " + term.typeInstance.sizeof());
                result.add("mul r9");
                result.add("add r8, rax");
                result.add("push r8");
                break;
            case Term.FACTOR:
                compileFactorAddress((Factor) term.get(0), result);
                break;
            default:
                throw new CompilationException("Assignable expression is not lvalue");
        }
    }

    public static void compileMultExpressionAddress(MultExpression multExpression, List<String> result) {
        if (multExpression.contents.size() == 1) {
            compileTermAddress((Term) multExpression.get(0), result);
            return;
        }
        throw new CompilationException("Assignable expression is not lvalue");
    }

    public static void compilePlusExpressionAddress(PlusExpression plusExpression, List<String> result) {
        if (plusExpression.contents.size() == 1) {
            compileMultExpressionAddress((MultExpression) plusExpression.get(0), result);
            return;
        }
        throw new CompilationException("Assignable expression is not lvalue");
    }

    public static void compileCompareExpressionAddress(CompareExpression compareExpression, List<String> result) {
        if (compareExpression.contents.size() == 1) {
            compilePlusExpressionAddress((PlusExpression) compareExpression.get(0), result);
            return;
        }
        throw new CompilationException("Assignable expression is not lvalue");
    }

    public static void compileEqualExpressionAddress(EqualExpression equalExpression, List<String> result) {
        if (equalExpression.contents.size() == 1) {
            compileCompareExpressionAddress((CompareExpression) equalExpression.get(0), result);
            return;
        }
        throw new CompilationException("Assignable expression is not lvalue");
    }

    public static void compileAndExpressionAddress(AndExpression andExpression, List<String> result) {
        if (andExpression.contents.size() == 1) {
            compileEqualExpressionAddress((EqualExpression) andExpression.get(0), result);
            return;
        }
        throw new CompilationException("Assignable expression is not lvalue");
    }

    public static void compileOrExpressionAddress(OrExpression orExpression, List<String> result) {
        if (orExpression.contents.size() == 1) {
            compileAndExpressionAddress((AndExpression) orExpression.get(0), result);
            return;
        }
        throw new CompilationException("Assignable expression is not lvalue");
    }

    public static void compileExpressionAddress(Expression expression, List<String> result) {
        switch (expression.mode) {
            case Expression.SIMPLE:
                compileOrExpressionAddress((OrExpression) expression.get(0), result);
                break;
            default:
                throw new CompilationException("Assignable expression is not lvalue");
        }
    }

    public static void compileFactor(Factor factor, List<String> result) {
        switch (factor.mode) {
            case Factor.EXPR:
                compileExpression((Expression) factor.get(0), result);
                break;
            case Factor.CHAR:
                // TODO ascii?
                result.add("mov r8, " + ASCIIUtility.getBytes(factor.value)[0]);
                result.add("push r8");
                break;
            case Factor.STRING:
                String label = newLabel("string");
                rodata.add(label + ": db \"" + factor.value + "\", 0");
                result.add("mov r8, " + label);
                result.add("push r8");
                break;
            case Factor.NUMBER:
                result.add("mov r8, " + factor.value);
                result.add("push r8");
                break;
            case Factor.FUNCTION:
                // TODO add vararg
                ExprList exprList = (ExprList) factor.get(0);
                List<TypeInstance> types = argsType.get(factor.value);
                if (types == null) {
                    throw new CompilationException("Missing declaration of " + factor.value);
                }
                if (types.size() != exprList.contents.size()) {
                    throw new CompilationException("Mismatched count of arguments: required " + types.size() + ", found: " + exprList.contents.size());
                }
                for (int i = 0; i < types.size(); i++) {
                    if (!types.get(i).equals(((TypedToken) exprList.get(i)).typeInstance)) {
                        throw new CompilationException("Type mismatch in function " + factor.value + ", argument " + (i + 1) + ": required " + types.get(i) + ", found " + ((TypedToken) exprList.get(i)).typeInstance);
                    }
                }
                if (exprList.contents.size() < 4) {
                    result.add("sub rsp, " + 8 * (4 - exprList.contents.size()));
                }
                for (int i = exprList.contents.size() - 1; i >= 0; i--) {
                    compileExpression((Expression) exprList.get(i), result);
                }
                result.add("mov rcx, [rsp]");
                result.add("mov rdx, [rsp + 8]");
                result.add("mov r8, [rsp + 16]");
                result.add("mov r9, [rsp + 24]");
                result.add("call " + factor.value);
                result.add("add rsp, " + 8 * Math.max(4, exprList.contents.size()));
                result.add("push rax");
                break;
            case Factor.VAR:
                result.add("mov r8, [rbp + " + scope.get(factor.value) + "]");
                result.add("push r8");
                break;
            default:
                throw new CompilationException("Unknown mode: " + factor.mode);
        }
    }

    public static void compileTerm(Term term, List<String> result) {
        switch (term.mode) {
            case Term.DEREFERENCE:
                compileTerm((Term) term.get(0), result);
                TypeInstance type = ((TypedToken) term.get(0)).typeInstance.dereference();
                switch (type.sizeof()) {
                    case 8:
                        result.add("pop r8");
                        result.add("mov r8, [r8]");
                        result.add("push r8");
                        break;
                    case 4:
                        result.add("pop r8");
                        result.add("xor r9, r9");
                        result.add("mov r9d, [r8]");
                        result.add("push r9");
                        break;
                    case 2:
                        result.add("pop r8");
                        result.add("xor r9, r9");
                        result.add("mov r9w, [r8]");
                        result.add("push r9");
                        break;
                    case 1:
                        result.add("pop r8");
                        result.add("xor r9, r9");
                        result.add("mov r9b, [r8]");
                        result.add("push r9");
                        break;
                    default:
                        throw new TypificationException("Unsupported sizeof: " + type.sizeof());
                }
                break;
            case Term.GET:
                compileTerm((Term) term.get(0), result);
                compileExpression((Expression) term.get(1), result);
                result.add("pop rax");
                result.add("pop r8");
                result.add("mov r9, " + term.typeInstance.sizeof());
                result.add("mul r9");
                result.add("add r8, rax");
                switch (term.typeInstance.sizeof()) {
                    case 8:
                        result.add("mov r9, [r8]");
                        break;
                    case 4:
                        result.add("xor r9, r9");
                        result.add("mov r9d, [r8]");
                        break;
                    case 2:
                        result.add("xor r9, r9");
                        result.add("mov r9w, [r8]");
                        break;
                    case 1:
                        result.add("xor r9, r9");
                        result.add("mov r9b, [r8]");
                        break;
                    default:
                        throw new TypificationException("Unsupported sizeof: " + term.typeInstance.sizeof());
                }
                result.add("push r9");
                break;
            case Term.CAST:
                compileTerm((Term) term.get(1), result);
                type = new TypeInstance(term.get(0).toString());
                TypeInstance oldType = ((TypedToken) term.get(1)).typeInstance;
                if (oldType.equals(type)) {
                    // equal types, cast does nothing
                    return;
                }
                if (oldType.pointer() && type.pointer()) {
                    // both pointers, do nothing
                    return;
                }
                if (oldType.equals(TypeInstance.CHAR) && type.equals(TypeInstance.INT)) {
                    // TODO maybe useless, I think no
                    result.add("pop r8");
                    result.add("xor r9, r9");
                    result.add("mov r9b, r8b");
                    result.add("push r9");
                    return;
                }
                if (oldType.equals(TypeInstance.INT) && type.equals(TypeInstance.CHAR)) {
                    // TODO maybe useless I think yes
                    result.add("pop r8");
                    result.add("xor r9, r9");
                    result.add("mov r9b, r8b");
                    result.add("push r9");
                    return;
                }
                throw new TypificationException("Cannot cast " + oldType + " to " + type);
            case Term.FACTOR:
                compileFactor((Factor) term.get(0), result);
                break;
            case Term.NOT:
                compileTerm((Term) term.get(0), result);
                String label = newLabel();
                result.add("pop r8");
                result.add("test r8, r8");
                result.add("mov r8, 1");
                result.add("jz " + label);
                result.add("xor r8, r8");
                result.add(label + ":");
                result.add("push r8");
                break;
            case Term.ENREFERENCE:
                compileTermAddress((Term) term.get(0), result);
                break;
            default:
                throw new CompilationException("Unknown mode: " + term.mode);
        }
    }

    public static void compileMultExpression(MultExpression expression, List<String> result) {
        int count = expression.contents.size();
        for (int i = count - 1; i >= 0; i--) {
            compileTerm((Term) expression.get(i), result);
        }
        for (int i = 1; i < count; i++) {
            TypedToken term1 = (TypedToken) expression.get(0);
            TypedToken term2 = (TypedToken) expression.get(i);
            if (!term1.typeInstance.equals(term2.typeInstance)) {
                throw new TypificationException("Type mismatch: x " + MultExpression.getChar(expression.ops.get(i)) + " y, x :: " + term1.typeInstance + ", y :: " + term2.typeInstance);
            }
            result.add("pop rax");
            result.add("pop r8");
            switch (expression.ops.get(i)) {
                case MultExpression.MUL_OP:
                    result.add("mul r8");
                    result.add("push rax");
                    break;
                case MultExpression.DIV_OP:
                    result.add("xor rdx, rdx");
                    result.add("div r8");
                    result.add("push rax");
                    break;
                case MultExpression.MOD_OP:
                    result.add("xor rdx, rdx");
                    result.add("div r8");
                    result.add("push rdx");
                    break;
                default: throw new IllegalArgumentException("Unknown op: " + expression.ops.get(i));
            }
        }
    }

    public static void compilePlusExpression(PlusExpression expression, List<String> result) {
        int count = expression.contents.size();
        for (int i = count - 1; i >= 0; i--) {
            compileMultExpression((MultExpression) expression.get(i), result);
        }
        for (int i = 1; i < count; i++) {
            TypedToken mult1 = (TypedToken) expression.get(0);
            TypedToken mult2 = (TypedToken) expression.get(i);
            if (mult1.typeInstance.pointer() && mult2.typeInstance.equals(TypeInstance.INT)) {
                result.add("pop r8");
                result.add("pop rax");
                result.add("mov r9, " + mult1.typeInstance.dereference().sizeof());
                result.add("mul r9");
                if (expression.signs.get(i)) {
                    result.add("add r8, rax");
                } else {
                    result.add("sub r8, rax");
                }
                result.add("push r8");
            } else {
                if (!mult1.typeInstance.equals(mult2.typeInstance)) {
                    throw new TypificationException("Type mismatch: x " + (expression.signs.get(i) ? "+" : "-") + " y, x :: " + mult1.typeInstance + ", y :: " + mult2.typeInstance);
                }
                result.add("pop r8");
                result.add("pop r9");
                if (expression.signs.get(i)) {
                    result.add("add r8, r9");
                } else {
                    result.add("sub r8, r9");
                }
                result.add("push r8");
            }
        }
    }

    public static void compileCompareExpression(CompareExpression expression, List<String> result) {
        int count = expression.contents.size();
        for (int i = count - 1; i >= 0; i--) {
            compilePlusExpression((PlusExpression) expression.get(i), result);
        }
        for (int i = 1; i < count; i++) {
            TypedToken token1 = (TypedToken) expression.get(0);
            TypedToken token2 = (TypedToken) expression.get(i);
            if (!token1.typeInstance.equals(token2.typeInstance)) {
                throw new TypificationException("Type mismatch: x " + CompareExpression.getString(expression.ops.get(i)) + " y, x :: " + token1.typeInstance + ", y :: " + token2.typeInstance);
            }
            switch (token1.typeInstance.sizeof()) {
                case 8:
                    result.add("pop r8");
                    result.add("pop r9");
                    break;
                case 4:
                    result.add("pop r10");
                    result.add("pop r11");
                    result.add("xor r8, r8");
                    result.add("xor r9, r9");
                    result.add("mov r8d, r10d");
                    result.add("mov r9d, r11d");
                    break;
                case 2:
                    result.add("pop r10");
                    result.add("pop r11");
                    result.add("xor r8, r8");
                    result.add("xor r9, r9");
                    result.add("mov r8w, r10w");
                    result.add("mov r9w, r11w");
                    break;
                case 1:
                    result.add("pop r10");
                    result.add("pop r11");
                    result.add("xor r8, r8");
                    result.add("xor r9, r9");
                    result.add("mov r8b, r10b");
                    result.add("mov r9b, r11b");
                    break;
                default:
                    throw new CompilationException("Unsupported sizeof: " + token1.typeInstance.sizeof());
            }
            result.add("cmp r8, r9");
            String label = newLabel();
            result.add("mov r8, 1");
            switch (expression.ops.get(i)) {
                case CompareExpression.GR_OP:
                    result.add("jg " + label);
                    break;
                case CompareExpression.LS_OP:
                    result.add("jl " + label);
                    break;
                case CompareExpression.GRE_OP:
                    result.add("jge " + label);
                    break;
                case CompareExpression.LSE_OP:
                    result.add("jle " + label);
                    break;
                default:
                    throw new CompilationException("Unknown op: " + expression.ops.get(i));
            }
            result.add("xor r8, r8");
            result.add(label + ":");
            result.add("push r8");
        }
    }

    public static void compileEqualExpression(EqualExpression expression, List<String> result) {
        int count = expression.contents.size();
        for (int i = count - 1; i >= 0; i--) {
            compileCompareExpression((CompareExpression) expression.get(i), result);
        }
        for (int i = 1; i < count; i++) {
            TypedToken token1 = (TypedToken) expression.get(0);
            TypedToken token2 = (TypedToken) expression.get(i);
            if (!token1.typeInstance.equals(token2.typeInstance)) {
                throw new TypificationException("Type mismatch: x " + EqualExpression.getString(expression.ops.get(i)) + " y, x :: " + token1.typeInstance + ", y :: " + token2.typeInstance);
            }
            switch (token1.typeInstance.sizeof()) {
                case 8:
                    result.add("pop r8");
                    result.add("pop r9");
                    break;
                case 4:
                    result.add("pop r10");
                    result.add("pop r11");
                    result.add("xor r8, r8");
                    result.add("xor r9, r9");
                    result.add("mov r8d, r10d");
                    result.add("mov r9d, r11d");
                    break;
                case 2:
                    result.add("pop r10");
                    result.add("pop r11");
                    result.add("xor r8, r8");
                    result.add("xor r9, r9");
                    result.add("mov r8w, r10w");
                    result.add("mov r9w, r11w");
                    break;
                case 1:
                    result.add("pop r10");
                    result.add("pop r11");
                    result.add("xor r8, r8");
                    result.add("xor r9, r9");
                    result.add("mov r8b, r10b");
                    result.add("mov r9b, r11b");
                    break;
                default:
                    throw new CompilationException("Unsupported sizeof: " + token1.typeInstance.sizeof());
            }
            result.add("cmp r8, r9");
            String label = newLabel();
            result.add("mov r8, 1");
            switch (expression.ops.get(i)) {
                case EqualExpression.EQ_OP:
                    result.add("je " + label);
                    break;
                case EqualExpression.NEQ_OP:
                    result.add("jne " + label);
                    break;
                default:
                    throw new CompilationException("Unknown op: " + expression.ops.get(i));
            }
            result.add("xor r8, r8");
            result.add(label + ":");
            result.add("push r8");
        }
    }

    public static void compileAndExpression(AndExpression expression, List<String> result) {
        int count = expression.contents.size();
        if (count > 1) {
            String zeroLabel = newLabel("zero");
            String afterLabel = newLabel("after");
            for (int i = 0; i < count; i++) {
                TypedToken token1 = (TypedToken) expression.get(0);
                TypedToken token2 = (TypedToken) expression.get(i);
                if (!token1.typeInstance.equals(token2.typeInstance)) {
                    throw new TypificationException("Type mismatch: x && y, x :: " + token1.typeInstance + ", y :: " + token2.typeInstance);
                }
                compileEqualExpression((EqualExpression) expression.get(i), result);
                result.add("pop r8");
                result.add("test r8, r8");
                result.add("jz " + zeroLabel);
            }
            // TODO maybe 'push 1' works?
            result.add("mov r8, 1");
            result.add("push r8");
            result.add("jmp " + afterLabel);
            result.add(zeroLabel + ":");
            result.add("xor r8, r8");
            result.add("push r8");
            result.add(afterLabel + ":");
        } else {
            compileEqualExpression((EqualExpression) expression.get(0), result);
        }
    }

    public static void compileOrExpression(OrExpression expression, List<String> result) {
        int count = expression.contents.size();
        if (count > 1) {
            String nonzeroLabel = newLabel("nonzero");
            String afterLabel = newLabel("after");
            for (int i = 0; i < count; i++) {
                TypedToken token1 = (TypedToken) expression.get(0);
                TypedToken token2 = (TypedToken) expression.get(i);
                if (!token1.typeInstance.equals(token2.typeInstance)) {
                    throw new TypificationException("Type mismatch: x || y, x :: " + token1.typeInstance + ", y :: " + token2.typeInstance);
                }
                compileAndExpression((AndExpression) expression.get(i), result);
                result.add("pop r8");
                result.add("test r8, r8");
                result.add("jnz " + nonzeroLabel);
            }
            // TODO maybe 'push 1' works?
            result.add("xor r8, r8");
            result.add("push r8");
            result.add("jmp " + afterLabel);
            result.add(nonzeroLabel + ":");
            result.add("mov r8, 1");
            result.add("push r8");
            result.add(afterLabel + ":");
        } else {
            compileAndExpression((AndExpression) expression.get(0), result);
        }
    }

    public static void compileExpression(Expression expression, List<String> result) {
        switch (expression.mode) {
            case Expression.ASSIGN:  // x = y
                compileExpression((Expression) expression.get(1), result);
                compileTermAddress((Term) expression.get(0), result);
                TypeInstance type = ((TypedToken) expression.get(0)).typeInstance;
                result.add("pop r8");
                result.add("pop r9");
                switch (type.sizeof()) {
                    case 8:
                        result.add("mov [r8], r9");
                        result.add("push r9");
                        break;
                    case 4:
                        result.add("xor r10, r10");
                        result.add("mov r10d, r9d");
                        result.add("mov [r8], r10d");
                        result.add("push r9");
                        break;
                    case 2:
                        result.add("xor r10, r10");
                        result.add("mov r10w, r9w");
                        result.add("mov [r8], r10w");
                        result.add("push r9");
                        break;
                    case 1:
                        result.add("xor r10, r10");
                        result.add("mov r10b, r9b");
                        result.add("mov [r8], r10b");
                        result.add("push r9");
                        break;
                    default:
                        throw new CompilationException("Unsupported sizeof: " + type.sizeof());
                }
                break;
            case Expression.SIMPLE:  // x
                compileOrExpression((OrExpression) expression.get(0), result);
                break;
            default:
                throw new CompilationException("Unknown mode: " + expression.mode);
        }
    }

    public static void compileLineCode(LineCode lineCode, List<String> result) {
        switch (lineCode.contentsType) {
            case LineCode.VARDEF_TYPE:
                result.add("sub rsp, 8"); // sizeof(int)
                rbpOffset -= 8;
                scope.put(lineCode.get(0).value, rbpOffset, new TypeInstance(lineCode.get(0).get(0).value)); // var name
                break;
            case LineCode.EXPR_TYPE:
                deduceExpressionType((Expression) lineCode.get(0));
                compileExpression((Expression) lineCode.get(0), result);
                result.add("add rsp, 8"); // clear result
                break;
            case LineCode.BLOCK_TYPE:
                compileBlockCode((BlockCode) lineCode.get(0), result);
                break;
            case LineCode.RETURN_TYPE:
                // TODO check return type == expr type
                deduceExpressionType((Expression) lineCode.get(0));
                compileExpression((Expression) lineCode.get(0), result);
                result.add("pop rax");

                // clear all local variables
                int scopeSize = scope.countLocalScopes();
                if (scopeSize > 0) {
                    result.add("add rsp, " + scopeSize * 8);
                }

                // restore saved regs
                result.add("pop rbp");

                // return
                result.add("ret");

                break;
            case LineCode.IF_TYPE:
                Expression expression = (Expression) lineCode.get(0);
                LineCode ifLine = (LineCode) lineCode.get(1);
                deduceExpressionType(expression);
                compileExpression(expression, result);
                String label = newLabel();
                result.add("pop r8");
                result.add("test r8, r8");
                result.add("jz " + label);
                compileLineCode(ifLine, result);
                result.add(label + ":");
                break;
            case LineCode.FOR_TYPE:
                Expression init = (Expression) lineCode.get(0);
                Expression condition = (Expression) lineCode.get(1);
                Expression iteration = (Expression) lineCode.get(2);
                LineCode line = (LineCode) lineCode.get(3);
                deduceExpressionType(init);
                deduceExpressionType(condition);
                deduceExpressionType(iteration);
                compileExpression(init, result);
                result.add("add rsp, 8");
                String startLabel = newLabel("startLoop");
                String endLabel = newLabel("endLoop");
                result.add(startLabel + ":");
                compileExpression(condition, result);
                result.add("pop r8");
                result.add("test r8, r8");
                result.add("jz " + endLabel);
                compileLineCode(line, result);
                compileExpression(iteration, result);
                result.add("add rsp, 8");
                result.add("jmp " + startLabel);
                result.add(endLabel + ":");
                break;
            default: throw new IllegalArgumentException("Unknown contentsType: " + lineCode.contentsType);
        }
    }

    public static void compileBlockCode(BlockCode blockCode, List<String> result) {
        scope.enterScope();
        LinesCode linesCode = (LinesCode) blockCode.get(0);
        for (Token token: linesCode.contents) {
            compileLineCode((LineCode) token, result);
        }
        int scopeSize = scope.leaveScope();
        result.add("add rsp, " + scopeSize * 8);
    }

    public static void compileFunction(Function function, List<String> result) {
        // global pointer
        result.add("global $" + function.getValue());
        // function's enter point
        result.add("$" + function.getValue() + ":");

        // save regs due to convention
        result.add("push rbp");
        result.add("mov rbp, rsp");

        // place fastcall args on stack
        if (function.get(Function.ARGS_INDEX).contents.size() >= 8) {
            result.add("mov [rbp + 40], r9");
        }
        if (function.get(Function.ARGS_INDEX).contents.size() >= 6) {
            result.add("mov [rbp + 32], r8");
        }
        if (function.get(Function.ARGS_INDEX).contents.size() >= 4) {
            result.add("mov [rbp + 24], rdx");
        }
        if (function.get(Function.ARGS_INDEX).contents.size() >= 2) {
            result.add("mov [rbp + 16], rcx");
        }

        scope.enterScope();

        ArgsList argsList = (ArgsList) function.get(Function.ARGS_INDEX);
        for (int i = 0; i < argsList.contents.size() / 2; i++) {
            scope.put(argsList.get(2 * i + 1).value, 8 * (i + 2), new TypeInstance(argsList.get(2 * i).value)); // sizeof(int, void*, ...) * i + sizeof(rbp)
        }
        rbpOffset = 0; // rsp == rbp + rbpOffset

        compileBlockCode((BlockCode) function.get(Function.CODE_INDEX), result);

        // final return if reached end of function
        // clear all local variables
        int scopeSize = scope.countLocalScopes();
        if (scopeSize > 0) {
            result.add("add rsp, " + scopeSize * 8);
        }

        // restore saved regs
        result.add("pop rbp");

        // return
        result.add("ret");
    }

    public static List<String> compileProgram(Program program) {
        List<String> result = new ArrayList<>();
        result.add("section .text");
        returnType = new HashMap<>();
        argsType = new HashMap<>();
        program.contents.stream().filter(token -> token instanceof Function || token instanceof FunctionDefinition).forEach(token -> {
            if (token instanceof FunctionDefinition) {
                result.add("extern " + token.value);
            }
            Type type = (Type) token.get(Function.TYPE_INDEX);
            returnType.put(token.value, new TypeInstance(type.value));
            ArgsList argsList = (ArgsList) token.get(Function.ARGS_INDEX);
            argsType.put(token.value,
                    argsList.contents.stream()
                            .filter(t -> t instanceof Type)
                            .map(t -> new TypeInstance(t.value))
                            .collect(Collectors.toList())
            );
        });
        program.contents.stream().filter(token -> token instanceof Function).forEach(
                token -> compileFunction((Function) token, result)
        );
        // TODO add const and change to rodata
        result.add("section .data");
        result.addAll(rodata);
        return result;
    }

}