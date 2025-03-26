<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html>
<head>
    <meta http-equiv="Cache-Control" content="no-store,no-cache,must-revalidate">
    <meta http-equiv="Pragma" content="no-cache">
    <meta http-equiv="Expires" content="-1">
    <title>Message</title>
    <style>
        body {
            background-image: url("icons/background.png");
            color: black;
            font-family: Arial, sans-serif;
            text-align: center;
            margin-top: 50px;
        }
        .message-container {
            border: 1px solid #ddd;
            padding: 20px;
            display: inline-block;
            background-color: #f8f8f8;
            border-radius: 5px;
        }
        a {
            color: blue;
            text-decoration: none;
            font-weight: bold;
        }
        a:hover {
            text-decoration: underline;
        }
    </style>
</head>
<body>

<div class="message-container">
    <h2><%= request.getAttribute("message") %></h2>
</div>

</body>
</html>
