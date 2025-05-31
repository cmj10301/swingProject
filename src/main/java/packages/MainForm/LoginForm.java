package packages.MainForm;

import net.miginfocom.swing.MigLayout;
import com.formdev.flatlaf.FlatClientProperties;
import packages.Classes.User;
import packages.DB.UserDAO;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.util.Optional;
import java.util.function.Consumer;

public class LoginForm extends JDialog {
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel cardPanel = new JPanel(cardLayout);

    // 로그인용 필드
    private JTextField loginIdField;
    private JPasswordField loginPwField;
    private JButton loginPwEyeBtn;
    private boolean showLoginPw = false;
    private Consumer<User> loginSuccessCallback; //콜백
    // 회원가입용 필드
    private JTextField signupIdField, signupEmailField, signupNameField, signupBirthField;
    private JPasswordField signupPwField, signupPwConfirmField;
    private JButton signupPwEyeBtn1, signupPwEyeBtn2;
    private boolean showSignupPw = false, showSignupPwConfirm = false;
    private JLabel signupPwMsg; // 확인메세지
    private JLabel signupIdMsg; // 아이디 중복 메시지
    private JToggleButton signupMaleBtn;
    private JToggleButton signupFemaleBtn;
    private User loggedInUser;

    public LoginForm(JFrame parent, Consumer<User> loginSuccessCallback) {
        super(parent, "로그인 / 회원가입", true);
        this.loginSuccessCallback = loginSuccessCallback;
        setSize(600, 700);
        setLocationRelativeTo(parent);

        // 위쪽 여백을 위한 panel
        JPanel outerPanel = new JPanel(new MigLayout("insets 100", "[grow,fill]", "[grow]"));

        cardPanel.add(createLoginPanel(), "login");
        cardPanel.add(createSignupPanel(), "signup");

        outerPanel.add(cardPanel, "grow");
        add(outerPanel);
    }
    // 로그인폼
    private JPanel createLoginPanel() {
        JPanel panel = new JPanel(new MigLayout(
                "wrap 1, insets 0",
                "[grow,fill]",
                "10[]10[]10[]10[]15[]"
        ));

        // 🔹 아이디 입력
        loginIdField = new JTextField();
        loginIdField.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "아이디 입력");
        loginIdField.setPreferredSize(new Dimension(0, 35));
        panel.add(loginIdField, "wmin 250, growx");

