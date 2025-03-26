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

public class LoginServlet extends HttpServlet {
    public LoginServlet() {
    }

    protected void doPost(HttpServletRequest var1, HttpServletResponse var2) throws ServletException, IOException {
        String var3 = var1.getParameter("email");
        String var4 = var1.getParameter("password");

        try (Connection var5 = DBConnection.getConnection()) {
            String var6 = "SELECT id, username FROM users WHERE email = ? AND password = ?";
            PreparedStatement var7 = var5.prepareStatement(var6);
            var7.setString(1, var3);
            var7.setString(2, var4);
            ResultSet var8 = var7.executeQuery();
            if (var8.next()) {
                int var9 = var8.getInt("id");
                String var10 = var8.getString("username");
                HttpSession var11 = var1.getSession();
                var11.setAttribute("id", var9);
                var11.setAttribute("username", var10);
                var2.sendRedirect("index.jsp");
            } else {
                var1.setAttribute("message", "Invalid email or password. <a href='login.jsp'>Try again</a>");
                var1.getRequestDispatcher("message.jsp").forward(var1, var2);
            }
        } catch (Exception var14) {
            var14.printStackTrace();
        }

    }
}
