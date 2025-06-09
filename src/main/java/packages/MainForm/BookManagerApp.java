package packages.MainForm;

import packages.Classes.Book;
import com.formdev.flatlaf.FlatLightLaf;
import net.miginfocom.swing.MigLayout;
import packages.Classes.User;
import packages.DB.BookDAO;
import packages.DB.RentalDAO;
import packages.MainForm.MyPage;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URL;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.List;

public class BookManagerApp extends JFrame {
    // 인스턴스 필드 (전역 상태 관리용)
    private JPanel topBar; // 상단바 패널
    private JPanel mainContentPanel; // 메인 콘텐츠 패널
    private JComboBox<String> sortCombo; // 정렬 조건 콤보박스
    private JComboBox<String> orderCombo; // 오름/내림차순 콤보박스
    private JPanel topResultPanel; // 검색 결과 상단 패널
    private JScrollPane scrollPane; // 스크롤 가능한 검색 결과 패널
    private JPanel resultContentPanel; // 검색 결과 내용 패널
    private JPanel bookPanel; // 베스트셀러 도서 목록 패널
    private List<Book> lastSearchResults = null; // 마지막 검색 결과 저장
    private String lastKeyword = ""; // 마지막 검색 키워드 저장
    private List<Book> bestsellerBooks = new ArrayList<>(); // 베스트셀러 도서 리스트
    private int startIndex = 0; // 베스트셀러 목록 시작 인덱스
    private JPanel bookRow; // 책 카드 행
    private User loggedInUser; // 현재 로그인된 사용자
    private JComboBox<String> categoryBox; // 검색 카테고리 콤보박스
    private JTextField searchField; // 검색어 입력 필드
    private JButton searchBtn; // 검색 버튼
    private JComboBox<String> countCombo; // 페이지당 결과 수 선택 콤보박스
    private int currentPage = 1;          // 현재 페이지 번호
    private JLabel logoLabel;
    private JPanel rightPanel;



    public BookManagerApp() {
        setTitle("도서 관리 프로그램 v1.0");
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setSize((int)(screenSize.width * 0.75), (int)(screenSize.height * 0.75));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        initComponents();
        setVisible(true);
    }

    // 전체 UI 초기화 메서드
    private void initComponents() {
        setLayout(new BorderLayout());

        // 공통 폰트 적용
        UIManager.put("Label.font", new Font("SansSerif", Font.PLAIN, 14));
        UIManager.put("Button.font", new Font("SansSerif", Font.PLAIN, 14));
        UIManager.put("ComboBox.font", new Font("SansSerif", Font.PLAIN, 14));
        UIManager.put("TextField.font", new Font("SansSerif", Font.PLAIN, 14));

        topBar = new JPanel(new MigLayout("fillx, insets 10", "[]push[]", "[]"));
        topBar.setBackground(Color.WHITE);
        topBar.setBackground(new Color(245, 245, 245));
        add(topBar, BorderLayout.NORTH);

        mainContentPanel = new JPanel(new MigLayout("wrap 1, insets 15", "[grow]", "[]"));
        JSeparator separator = new JSeparator(SwingConstants.HORIZONTAL);
        mainContentPanel.add(separator, "growx, gaptop 10, gapbottom 10");

        mainContentPanel.setBackground(Color.WHITE);

        add(mainContentPanel, BorderLayout.CENTER);

        sortCombo = new JComboBox<>(new String[]{"정렬없음", "제목순", "재고순"});
        orderCombo = new JComboBox<>(new String[]{"오름차순", "내림차순"});

        sortCombo.addActionListener(e -> {
            if (!lastKeyword.isEmpty()) searchBtn.doClick();
        });
        orderCombo.addActionListener(e -> {
            if (!lastKeyword.isEmpty()) searchBtn.doClick();
        });

        updateTopBar();
        showMainView();
    }

    private JButton createStyledButton(String text) {
        JButton btn = new JButton(text);
        btn.setBackground(new Color(52, 152, 219));  // 파란색 계열
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }


