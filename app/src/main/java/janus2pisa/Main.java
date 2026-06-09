package janus2pisa;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;

public class Main {
  public static void main(String[] args) {
    String fib =
        """
            procedure fib
                if n=0 then x1 += 1
                            x2 += 1
                        else n -= 1
                            call fib
                            x1 += x2
                            x1 <=> x2
                fi x1=x2
            """;

    // 1. Lexer
    JanusLexer lexer = new JanusLexer(CharStreams.fromString(fib));
    CommonTokenStream tokens = new CommonTokenStream(lexer);

    // 2. Parser
    JanusParser parser = new JanusParser(tokens);

    // 3. first rule
    ParseTree tree = parser.prog();

    // 4. print ast
    System.out.println("AST:");
    System.out.println(tree.toStringTree(parser));
  }
}
