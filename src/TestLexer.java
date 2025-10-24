import java_cup.runtime.*;
import java.io.*;

public class TestLexer {
    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("Usage: java TestLexer <input.test>");
            System.exit(1);
        }

        try {
            FileReader reader = new FileReader(args[0]);
            Lexer lexer = new Lexer(reader);

            System.out.println("Testing Lexer on: " + args[0]);
            System.out.println("---------------------------\n");

            Symbol token;
            int count = 0;

            while (true) {
                token = lexer.next_token();
                count++;

                String tokenName = sym.terminalNames[token.sym];
                String tokenValue = token.value != null ? " = " + token.value : "";

                System.out.printf("%3d. %-15s%s (line %d, col %d)\n",
                        count,
                        tokenName,
                        tokenValue,
                        token.left,
                        token.right);

                // Stop at EOF
                if (token.sym == sym.EOF) {
                    break;
                }

                // Safety: stop after 100 tokens
                if (count > 100) {
                    System.err.println("\nStopped after 100 tokens (possible infinite loop)");
                    break;
                }
            }

            System.out.println("\n--------------------------------------");
            System.out.println("Total tokens: " + count);

        } catch (FileNotFoundException e) {
            System.err.println("Error: File not found: " + args[0]);
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}