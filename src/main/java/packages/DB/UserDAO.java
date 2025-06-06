package packages.DB;

import packages.Classes.User;

import java.sql.*;
import java.util.Optional;

public class UserDAO {
    private static UserDAO instance = new UserDAO();
    public static UserDAO getInstance() {
        return instance;
    }
    // ✅ 아이디 중복확인
    public boolean isUsernameDuplicate(String username) {
        String sql = "SELECT COUNT(*) FROM user WHERE username = ?";
        try (Connection conn = DBConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return true; // 예외 발생 시 중복된 걸로 처리
    }

    // ✅ 회원가입
    public boolean registerUser(User user) {
        String sql = "INSERT INTO user (username, password, email, name, birth_year, gender) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, user.getUsername());
            pstmt.setString(2, user.getPassword());
            pstmt.setString(3, user.getEmail());
            pstmt.setString(4, user.getName());
            pstmt.setInt(5, user.getBirthYear());
            pstmt.setString(6, String.valueOf(user.getGender()));
            return pstmt.executeUpdate() == 1;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // ✅ 로그인 검증
    public Optional<User> login(String username, String password) {
        String sql = "SELECT * FROM user WHERE username = ? AND password = ?";
        try (Connection conn = DBConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return Optional.of(User.builder()
                        .userId(rs.getInt("user_id"))
                        .username(rs.getString("username"))
                        .password(rs.getString("password"))
                        .email(rs.getString("email"))
                        .name(rs.getString("name"))
                        .birthYear(rs.getInt("birth_year"))
                        .gender(rs.getString("gender").charAt(0))
                        .build());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    public boolean updateUserInfo(User user) {
        String sql = "UPDATE user SET name = ?, email = ?, gender = ? WHERE user_id = ?";
        try (Connection conn = DBConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, user.getName());
            pstmt.setString(2, user.getEmail());
            pstmt.setString(3, String.valueOf(user.getGender()));  // "M" 또는 "F"
            pstmt.setInt(4, user.getUserId());
            return pstmt.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }



}
