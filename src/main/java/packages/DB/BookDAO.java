package packages.DB;

import packages.Classes.Book;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BookDAO {
    public List<Book> searchBooks(String category, String keyword, String sortBy, String orderBy) {
        List<Book> list = new ArrayList<>();

        String sql = "select * from book where " + category + " LIKE ?";
        String column = "";
        if (sortBy.equals("제목순")) {
            column = "title";
        } else if (sortBy.equals("재고순")) {
            column = "stock";
        }

        if (!column.isEmpty()) {
            String order = orderBy.equals("오름차순") ? "ASC" : "DESC";
            sql += " ORDER BY " + column + " " + order;
        }
        try (
                Connection conn = DBConnector.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, "%" + keyword + "%");//첫번째 물음표 가 1
                ResultSet rs = pstmt.executeQuery();

                while (rs.next()) {
                    list.add(Book.builder()
                            .book_id(rs.getInt("book_id"))
                            .title(rs.getString("title"))
                            .author(rs.getString("author"))
                            .publisher(rs.getString("publisher"))
                                    .stock(rs.getInt("stock"))
                            .imagePath(rs.getString("image_path"))
                                    .total_rent_count(rs.getInt("total_rent_count"))
                            .build());
                }
           // System.out.println(sql); 확인용
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
                list.add(Book.builder()
                        .book_id(rs.getInt("book_id"))
                        .title(rs.getString("title"))
                        .author(rs.getString("author"))
                        .publisher(rs.getString("publisher"))
                        .stock(rs.getInt("stock"))
                        .imagePath(rs.getString("image_path"))
                        .total_rent_count(rs.getInt("total_rent_count"))
                        .build());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<Book> getMostRentedBooks(int limit) {
        List<Book> list = new ArrayList<>();
        String sql = """
        SELECT b.*, COUNT(r.id) AS rent_count
        FROM book b
        LEFT JOIN rentals r ON b.book_id = r.book_id
        GROUP BY b.book_id
        ORDER BY rent_count DESC
        LIMIT ?
    """;

        try (Connection conn = DBConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, limit);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Book book = Book.builder()
                        .book_id(rs.getInt("book_id"))
                        .title(rs.getString("title"))
                        .author(rs.getString("author"))
                        .publisher(rs.getString("publisher"))
                        .stock(rs.getInt("stock"))
                        .imagePath(rs.getString("image_path"))
                        .build();
                list.add(book);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
    // Book 삽입
    public boolean insertBook(Book book) {
        String sql = "INSERT INTO book (title, author, publisher, stock, image_path, total_rent_count) VALUES (?, ?, ?, ?, ?, 0)";
        try (Connection conn = DBConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, book.getTitle());
            pstmt.setString(2, book.getAuthor());
            pstmt.setString(3, book.getPublisher());
            pstmt.setInt(4, book.getStock());
            pstmt.setString(5, book.getImagePath());
            return pstmt.executeUpdate() == 1;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Book 수정
    public boolean updateBook(Book book) {
        String sql = "UPDATE book SET title = ?, author = ?, publisher = ?, stock = ?, image_path = ? WHERE book_id = ?";
        try (Connection conn = DBConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, book.getTitle());
            pstmt.setString(2, book.getAuthor());
            pstmt.setString(3, book.getPublisher());
            pstmt.setInt(4, book.getStock());
            pstmt.setString(5, book.getImagePath());
            pstmt.setInt(6, book.getBook_id());
            return pstmt.executeUpdate() == 1;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Book 삭제
    public boolean deleteBook(int bookId) {
        String sql = "DELETE FROM book WHERE book_id = ?";
        try (Connection conn = DBConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, bookId);
            return pstmt.executeUpdate() == 1;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }



}