        // 🔹 비밀번호 입력 + 👁 버튼
        loginPwField = new JPasswordField();
        loginPwField.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "비밀번호 입력");
        loginPwField.setPreferredSize(new Dimension(0, 35));
        loginPwEyeBtn = new JButton("👁");
        loginPwEyeBtn.setPreferredSize(new Dimension(40, 35));

        JPanel pwPanel = new JPanel(new MigLayout("insets 0", "[grow][40!]", "[]"));
        pwPanel.add(loginPwField, "wmin 250, growx");
        pwPanel.add(loginPwEyeBtn);
        panel.add(pwPanel);

        loginPwEyeBtn.addActionListener(e -> {
            showLoginPw = !showLoginPw;
            togglePassword(loginPwField, loginPwEyeBtn, showLoginPw);
        });

        // 🔹 로그인 버튼
        JButton loginBtn = new JButton("로그인");
        loginBtn.setPreferredSize(new Dimension(0, 40));
        loginBtn.setFocusPainted(false);
        loginBtn.setBackground(Color.LIGHT_GRAY);
        panel.add(loginBtn, "wmin 250, growx");
        loginBtn.addActionListener(e -> {
            String id = loginIdField.getText();
            String pw = new String(loginPwField.getPassword());

            UserDAO userDAO = new UserDAO();
            Optional<User> result = userDAO.login(id, pw);

            if (result.isPresent()) {
                loggedInUser = result.get();
                if (loginSuccessCallback != null) {
                    loginSuccessCallback.accept(loggedInUser);  // ← 북매니저에게 전달
                }
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "아이디 또는 비밀번호가 틀렸습니다.");
            }
        });


        // 🔹 아이디 저장 + 찾기 링크
        JCheckBox saveId = new JCheckBox("아이디 저장");
        JLabel findId = new JLabel("아이디 찾기");
        JLabel findPw = new JLabel("비밀번호 찾기");

        findId.setForeground(Color.GRAY);
        findPw.setForeground(Color.GRAY);
        findId.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        findPw.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JPanel optionPanel = new JPanel(new MigLayout("insets 0", "[grow][grow]", "[]"));
        optionPanel.add(saveId, "align left");

        JPanel rightPanel = new JPanel(new MigLayout("insets 0", "[]10[]", "[]"));
        rightPanel.setOpaque(false);
        rightPanel.add(findId);
        rightPanel.add(findPw);

        optionPanel.add(rightPanel, "align right");
        panel.add(optionPanel);

        // 🔹 회원가입 버튼
        JButton joinBtn = new JButton("회원가입");
        joinBtn.setPreferredSize(new Dimension(0, 40));
        joinBtn.setFocusPainted(false);
        panel.add(joinBtn, "wmin 250, growx");



        joinBtn.addActionListener(e -> cardLayout.show(cardPanel, "signup"));

        return panel;
    }

    private JPanel createSignupPanel() {
        JPanel panel = new JPanel(new MigLayout(
                "wrap 1, insets 0",
                "[grow,fill]",
                "10[]10[]10[]10[]10[]10[]10[]10[]10[]10[]"));

        // 🔹 아이디 입력 + 중복확인
        signupIdField = new JTextField();
        signupIdField.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "아이디 입력");
        JButton checkBtn = new JButton("중복확인");
        signupIdField.setPreferredSize(new Dimension(0, 35));
        checkBtn.setPreferredSize(new Dimension(80, 35));

        JPanel idPanel = new JPanel(new MigLayout("insets 0", "[grow][80!]", "[]"));
        idPanel.add(signupIdField, "wmin 250, growx");
        idPanel.add(checkBtn, "w 80!");
        panel.add(idPanel);

        // 🔹 아이디 사용 가능 메시지
        signupIdMsg = new JLabel("");
        signupIdMsg.setForeground(Color.BLUE);
        panel.add(signupIdMsg, "gapleft 5, wrap");
        checkBtn.addActionListener(e -> {
            String username = signupIdField.getText().trim();
            if (username.isEmpty()) {
                signupIdMsg.setText("아이디를 입력해주세요.");
                signupIdMsg.setForeground(Color.RED);
                return;
            }

            UserDAO userDAO = new UserDAO();
            boolean isDuplicate = userDAO.isUsernameDuplicate(username);

            if (isDuplicate) {
                signupIdMsg.setText("이미 존재하는 아이디입니다.");
                signupIdMsg.setForeground(Color.RED);
            } else {
                signupIdMsg.setText("사용 가능한 아이디입니다.");
                signupIdMsg.setForeground(Color.BLUE);
            }
        });

        // 🔹 비밀번호 입력 + 보기 버튼
        signupPwField = new JPasswordField();
        signupPwField.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "비밀번호 입력");
        signupPwEyeBtn1 = new JButton("👁");
        signupPwField.setPreferredSize(new Dimension(0, 35));
        signupPwEyeBtn1.setPreferredSize(new Dimension(40, 35));

        JPanel pwPanel = new JPanel(new MigLayout("insets 0", "[grow][40!]", "[]"));
        pwPanel.add(signupPwField, "wmin 250, growx");
        pwPanel.add(signupPwEyeBtn1, "w 40!");
        panel.add(pwPanel);

        // 🔹 비밀번호 확인 + 보기 버튼
        signupPwConfirmField = new JPasswordField();
        signupPwConfirmField.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "비밀번호 확인");
        signupPwEyeBtn2 = new JButton("👁");
        signupPwConfirmField.setPreferredSize(new Dimension(0, 35));
        signupPwEyeBtn2.setPreferredSize(new Dimension(40, 35));

        JPanel pwConfirmPanel = new JPanel(new MigLayout("insets 0", "[grow][40!]", "[]"));
        pwConfirmPanel.add(signupPwConfirmField, "wmin 250, growx");
        pwConfirmPanel.add(signupPwEyeBtn2, "w 40!");
        panel.add(pwConfirmPanel);

        signupPwEyeBtn1.addActionListener(e -> {
            showSignupPw = !showSignupPw;
            togglePassword(signupPwField, signupPwEyeBtn1, showSignupPw);
        });

        signupPwEyeBtn2.addActionListener(e -> {
            showSignupPwConfirm = !showSignupPwConfirm;
            togglePassword(signupPwConfirmField, signupPwEyeBtn2, showSignupPwConfirm);
        });

        // 🔹 비밀번호 확인 메시지
        signupPwMsg = new JLabel("");
        signupPwConfirmField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                checkPasswordMatch();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                checkPasswordMatch();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                checkPasswordMatch();
            }
        });
        panel.add(signupPwMsg, "gapleft 5, wrap");

        // 🔹 이메일 입력
        signupEmailField = new JTextField();
        signupEmailField.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "이메일 입력");
        signupEmailField.setPreferredSize(new Dimension(0, 35));
        panel.add(signupEmailField, "wmin 250, growx");

        // 🔹 이름 입력
        signupNameField = new JTextField();
        signupNameField.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "이름 입력");
        signupNameField.setPreferredSize(new Dimension(0, 35));
        panel.add(signupNameField, "wmin 250, growx");

        // 🔹 생년 + 성별
        signupBirthField = new JTextField();
        signupBirthField.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "출생년도 입력");
        signupMaleBtn = new JToggleButton("남");
        signupFemaleBtn = new JToggleButton("여");

        ButtonGroup genderGroup = new ButtonGroup();
        genderGroup.add(signupMaleBtn);
        genderGroup.add(signupFemaleBtn);

        signupBirthField.setPreferredSize(new Dimension(0, 35));
        signupMaleBtn.setPreferredSize(new Dimension(50, 35));
        signupFemaleBtn.setPreferredSize(new Dimension(50, 35));

        JPanel sexPanel = new JPanel(new MigLayout("insets 0", "[50!][50!]", "[]"));
        sexPanel.add(signupMaleBtn);
        sexPanel.add(signupFemaleBtn);

        JPanel birthSexPanel = new JPanel(new MigLayout("insets 0", "[grow][]", "[]"));
        birthSexPanel.add(signupBirthField, "wmin 150, growx");
        birthSexPanel.add(sexPanel, "gapleft 50");
        panel.add(birthSexPanel);

        // 🔹 회원가입 + 돌아가기 버튼
        JButton signupBtn = new JButton("회원가입");
        JButton backBtn = new JButton("뒤로가기");
        signupBtn.setPreferredSize(new Dimension(0, 40));
        backBtn.setPreferredSize(new Dimension(0, 40));

        JPanel btnPanel = new JPanel(new MigLayout("insets 0", "[grow][grow]", "[]"));
        btnPanel.add(signupBtn, "growx");
        btnPanel.add(backBtn, "growx");
        panel.add(btnPanel);

        signupBtn.addActionListener(e -> {
            String id = signupIdField.getText().trim();
            String pw = new String(signupPwField.getPassword());
            String pwCheck = new String(signupPwConfirmField.getPassword());
            String email = signupEmailField.getText().trim();
            String name = signupNameField.getText().trim();
            String birthText = signupBirthField.getText().trim();

            if (id.isEmpty() || pw.isEmpty() || email.isEmpty() || name.isEmpty() || birthText.isEmpty()) {
                JOptionPane.showMessageDialog(null, "모든 항목을 입력해주세요.");
                return;
            }

            if (!pw.equals(pwCheck)) {
                JOptionPane.showMessageDialog(null, "비밀번호가 일치하지 않습니다.");
                return;
            }

            int birthYear;
            try {
                birthYear = Integer.parseInt(birthText);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(null, "출생년도는 숫자만 입력해주세요.");
                return;
            }

            char gender = ' ';
            if (signupMaleBtn.isSelected()) {
                gender = 'M';
            } else if (signupFemaleBtn.isSelected()) {
                gender = 'F';
            } else {
                JOptionPane.showMessageDialog(null, "성별을 선택해주세요.");
                return;
            }

            User user = User.builder()
                    .username(id)
                    .password(pw)
                    .email(email)
                    .name(name)
                    .birthYear(birthYear)
                    .gender(gender)
                    .build();

            UserDAO userDAO = new UserDAO();
            boolean success = userDAO.registerUser(user);

            if (success) {
                JOptionPane.showMessageDialog(null, "회원가입이 완료되었습니다!");
                // TODO: 로그인 화면으로 전환하거나 초기화 처리
            } else {
                JOptionPane.showMessageDialog(null, "회원가입에 실패했습니다. 관리자에게 문의하세요.");
            }
        });

        backBtn.addActionListener(e -> cardLayout.show(cardPanel, "login"));

        return panel;
    }

    private void togglePassword(JPasswordField field, JButton button, boolean isVisible) {
        if (isVisible) {
            field.setEchoChar((char) 0);
            button.setText("🔓");
        } else {
            field.setEchoChar('●');
            button.setText("👁");
        }
    }

    private void checkPasswordMatch() {
        String pw1 = new String(signupPwField.getPassword());
        String pw2 = new String(signupPwConfirmField.getPassword());

        if (pw1.equals(pw2)) {
            signupPwMsg.setText("올바른 비밀번호입니다.");
            signupPwMsg.setForeground(Color.BLUE);
        } else {
            signupPwMsg.setText("비밀번호가 일치하지 않습니다.");
            signupPwMsg.setForeground(Color.RED);
        }
    }

}
