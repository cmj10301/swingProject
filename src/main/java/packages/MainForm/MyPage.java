package packages.MainForm;

import net.miginfocom.swing.MigLayout;
import packages.Classes.Rental;
import packages.Classes.User;
import packages.DB.RentalDAO;
import packages.DB.UserDAO;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class MyPage extends JDialog {
    private JTextField nameField, emailField;
    private JRadioButton maleRadio, femaleRadio;
    private User user;
    private Runnable onUpdateCallback;

    private String originalName, originalEmail;
    private char originalGender;

    public MyPage(JFrame parent, User user, Runnable onUpdateCallback) {
        super(parent, "마이페이지", true);
        this.user = user;
        this.onUpdateCallback = onUpdateCallback;

        this.originalName = user.getName();
        this.originalEmail = user.getEmail();
        this.originalGender = user.getGender();

        setLayout(new BorderLayout());
        setSize(600, 500);
        setLocationRelativeTo(parent);

        JPanel formPanel = new JPanel(new MigLayout("wrap 2", "[right]20[250]", "10[]10[]10[]10[]20[]"));

        JLabel titleLabel = new JLabel("회원정보 수정");
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 18f));
        formPanel.add(titleLabel, "span 2, center");

        formPanel.add(new JLabel("아이디:"));
        formPanel.add(new JLabel(user.getUsername()));

        formPanel.add(new JLabel("이름:"));
        nameField = new JTextField(originalName);
        formPanel.add(nameField);

        formPanel.add(new JLabel("이메일:"));
        emailField = new JTextField(originalEmail);
        formPanel.add(emailField);

        formPanel.add(new JLabel("성별:"));
        JPanel genderPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        maleRadio = new JRadioButton("남");
        femaleRadio = new JRadioButton("여");
        ButtonGroup group = new ButtonGroup();
        group.add(maleRadio);
        group.add(femaleRadio);
        genderPanel.add(maleRadio);
        genderPanel.add(femaleRadio);
        if (originalGender == 'M') maleRadio.setSelected(true);
        else femaleRadio.setSelected(true);
        formPanel.add(genderPanel);

        JButton confirmBtn = new JButton("회원정보 수정");
        confirmBtn.addActionListener(e -> updateUserInfo());
        formPanel.add(confirmBtn, "span 2, center");

        add(formPanel, BorderLayout.NORTH);

        // 🔽 대출 내역 테이블
        JTable rentalTable = createRentalTable();
        JScrollPane scrollPane = new JScrollPane(rentalTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder("대출 / 반납 내역"));
        add(scrollPane, BorderLayout.CENTER);
    }

    private JTable createRentalTable() {
        String[] columns = {"도서 제목", "저자", "대출일", "반납일"};
        DefaultTableModel model = new DefaultTableModel(columns, 0);
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        RentalDAO dao = new RentalDAO();
        List<Rental> rentals = dao.getRentalHistoryByUserId(user.getUserId());

        for (Rental rental : rentals) {
            String title = rental.getBook().getTitle();
            String author = rental.getBook().getAuthor();
            String rentDate = rental.getRentDate().format(fmt);
            String returnDate = (rental.getReturnDate() != null)
                    ? rental.getReturnDate().format(fmt) : "대여 중";

            model.addRow(new Object[]{title, author, rentDate, returnDate});
        }

        JTable table = new JTable(model);
        table.setRowHeight(25);
        return table;
    }

    private void updateUserInfo() {
        String newName = nameField.getText().trim();
        String newEmail = emailField.getText().trim();
        char gender = maleRadio.isSelected() ? 'M' : 'F';

        boolean isChanged = !(newName.equals(originalName) &&
                newEmail.equals(originalEmail) &&
                gender == originalGender);

        if (!isChanged) {
            JOptionPane.showMessageDialog(this, "변경된 내용이 없습니다.");
            return;
        }

        user.setName(newName);
        user.setEmail(newEmail);
        user.setGender(gender);

        boolean result = UserDAO.getInstance().updateUserInfo(user);
        if (result) {
            JOptionPane.showMessageDialog(this, "수정 완료!");
            if (onUpdateCallback != null) onUpdateCallback.run();
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, "수정 실패!");
        }
    }
}
