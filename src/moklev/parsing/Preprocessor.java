package moklev.parsing;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Scanner;

/**
 * @author Моклев Вячеслав
 */
public class Preprocessor {

    private static class SmartTokenizer implements Iterator<String> {
        private Parser parser;
        private String skipped;

        public SmartTokenizer(String s) {
            parser = new EmptyParser(s);
            skipped = "";
        }

        @Override
        public boolean hasNext() {
            int initPos = parser.getPosition();
            Parsers.skipSpaces(parser);
            boolean result = parser.getPosition() != parser.getString().length();
            parser.setPosition(initPos);
            return result;
        }

        @Override
        public String next() {
            int initPos = parser.getPosition();
            Parsers.skipSpaces(parser);
            skipped = parser.getString().substring(initPos, parser.getPosition());
            if (Parsers.parseSequence(parser, "\"")) {
                String s = Parsers.parseUntil(parser, '"');
                return "\"" + s + "\"";
            }
            if (Parsers.parseSequence(parser, "#")) {
                String s = Parsers.parseLexem(parser);
                return "#" + (s == null ? "" : s);
            }
            String s = Parsers.parseLexem(parser);
            if (s != null) {
                return s;
            }
            return "" + Parsers.parseChar(parser);
        }

        public String getRestLine() {
            return Parsers.parseUntil(parser, '\n');
        }

        public String getSkipped() {
            return skipped;
        }

    }

    private static boolean isLexematical(char c) {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || c == '_';
    }

    private static String lowLevelPreprocess(String s) {
        return s.replace("\\\n", "");
    }

    public static void process(File source, File destination) {
        Map<String, String> defineMap = new HashMap<>();
        try (Scanner scanner = new Scanner(source);
             PrintWriter printWriter = new PrintWriter(destination)) {
            SmartTokenizer tokenizer = new SmartTokenizer(lowLevelPreprocess(Main.readFully(scanner)));
            while (tokenizer.hasNext()) {
                String s = tokenizer.next();
                if (isLexematical(s.charAt(0))) {
                    String t = defineMap.get(s);
                    if (t == null) {
                        printWriter.print(tokenizer.getSkipped());
                        printWriter.print(s);
                    } else {
                        printWriter.print(tokenizer.getSkipped());
                        printWriter.print(t);
                    }
                } else if (s.charAt(0) == '#') {
                    switch (s.substring(1)) {
                        case "define":
                            printWriter.print(tokenizer.getSkipped());
                            String lexem = Parsers.parseLexem(tokenizer.parser);
                            StringBuilder sb = new StringBuilder();
                            SmartTokenizer localTokenizer = new SmartTokenizer(tokenizer.getRestLine());
                            if (localTokenizer.hasNext()) {
                                sb.append(localTokenizer.next());
                            }
                            while (localTokenizer.hasNext()) {
                                sb.append(' ').append(localTokenizer.next());
                            }
                            defineMap.put(lexem, sb.toString());
                            break;
                        case "include":
                            break;
                        default:
                            throw new PreprocessingException("Unknown preprocessor command: " + s.substring(1));
                    }
                } else {
                    printWriter.print(tokenizer.getSkipped());
                    printWriter.print(s);
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

}
