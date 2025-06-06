package packages.MainForm;

import net.miginfocom.swing.MigLayout;
import packages.Classes.User;
import packages.DB.UserDAO;

import javax.swing.*;
import java.awt.*;

public class MyPage extends JDialog {
    private JTextField nameField, emailField;
    private JRadioButton maleRadio, femaleRadio;
    private User user;
    private Runnable onUpdateCallback;

    public MyPage(JFrame parent, User user, Runnable onUpdateCallback) {
        super(parent, "마이페이지", true); // 모달 다이얼로그
        this.user = user;
        this.onUpdateCallback = onUpdateCallback;
        setLayout(new MigLayout(
                "wrap 2, align center",   // 가운데 정렬
                "[right]20[150!]",        // 라벨 + 필드 배치
                "10[]10[]10[]10[]20[]"
        ));
        JLabel titleLabel = new JLabel("<회원정보수정>");
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 18f));
        add(titleLabel, "span 2, center");
        setSize(400, 300);
        setLocationRelativeTo(parent);

        add(new JLabel("아이디:"));
        add(new JLabel(user.getUsername()));

        add(new JLabel("이름:"));
        nameField = new JTextField(user.getName());
        add(nameField, "w 150::200");


        add(new JLabel("이메일:"));
        emailField = new JTextField(user.getEmail());
        add(emailField, "w 150::250");

        add(new JLabel("성별:"));
        JPanel genderPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        maleRadio = new JRadioButton("남");
        femaleRadio = new JRadioButton("여");
        ButtonGroup group = new ButtonGroup();
        group.add(maleRadio);
        group.add(femaleRadio);
        genderPanel.add(maleRadio);
        genderPanel.add(femaleRadio);
        if (user.getGender() == 'M') maleRadio.setSelected(true);
        else femaleRadio.setSelected(true);
        add(genderPanel);

        JButton confirmBtn = new JButton("확인");
        confirmBtn.addActionListener(e -> updateUserInfo());
        add(confirmBtn, "span 2, center");
    }

    private void updateUserInfo() {
        String newName = nameField.getText().trim();
        String newEmail = emailField.getText().trim();
        char gender = maleRadio.isSelected() ? 'M' : 'F';

        user.setName(newName);
        user.setEmail(newEmail);
        user.setGender(gender);

        boolean result = UserDAO.getInstance().updateUserInfo(user);
        if (result) {
            JOptionPane.showMessageDialog(this, "수정 완료!");
            if (onUpdateCallback != null) onUpdateCallback.run(); // topbar 갱신용
            dispose(); // 창 닫기
        } else {
            JOptionPane.showMessageDialog(this, "수정 실패!");
        }
    }
}
