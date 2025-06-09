package packages.MainForm;

import net.miginfocom.swing.MigLayout;
import com.formdev.flatlaf.FlatClientProperties;
import packages.Classes.User;
import packages.DB.UserDAO;

import javax.swing.*;
import javax.swing.border.AbstractBorder;
import javax.swing.border.Border;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.prefs.Preferences;

public class LoginForm extends JDialog {
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel cardPanel = new JPanel(cardLayout);

    // 로그인용 필드
    private JTextField loginIdField;
    private JPasswordField loginPwField;
    private JCheckBox saveId;
    private JButton loginPwEyeBtn;
    private boolean showLoginPw = false;
    private String pwResetTargetId = null;  // 비밀번호 변경 대상 ID 저장용


    // 회원가입용 필드
    private JTextField signupIdField, signupEmailField, signupNameField, signupBirthField;
    private JPasswordField signupPwField, signupPwConfirmField;
    private JButton signupPwEyeBtn1, signupPwEyeBtn2;
    private boolean showSignupPw = false, showSignupPwConfirm = false;
    private JLabel signupPwMsg, signupIdMsg;
    private JToggleButton signupMaleBtn, signupFemaleBtn;
    private User loggedInUser;  // 추가 필요

    private final Preferences prefs = Preferences.userRoot().node("BookLogin");
    private final Consumer<User> loginSuccessCallback;

    public LoginForm(JFrame parent, Consumer<User> loginSuccessCallback) {
        super(parent, "로그인 / 회원가입", true);
        this.loginSuccessCallback = loginSuccessCallback;
        setSize(500, 650);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());

        // 기본 스타일
        UIManager.put("Label.font", new Font("맑은 고딕", Font.PLAIN, 13));
        UIManager.put("Button.font", new Font("맑은 고딕", Font.BOLD, 13));
        UIManager.put("TextField.font", new Font("맑은 고딕", Font.PLAIN, 13));