    // 검색창을 따로 생성해주는 메서드
    private JPanel createSearchPanel() {
        JPanel searchPanel = new JPanel(new MigLayout("insets 5", "[][]15[][]", "[]"));
        searchPanel.setBackground(Color.WHITE);

        Map<String, String> categoryMap = new HashMap<>();
        categoryMap.put("제목", "title");
        categoryMap.put("저자", "author");
        categoryMap.put("출판사", "publisher");

        categoryBox = new JComboBox<>(new String[]{"제목", "저자", "출판사"});
        searchField = new JTextField(20);
        searchBtn = createStyledButton("검색");
        countCombo = new JComboBox<>(new String[]{"5건", "10건", "20건", "30건"});

        searchBtn.addActionListener(e -> {
            String displayText = (String) categoryBox.getSelectedItem();
            String category = categoryMap.get(displayText);
            String keyword = searchField.getText().trim();

            if (keyword.isEmpty()) {
                JOptionPane.showMessageDialog(this, "검색어를 입력해주세요.");
                return;
            }

            String sortBy = (String) sortCombo.getSelectedItem();
            String orderBy = (String) orderCombo.getSelectedItem();

            BookDAO dao = new BookDAO();
            List<Book> results = dao.searchBooks(category, keyword, sortBy, orderBy);

            lastSearchResults = results;
            lastKeyword = keyword;
            currentPage = 1;

            showSearchResults(results, keyword);
        });

        countCombo.addActionListener(e -> {
            if (lastSearchResults != null && !lastSearchResults.isEmpty()) {
                currentPage = 1;
                showSearchResults(lastSearchResults, lastKeyword);
            }
        });

        searchPanel.add(categoryBox);
        searchPanel.add(searchField, "w 250!");
        searchPanel.add(searchBtn, "gapleft 10");

        return searchPanel;
    }




