<?php
/**
 * API Endpoint for User Login.
 */
header('Content-Type: application/json');
require_once 'db_config.php';

if ($_SERVER['REQUEST_METHOD'] == 'POST') {
    $user = $_POST['username'] ?? '';
    $pass = $_POST['password'] ?? '';

    if (empty($user) || empty($pass)) {
        echo json_encode(["status" => "error", "message" => "Username and password are required"]);
        exit();
    }

    try {
        $stmt = $conn->prepare("SELECT * FROM users WHERE username = :u");
        $stmt->bindParam(':u', $user);
        $stmt->execute();
        $userData = $stmt->fetch(PDO::FETCH_ASSOC);

        if ($userData && password_verify($pass, $userData['password'])) {
            echo json_encode([
                "status" => "success",
                "message" => "Login successful",
                "user" => [
                    "username" => $userData['username'],
                    "role" => $userData['role']
                ]
            ]);
        } else {
            echo json_encode(["status" => "error", "message" => "Invalid username or password"]);
        }
    } catch(PDOException $e) {
        echo json_encode(["status" => "error", "message" => $e->getMessage()]);
    }
} else {
    echo json_encode(["status" => "error", "message" => "Invalid request method"]);
}
?>
