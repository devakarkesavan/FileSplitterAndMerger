<%@ page import="java.sql.*, utils.DBConnection" %>
<%
    HttpSession sessionObj = request.getSession(false);
    if (sessionObj == null || sessionObj.getAttribute("username") == null) {
        response.sendRedirect("login.jsp");
        return;
    }
    Integer id = (Integer) sessionObj.getAttribute("id");
    String username = (String) sessionObj.getAttribute("username");
%>

<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta http-equiv="Cache-Control" content="no-store,no-cache,must-revalidate">
    <meta http-equiv="Pragma" content="no-cache">
    <meta http-equiv="Expires" content="-1">
    <title>File Split & Merge</title>
    <style>
        /* Global Styling */
        * {
            margin: 0;
            padding: 0;
            box-sizing: border-box;
            font-family: Arial, sans-serif;
        }

        body {
            background-image: url("icons/background.png");
            color: black;
            text-align: center;
            padding: 20px;
        }

        h2 {
            margin-bottom: 20px;
        }

        .container {
            background: rgba(255, 255, 255, 0.1);
            padding: 20px;
            border-radius: 10px;
            width: 50%;
            margin: auto;
            box-shadow: 0 0 10px rgba(0, 0, 0, 0.2);
        }

        input[type="file"] {
            display: block;
            margin: 15px auto;
            padding: 10px;
            border-radius: 5px;
            background: white;
            color: black;
            width: 80%;
            border: none;
        }

        button {
            padding: 10px 20px;
            background: #ff8c00;
            border: none;
            color: white;
            font-size: 16px;
            border-radius: 5px;
            cursor: pointer;
            transition: 0.3s;
        }

        button:hover {
            background: #ff6600;
        }

        /* File Cards */
        .file-container {
            display: flex;
            flex-wrap: wrap;
            gap: 15px;
            justify-content: center;
            margin-top: 20px;
        }

        .file-card {
            width: 140px;
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

        .file-card:hover {
            transform: scale(1.05);
            box-shadow: 4px 6px 12px rgba(0, 0, 0, 0.2);
        }

        .file-icon {
            width: 50px;  /* Adjust size as needed */
            height: 50px;
            object-fit: contain;
            margin-bottom: 8px;
        }

        .file-name {
            font-size: 14px;
            font-weight: 500;
            color: #333;
            word-wrap: break-word;
        }


        .folder-container {
            display: flex;
            flex-wrap: wrap;
            gap: 15px;
            justify-content: center;
            margin-top: 20px;
        }

        .folder-card {
            width: 150px;
            height: 150px;
            background: #fff;
            display: flex;
            flex-direction: column;
            align-items: center;
            justify-content: center;
            padding: 10px;
            border-radius: 10px;
            box-shadow: 2px 4px 10px rgba(0, 0, 0, 0.2);
            transition: 0.3s;
            cursor: pointer;
        }

        .folder-card:hover {
            transform: scale(1.05);
            box-shadow: 4px 6px 15px rgba(0, 0, 0, 0.3);
        }

        .folder-card img {
            width: 60px;
            height: 60px;
            object-fit: contain;
            margin-bottom: 10px;
        }

        .folder-card span {
            font-size: 16px;
            font-weight: bold;
            color: #333;
        }


        /* Logout Link */
        .logout {
            position: absolute;
            top: 10px;
            right: 20px;
            background: red;
            padding: 8px 12px;
            border-radius: 5px;
            text-decoration: none;
            color: white;
            font-weight: bold;
            transition: 0.3s;
        }

        .logout:hover {
            background: darkred;
        }
        .modal {
            display: none; /* Hide modal initially */
            position: fixed;
            top: 0;
            left: 0;
            width: 100%;
            height: 100%;
            background: rgba(0, 0, 0, 0.5);
            justify-content: center;
            align-items: center;
        }


    </style>
</head>
<body>
    <a href="LogoutServlet" class="logout">Logout</a>
    <h2>Welcome, <%= username %>!</h2>

        <div class="container">
            <h2>File System</h2>
            <button class="button" onclick="openFolderModal()">📁 Add Folder</button>

            <div class="folder-container">
                <%
                    try (Connection conn = DBConnection.getConnection()) {
                        String sql = "SELECT id, folder_name FROM user_folders WHERE user_id=? AND (parent_folder_id IS NULL OR parent_folder_id=0)";
                        PreparedStatement pstmt = conn.prepareStatement(sql);
                        pstmt.setInt(1, id);
                        ResultSet rs = pstmt.executeQuery();

                        while (rs.next()) {
                            int folderId = rs.getInt("id");
                            String folderName = rs.getString("folder_name");
                            String folderImage = "icons/folder.png";
                %>
                            <div class="folder-card" onclick="openFolder(<%= folderId %>)">
                                <img src="<%= folderImage %>" alt="Folder">
                                <span><%= folderName %></span>
                            </div>
                <%
                        }
                    } catch (Exception e) {
                        out.println("<p>Error: " + e.getMessage() + "</p>");
                    }
                %>
            </div>

        </div>

        <div id="folderModal" class="modal">
            <div class="modal-content">
                <h3>Create Folder</h3>
                <input type="text" id="folderName" placeholder="Enter folder name">
                <button onclick="createFolder()">Create</button>
                <button onclick="closeFolderModal()">Cancel</button>
            </div>
        </div>

        <script>
        window.addEventListener("pageshow", function (event) {
                if (event.persisted) {
                    window.location.reload();
                }
            });
            function openFolder(folderId) {
                window.location.href = 'folder.jsp?folderId=' + folderId;
            }

            function openFolderModal() {
                document.getElementById('folderModal').style.display = 'flex';
            }

            function closeFolderModal() {
                document.getElementById('folderModal').style.display = 'none';
            }

function createFolder() {
    let folderName = document.getElementById('folderName').value;
    if (!folderName) return alert("Enter a folder name!");

    let parentFolderId = null; 

    fetch('CreateFolderServlet', {
        method: 'POST',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        body: new URLSearchParams({
            folderName: folderName,
            parentFolderId: parentFolderId 
        })
    }).then(response => {
        if (!response.ok) {
            return response.text().then(text => { throw new Error(text); });
        }
        location.reload();
    }).catch(error => alert("Error: " + error.message));
}

        </script>


</body>
</html>
