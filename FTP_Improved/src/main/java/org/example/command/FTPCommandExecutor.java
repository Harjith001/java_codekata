package org.example.command;

import org.antlr.v4.runtime.tree.ParseTree;
import org.example.parser.FTPCommandBaseVisitor;
import org.example.parser.FTPCommandParser;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class FTPCommandExecutor extends FTPCommandBaseVisitor<String> {

    private static final String SERVER_DIR = "server_directory";
    private boolean isLoggedIn = false;

    public FTPCommandExecutor() {
        File dir = new File(SERVER_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    public String execute(ParseTree tree) {
        return tree.accept(this);
    }

    @Override
    public String visitCommand(FTPCommandParser.CommandContext ctx) {
        // LOGIN is allowed without authentication
        if (ctx.LOGIN() != null) {
            return handleLogin(ctx.username().getText(), ctx.password().getText());
        }

        // All other commands require authentication
        if (!isLoggedIn) {
            return "Error: Not authenticated. Please LOGIN first.";
        }

        if (ctx.LIST() != null) {
            return handleList();
        } else if (ctx.GET() != null) {
            return handleGet(ctx.filename().getText());
        } else if (ctx.PUT() != null) {
            return handlePut(ctx.filename().getText());
        }

        return "Unknown command";
    }

    private String handleLogin(String username, String password) {
        // Here you can implement real auth if needed
        if ("user".equals(username) && "pass".equals(password)) {
            isLoggedIn = true;
            return "Login successful.";
        }
        return "Login failed. Invalid credentials.";
    }

    private String handleList() {
        File dir = new File(SERVER_DIR);
        String[] files = dir.list();
        if (files == null || files.length == 0) return "No files in server.";
        return String.join("\n", files);
    }

    private String handleGet(String filename) {
        File file = new File(SERVER_DIR, filename);
        if (!file.exists()) return "Error: File not found.";
        try {
            return Files.readString(file.toPath());
        } catch (IOException e) {
            return "Error reading file: " + e.getMessage();
        }
    }

    private String handlePut(String filename) {
        File file = new File(SERVER_DIR, filename);
        try {
            Files.writeString(file.toPath(), "Sample content for " + filename);
            return "File uploaded: " + filename;
        } catch (IOException e) {
            return "Error writing file: " + e.getMessage();
        }
    }
}
