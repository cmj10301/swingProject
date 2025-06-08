package packages.DB;

import packages.Classes.Book;
import packages.Classes.Rental;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class RentalDAO {

    // 현재 대출 여부 확인
    public boolean isBookRentedByUser(int userId, int bookId) {
        String sql = "SELECT * FROM rentals WHERE user_id = ? AND book_id = ? AND return_date IS NULL";
        try (Connection conn = DBConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            pstmt.setInt(2, bookId);

            ResultSet rs = pstmt.executeQuery();
            return rs.next();  // 대출 기록 존재하면 true

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // 대여하기
    public boolean rentBook(int userId, int bookId) {
        String insertRental = "INSERT INTO rentals (user_id, book_id, rent_date) VALUES (?, ?, NOW())";
        String updateBook = "UPDATE book SET total_rent_count = total_rent_count + 1, stock = stock - 1 WHERE book_id = ?";
        try (Connection conn = DBConnector.getConnection()) {
            conn.setAutoCommit(false);  // 트랜잭션 시작

            try (PreparedStatement pstmt1 = conn.prepareStatement(insertRental);
                 PreparedStatement pstmt2 = conn.prepareStatement(updateBook)) {

                pstmt1.setInt(1, userId);
                pstmt1.setInt(2, bookId);
                pstmt1.executeUpdate();

                pstmt2.setInt(1, bookId);
                pstmt2.executeUpdate();

                conn.commit();
                return true;

            } catch (SQLException e) {
                conn.rollback();
                e.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }


    // 반납하기
    public boolean returnBook(int userId, int bookId) {
        String sql = "UPDATE rentals SET return_date = NOW() " +
                "WHERE user_id = ? AND book_id = ? AND return_date IS NULL";
        try (Connection conn = DBConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setInt(2, bookId);
            return pstmt.executeUpdate() == 1;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // 대출 내역 조회 (Book 정보 포함)
    public List<Rental> getRentalHistoryByUserId(int userId) {
        List<Rental> list = new ArrayList<>();
        String sql = """
            SELECT r.id AS rental_id, r.user_id, r.book_id, r.rent_date, r.return_date,
                   b.title, b.author, b.publisher, b.image_path, b.stock, b.total_rent_count
            FROM rentals r
            JOIN book b ON r.book_id = b.book_id
            WHERE r.user_id = ?
            ORDER BY r.rent_date DESC
        """;

        try (Connection conn = DBConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Book book = Book.builder()
                        .book_id(rs.getInt("book_id"))
                        .title(rs.getString("title"))
                        .author(rs.getString("author"))
                        .publisher(rs.getString("publisher"))
                        .imagePath(rs.getString("image_path"))
                        .stock(rs.getInt("stock"))
                        .total_rent_count(rs.getInt("total_rent_count"))
                        .build();

                Rental rental = Rental.builder()
                        .id(rs.getInt("rental_id"))
                        .userId(rs.getInt("user_id"))
                        .bookId(rs.getInt("book_id"))
                        .rentDate(rs.getDate("rent_date").toLocalDate())
                        .returnDate(rs.getDate("return_date") != null ? rs.getDate("return_date").toLocalDate() : null)
                        .book(book)
                        .build();

                list.add(rental);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

    public List<Book> getBestSellerBooks(int limit) {
        List<Book> list = new ArrayList<>();
        String sql = "SELECT * FROM book ORDER BY total_rent_count DESC LIMIT ?";
        try (Connection conn = DBConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, limit);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                list.add(new Book(
                        rs.getString("image_path"),
                        rs.getString("title"),
                        rs.getString("author"),
                        rs.getString("publisher"),
                        rs.getInt("book_id"),
                        rs.getInt("stock"),
                        rs.getInt("total_rent_count")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<Book> getRecentBestsellers(int limit) {
        List<Book> list = new ArrayList<>();
        String sql = """
        SELECT b.*, COUNT(r.id) AS recent_rent_count
        FROM rentals r
        JOIN book b ON r.book_id = b.book_id
        WHERE r.rent_date >= DATE_SUB(CURDATE(), INTERVAL 30 DAY)
        GROUP BY r.book_id
        ORDER BY recent_rent_count DESC
        LIMIT ?
    """;
        try (Connection conn = DBConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, limit);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                list.add(new Book(
                        rs.getString("image_path"),
                        rs.getString("title"),
                        rs.getString("author"),
                        rs.getString("publisher"),
                        rs.getInt("book_id"),
                        rs.getInt("stock"),
                        rs.getInt("total_rent_count")
                ));

            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }


}
