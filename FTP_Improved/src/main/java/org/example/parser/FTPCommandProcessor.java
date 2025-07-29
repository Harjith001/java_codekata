package org.example.parser;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.example.command.FTPCommandExecutor;

import java.io.IOException;
import java.io.StringReader;

public class FTPCommandProcessor {
    private final FTPCommandExecutor executor;

    public FTPCommandProcessor(FTPCommandExecutor executor) {
        this.executor = executor;
    }

    public String process(String input) throws IOException {
        CharStream charStream = CharStreams.fromReader(new StringReader(input));
        FTPCommandLexer lexer = new FTPCommandLexer(charStream);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        FTPCommandParser parser = new FTPCommandParser(tokens);
        ParseTree tree = parser.command();

        return executor.execute(tree);
    }
}
