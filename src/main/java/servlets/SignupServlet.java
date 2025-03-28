package servlets;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import utils.DBConnection;

public class SignupServlet extends HttpServlet {

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String username = request.getParameter("username");
        String email = request.getParameter("email");
        String password = request.getParameter("password");

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getConnection();
            String checkUsername = "SELECT * FROM users WHERE username = ?";
            pstmt = conn.prepareStatement(checkUsername);
            pstmt.setString(1, username);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                request.setAttribute("message", "Username already exists! Please choose another one. <a href='signup.jsp'>Signup</a>");
                request.getRequestDispatcher("message.jsp").forward(request, response);
                return;
            }

            String checkUser = "SELECT * FROM users WHERE email = ?";
            pstmt = conn.prepareStatement(checkUser);
            pstmt.setString(1, email);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                request.setAttribute("message", "User already exists! <a href='login.jsp'>Login</a>");
                request.getRequestDispatcher("message.jsp").forward(request, response);
                return;
            }

            conn.setAutoCommit(false);

            String insertUser = "INSERT INTO users (username, email, password) VALUES (?, ?, ?)";
            pstmt = conn.prepareStatement(insertUser, Statement.RETURN_GENERATED_KEYS);
            pstmt.setString(1, username);
            pstmt.setString(2, email);
            pstmt.setString(3, password);
            pstmt.executeUpdate();

            int userId = 0;
            ResultSet generatedKeys = pstmt.getGeneratedKeys();
            if (generatedKeys.next()) {
                userId = generatedKeys.getInt(1);
            }

            String insertRole = "INSERT INTO user_roles (username, role) VALUES (?, ?)";
            pstmt = conn.prepareStatement(insertRole);
            pstmt.setString(1, username);
            pstmt.setString(2, "user");
            pstmt.executeUpdate();

            conn.commit();

            String uploadPath = getServletContext().getRealPath("/") + "uploads/" + username;
            new File(uploadPath).mkdirs();

            request.setAttribute("message", "Signup successful! <a href='login.jsp'>Login here</a>");
            request.getRequestDispatcher("message.jsp").forward(request, response);

        } catch (Exception e) {
            try {
                if (conn != null) conn.rollback();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            e.printStackTrace();
            request.setAttribute("message", "Error during signup. Please try again.");
            request.getRequestDispatcher("message.jsp").forward(request, response);
        } finally {
            try {
                if (rs != null) rs.close();
                if (pstmt != null) pstmt.close();
                if (conn != null) {
                    conn.setAutoCommit(true);
                    conn.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}