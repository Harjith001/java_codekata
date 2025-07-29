package org.example.command;

import org.antlr.v4.runtime.tree.ParseTree;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;
import org.example.parser.FTPCommandBaseVisitor;
import org.example.parser.FTPCommandParser;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;

public class FTPCommandExecutor extends FTPCommandBaseVisitor<String> {
    private static final String SERVER_DIR = "server_directory";
    private boolean isLoggedIn = false;
    private static final Logger LOG = (Logger) LogManager.getLogger(FTPCommandExecutor.class);
    private static final String CRLF = "\r\n";
    private static final String HEADER_FORMAT = "Content-Length: %d" + CRLF + CRLF;

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
        String response;

        if (ctx.LOGIN() != null) {
            response = handleLogin(ctx.username().getText(), ctx.password().getText());
            return formatResponse(response);
        }

        if (!isLoggedIn) {
            LOG.info("User not authenticated");
            return formatResponse("Error: Not authenticated. Please LOGIN first.");
        }

        if (ctx.LIST() != null) {
            response = handleList();
        } else if (ctx.GET() != null) {
            response = handleGet(ctx.filename().getText());
        } else if (ctx.PUT() != null) {
            response = handlePut(ctx.filename().getText(), ctx.content().getText());
        } else if (ctx.QUIT() != null) {
            return "abort";  // Special case - no formatting
        } else {
            response = "Unknown command";
        }

        return formatResponse(response);
    }

    private String formatResponse(String content) {
        if (content.equals("abort")) {
            return content;
        }
        return String.format(HEADER_FORMAT, content.length()) + content;
    }

    private String handleLogin(String username, String password) {
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
        return String.join(CRLF, files);
    }

    private String handleGet(String filename) {
        File file = new File(SERVER_DIR, filename);
        if (!file.exists()) return "Error: File not found.";
        try {
            return Files.readString(file.toPath());
        } catch (IOException e) {
            LOG.error("Error reading file ", e);
            return "Error reading file: " + e.getMessage();
        }
    }

    private String handlePut(String filename, String content) {
        File file = new File(SERVER_DIR, filename);
        try {
            Files.writeString(file.toPath(), content, StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING);
            return "File uploaded: " + filename;
        } catch (IOException e) {
            LOG.error("Error writing file ", e);
            return "Error writing file: " + e.getMessage();
        }
    }
}