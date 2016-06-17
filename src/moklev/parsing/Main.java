package moklev.parsing;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Scanner;

/**
 * @author Моклев Вячеслав
 */
public class Main {

    static String readFully(Scanner scanner) {
        StringBuilder sb = new StringBuilder();
        while (scanner.hasNext()) {
            sb.append(scanner.nextLine()).append('\n');
        }
        return sb.toString();
    }

    public static void main(String[] args) throws FileNotFoundException {
        Preprocessor.process(new File("source.c"), new File("processed.c"));
        Token result = new CppParser(
                readFully(new Scanner(new File("processed.c")))
        ).parse();
        System.out.println(result);
        List<String> resList = Compiler.compileProgram((Program) result);
        resList.forEach(System.out::println);
        PrintWriter printWriter = new PrintWriter(new File("out.asm"));
        resList.forEach(printWriter::println);
        printWriter.close();
    }

}
