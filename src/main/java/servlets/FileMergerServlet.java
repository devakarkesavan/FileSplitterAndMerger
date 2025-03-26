package servlets;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.ServletException;
import javax.servlet.http.*;
import java.sql.*;
import utils.DBConnection;

public class FileMergerServlet extends HttpServlet {
    private static final String AES_KEY = "1234567890123456";

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String fileName = request.getParameter("fileName");
        HttpSession session = request.getSession(false);
        String username = (session != null) ? (String) session.getAttribute("username") : "unknown";

        List<String> chunkPaths = getChunkPathsFromDB(username, fileName);
        if (chunkPaths == null || chunkPaths.isEmpty()) {
            response.getWriter().write("error: No chunks found!");
            return;
        }

        chunkPaths.sort(Comparator.comparingInt(FileMergerServlet::extractChunkIndex));

        try {
            Cipher cipher = Cipher.getInstance("AES");
            SecretKey secretKey = new SecretKeySpec(AES_KEY.getBytes(), "AES");
            cipher.init(Cipher.DECRYPT_MODE, secretKey);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            for (String chunkPath : chunkPaths) {
                byte[] encryptedChunk = Files.readAllBytes(Paths.get(chunkPath));
                byte[] decryptedChunk = cipher.doFinal(encryptedChunk);
                outputStream.write(decryptedChunk);
            }

            byte[] mergedFileBytes = outputStream.toByteArray();

            response.setContentType("application/octet-stream");
            response.setHeader("Content-Disposition", "attachment; filename=" + fileName);
            response.getOutputStream().write(mergedFileBytes);
        } catch (Exception e) {
            e.printStackTrace();
            response.getWriter().write("Error: " + e.getMessage());
        }
    }

    private List<String> getChunkPathsFromDB(String username, String fileName) {
        List<String> chunkPaths = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT chunk_folder FROM files WHERE username = ? AND file_name = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, username);
            pstmt.setString(2, fileName);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                String[] paths = rs.getString("chunk_folder").split(",");
                chunkPaths.addAll(Arrays.asList(paths));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return chunkPaths;
    }

    private static int extractChunkIndex(String filePath) {
        try {
            String fileName = new File(filePath).getName();
            String[] parts = fileName.split("_");
            return Integer.parseInt(parts[1]);
        } catch (Exception e) {
            return Integer.MAX_VALUE;
        }
    }
}
