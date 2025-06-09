package packages.MainForm;

import packages.Classes.Book;
import packages.DB.BookDAO;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.util.function.Consumer;

public class BookFormDialog extends JDialog {
    private JTextField titleField, authorField, publisherField, stockField, imagePathField;
    private final Book editingBook;
    private final Consumer<Boolean> onSavedCallback;

    public BookFormDialog(Frame parent, Book book, Consumer<Boolean> callback) {
        super(parent, true);
        this.editingBook = book;
        this.onSavedCallback = callback;
        setTitle(book == null ? "📘 새 도서 추가" : "✏ 도서 정보 수정");
        setSize(400, 350);
        setLocationRelativeTo(parent);
        initUI();
    }

    private void initUI() {
        JPanel panel = new JPanel(new MigLayout("wrap 2", "[][grow,fill]", "[]10[]10[]10[]10[]10[]20[]"));
        panel.setBackground(Color.WHITE);

        titleField = new JTextField();
        authorField = new JTextField();
        publisherField = new JTextField();
        stockField = new JTextField();
        imagePathField = new JTextField();

        panel.add(new JLabel("제목"));         panel.add(titleField);
        panel.add(new JLabel("저자"));         panel.add(authorField);
        panel.add(new JLabel("출판사"));       panel.add(publisherField);
        panel.add(new JLabel("재고"));         panel.add(stockField);
        panel.add(new JLabel("이미지 경로"));  panel.add(imagePathField);

        if (editingBook != null) {
            titleField.setText(editingBook.getTitle());
            authorField.setText(editingBook.getAuthor());
            publisherField.setText(editingBook.getPublisher());
            stockField.setText(String.valueOf(editingBook.getStock()));
            imagePathField.setText(editingBook.getImagePath());
        }

        JButton saveBtn = new JButton("저장");
        saveBtn.addActionListener(e -> saveBook());
        JButton cancelBtn = new JButton("취소");
        cancelBtn.addActionListener(e -> dispose());

        panel.add(saveBtn, "span, split 2, center");
        panel.add(cancelBtn);

        add(panel);
    }

    private void saveBook() {
        try {
            String title = titleField.getText().trim();
            String author = authorField.getText().trim();
            String publisher = publisherField.getText().trim();
            int stock = Integer.parseInt(stockField.getText().trim());
            String imagePath = imagePathField.getText().trim();

            if (title.isEmpty() || author.isEmpty()) {
                JOptionPane.showMessageDialog(this, "제목과 저자는 필수입니다.");
                return;
            }

            // ✅ 기본 이미지 경로 설정
            if (imagePath.isEmpty()) {
                imagePath = "default_book.png"; // 또는 존재하는 기본 이미지 이름
            }

            BookDAO dao = new BookDAO();
            boolean success;
            if (editingBook == null) {
                // 새 도서 삽입
                Book newBook = Book.builder()
                        .title(title)
                        .author(author)
                        .publisher(publisher)
                        .stock(stock)
                        .imagePath(imagePath)
                        .total_rent_count(0) // ✅ 기본값 0 설정
                        .build();
                success = dao.insertBook(newBook);
            } else {
                // 기존 도서 수정
                editingBook.setTitle(title);
                editingBook.setAuthor(author);
                editingBook.setPublisher(publisher);
                editingBook.setStock(stock);
                editingBook.setImagePath(imagePath);
                success = dao.updateBook(editingBook);
            }

            if (success) {
                JOptionPane.showMessageDialog(this, "도서 정보가 저장되었습니다.");
                if (onSavedCallback != null) onSavedCallback.accept(true);
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "저장 실패. 다시 시도해주세요.");
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "오류: " + e.getMessage());
        }
    }

}
