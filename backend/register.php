<?php
/**
 * API Endpoint for User Registration.
 */
header('Content-Type: application/json');
require_once 'db_config.php';

if ($_SERVER['REQUEST_METHOD'] == 'POST') {
    $user = $_POST['username'] ?? '';
    $pass = $_POST['password'] ?? '';
    $role = $_POST['role'] ?? '';

    if (empty($user) || empty($pass) || empty($role)) {
        echo json_encode(["status" => "error", "message" => "All fields are required"]);
        exit();
    }

    // Hash the password for security
    $hashed_pass = password_hash($pass, PASSWORD_DEFAULT);

    try {
        $stmt = $conn->prepare("INSERT INTO users (username, password, role) VALUES (:u, :p, :r)");
        $stmt->bindParam(':u', $user);
        $stmt->bindParam(':p', $hashed_pass);
        $stmt->bindParam(':r', $role);

        if ($stmt->execute()) {
            echo json_encode(["status" => "success", "message" => "User registered successfully"]);
        } else {
            echo json_encode(["status" => "error", "message" => "Registration failed"]);
        }
    } catch(PDOException $e) {
        if ($e->getCode() == 23000) { // Integrity constraint violation: 1062 Duplicate entry
            echo json_encode(["status" => "error", "message" => "Username already exists"]);
        } else {
            echo json_encode(["status" => "error", "message" => $e->getMessage()]);
        }
    }
} else {
    echo json_encode(["status" => "error", "message" => "Invalid request method"]);
}
?>
