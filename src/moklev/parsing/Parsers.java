package moklev.parsing;

import javafx.util.Pair;

/**
 * @author Моклев Вячеслав
 */
public class Parsers {

    private static boolean isSpaceLike(char c) {
        return c == ' ' || c == '\n' || c == '\t';
    }

    static void skipSpaces(Parser parser) {
        String string = parser.getString();
        int pos = parser.getPosition();
        while (true) {
            int initPos = pos;
            // skip space symbols
            while (pos < string.length() && isSpaceLike(string.charAt(pos))) {
                pos++;
            }
            // skip one-line comments
            if (pos < string.length() - 1 && string.charAt(pos) == '/' && string.charAt(pos + 1) == '/') {
                while (pos < string.length()) {
                    if (string.charAt(pos) == '\n') {
                        pos++;
                        break;
                    }
                    pos++;
                }
            }
            // skip multi-line comments
            if (pos < string.length() - 1 && string.charAt(pos) == '/' && string.charAt(pos + 1) == '*') {
                while (pos < string.length()) {
                    char c1 = string.charAt(pos);
                    char c2 = 0;
                    if (pos < string.length() - 1) {
                        c2 = string.charAt(pos + 1);
                    }
                    if (c1 == '*' && c2 == '/') {
                        pos += 2;
                        break;
                    }
                    pos++;
                }
            }
            if (initPos == pos) {
                break;
            }
        }
        parser.setPosition(pos);
    }

    private static boolean isLexematical(char c) {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || c == '_';
    }

    static boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    /**
     * Parse a lexem from string. Lexem is a sequence of
     * alphabetical symbols, digits and underscore: ['a'..'z', 'A'..'Z', '0'..'9', '_'],
     * but first symbol is not a digit
     * @param parser contains string and current position
     * and new offset (offset doesn't changes if lexem wasn't parsed)
     */
    public static String parseLexem(Parser parser) {
        skipSpaces(parser);
        String string = parser.getString();
        int offset = parser.getPosition();
        int initial_offset = offset;
        if (offset >= string.length() || !isLexematical(string.charAt(offset))) {
            return null;
        }
        offset++;
        while (offset < string.length() &&
                (isLexematical(string.charAt(offset)) || isDigit(string.charAt(offset)))) {
            offset++;
        }
        parser.setPosition(offset);
        return string.substring(initial_offset, offset);
    }

    /**
     * Parse a given pattern from string.
     * @param parser contains string and current position
     * @param pattern string to parse
     * and new offset (offset doesn't changes if sequence wasn't parsed)
     */
    public static boolean parseSequence(Parser parser, String pattern) {
        skipSpaces(parser);
        int offset = parser.getPosition();
        if (parser.getString().startsWith(pattern, offset))  {
            parser.setPosition(offset + pattern.length());
            return true;
        } else {
            return false;
        }
    }

    /**
     * Parse an integer from string. Integer is a sequence of
     * digits: ['0'..'9']
     * @param parser contains string and current position
     * and new offset (offset doesn't changes if integer wasn't parsed)
     */
    public static String parseInteger(Parser parser) {
        skipSpaces(parser);
        String string = parser.getString();
        int offset = parser.getPosition();
        int initial_offset = offset;
        if (offset >= string.length() || !isDigit(string.charAt(offset))) {
            return null;
        }
        offset++;
        while (offset < string.length() && isDigit(string.charAt(offset))) {
            offset++;
        }
        parser.setPosition(offset);
        return string.substring(initial_offset, offset);
    }

    public static char parseChar(Parser parser) {
        String string = parser.getString();
        int offset = parser.getPosition();
        if (offset < string.length()) {
            parser.setPosition(offset + 1);
            return string.charAt(offset);
        } else {
            return '\0';
        }
    }

    public static String parseUntil(Parser parser, char delim) {
        String string = parser.getString();
        int offset = parser.getPosition();
        int initial_offset = offset;
        while (offset < string.length() && string.charAt(offset) != delim) {
            offset++;
        }
        if (offset == string.length() && string.charAt(string.length() - 1) != delim) {
            return null;
        }
        parser.setPosition(offset + 1);
        return string.substring(initial_offset, offset);
    }

}
