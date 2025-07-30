package org.example.command;

import org.antlr.v4.runtime.tree.ParseTree;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;
import org.example.connection.ClientSession;
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

    public String execute(ParseTree tree, ClientSession session) {
        this.currentSession = session;
        return tree.accept(this);
    }

    private ClientSession currentSession;

    @Override
    public String visitCommand(FTPCommandParser.CommandContext ctx) {
        String response;

        if (ctx.LOGIN() != null) {
            response = handleLogin(ctx.username().getText(), ctx.password().getText());
            return formatResponse(response);
        }

        if (!isLoggedIn) {
            LOG.info("User not authenticated");
            return formatResponse("530 Not authenticated. Please LOGIN first.");
        }

        if (ctx.LIST() != null) {
            response = handleList();
        } else if (ctx.GET() != null) {
            response = handleGet(ctx.filename().getText());
        } else if (ctx.PUT() != null) {
            return handlePutCommand(ctx.filename().getText(),
                    Integer.parseInt(ctx.length().getText()));
        } else if (ctx.QUIT() != null) {
            return "abort";
        } else {
            response = "500 Unknown command";
        }

        return formatResponse(response);
    }

    private String formatResponse(String content) {
        if (content.equals("abort") || content.equals("file_upload_ready")) {
            return content;
        }
        return String.format(HEADER_FORMAT, content.length()) + content;
    }

    private String handleLogin(String username, String password) {
        if ("user".equals(username) && "pass".equals(password)) {
            isLoggedIn = true;
            return "230 Login successful.";
        }
        return "530 Login failed. Invalid credentials.";
    }

    private String handleList() {
        File dir = new File(SERVER_DIR);
        String[] files = dir.list();
        if (files == null || files.length == 0) {
            return "226 No files in server.";
        }

        StringBuilder fileList = new StringBuilder();
        for (String filename : files) {
            File file = new File(dir, filename);
            fileList.append(String.format("%-20s %10d bytes%s",
                    filename, file.length(), CRLF));
        }
        return "226 Directory listing:" + CRLF + fileList.toString();
    }

    private String handleGet(String filename) {
        File file = new File(SERVER_DIR, filename);
        if (!file.exists()) {
            return "550 Error: File not found.";
        }
        try {
            byte[] fileData = Files.readAllBytes(file.toPath());
            return "226 File transfer successful." + CRLF + new String(fileData);
        } catch (IOException e) {
            LOG.error("Error reading file", e);
            return "550 Error reading file: " + e.getMessage();
        }
    }

    private String handlePutCommand(String filename, int fileSize) {
        try {
            if (fileSize <= 0) {
                return formatResponse("550 Invalid file size");
            }

            if (fileSize > currentSession.getMaxFileSize()) {
                return formatResponse("552 File size exceeds maximum allowed size");
            }

            currentSession.startFileUpload(filename, fileSize);
            return "file_upload_ready";
        } catch (IOException e) {
            LOG.error("Error starting file upload", e);
            return formatResponse("550 Error starting file upload: " + e.getMessage());
        }
    }

    public String saveFile(String filename, byte[] data) throws IOException {
        File file = new File(SERVER_DIR, filename);
        try {
            Files.write(file.toPath(), data,
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            return "226 File uploaded successfully: " + filename +
                    " (" + data.length + " bytes)";
        } catch (IOException e) {
            LOG.error("Error writing file", e);
            throw new IOException("Error writing file: " + e.getMessage());
        }
    }
}
