<%@ page import="java.sql.*, java.util.*" %>
<%@ page import="utils.DBConnection" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<%
    int folderId = Integer.parseInt(request.getParameter("folderId"));
    Connection conn = DBConnection.getConnection(); // Declare once

    // Fetch subfolders
    PreparedStatement folderStmt = conn.prepareStatement("SELECT id, folder_name FROM user_folders WHERE parent_folder_id = ?");
    folderStmt.setInt(1, folderId);
    ResultSet folderRs = folderStmt.executeQuery();
%>

<html>
<head>
<meta http-equiv="Cache-Control" content="no-store,no-cache,must-revalidate">
<meta http-equiv="Pragma" content="no-cache">
<meta http-equiv="Expires" content="-1">
    <title>File Manager</title>
    <script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>
    <style>
        body {
            font-family: Arial, sans-serif;
            background-image: url("icons/background.png");
            background-size: cover;
            color: black;
            margin: 0;
            padding: 20px;
            text-align: center;
        }

        .container {
            background: rgba(255, 255, 255, 0.2);
            max-width: 800px;
            margin: auto;
            padding: 20px;
            border-radius: 10px;
        }

        .btn-container {
            display: flex;
            justify-content: center;
            gap: 10px;
            margin-bottom: 20px;
        }

        .btn {
            padding: 10px 15px;
            font-size: 14px;
            border: none;
            border-radius: 5px;
            cursor: pointer;
            transition: 0.3s;
        }

        .btn:hover { opacity: 0.8; }

        .btn-back { background: #6c757d; color: white; }
        .btn-folder { background: #007bff; color: white; }
        .btn-file { background: #28a745; color: white; }

        .section-title {
            margin-top: 20px;
            font-size: 20px;
            font-weight: bold;
            color: #333;
        }

        .items-container {
            display: flex;
            flex-wrap: wrap;
            justify-content: center;
            gap: 15px;
            margin-top: 15px;
        }

        .folder-card, .file-card {
            width: 120px;
            height: 140px;
            background: white;
            display: flex;
            flex-direction: column;
            align-items: center;
            justify-content: center;
            padding: 10px;
            border-radius: 8px;
            box-shadow: 2px 4px 8px rgba(0, 0, 0, 0.1);
            transition: 0.3s;
            cursor: pointer;
            text-align: center;
        }

        .folder-card:hover, .file-card:hover {
            transform: scale(1.05);
            box-shadow: 4px 6px 12px rgba(0, 0, 0, 0.2);
        }

        .folder-card img, .file-card img {
            width: 50px;
            height: 50px;
            margin-bottom: 8px;
        }

        .file-container {
            display: flex;
            flex-wrap: wrap;
            justify-content: center;
            gap: 10px;
            margin-top: 15px;
        }

        .file-card {
            width: 120px;
            height: 140px;
            background: white;
            display: flex;
            flex-direction: column;
            align-items: center;
            justify-content: center;
            padding: 10px;
            border-radius: 8px;
            box-shadow: 2px 4px 8px rgba(0, 0, 0, 0.1);
            cursor: pointer;
            transition: 0.3s;
            text-align: center;
        }

        .file-card:hover {
            background-color: #f1f1f1;
            transform: scale(1.05);
        }

        .upload-btn {
            background: #28a745;
            color: white;
            padding: 10px;
            cursor: pointer;
            border-radius: 5px;
        }

        .upload-btn:hover { background: #218838; }

        #fileInput { display: none; }
    </style>
</head>
<body>
    <div class="container">
        <div class="btn-container">
            <button class="btn btn-back" onclick="history.back()"> Back</button>
            <button class="btn btn-folder" onclick="showAddFolder()"> Add Folder</button>
            <label class="upload-btn">
                Upload & Split File
                <input type="file" id="fileInput" />
            </label>
        </div>

        <h2 class="section-title"> Sub-Folders</h2>
        <div class="items-container">
            <% boolean hasFolders = false; %>
            <% while (folderRs.next()) { hasFolders = true; %>
                <div class="folder-card" onclick="location.href='folder.jsp?folderId=<%= folderRs.getInt("id") %>'">
                    <img src="icons/folder.png" alt="Folder">
                    <span><%= folderRs.getString("folder_name") %></span>
                </div>
            <% } %>
            <% if (!hasFolders) { %>
                <p>No sub-folders available.</p>
            <% } %>
        </div>

        <h2 class="section-title">Files in Folder</h2>
        <div class="file-container">
            <%
                HttpSession sessionObj = request.getSession(false);
                String username = (sessionObj != null) ? (String) sessionObj.getAttribute("username") : null;

                if (username != null) {
                    String sql = "SELECT file_name, COUNT(*) AS duplicate_count FROM files WHERE username = ? AND folder_id = ? GROUP BY file_name, folder_id, username HAVING COUNT(*) > 1";
                    PreparedStatement pstmt = conn.prepareStatement(sql);
                    pstmt.setString(1, username);
                    pstmt.setInt(2, folderId);
                    ResultSet rs = pstmt.executeQuery();

                    while (rs.next()) {
                        String fileName = rs.getString("file_name");
                        String fileExtension = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
                        String fileIcon;

                        switch (fileExtension) {
                            case "pdf": fileIcon = "icons/pdficon.png"; break;
                            case "doc": case "docx": fileIcon = "icons/word.png"; break;
                            case "xls": case "xlsx": fileIcon = "icons/sheets.png"; break;
                            case "ppt": case "pptx": fileIcon = "icons/ppt.png"; break;
                            case "jpg": case "jpeg": case "png": case "gif": case "svg": fileIcon = "icons/picture.png"; break;
                            case "mp4": case "avi": case "mov": case "mkv": fileIcon = "icons/video.png"; break;
                            case "mp3": case "wav": fileIcon = "icons/audio.png"; break;
                            case "zip": case "rar": case "tar": case "7z": fileIcon = "icons/zip-folder.png"; break;
                            case "txt": fileIcon = "icons/txt.png"; break;
                            case "html": case "css": case "js": case "java": case "py": case "cpp": case "c": fileIcon = "icons/code.png"; break;
                        default: fileIcon = "icons/file.png";
                                    }

            %>
                        <div class="file-card" onclick="mergeFile('<%= fileName %>')">
                            <img src="<%= fileIcon %>" alt="File Icon">
                            <span><%= fileName %></span>
                        </div>
            <%
                    }
                }
            %>
        </div>
    </div>

    <script>
    function showAddFolder() {
            let name = prompt("Enter folder name:");
            if (name) {
                fetch("CreateFolderServlet", {
                    method: "POST",
                    headers: { "Content-Type": "application/x-www-form-urlencoded" },
                    body: "folderName=" + encodeURIComponent(name) + "&parentFolderId=" + <%= folderId %>
                }).then(() => location.reload());
            }
        }

        const urlParams = new URLSearchParams(window.location.search);
        const folderId = urlParams.get('folderId');

        $(document).ready(function() {
            $("#fileInput").change(function () {
                let file = this.files[0];
                if (!file) return;

                let formData = new FormData();
                formData.append("file", file);
                if (folderId) {
                    formData.append("folderId", folderId);
                }

                $.ajax({
                    url: "FileSplitterServlet",
                    type: "POST",
                    data: formData,
                    processData: false,
                    contentType: false,
                    success: function () {
                        alert("File uploaded and split successfully!");
                        location.reload();
                    },
                    error: function () {
                        alert("File upload failed. Please try again.");
                    }
                });
            });
        });

        function mergeFile(fileName) {
            if (folderId) {
                window.location.href = "FileMergerServlet?fileName=" + encodeURIComponent(fileName) + "&folderId=" + encodeURIComponent(folderId);
            } else {
                alert("Folder ID missing!");
            }
        }
    </script>
</body>
</html>
