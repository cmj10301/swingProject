package packages.MainForm;

import packages.Classes.Book;
import com.formdev.flatlaf.FlatLightLaf;
import net.miginfocom.swing.MigLayout;
import packages.Classes.User;
import packages.DB.BookDAO;
import packages.MainForm.MyPage;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URL;
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

        topBar = new JPanel(new MigLayout("fillx", "", "[]"));
        add(topBar, BorderLayout.NORTH);

        mainContentPanel = new JPanel(new MigLayout("wrap 1", "[grow]", "[]"));
        add(mainContentPanel, BorderLayout.CENTER);

        sortCombo = new JComboBox<>(new String[]{"정렬없음", "제목순", "재고순"});
        orderCombo = new JComboBox<>(new String[]{"오름차순", "내림차순"});

        updateTopBarToLoggedOut(); // 초기 상단바
        showMainView(); // 초기 메인화면
    }

    // 검색창을 따로 생성해주는 메서드
    private JPanel createSearchPanel() {
        JPanel searchPanel = new JPanel(new MigLayout("", "[][][]", "[]"));

        Map<String, String> categoryMap = new HashMap<>();
        categoryMap.put("제목", "title");
        categoryMap.put("저자", "author");
        categoryMap.put("출판사", "publisher");

        categoryBox = new JComboBox<>(new String[]{"제목", "저자", "출판사"});
        searchField = new JTextField();
        searchBtn = new JButton("검색");

        searchBtn.addActionListener(e -> {
            String displayText = (String) categoryBox.getSelectedItem();
            String category = categoryMap.get(displayText);
            String keyword = searchField.getText();
            String sortBy = (String) sortCombo.getSelectedItem();
            String orderBy = (String) orderCombo.getSelectedItem();

            BookDAO dao = new BookDAO();
            List<Book> results = dao.searchBooks(category, keyword, sortBy, orderBy);

            lastSearchResults = results;
            lastKeyword = keyword;

            showSearchResults(results, keyword);
        });

        searchPanel.add(categoryBox);
        searchPanel.add(searchField, "wmin 250");
        searchPanel.add(searchBtn);

        return searchPanel;
    }

    // 상단바 공통 구성 (로고 + 검색창)
    private void updateTopBarBase() {
        topBar.removeAll();
        topBar.add(new JLabel(new ImageIcon(
                new ImageIcon(getClass().getResource("/icon.png"))
                        .getImage().getScaledInstance(70, 50, Image.SCALE_SMOOTH)
        )), "align left");

        topBar.add(Box.createHorizontalGlue(), "pushx");
        topBar.add(createSearchPanel(), "align center");
    }

    // 로그인 상태일 때 상단바 구성
    public void updateTopBarAfterLogin(User user) {
        updateTopBarBase();

        JLabel userIcon = new JLabel("👤");
        userIcon.setFont(new Font("SansSerif", Font.PLAIN, 18));

        JLabel usernameLabel = new JLabel(user.getName() + " 님");
        usernameLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        usernameLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        usernameLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                new MyPage(BookManagerApp.this, loggedInUser, () -> {
                    updateTopBarAfterLogin(loggedInUser);
                }).setVisible(true);
            }
        });

        JButton logoutBtn = new JButton("로그아웃");
        logoutBtn.addActionListener(e -> {
            this.loggedInUser = null;
            updateTopBarToLoggedOut();
        });

        topBar.add(userIcon, "gapleft 50");
        topBar.add(usernameLabel);
        topBar.add(logoutBtn);

        topBar.revalidate();
        topBar.repaint();
    }

    // 로그아웃 상태일 때 상단바 구성
    public void updateTopBarToLoggedOut() {
        updateTopBarBase();

        JButton loginBtn = new JButton("로그인");
        loginBtn.addActionListener(e -> {
            LoginForm loginForm = new LoginForm(this, user -> {
                this.loggedInUser = user;
                updateTopBarAfterLogin(user);
            });
            loginForm.setVisible(true);
        });

        topBar.add(loginBtn, "align right, gapleft 50");
        topBar.revalidate();
        topBar.repaint();
    }

    // 메인 화면 (베스트셀러 슬라이드 등)
    private void showMainView() {
        mainContentPanel.removeAll();

        JLabel bestTitle = new JLabel("최근 베스트셀러");
        bestTitle.setFont(bestTitle.getFont().deriveFont(Font.BOLD, 20f));
        mainContentPanel.add(bestTitle, "align left");

        bookPanel = new JPanel(new MigLayout("", "[][][]", "[]"));

        JButton leftBtn = new JButton("<");
        leftBtn.addActionListener(e -> {
            startIndex = (startIndex - 1 + bestsellerBooks.size()) % bestsellerBooks.size();
            updateBookRow();
        });
        bookPanel.add(leftBtn, "align left, split 3");

        bookRow = new JPanel(new MigLayout("", "[][][][]", "[]"));
        bookPanel.add(bookRow, "align center");

        JButton rightBtn = new JButton(">");
        rightBtn.addActionListener(e -> {
            startIndex = (startIndex + 1) % bestsellerBooks.size();
            updateBookRow();
        });
        bookPanel.add(rightBtn);

        mainContentPanel.add(bookPanel, "align center");

        JLabel totalRental = new JLabel("누적 대여량 높은 순");
        totalRental.setFont(totalRental.getFont().deriveFont(Font.BOLD, 20f));
        mainContentPanel.add(totalRental, "align left");

        BookDAO dao = new BookDAO();
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
        JPanel panel = new JPanel(new MigLayout("wrap 1", "center", "[]10[]"));
        panel.setPreferredSize(new Dimension(140, 230));
        URL imgURL = getClass().getResource("/" + book.getImagePath());
        JLabel image = new JLabel();
        image.setPreferredSize(new Dimension(120, 160));
        image.setHorizontalAlignment(SwingConstants.CENTER);
        image.setVerticalAlignment(SwingConstants.CENTER);

        if (imgURL != null) {
            image.setIcon(new ImageIcon(
                    new ImageIcon(imgURL).getImage().getScaledInstance(120, 160, Image.SCALE_SMOOTH)
            ));
        } else {
            image.setText("이미지 없음");
        }

        JLabel label = new JLabel("<html><div style='text-align: center; width: 120px;'>"
                +book.getTitle()  + "</div></html>");
        label.setPreferredSize(new Dimension(120, 40));
        label.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(image);
        panel.add(label);
        panel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        panel.addMouseListener(new MouseAdapter() {
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

        topResultPanel = new JPanel(new MigLayout("fillx", "[grow][]", "[]"));
        JLabel resultMsg = new JLabel("검색어 '" + keyword + "' 총 " + results.size() + "건 검색되었습니다.");
        resultMsg.setFont(resultMsg.getFont().deriveFont(Font.BOLD, 16f));
        topResultPanel.add(resultMsg, "align left");

        JPanel filterPanel = new JPanel();
        filterPanel.add(sortCombo);
        filterPanel.add(orderCombo);
        JComboBox<String> countCombo = new JComboBox<>(new String[]{"10건", "20건", "30건", "50건"});
        filterPanel.add(countCombo);
        topResultPanel.add(filterPanel, "align right");

        resultContentPanel = new JPanel(new MigLayout("wrap 1", "[grow]", "[]10[]"));
        for (Book book : results) {
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

        JPanel bottomPanel = new JPanel(new MigLayout("fillx", "[grow][]", "[]"));
        JButton backBtn = new JButton("뒤로가기");
        backBtn.addActionListener(ev -> showMainView());
        bottomPanel.add(new JLabel());
        bottomPanel.add(backBtn, "align right");

        mainContentPanel.add(topResultPanel, "growx");
        mainContentPanel.add(scrollPane, "grow");
        mainContentPanel.add(bottomPanel, "growx");

        mainContentPanel.revalidate();
        mainContentPanel.repaint();
    }

    // 상세 정보 화면
    private void showBookDetail(Book book,Runnable backCallback) {
        mainContentPanel.removeAll();

        JPanel detailPanel = new JPanel(new MigLayout("wrap 2,align center, insets 20",
                "[grow 0]20[grow 0]", "[]20[]20[]20[]"));

        URL imgURL = getClass().getResource("/" + book.getImagePath());
        JLabel imgLabel = new JLabel();
        if (imgURL != null) {
            imgLabel.setIcon(new ImageIcon(
                    new ImageIcon(imgURL).getImage().getScaledInstance(160, 220, Image.SCALE_SMOOTH)
            ));
        } else {
            imgLabel.setText("이미지 없음");
        }
        detailPanel.add(imgLabel, "span 1 5");

        detailPanel.add(new JLabel("<html><h2>" + book.getTitle() + "</h2></html>"), "wrap");
        detailPanel.add(new JLabel("저자: " + book.getAuthor()), "wrap");
        detailPanel.add(new JLabel("출판사: " + book.getPublisher()), "wrap");
        detailPanel.add(new JLabel("재고수량: " + book.getStock()), "wrap");
        detailPanel.add(new JLabel("대여가능여부: " + (book.getStock() > 0 ? "가능" : "불가")), "wrap");

        JPanel btnPanel = new JPanel(new MigLayout("", "[]10[]", "[]"));
        btnPanel.setOpaque(false);

        JButton rentBtn = new JButton("대여하기");
        rentBtn.setPreferredSize(new Dimension(120, 40));
        JButton returnBtn = new JButton("반납하기");
        returnBtn.setPreferredSize(new Dimension(120, 40));

        btnPanel.add(rentBtn);
        btnPanel.add(returnBtn);
        detailPanel.add(btnPanel, "span, align center, wrap");

        JButton backBtn = new JButton("뒤로가기");
        backBtn.setPreferredSize(new Dimension(120, 40));
        backBtn.addActionListener(e -> backCallback.run());
        detailPanel.add(backBtn, "span, align right");

        mainContentPanel.add(detailPanel, "grow");
        mainContentPanel.revalidate();
        mainContentPanel.repaint();
    }

    // 메인 실행 진입점
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (Exception e) {
            e.printStackTrace();
        }
        SwingUtilities.invokeLater(BookManagerApp::new);
    }
}
