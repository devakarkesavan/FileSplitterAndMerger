package servlets;

import java.io.*;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class FileListServlet extends HttpServlet {
    private static final String OUTPUT_DIR = "C:/split_files";

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        File dir = new File(OUTPUT_DIR);
        if (!dir.exists() || !dir.isDirectory()) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Storage directory not found.");
            return;
        }

        StringBuilder fileList = new StringBuilder();
        for (File file : dir.listFiles()) {
            if (file.getName().endsWith(".part0")) {
                String baseName = file.getName().replace(".part0", "");
                fileList.append("<div class='file-card' onclick='mergeFile(\"").append(baseName).append("\")'>")
                        .append(baseName).append("</div>");
            }
        }
        response.setContentType("text/html");
        response.getWriter().write(fileList.toString());
    }
}