        cardPanel.add(createLoginPanel(), "login");
        cardPanel.add(createSignupPanel(), "signup");
        cardPanel.add(createFindIdForm(), "findId");
        cardPanel.add(createFindPwForm(), "findPw");
        cardPanel.add(createPwResetForm(), "resetPw");
        cardPanel.add(createPwChangeCompletePanel(), "pwChanged");
        add(cardPanel, BorderLayout.CENTER);
    }

    private JPanel createLoginPanel() {
        JPanel panel = new JPanel(new MigLayout("wrap 1, insets 80", "[grow,fill]", "[]15[]15[]15[]10[]15[]"));
        panel.setBackground(Color.WHITE);

        // 🔹 아이디 입력
        loginIdField = new JTextField();
        loginIdField.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "아이디를 입력해 주세요.");
        loginIdField.setPreferredSize(new Dimension(250, 40));
        loginIdField.setBorder(new RoundedBorder(new Color(200, 200, 200), 10));
        panel.add(loginIdField);

        // 🔹 비밀번호 입력 + 보기 버튼
        loginPwField = new JPasswordField();
        loginPwField.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "비밀번호를 입력해 주세요.");
        loginPwField.setPreferredSize(new Dimension(250, 40));
        loginPwField.setBorder(new RoundedBorder(new Color(200, 200, 200), 10));

        // 기존 코드
        ImageIcon rawIcon = new ImageIcon(getClass().getResource("/eye_icon.png"));
        Image scaledImg = rawIcon.getImage().getScaledInstance(18, 18, Image.SCALE_SMOOTH); // 더 작게
        ImageIcon resizedIcon = new ImageIcon(scaledImg);

        loginPwEyeBtn = new JButton(resizedIcon);
        loginPwEyeBtn.setPreferredSize(new Dimension(30, 30)); // 크기 줄임
        loginPwEyeBtn.setContentAreaFilled(false);
        loginPwEyeBtn.setBorderPainted(false);
        loginPwEyeBtn.setFocusPainted(false);
        loginPwEyeBtn.setMargin(new Insets(0, 0, 0, 0)); // 여백 제거



        JPanel pwPanel = new JPanel(new MigLayout("insets 0", "[grow][40!]", "[]"));
        pwPanel.setBackground(Color.WHITE);
        pwPanel.add(loginPwField, "growx");
        pwPanel.add(loginPwEyeBtn);
        panel.add(pwPanel);

        loginPwEyeBtn.addActionListener(e -> {
            showLoginPw = !showLoginPw;
            if (showLoginPw) {
                loginPwField.setEchoChar((char) 0);
            } else {
                loginPwField.setEchoChar('●');
            }
        });

        // 🔹 로그인 버튼
        JButton loginBtn = new JButton("로그인");
        loginBtn.setBackground(new Color(220, 220, 220));
        loginBtn.setPreferredSize(new Dimension(250, 40));
        loginBtn.setBorder(new RoundedBorder(new Color(200, 200, 200), 10));
        loginBtn.setFont(new Font("맑은 고딕", Font.BOLD, 18));

        panel.add(loginBtn, "growx");

        // 🔹 아이디 저장 및 찾기 링크
        JPanel optionPanel = new JPanel(new BorderLayout());
        optionPanel.setOpaque(false);

        saveId = new JCheckBox("아이디 저장");
        saveId.setBackground(Color.WHITE);
        saveId.setFont(new Font("맑은 고딕", Font.PLAIN, 12));
        optionPanel.add(saveId, BorderLayout.WEST);

        JPanel rightOption = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightOption.setOpaque(false);
        JLabel findId = new JLabel("아이디 찾기");
        JLabel findPw = new JLabel("비밀번호 찾기");

        for (JLabel lbl : new JLabel[]{findId, findPw}) {
            lbl.setForeground(Color.GRAY);
            lbl.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            lbl.setFont(new Font("맑은 고딕", Font.PLAIN, 12));
        }

        rightOption.add(findId);
        rightOption.add(findPw);
        optionPanel.add(rightOption, BorderLayout.EAST);
        panel.add(optionPanel, "growx");

        // 🔹 회원가입 버튼
        JButton joinBtn = new JButton("회원가입");
        joinBtn.setPreferredSize(new Dimension(250, 40));
        joinBtn.setFocusPainted(false);
        joinBtn.setBackground(Color.WHITE);
        joinBtn.setBorder(BorderFactory.createLineBorder(new Color(100, 100, 100)));
        joinBtn.setFont(new Font("맑은 고딕", Font.BOLD, 18));
        panel.add(joinBtn, "growx");

        // 아이디 저장 불러오기
        String savedId = prefs.get("savedId", "");
        if (!savedId.isEmpty()) {
            loginIdField.setText(savedId);
            saveId.setSelected(true);
        }

        // 🔹 로그인 동작
        loginBtn.addActionListener(e -> {
            String id = loginIdField.getText().trim();
            String pw = new String(loginPwField.getPassword());

            if (saveId.isSelected()) prefs.put("savedId", id);
            else prefs.remove("savedId");

            Optional<User> result = new UserDAO().login(id, pw);
            if (result.isPresent()) {
                loggedInUser = result.get();
                if (loginSuccessCallback != null) loginSuccessCallback.accept(loggedInUser);
                dispose();
            } else {
                JOptionPane.showMessageDialog(LoginForm.this, "아이디 또는 비밀번호가 틀렸습니다.");
            }
        });

        // 🔹 회원가입으로 이동
        joinBtn.addActionListener(e -> cardLayout.show(cardPanel, "signup"));

        findId.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                cardLayout.show(cardPanel, "findId");
            }
        });

        findPw.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                cardLayout.show(cardPanel, "findPw");
            }
        });

        return panel;
    }

    // ▶ 커스텀 둥근 테두리 클래스
    public class RoundedBorder extends AbstractBorder {
        private final Color color;
        private final int radius;

        public RoundedBorder(Color color, int radius) {
            this.color = color;
            this.radius = radius;
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setColor(color);
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.drawRoundRect(x, y, width - 1, height - 1, radius, radius);
            g2.dispose();
        }

        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(6, 10, 6, 10);
        }

        @Override
        public Insets getBorderInsets(Component c, Insets insets) {
            return getBorderInsets(c);
        }
    }



    private JPanel createSignupPanel() {
        JPanel panel = new JPanel(new MigLayout("wrap 1, insets 20", "[grow,fill]", "[]10[]10[]10[]10[]10[]10[]10[]10[]10[]"));
        panel.setBackground(Color.WHITE);

        Color borderGray = new Color(180, 180, 180);
        int cornerRadius = 15;

        // 🔹 아이디 입력 + 중복확인
        signupIdField = new JTextField();
        signupIdField.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "아이디를 입력해 주세요.");
        signupIdField.setPreferredSize(new Dimension(250, 40));
        signupIdField.setBorder(new RoundedBorder(borderGray, cornerRadius));

        JButton checkBtn = createGrayButton("중복확인");
        JPanel idPanel = new JPanel(new MigLayout("insets 0", "[grow][100!]", "[]"));
        idPanel.setOpaque(false);
        idPanel.add(signupIdField, "growx");
        idPanel.add(checkBtn);
        panel.add(idPanel);

        signupIdMsg = new JLabel(" ");
        signupIdMsg.setFont(new Font("맑은 고딕", Font.PLAIN, 12));
        panel.add(signupIdMsg);

        checkBtn.addActionListener(e -> {
            String username = signupIdField.getText().trim();
            if (username.isEmpty()) {
                signupIdMsg.setText("아이디를 입력해주세요.");
                signupIdMsg.setForeground(Color.RED);
                return;
            }
            boolean isDuplicate = new UserDAO().isUsernameDuplicate(username);
            signupIdMsg.setText(isDuplicate ? "이미 존재하는 아이디입니다." : "사용 가능한 아이디입니다.");
            signupIdMsg.setForeground(isDuplicate ? Color.RED : Color.BLUE);
        });

        // 🔹 비밀번호 입력
        signupPwField = new JPasswordField();
        signupPwField.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "비밀번호를 입력해 주세요.");
        signupPwField.setPreferredSize(new Dimension(250, 40));
        signupPwField.setBorder(new RoundedBorder(borderGray, cornerRadius));

        // 기존 코드
        ImageIcon rawIcon = new ImageIcon(getClass().getResource("/eye_icon.png"));
        Image scaledImg = rawIcon.getImage().getScaledInstance(18, 18, Image.SCALE_SMOOTH); // 더 작게
        ImageIcon resizedIcon = new ImageIcon(scaledImg);

        signupPwEyeBtn1 = new JButton(resizedIcon);
        signupPwEyeBtn1.setPreferredSize(new Dimension(30, 30)); // 크기 줄임
        signupPwEyeBtn1.setContentAreaFilled(false);
        signupPwEyeBtn1.setBorderPainted(false);
        signupPwEyeBtn1.setFocusPainted(false);
        signupPwEyeBtn1.setMargin(new Insets(0, 0, 0, 0)); // 여백 제거


        JPanel pwPanel = new JPanel(new MigLayout("insets 0", "[grow][30!]", "[]"));
        pwPanel.setOpaque(false);
        pwPanel.add(signupPwField, "growx");
        pwPanel.add(signupPwEyeBtn1);
        panel.add(pwPanel);

        signupPwEyeBtn1.addActionListener(e -> {
            showSignupPw = !showSignupPw;
            signupPwField.setEchoChar(showSignupPw ? (char) 0 : '●');
        });

        // 🔹 비밀번호 확인
        signupPwConfirmField = new JPasswordField();
        signupPwConfirmField.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "비밀번호 확인");
        signupPwConfirmField.setPreferredSize(new Dimension(250, 40));
        signupPwConfirmField.setBorder(new RoundedBorder(borderGray, cornerRadius));

        signupPwEyeBtn2 = new JButton(resizedIcon);
        signupPwEyeBtn2.setPreferredSize(new Dimension(30, 30)); // 크기 줄임
        signupPwEyeBtn2.setContentAreaFilled(false);
        signupPwEyeBtn2.setBorderPainted(false);
        signupPwEyeBtn2.setFocusPainted(false);
        signupPwEyeBtn2.setMargin(new Insets(0, 0, 0, 0)); // 여백 제거

        JPanel pwConfirmPanel = new JPanel(new MigLayout("insets 0", "[grow][30!]", "[]"));
        pwConfirmPanel.setOpaque(false);
        pwConfirmPanel.add(signupPwConfirmField, "growx");
        pwConfirmPanel.add(signupPwEyeBtn2);
        panel.add(pwConfirmPanel);

        signupPwEyeBtn2.addActionListener(e -> {
            showSignupPwConfirm = !showSignupPwConfirm;
            signupPwConfirmField.setEchoChar(showSignupPwConfirm ? (char) 0 : '●');
        });

        signupPwMsg = new JLabel(" ");
        signupPwMsg.setFont(new Font("맑은 고딕", Font.PLAIN, 12));
        panel.add(signupPwMsg);

        signupPwConfirmField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { checkPasswordMatch(); }
            public void removeUpdate(DocumentEvent e) { checkPasswordMatch(); }
            public void changedUpdate(DocumentEvent e) { checkPasswordMatch(); }
        });

        // 🔹 이메일, 이름
        signupEmailField = new JTextField();
        signupEmailField.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "이메일 주소");
        signupEmailField.setPreferredSize(new Dimension(250, 40));
        signupEmailField.setBorder(new RoundedBorder(borderGray, cornerRadius));
        panel.add(signupEmailField);

        signupNameField = new JTextField();
        signupNameField.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "이름");
        signupNameField.setPreferredSize(new Dimension(250, 40));
        signupNameField.setBorder(new RoundedBorder(borderGray, cornerRadius));
        panel.add(signupNameField);

        // 🔹 생년 + 성별
        signupBirthField = new JTextField();
        signupBirthField.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "출생년도");
        signupBirthField.setPreferredSize(new Dimension(120, 40));
        signupBirthField.setBorder(new RoundedBorder(borderGray, cornerRadius));

        signupMaleBtn = new JToggleButton("남");
        signupFemaleBtn = new JToggleButton("여");
        Font genderFont = new Font("맑은 고딕", Font.BOLD, 13);
        Dimension genderSize = new Dimension(60, 40);

        for (JToggleButton btn : new JToggleButton[]{signupMaleBtn, signupFemaleBtn}) {
            btn.setFont(genderFont);
            btn.setPreferredSize(genderSize);
            btn.setBackground(Color.WHITE);
            btn.setFocusPainted(false);
            btn.setBorder(new RoundedBorder(Color.GRAY, cornerRadius));
        }

        ButtonGroup genderGroup = new ButtonGroup();
        genderGroup.add(signupMaleBtn);
        genderGroup.add(signupFemaleBtn);

        JPanel genderPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        genderPanel.setOpaque(false);
        genderPanel.add(signupMaleBtn);
        genderPanel.add(signupFemaleBtn);

        JPanel birthGenderPanel = new JPanel(new MigLayout("insets 0", "[grow][]", "[]"));
        birthGenderPanel.setOpaque(false);
        birthGenderPanel.add(signupBirthField, "growx");
        birthGenderPanel.add(genderPanel, "gapleft 20");
        panel.add(birthGenderPanel, "growx");

        // 🔹 회원가입 버튼
        JButton signupBtn = createBlueButton("회원가입");
        signupBtn.setFont(new Font("맑은 고딕", Font.BOLD, 15));
        signupBtn.setPreferredSize(new Dimension(250, 40));
        panel.add(signupBtn);

        signupBtn.addActionListener(e -> {
            String id = signupIdField.getText().trim();
            String pw = new String(signupPwField.getPassword());
            String pwCheck = new String(signupPwConfirmField.getPassword());
            String email = signupEmailField.getText().trim();
            String name = signupNameField.getText().trim();
            String birthText = signupBirthField.getText().trim();

            if (id.isEmpty() || pw.isEmpty() || pwCheck.isEmpty() || email.isEmpty() || name.isEmpty() || birthText.isEmpty()) {
                JOptionPane.showMessageDialog(this, "모든 필드를 입력해주세요.");
                return;
            }

            if (!pw.equals(pwCheck)) {
                JOptionPane.showMessageDialog(this, "비밀번호가 일치하지 않습니다.");
                return;
            }

            int birthYear;
            try {
                birthYear = Integer.parseInt(birthText);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "출생년도는 숫자만 입력해주세요.");
                return;
            }

            char gender = signupMaleBtn.isSelected() ? 'M' : (signupFemaleBtn.isSelected() ? 'F' : ' ');
            if (gender == ' ') {
                JOptionPane.showMessageDialog(this, "성별을 선택해주세요.");
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

            boolean result = new UserDAO().registerUser(user);
            if (result) {
                JOptionPane.showMessageDialog(this, "회원가입이 완료되었습니다!");
                cardLayout.show(cardPanel, "login");
            } else {
                JOptionPane.showMessageDialog(this, "회원가입 실패. 다시 시도해주세요.");
            }
        });


        return panel;
    }

    private JButton createGrayButton(String text) {
        JButton btn = new JButton(text);
        btn.setBackground(new Color(220, 220, 220)); // 밝은 회색
        btn.setFocusPainted(false);
        btn.setBorder(new RoundedBorder(Color.GRAY, 15));
        btn.setPreferredSize(new Dimension(100, 40));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setFont(new Font("맑은 고딕", Font.BOLD, 13));

        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(new Color(200, 200, 200)); // 조금 어두운 회색
            }

            public void mouseExited(MouseEvent e) {
                btn.setBackground(new Color(220, 220, 220));
            }
        });

        return btn;
    }

    private JButton createBlueButton(String text) {
        JButton btn = new JButton(text);
        btn.setBackground(new Color(0, 120, 215)); // 기본 파란색
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorder(new RoundedBorder(new Color(0, 100, 180), 15));
        btn.setPreferredSize(new Dimension(250, 40));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setFont(new Font("맑은 고딕", Font.BOLD, 15));

        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(new Color(30, 144, 255)); // 호버 시 연한 파랑
            }

            public void mouseExited(MouseEvent e) {
                btn.setBackground(new Color(0, 120, 215));
            }
        });

        return btn;
    }

    private void styleIconButton(JButton btn) {
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setMargin(new Insets(0, 0, 0, 0)); // 여백 제거
        btn.setPreferredSize(new Dimension(30, 30)); // 작고 깔끔하게
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
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

    public JPanel createFindIdForm() {
        JPanel panel = new JPanel(new MigLayout("wrap 1, insets 80", "[grow, center]", "[]20[]"));
        panel.setBackground(Color.WHITE);

        JTextField emailField = createPlaceholderField("이메일을 입력해주세요.");
        JButton findBtn = createPrimaryButton("찾기");

        panel.add(emailField, "w 250!");
        panel.add(findBtn, "w 250!");

        findBtn.addActionListener(e -> {
            String email = emailField.getText().trim();
            if (email.isEmpty()) {
                JOptionPane.showMessageDialog(this, "이메일을 입력해주세요.");
                return;
            }

            Optional<String> foundId = new UserDAO().findUsernameByEmail(email);
            if (foundId.isPresent()) {
                String realId = foundId.get();
                String masked = maskId(realId);
                JPanel resultPanel = createFindIdResult(masked, realId); // 수정된 메서드 사용
                cardPanel.add(resultPanel, "findIdResult");
                cardLayout.show(cardPanel, "findIdResult");
            } else {
                JOptionPane.showMessageDialog(this, "일치하는 아이디가 없습니다.");
            }
        });

        return panel;
    }



    public JPanel createFindIdResult(String maskedId, String realId) {
        JPanel panel = new JPanel(new MigLayout("wrap 1, insets 80", "[grow, center]", "[]10[]20[]10[]"));
        panel.setBackground(Color.WHITE);

        JLabel resultLabel = new JLabel("<html><b>회원님의 아이디는 다음과 같습니다.</b></html>", SwingConstants.CENTER);
        JLabel idLabel = new JLabel(maskedId, SwingConstants.CENTER);
        idLabel.setForeground(Color.BLUE);

        JButton loginBtn = createPrimaryButton("로그인");
        JLabel resetPw = new JLabel("<html><u>비밀번호 재설정</u></html>");
        resetPw.setForeground(new Color(40, 100, 200));
        resetPw.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        loginBtn.addActionListener(e -> {
            cardLayout.show(cardPanel, "login");
            loginIdField.setText(realId); // 💡 자동입력
        });

        resetPw.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                pwResetTargetId = realId;
                cardLayout.show(cardPanel, "resetPw");
            }
        });

        panel.add(resultLabel, "growx");
        panel.add(idLabel, "growx");
        panel.add(loginBtn, "w 250!");
        panel.add(resetPw);

        return panel;
    }


    public JPanel createFindPwForm() {
        JPanel panel = new JPanel(new MigLayout("wrap 1, insets 80", "[grow, center]", "[]10[]20[]"));
        panel.setBackground(Color.WHITE);

        JTextField idField = createPlaceholderField("아이디를 입력해주세요.");
        JTextField emailField = createPlaceholderField("이메일을 입력해주세요.");
        JButton findBtn = createPrimaryButton("찾기");

        panel.add(idField, "w 250!");
        panel.add(emailField, "w 250!");
        panel.add(findBtn, "w 250!");

        findBtn.addActionListener(e -> {
            String id = idField.getText().trim();
            String email = emailField.getText().trim();

            if (id.isEmpty() || email.isEmpty()) {
                JOptionPane.showMessageDialog(this, "모든 정보를 입력해주세요.");
                return;
            }

            boolean exists = new UserDAO().existsByIdAndEmail(id, email);
            if (exists) {
                pwResetTargetId = id; // 💡 아이디 저장
                cardLayout.show(cardPanel, "resetPw");
            } else {
                JOptionPane.showMessageDialog(this, "일치하는 사용자 정보가 없습니다.");
            }
        });

        return panel;
    }


    public JPanel createPwResetForm() {
        JPanel panel = new JPanel(new MigLayout("wrap 1, insets 80", "[grow, center]", "[]10[]20[]"));
        panel.setBackground(Color.WHITE);

        JPasswordField oldPwField = createPasswordField("기존 비밀번호를 입력해주세요.");
        JPasswordField newPwField = createPasswordField("새 비밀번호를 입력해주세요.");
        JButton changeBtn = createPrimaryButton("비밀번호 변경");

        panel.add(oldPwField, "w 250!");
        panel.add(newPwField, "w 250!");
        panel.add(changeBtn, "w 250!");

        changeBtn.addActionListener(e -> {
            String newPw = new String(newPwField.getPassword()).trim();

            if (newPw.isEmpty()) {
                JOptionPane.showMessageDialog(this, "새 비밀번호를 입력해주세요.");
                return;
            }

            boolean updated = new UserDAO().updatePassword(pwResetTargetId, newPw); // pwResetTargetId는 findPwForm에서 저장된 아이디
            if (updated) {
                cardLayout.show(cardPanel, "pwChanged");
            } else {
                JOptionPane.showMessageDialog(this, "비밀번호 변경 실패");
            }
        });

        return panel;
    }


    public JPanel createPwChangeCompletePanel() {
        JPanel panel = new JPanel(new MigLayout("wrap 1, insets 80", "[grow, center]", "[]20[]"));
        panel.setBackground(Color.WHITE);

        JLabel msg = new JLabel("변경이 완료되었습니다.", SwingConstants.CENTER);
        msg.setFont(new Font("맑은 고딕", Font.BOLD, 14));
        msg.setHorizontalAlignment(SwingConstants.CENTER); // 중앙 정렬

        JButton loginBtn = createPrimaryButton("로그인하기");
        loginBtn.addActionListener(e -> {
            cardLayout.show(cardPanel, "login");
            if (pwResetTargetId != null) {
                loginIdField.setText(pwResetTargetId); // 아이디 자동입력
            }
        });

        panel.add(msg, "growx");
        panel.add(loginBtn, "w 250!");

        return panel;
    }



    private JTextField createPlaceholderField(String text) {
        JTextField field = new JTextField();
        field.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, text);
        field.setPreferredSize(new Dimension(250, 40));
        field.setBorder(new RoundedBorder(new Color(180,180,180), 10));
        return field;
    }

    private JPasswordField createPasswordField(String text) {
        JPasswordField field = new JPasswordField();
        field.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, text);
        field.setPreferredSize(new Dimension(250, 40));
        field.setBorder(new RoundedBorder(new Color(180,180,180), 10));
        return field;
    }

    private JButton createPrimaryButton(String text) {
        JButton btn = new JButton(text);
        btn.setForeground(Color.BLACK);
        btn.setBackground(new Color(80, 140, 250));
        btn.setFocusPainted(false);
        btn.setBorder(new RoundedBorder(new Color(60, 120, 220), 10));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(new Color(60, 120, 220));
            }
            public void mouseExited(MouseEvent e) {
                btn.setBackground(new Color(80, 140, 250));
            }
        });
        return btn;
    }

    private String maskId(String id) {
        if (id.length() <= 3) return id + "****";
        return id.substring(0, 3) + "****";
    }


}
