<!DOCTYPE html>
<html lang="en">
<head>
<meta http-equiv="Cache-Control" content="no-store,no-cache,must-revalidate">
<meta http-equiv="Pragma" content="no-cache">
<meta http-equiv="Expires" content="-1">
    <title>Signup</title>
    <style>
        body {
            font-family: 'Arial', sans-serif;
            background-image: url("icons/background.png");
            color: black;
            text-align: center;
            margin: 0;
            padding: 0;
        }
        .container {
            background: rgba(255, 255, 255, 0.2);
            padding: 30px;
            margin-top: 50px;
            display: inline-block;
            border-radius: 10px;
        }
        input {
            width: 80%;
            padding: 10px;
            margin: 10px 0;
            border: none;
            border-radius: 5px;
            font-size: 16px;
        }
        button {
            padding: 10px 20px;
            margin-top: 10px;
            border: none;
            border-radius: 5px;
            background: #667eea;
            color: white;
            font-size: 16px;
            cursor: pointer;
            transition: 0.3s;
        }
        button:hover {
            background: #764ba2;
        }
        a {
            color: blue;
            font-weight: bold;
        }
    </style>
</head>
<body>
    <div class="container">
        <h1>Signup Page</h1>
        <form action="SignupServlet" method="post">
            Username: <input type="text" name="username" required><br>
            Email: <input type="email" name="email" required><br>
            Password: <input type="password" name="password" required><br>
            <button type="submit">Sign Up</button>
            Already have an account <a href="login.jsp">Login</a>
        </form>
    </div>
</body>
</html>
