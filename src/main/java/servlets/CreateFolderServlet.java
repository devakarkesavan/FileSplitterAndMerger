package servlets;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import utils.DBConnection;

public class CreateFolderServlet extends HttpServlet {
    public CreateFolderServlet() {
    }

    protected void doPost(HttpServletRequest var1, HttpServletResponse var2) throws ServletException, IOException {
        HttpSession var3 = var1.getSession();
        String var4 = (String)var3.getAttribute("username");
        if (var4 == null) {
            var2.sendRedirect("login.jsp");
        } else {
            String var5 = var1.getParameter("folderName");
            String var6 = var1.getParameter("parentFolderId");
            if (var5 != null && !var5.trim().isEmpty()) {
                try {
                    try (Connection var7 = DBConnection.getConnection()) {
                        PreparedStatement var8 = var7.prepareStatement("SELECT id FROM users WHERE username = ?");
                        var8.setString(1, var4);
                        ResultSet var9 = var8.executeQuery();
                        if (var9.next()) {
                            int var10 = var9.getInt("id");
                            PreparedStatement var11 = var7.prepareStatement("INSERT INTO user_folders (folder_name, user_id, parent_folder_id) VALUES (?, ?, ?)");
                            var11.setString(1, var5);
                            var11.setInt(2, var10);
                            if (var6 != null && !var6.isEmpty() && !var6.equals("null")) {
                                var11.setInt(3, Integer.parseInt(var6));
                            } else {
                                var11.setNull(3, 4);
                            }

                            var11.executeUpdate();
                            var2.sendRedirect("index.jsp");
                            return;
                        }

                        var2.sendRedirect("index.jsp?error=User not found");
                    }

                } catch (Exception var14) {
                    var14.printStackTrace();
                    var2.sendRedirect("index.jsp?error=Error creating folder");
                }
            } else {
                var2.sendRedirect("index.jsp?error=Folder name required");
            }
        }
    }
}
