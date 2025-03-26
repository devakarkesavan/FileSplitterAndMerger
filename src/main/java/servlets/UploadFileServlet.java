package servlets;

import utils.DBConnection;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;

//@WebServlet("/UploadFileServlet")
//@MultipartConfig(fileSizeThreshold = 1024 * 1024, maxFileSize = 10 * 1024 * 1024)
public class UploadFileServlet extends HttpServlet {
    private static final String UPLOAD_DIR = "uploads";

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();
        String username = (String) session.getAttribute("username");

        if (username == null) {
            response.sendRedirect("login.jsp");
            return;
        }

        int folderId = Integer.parseInt(request.getParameter("folderId"));
        Part filePart = request.getPart("file");
        String fileName = filePart.getSubmittedFileName();

        if (fileName == null || fileName.trim().isEmpty()) {
            response.sendRedirect("folder.jsp?folderId=" + folderId + "&error=File required");
            return;
        }

        Connection conn = null;
        try {
            conn = DBConnection.getConnection();

            PreparedStatement userStmt = conn.prepareStatement("SELECT id FROM users WHERE username = ?");
            userStmt.setString(1, username);
            ResultSet userRs = userStmt.executeQuery();
            int userId = -1;
            if (userRs.next()) userId = userRs.getInt("id");

            String uploadPath = getServletContext().getRealPath("") + File.separator + UPLOAD_DIR;
            File uploadDir = new File(uploadPath);
            if (!uploadDir.exists()) uploadDir.mkdir();

            String filePath = uploadPath + File.separator + fileName;
            filePart.write(filePath);

            PreparedStatement fileStmt = conn.prepareStatement(
                    "INSERT INTO user_files (file_name, file_path, folder_id, user_id) VALUES (?, ?, ?, ?)");
            fileStmt.setString(1, fileName);
            fileStmt.setString(2, filePath);
            fileStmt.setInt(3, folderId);
            fileStmt.setInt(4, userId);
            fileStmt.executeUpdate();

            response.sendRedirect("folder.jsp?folderId=" + folderId);

        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect("folder.jsp?folderId=" + folderId + "&error=Error uploading file");
        }
    }
}
