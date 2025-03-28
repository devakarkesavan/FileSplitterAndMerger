package servlets;

import utils.DBConnection;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.http.*;
import java.sql.*;

@MultipartConfig
public class FileSplitterServlet extends HttpServlet {
    private static final String UPLOAD_DIR = "users";
    private static final String AES_KEY = "1234567890123456";

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        String username = (session != null) ? (String) session.getAttribute("username") : "unknown";

        String folderIdStr = request.getParameter("folderId");
        if (folderIdStr == null || folderIdStr.isEmpty()) {
            response.getWriter().write("Error: folderId is missing.");
            return;
        }

        int folderId;
        folderId = Integer.parseInt(folderIdStr.trim());

//        try {
//        } catch (NumberFormatException e) {
//            response.getWriter().write("Error: Invalid folderId format.");
//            return;
//        }

        Part filePart = request.getPart("file");
        String fileName = Paths.get(filePart.getSubmittedFileName()).getFileName().toString();

        String fileUploadPath = getServletContext().getRealPath("") + File.separator + UPLOAD_DIR + File.separator + username + File.separator + fileName;
        File fileUploadDir = new File(fileUploadPath);
        if (!fileUploadDir.exists()) fileUploadDir.mkdirs();

        InputStream fileContent = filePart.getInputStream();
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        byte[] temp = new byte[1024];
        int bytesRead;
        while ((bytesRead = fileContent.read(temp, 0, temp.length)) != -1) {
            buffer.write(temp, 0, bytesRead);
        }
        byte[] fileBytes = buffer.toByteArray();

        int numChunks = new Random().nextInt(11) + 10;
        int chunkSize = fileBytes.length / numChunks;

        int numFolders = new Random().nextInt(6) + 5;
        Map<Integer, String> folderMap = new HashMap<>();
        List<String> folderPaths = new ArrayList<>();

        for (int i = 1; i <= numFolders; i++) {
            String folderUUID = UUID.randomUUID().toString();
            String folderPath = fileUploadPath + File.separator + folderUUID;
            File folder = new File(folderPath);
            if (!folder.exists()) folder.mkdirs();
            folderMap.put(i, folderPath);
            folderPaths.add(folderPath);
        }

        Map<String, String> chunkFolderMap = new HashMap<>();
        List<String> chunkPaths = new ArrayList<>();

        try {
            Cipher cipher = Cipher.getInstance("AES");
            SecretKey secretKey = new SecretKeySpec(AES_KEY.getBytes(), "AES");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);

            List<String> folderList = new ArrayList<>(folderPaths);
            int folderIndex = 0;

            for (int i = 0; i < numChunks; i++) {
                int start = i * chunkSize;
                int end = (i == numChunks - 1) ? fileBytes.length : (start + chunkSize);
                byte[] chunkData = Arrays.copyOfRange(fileBytes, start, end);
                byte[] encryptedChunk = cipher.doFinal(chunkData);

                String chunkFolder = folderList.get(folderIndex);
                String chunkFileName = UUID.randomUUID().toString();
                File chunkFile = new File(chunkFolder, chunkFileName);
                Files.write(chunkFile.toPath(), encryptedChunk);
                chunkPaths.add(chunkFile.getAbsolutePath());

                chunkFolderMap.put(chunkFile.getAbsolutePath(), chunkFolder);

                folderIndex = (folderIndex + 1) % folderList.size();
            }

            saveFileMetadata(username, fileName, chunkPaths, chunkFolderMap, folderId);
            response.getWriter().write("File uploaded, split, and encrypted successfully.");
        } catch (Exception e) {
            e.printStackTrace();
            response.getWriter().write("Error: " + e.getMessage());
        }
        finally {
            if (fileContent != null) try { fileContent.close(); } catch (IOException e) { e.printStackTrace(); }
            if (buffer != null) try { buffer.close(); } catch (IOException e) { e.printStackTrace(); }
        }
    }

    private void saveFileMetadata(String username, String fileName, List<String> chunkPaths, Map<String, String> chunkFolderMap, int folderId) {
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "INSERT INTO files (username, file_name, chunk_folder, folder_id) VALUES (?, ?, ?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(sql);

            for (String chunkPath : chunkPaths) {
                pstmt.setString(1, username);
                pstmt.setString(2, fileName);
                pstmt.setString(3, chunkPath);
                pstmt.setInt(4, folderId);
                pstmt.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