    public void updateTopBar() {
        topBar.removeAll();
        topBar.setLayout(new MigLayout("insets 10, fillx", "[]push[]push[]", "center"));
        topBar.setBackground(new Color(245, 245, 245));

        // 왼쪽 로고
        if (logoLabel == null) {
            ImageIcon icon = new ImageIcon(getClass().getResource("/book_icon.png"));
            Image resized = icon.getImage().getScaledInstance(32, 32, Image.SCALE_SMOOTH);
            logoLabel = new JLabel(new ImageIcon(resized));
            logoLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            logoLabel.setToolTipText("홈으로");
            logoLabel.addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent e) {
                    showMainView();
                }
            });
        }
        topBar.add(logoLabel);

        // 중앙 검색창
        topBar.add(createSearchPanel(), "align center");

        // 오른쪽 유저 패널
        topBar.add(createUserPanel(), "align right");

        topBar.revalidate();
        topBar.repaint();
    }



    private JPanel createUserPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        panel.setOpaque(false);

        if (loggedInUser != null) {
            JPanel userInfo = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
            userInfo.setOpaque(false);

            // ✅ 관리자 페이지 버튼 (username으로 비교)
            if ("admin1234".equals(loggedInUser.getUsername())) {
                JButton adminBtn = new JButton("관리자 페이지");
                adminBtn.setFocusPainted(false);
                adminBtn.setFont(new Font("맑은 고딕", Font.PLAIN, 12));
                adminBtn.setBorder(BorderFactory.createLineBorder(Color.GRAY));
                adminBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                adminBtn.addActionListener(e -> showAdminPanel());
                panel.add(adminBtn); // ✅ 꼭 panel에 붙이기!
            }

            JLabel userIcon = new JLabel("👤");
            JLabel nameLabel = new JLabel(loggedInUser.getName() + " 님");
            nameLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 13));
            nameLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            nameLabel.setToolTipText("마이페이지");

            nameLabel.addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent e) {
                    new MyPage(BookManagerApp.this, loggedInUser, () -> updateTopBar()).setVisible(true);
                }
            });

            userInfo.add(userIcon);
            userInfo.add(nameLabel);
            panel.add(userInfo); // ✅ 이 부분도 빠지면 안 됨

            JButton logoutBtn = new JButton("로그아웃");
            logoutBtn.setFocusPainted(false);
            logoutBtn.setFont(new Font("맑은 고딕", Font.PLAIN, 12));
            logoutBtn.setBorder(BorderFactory.createLineBorder(Color.GRAY));
            logoutBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            logoutBtn.addActionListener(e -> {
                loggedInUser = null;
                updateTopBar();
            });

            panel.add(logoutBtn);

        } else {
            JButton loginBtn = new JButton("로그인");
            loginBtn.setFocusPainted(false);
            loginBtn.setFont(new Font("맑은 고딕", Font.PLAIN, 12));
            loginBtn.setBorder(BorderFactory.createLineBorder(Color.GRAY));
            loginBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            loginBtn.addActionListener(e -> {
                LoginForm loginForm = new LoginForm(this, user -> {
                    this.loggedInUser = user;
                    updateTopBar();
                });
                loginForm.setVisible(true);
            });

            panel.add(loginBtn);
        }

        return panel;
    }





    // 메인 화면 (베스트셀러 슬라이드 등)
    private void showMainView() {
        mainContentPanel.removeAll();

        // 📚 최근 베스트셀러
        JLabel bestTitle = new JLabel("최근 베스트셀러");
        bestTitle.setFont(bestTitle.getFont().deriveFont(Font.BOLD, 20f));
        mainContentPanel.add(bestTitle, "align left, gapbottom 10");

        bookPanel = new JPanel(new MigLayout("", "[]20[]20[]20[]", "[]"));
        bookPanel.setBackground(Color.WHITE);

        // showMainView() 내부의 leftBtn, rightBtn 부분을 아래처럼 수정하세요:

// 🔹 왼쪽 화살표 버튼
        ImageIcon leftIcon = new ImageIcon(getClass().getResource("/left_arrow.png"));
        Image scaledLeft = leftIcon.getImage().getScaledInstance(24, 24, Image.SCALE_SMOOTH);
        JButton leftBtn = new JButton(new ImageIcon(scaledLeft));
        leftBtn.setContentAreaFilled(false);
        leftBtn.setBorderPainted(false);
        leftBtn.setFocusPainted(false);
        leftBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        leftBtn.addActionListener(e -> {
            startIndex = (startIndex - 1 + bestsellerBooks.size()) % bestsellerBooks.size();
            updateBookRow();
        });

// 🔹 오른쪽 화살표 버튼
        ImageIcon rightIcon = new ImageIcon(getClass().getResource("/right_arrow.png"));
        Image scaledRight = rightIcon.getImage().getScaledInstance(24, 24, Image.SCALE_SMOOTH);
        JButton rightBtn = new JButton(new ImageIcon(scaledRight));
        rightBtn.setContentAreaFilled(false);
        rightBtn.setBorderPainted(false);
        rightBtn.setFocusPainted(false);
        rightBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        rightBtn.addActionListener(e -> {
            startIndex = (startIndex + 1) % bestsellerBooks.size();
            updateBookRow();
        });

        leftBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        leftBtn.addActionListener(e -> {
            startIndex = (startIndex - 1 + bestsellerBooks.size()) % bestsellerBooks.size();
            updateBookRow();
        });

        rightBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        rightBtn.addActionListener(e -> {
            startIndex = (startIndex + 1) % bestsellerBooks.size();
            updateBookRow();
        });

        bookRow = new JPanel(new MigLayout("", "[]20[]20[]20[]", "[]"));
        bookRow.setBackground(Color.WHITE);

        bookPanel.add(leftBtn, "aligny center");
        bookPanel.add(bookRow);
        bookPanel.add(rightBtn, "aligny center");

        mainContentPanel.add(bookPanel, "align center, gapbottom 20");

        // 📈 누적 대여량 높은 순
        JLabel rentTitle = new JLabel("누적 대여량 높은 순");
        rentTitle.setFont(rentTitle.getFont().deriveFont(Font.BOLD, 20f));
        mainContentPanel.add(rentTitle, "align left, gapbottom 10");

        JPanel rentPanel = new JPanel(new MigLayout("", "[]20[]20[]20[]", "[]"));
        rentPanel.setBackground(Color.WHITE);

        BookDAO dao = new BookDAO();
        List<Book> mostRentedBooks = dao.getMostRentedBooks(4);
        for (Book b : mostRentedBooks) {
            rentPanel.add(createBookCard(b));
        }
        mainContentPanel.add(rentPanel, "align center");

        // 📚 베스트셀러 목록 로딩
        bestsellerBooks = dao.getBestSellerBooks(10);
        updateBookRow();

        mainContentPanel.revalidate();
        mainContentPanel.repaint();
    }


    // 책 카드 갱신
    private void updateBookRow() {
        bookRow.removeAll();
        for (int i = 0; i < 4; i++) {
            int index = (startIndex + i) % bestsellerBooks.size();
            Book book = bestsellerBooks.get(index);
            JPanel card = createBookCard(book);
            bookRow.add(card);
        }
        bookRow.revalidate();
        bookRow.repaint();
    }

    // 책 카드 UI
    private JPanel createBookCard(Book book) {
        JPanel panel = new JPanel(new MigLayout("wrap 1, center", "center", "[]10[]")) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // ▶ 오른쪽 하단 그림자
                int shadowSize = 8;
                int arc = 15;
                int x = shadowSize / 2;
                int y = shadowSize / 2;
                int width = getWidth() - shadowSize;
                int height = getHeight() - shadowSize;

                g2.setColor(new Color(0, 0, 0, 40)); // 연한 회색 그림자
                g2.fillRoundRect(x + 4, y + 4, width, height, arc, arc);
                g2.dispose();
            }
        };

        panel.setOpaque(false);
        panel.setPreferredSize(new Dimension(140, 230));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel.setBackground(Color.WHITE);
        panel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // 이미지
        JLabel image = new JLabel();
        image.setPreferredSize(new Dimension(120, 160));
        image.setHorizontalAlignment(SwingConstants.CENTER);

        URL imgURL = getClass().getResource("/" + book.getImagePath());
        if (imgURL != null) {
            image.setIcon(new ImageIcon(new ImageIcon(imgURL).getImage().getScaledInstance(120, 160, Image.SCALE_SMOOTH)));
        } else {
            image.setText("이미지 없음");
        }

        // 책 제목
        JLabel label = new JLabel("<html><div style='text-align: center; width: 120px;'>" + book.getTitle() + "</div></html>");
        label.setFont(new Font("SansSerif", Font.PLAIN, 13));
        label.setHorizontalAlignment(SwingConstants.CENTER);

        panel.add(image);
        panel.add(label);

        // 🔸 3D hover 애니메이션
        panel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                panel.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createEmptyBorder(4, 4, 4, 4),
                        BorderFactory.createLineBorder(new Color(100, 100, 100, 100), 1)
                ));
                panel.setLocation(panel.getX(), panel.getY() - 4);
                panel.setBackground(new Color(250, 250, 250));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
                panel.setLocation(panel.getX(), panel.getY() + 4);
                panel.setBackground(Color.WHITE);
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                showBookDetail(book, () -> showMainView());
            }
        });

        return panel;
    }





    // 검색 결과 화면 출력
    private void showSearchResults(List<Book> results, String keyword) {
        mainContentPanel.removeAll();

        int itemsPerPage = 5;
        String selectedCount = (String) countCombo.getSelectedItem();
        if (selectedCount != null && selectedCount.endsWith("건")) {
            itemsPerPage = Integer.parseInt(selectedCount.replace("건", ""));
        }

        int totalResults = results.size();
        int totalPages = (int) Math.ceil((double) totalResults / itemsPerPage);

        int fromIndex = (currentPage - 1) * itemsPerPage;
        int toIndex = Math.min(fromIndex + itemsPerPage, totalResults);
        List<Book> pageResults = results.subList(fromIndex, toIndex);

        topResultPanel = new JPanel(new MigLayout("fillx", "[grow][]", "[]"));
        JLabel resultMsg = new JLabel("검색어 '" + keyword + "' 총 " + results.size() + "건 검색되었습니다.");
        resultMsg.setFont(resultMsg.getFont().deriveFont(Font.BOLD, 16f));
        topResultPanel.add(resultMsg, "align left");

        JPanel filterPanel = new JPanel();
        filterPanel.add(categoryBox);
        filterPanel.add(sortCombo);
        filterPanel.add(orderCombo);
        filterPanel.add(countCombo);
        topResultPanel.add(filterPanel, "align right");

        resultContentPanel = new JPanel(new MigLayout("wrap 1", "[grow]", "[]10[]"));
        for (Book book : pageResults) {
            JPanel row = new JPanel(new MigLayout("", "[][grow]", "[]"));
            URL imgURL = getClass().getResource("/" + book.getImagePath());
            JLabel imgLabel = new JLabel();
            if (imgURL != null) {
                imgLabel.setIcon(new ImageIcon(
                        new ImageIcon(imgURL).getImage().getScaledInstance(80, 120, Image.SCALE_SMOOTH)
                ));
            } else {
                imgLabel.setText("이미지 없음");
            }

            String text = "<html><b>" + book.getTitle() + "</b>"
                    + "<br>  저자: " + book.getAuthor()
                    + "<br>  출판사: " + book.getPublisher()
                    + "<br>  재고수량: " + book.getStock()
                    + "<br>  대여 가능 여부: " + (book.getStock() > 0 ? "가능" : "불가") + "</html>";
            JLabel infoLabel = new JLabel(text);

            row.add(imgLabel);
            row.add(infoLabel, "growx");

            row.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            row.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    showBookDetail(book, () -> showSearchResults(lastSearchResults, lastKeyword));
                }
            });

            resultContentPanel.add(row, "growx");
        }

        scrollPane = new JScrollPane(resultContentPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        JPanel pagePanel = new JPanel();
        for (int i = 1; i <= totalPages; i++) {
            int page = i;
            JButton pageBtn = new JButton(String.valueOf(i));
            pageBtn.addActionListener(ev -> {
                currentPage = page;
                showSearchResults(results, keyword);
            });
            pagePanel.add(pageBtn);
        }

        JPanel bottomPanel = new JPanel(new MigLayout("fillx", "[grow][]", "[]"));
        JButton backBtn = new JButton("뒤로가기");
        backBtn.addActionListener(ev -> showMainView());
        bottomPanel.add(new JLabel());
        bottomPanel.add(backBtn, "align right");

        mainContentPanel.add(topResultPanel, "growx");
        mainContentPanel.add(scrollPane, "grow");
        mainContentPanel.add(pagePanel, "align center");
        mainContentPanel.add(bottomPanel, "growx");

        mainContentPanel.revalidate();
        mainContentPanel.repaint();
    }


    // 상세 정보 화면
    // 상세 정보 화면
    private void showBookDetail(Book book, Runnable backCallback) {
        if (loggedInUser == null) {
            JOptionPane.showMessageDialog(this, "로그인이 필요합니다.");
            LoginForm loginForm = new LoginForm(this, user -> {
                this.loggedInUser = user;
                updateTopBar();
                showBookDetail(book, backCallback);
            });
            loginForm.setVisible(true);
            return;
        }

        mainContentPanel.removeAll();

        JPanel detailWrapper = new JPanel(new BorderLayout());
        detailWrapper.setBackground(Color.WHITE);

        JPanel detailPanel = new JPanel(new MigLayout("wrap 2, center, insets 20", "[grow 0][grow 0]", "[]20[]20[]20[]"));
        detailPanel.setBackground(Color.WHITE);

        JLabel imgLabel = new JLabel();
        URL imgURL = getClass().getResource("/" + book.getImagePath());
        if (imgURL != null) {
            imgLabel.setIcon(new ImageIcon(new ImageIcon(imgURL).getImage().getScaledInstance(240, 330, Image.SCALE_SMOOTH)));
        } else {
            imgLabel.setText("이미지 없음");
            imgLabel.setPreferredSize(new Dimension(240, 330));
        }
        detailPanel.add(imgLabel, "span 1 5, align center");

        JLabel title = new JLabel("<html><h2>" + book.getTitle() + "</h2></html>");
        title.setFont(new Font("SansSerif", Font.BOLD, 20));
        detailPanel.add(title, "span, align center");

        JLabel author = new JLabel("저자: " + book.getAuthor());
        JLabel publisher = new JLabel("출판사: " + book.getPublisher());
        JLabel stock = new JLabel("재고수량: " + book.getStock());
        JLabel available = new JLabel("대여 가능 여부: " + (book.getStock() > 0 ? "가능" : "불가"));

        for (JLabel label : new JLabel[]{author, publisher, stock, available}) {
            label.setFont(new Font("SansSerif", Font.PLAIN, 16));
            detailPanel.add(label, "span, align center");
        }

        RentalDAO rentalDao = new RentalDAO();
        boolean isRented = rentalDao.isBookRentedByUser(loggedInUser.getUserId(), book.getBook_id());

        JButton rentBtn = createStyledButton("대여하기");
        rentBtn.setEnabled(!isRented && book.getStock() > 0);
        rentBtn.addActionListener(e -> {
            if (rentalDao.rentBook(loggedInUser.getUserId(), book.getBook_id())) {
                JOptionPane.showMessageDialog(null, "도서를 대여하였습니다.");
                book.setStock(book.getStock() - 1);
                showBookDetail(book, backCallback);
            } else {
                JOptionPane.showMessageDialog(null, "대여에 실패했습니다.");
            }
        });

        JButton returnBtn = createStyledButton("반납하기");
        returnBtn.setEnabled(isRented);
        returnBtn.addActionListener(e -> {
            Optional<LocalDate> rentDateOpt = rentalDao.getRentDate(loggedInUser.getUserId(), book.getBook_id());

            if (rentDateOpt.isPresent()) {
                showReturnFeeConfirmDialog(rentDateOpt.get(), () -> {
                    if (rentalDao.returnBook(loggedInUser.getUserId(), book.getBook_id())) {
                        JOptionPane.showMessageDialog(null, "반납이 완료되었습니다.");
                        book.setStock(book.getStock() + 1);
                        showBookDetail(book, backCallback);
                    } else {
                        JOptionPane.showMessageDialog(null, "반납 실패.");
                    }
                });
            } else {
                JOptionPane.showMessageDialog(null, "대여 기록을 찾을 수 없습니다.");
            }
        });

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        btnPanel.setOpaque(false);
        btnPanel.add(rentBtn);
        btnPanel.add(returnBtn);

        detailPanel.add(btnPanel, "span, align center");

        detailWrapper.add(detailPanel, BorderLayout.CENTER);

        // 뒤로가기 버튼 고정
        JButton backBtn = createStyledButton("뒤로가기");
        JPanel backPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 10));
        backPanel.setOpaque(false);
        backPanel.add(backBtn);
        backBtn.addActionListener(e -> backCallback.run());

        detailWrapper.add(backPanel, BorderLayout.SOUTH);

        mainContentPanel.add(detailWrapper, "grow");
        mainContentPanel.revalidate();
        mainContentPanel.repaint();
    }


    // 메인 실행 진입점
    public static void main(String[] args) {
        UIManager.put("Label.font", new Font("맑은 고딕", Font.PLAIN, 13));
        UIManager.put("Button.font", new Font("맑은 고딕", Font.BOLD, 13));
        UIManager.put("TextField.font", new Font("맑은 고딕", Font.PLAIN, 13));
        UIManager.put("ComboBox.font", new Font("맑은 고딕", Font.PLAIN, 13));
        UIManager.put("Table.font", new Font("맑은 고딕", Font.PLAIN, 13));

        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (Exception e) {
            e.printStackTrace();
        }
        SwingUtilities.invokeLater(BookManagerApp::new);
    }

    private void showAdminPanel() {
        mainContentPanel.removeAll();

        JLabel title = new JLabel("📚 도서 관리 (관리자 전용)");
        title.setFont(new Font("맑은 고딕", Font.BOLD, 20));
        mainContentPanel.add(title, "wrap");

        BookDAO dao = new BookDAO();
        List<Book> allBooks = dao.searchBooks("title", "", "제목순", "오름차순");

        String[] columns = {"ID", "제목", "저자", "출판사", "재고", "총대여", "이미지경로"};


        DefaultTableModel model = new DefaultTableModel(columns, 0);
        JTable table = new JTable(model);

        for (Book b : allBooks) {
            model.addRow(new Object[]{
                    b.getBook_id(),
                    b.getTitle(),
                    b.getAuthor(),
                    b.getPublisher(),
                    b.getStock(),
                    b.getTotal_rent_count(),
                    b.getImagePath()   // ✅ 추가
            });

        }

        JScrollPane scroll = new JScrollPane(table);
        mainContentPanel.add(scroll, "grow, wrap");

        // 삭제된 책 ID 저장
        Set<Integer> deleteList = new HashSet<>();

        // 📘 추가 버튼
        JButton addBtn = createStyledButton("📘 도서 추가");
        addBtn.addActionListener(e -> {
            model.addRow(new Object[]{"", "", "", "", 0, 0, "default.png"});
        });

        // ❌ 삭제 버튼
        JButton deleteBtn = createStyledButton("🗑 삭제");
        deleteBtn.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow >= 0) {
                Object idObj = model.getValueAt(selectedRow, 0);
                if (idObj != null && !idObj.toString().isBlank()) {
                    int bookId = Integer.parseInt(idObj.toString());
                    deleteList.add(bookId);  // ✅ 삭제 리스트에 추가
                }
                model.removeRow(selectedRow);
            }
        });


        // 💾 저장 버튼
        JButton saveBtn = createStyledButton("💾 파일 저장");
        saveBtn.addActionListener(e -> {
            List<Book> updated = new ArrayList<>();

            for (int i = 0; i < model.getRowCount(); i++) {
                Object idObj = model.getValueAt(i, 0);
                int bookId = (idObj != null && !idObj.toString().isBlank()) ? Integer.parseInt(idObj.toString()) : 0;

                Book book = new Book();
                book.setBook_id(bookId);  // ✅ 이 값이 0이면 무조건 insert로 감
                book.setTitle(model.getValueAt(i, 1).toString());
                book.setAuthor(model.getValueAt(i, 2).toString());
                book.setPublisher(model.getValueAt(i, 3).toString());
                book.setStock(Integer.parseInt(model.getValueAt(i, 4).toString()));
                book.setTotal_rent_count(Integer.parseInt(model.getValueAt(i, 5).toString()));

                Object imgObj = model.getValueAt(i, 6);
                book.setImagePath(imgObj != null ? imgObj.toString() : "default.png");

                updated.add(book);
            }

            for (int id : deleteList) {
                dao.deleteBook(id);  // ✅ 이 deleteList가 비어있으면 삭제가 안 됨
            }

            for (Book b : updated) {
                if (b.getBook_id() == 0) {
                    dao.insertBook(b);  // 새 책
                } else {
                    dao.updateBook(b);  // 기존 책
                }
            }

            JOptionPane.showMessageDialog(this, "DB에 저장되었습니다.");
            showAdminPanel(); // 변경사항 반영해서 다시 테이블 보여줌
        });



        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.setOpaque(false);
        btnPanel.add(addBtn);
        btnPanel.add(deleteBtn);
        btnPanel.add(saveBtn);
        mainContentPanel.add(btnPanel, "wrap");

        mainContentPanel.revalidate();
        mainContentPanel.repaint();
    }


    // 반납 수수료 다이얼로그
    // ✅ 유지할 메서드: 수수료 확인 다이얼로그
    private void showReturnFeeConfirmDialog(LocalDate rentDate, Runnable onConfirmed) {
        LocalDate today = LocalDate.now();
        long daysOverdue = ChronoUnit.DAYS.between(rentDate.plusDays(7), today);
        int fee = (int) (Math.max(0, daysOverdue) / 7) * 1000;

        // ✅ 수수료가 없으면 바로 onConfirmed 실행
        if (fee == 0) {
            if (onConfirmed != null) onConfirmed.run();
            return;
        }

        // 수수료가 있는 경우에만 다이얼로그 표시
        JLabel title = new JLabel("⏰ 반납 기한 초과로 수수료가 발생합니다.");
        title.setFont(new Font("맑은 고딕", Font.BOLD, 14));
        JLabel rentLabel = new JLabel("대여일: " + rentDate);
        JLabel todayLabel = new JLabel("반납일: " + today);
        JLabel feeLabel = new JLabel("수수료: " + fee + "원");
        feeLabel.setForeground(Color.RED);
        feeLabel.setFont(new Font("맑은 고딕", Font.BOLD, 16));

        JButton confirmBtn = new JButton("확인");
        JButton cancelBtn = new JButton("취소");

        confirmBtn.addActionListener(e -> {
            if (onConfirmed != null) onConfirmed.run();
            SwingUtilities.getWindowAncestor(confirmBtn).dispose();
        });

        cancelBtn.addActionListener(e -> {
            SwingUtilities.getWindowAncestor(cancelBtn).dispose();
        });

        JPanel panel = new JPanel(new MigLayout("wrap 1, center", "[]", "[]10[]10[]20[]"));
        panel.add(title);
        panel.add(rentLabel);
        panel.add(todayLabel);
        panel.add(feeLabel);

        JPanel btns = new JPanel();
        btns.add(confirmBtn);
        btns.add(cancelBtn);
        panel.add(btns);

        JOptionPane.showMessageDialog(null, panel, "수수료 확인", JOptionPane.PLAIN_MESSAGE);
    }





}
