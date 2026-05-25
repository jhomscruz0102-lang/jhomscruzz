package finals;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.datatransfer.StringSelection;
import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

// ==================== BASE CONVERTER LOGIC ====================
class BaseConverter {

    public static boolean validateInput(String value, int base) {
        if (value == null || value.trim().isEmpty()) return false;
        value = value.trim().toUpperCase();
        if (base > 16) base = 16;
        String validChars = "0123456789ABCDEF".substring(0, base);
        for (char c : value.toCharArray()) {
            if (validChars.indexOf(c) == -1) return false;
        }
        return true;
    }

    public static long convertToDecimal(String value, int base) {
        if (!validateInput(value, base))
            throw new IllegalArgumentException("Invalid input for base " + base);
        return Long.parseLong(value.trim().toUpperCase(), base);
    }

    public static String convertFromDecimal(long decimal, int base) {
        if (decimal < 0) return "-" + Long.toString(Math.abs(decimal), base).toUpperCase();
        return Long.toString(decimal, base).toUpperCase();
    }

    public static long performArithmetic(long num1, long num2, String op) {
        return switch (op) {
            case "+"  -> num1 + num2;
            case "-"  -> num1 - num2;
            case "×"  -> num1 * num2;
            case "÷"  -> { if (num2 == 0) throw new ArithmeticException("Division by zero"); yield num1 / num2; }
            case "%"  -> { if (num2 == 0) throw new ArithmeticException("Modulo by zero"); yield num1 % num2; }
            case "^"  -> (long) Math.pow(num1, num2);
            default   -> 0L;
        };
    }

    // Bitwise operations
    public static long bitwiseAnd(long a, long b)  { return a & b; }
    public static long bitwiseOr(long a, long b)   { return a | b; }
    public static long bitwiseXor(long a, long b)  { return a ^ b; }
    public static long bitwiseNot(long a)           { return ~a & 0xFFFFFFFFL; }
    public static long bitwiseNand(long a, long b)  { return (~(a & b)) & 0xFFFFFFFFL; }
    public static long bitwiseNor(long a, long b)   { return (~(a | b)) & 0xFFFFFFFFL; }
    public static long leftShift(long a, int n)     { return (a << n) & 0xFFFFFFFFL; }
    public static long rightShift(long a, int n)    { return a >> n; }

    public static String toBinaryPadded(long n, int minBits) {
        String bin = Long.toBinaryString(Math.abs(n));
        int pad = Math.max(minBits, ((bin.length() + 7) / 8) * 8);
        return String.format("%" + pad + "s", bin).replace(' ', '0');
    }

    // Convert between any bases 2-36
    public static String convertAnyBase(String value, int fromBase, int toBase) {
        try {
            long decimal = Long.parseLong(value.trim(), fromBase);
            return Long.toString(decimal, toBase).toUpperCase();
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid number for base " + fromBase);
        }
    }
}

// ==================== HISTORY ENTRY ====================
class HistoryEntry {
    public final String expression;
    public final String result;
    public final String timestamp;
    public final String type; // "ARITH", "CONV", "BITWISE"

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("HH:mm:ss");

    public HistoryEntry(String expression, String result, String type) {
        this.expression  = expression;
        this.result      = result;
        this.type        = type;
        this.timestamp   = LocalDateTime.now().format(FMT);
    }

    @Override
    public String toString() {
        return "[" + timestamp + "] [" + type + "] " + expression + " = " + result;
    }
}

// ==================== HISTORY MANAGER ====================
class HistoryManager {
    private final ArrayList<HistoryEntry> history = new ArrayList<>();

    public void add(String expression, String result, String type) {
        history.add(0, new HistoryEntry(expression, result, type));
    }

    public ArrayList<HistoryEntry> getHistory() { return history; }
    public void clear()                          { history.clear(); }
    public int  size()                           { return history.size(); }

    public void remove(int index) {
        if (index >= 0 && index < history.size()) history.remove(index);
    }

    public void exportToFile(String path) throws IOException {
        try (PrintWriter w = new PrintWriter(new FileWriter(path))) {
            w.println("BASE CALCULATOR HISTORY");
            w.println("=".repeat(50));
            w.println();
            for (HistoryEntry e : history) w.println(e);
        }
    }
}

// ==================== THEME CONSTANTS ====================
class Theme {
    static final Color BG         = new Color(6, 10, 16);
    static final Color PANEL      = new Color(13, 20, 32);
    static final Color CARD       = new Color(17, 24, 39);
    static final Color BORDER     = new Color(30, 45, 66);
    static final Color ACCENT     = new Color(0, 212, 255);
    static final Color ACCENT2    = new Color(124, 58, 237);
    static final Color ACCENT3    = new Color(16, 185, 129);
    static final Color ACCENT4    = new Color(245, 158, 11);
    static final Color TEXT       = new Color(226, 232, 240);
    static final Color MUTED      = new Color(74, 85, 104);
    static final Color SOFT       = new Color(136, 146, 160);
    static final Color RED        = new Color(239, 68, 68);
    static final Color GREEN      = new Color(16, 185, 129);

    static final Font MONO_PLAIN  = new Font("JetBrains Mono", Font.PLAIN, 13);
    static final Font MONO_BOLD   = new Font("JetBrains Mono", Font.BOLD, 13);
    static final Font MONO_SMALL  = new Font("JetBrains Mono", Font.PLAIN, 11);
    static final Font TITLE_FONT  = new Font("Segoe UI", Font.BOLD, 22);
    static final Font LABEL_FONT  = new Font("Segoe UI", Font.BOLD, 10);

    static JTextField styledField() {
        JTextField f = new JTextField();
        f.setBackground(BG);
        f.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER, 1),
            BorderFactory.createEmptyBorder(6, 10, 6, 10)));
        f.setForeground(TEXT);
        f.setCaretColor(ACCENT);
        f.setFont(MONO_PLAIN);
        return f;
    }

    static JComboBox<String> styledCombo(String... items) {
        JComboBox<String> c = new JComboBox<>(items);
        c.setBackground(BG);
        c.setForeground(TEXT);
        c.setFont(MONO_PLAIN);
        c.setBorder(BorderFactory.createLineBorder(BORDER, 1));
        ((JLabel) c.getRenderer()).setBackground(BG);
        return c;
    }

    static JButton primaryBtn(String text) {
        JButton b = new JButton(text);
        b.setBackground(ACCENT);
        b.setForeground(Color.BLACK);
        b.setFont(new Font("Segoe UI", Font.BOLD, 12));
        b.setBorder(BorderFactory.createEmptyBorder(8, 18, 8, 18));
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { b.setBackground(new Color(0, 185, 217)); }
            public void mouseExited(MouseEvent e)  { b.setBackground(ACCENT); }
        });
        return b;
    }

    static JButton secondaryBtn(String text) {
        JButton b = new JButton(text);
        b.setBackground(CARD);
        b.setForeground(SOFT);
        b.setFont(new Font("Segoe UI", Font.BOLD, 12));
        b.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER, 1),
            BorderFactory.createEmptyBorder(7, 14, 7, 14)));
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { b.setForeground(TEXT); b.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(SOFT, 1), BorderFactory.createEmptyBorder(7, 14, 7, 14))); }
            public void mouseExited(MouseEvent e)  { b.setForeground(SOFT); b.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(BORDER, 1), BorderFactory.createEmptyBorder(7, 14, 7, 14))); }
        });
        return b;
    }

    static JButton dangerBtn(String text) {
        JButton b = new JButton(text);
        b.setBackground(new Color(50, 15, 15));
        b.setForeground(RED);
        b.setFont(new Font("Segoe UI", Font.BOLD, 11));
        b.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(100, 30, 30), 1),
            BorderFactory.createEmptyBorder(6, 12, 6, 12)));
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    static JButton purpleBtn(String text) {
        JButton b = new JButton(text);
        b.setBackground(ACCENT2);
        b.setForeground(Color.WHITE);
        b.setFont(new Font("Segoe UI", Font.BOLD, 12));
        b.setBorder(BorderFactory.createEmptyBorder(8, 18, 8, 18));
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    static JLabel sectionLabel(String text) {
        JLabel l = new JLabel(text);
        l.setForeground(MUTED);
        l.setFont(new Font("Segoe UI", Font.BOLD, 10));
        return l;
    }

    static JPanel cardPanel() {
        JPanel p = new JPanel();
        p.setBackground(PANEL);
        p.setBorder(BorderFactory.createLineBorder(BORDER, 1));
        return p;
    }
}

// ==================== BIT PANEL ====================
class BitPanel extends JPanel {
    private long value = 0;
    private int  bits  = 32;
    private Color onColor  = Theme.ACCENT2;
    private Color offColor = Theme.BORDER;

    public BitPanel() {
        setBackground(Theme.BG);
        setPreferredSize(new Dimension(0, 36));
        setBorder(BorderFactory.createLineBorder(Theme.BORDER, 1));
    }

    public void setValue(long v, int numBits) {
        this.value = v;
        this.bits  = Math.max(8, ((Long.toBinaryString(Math.abs(v)).length() + 7) / 8) * 8);
        this.bits  = Math.min(bits, numBits);
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int w = getWidth(); int h = getHeight();
        int cellW = Math.min(28, (w - 8) / bits);
        int cellH = h - 8;
        int startX = (w - cellW * bits) / 2;

        for (int i = bits - 1; i >= 0; i--) {
            int col = bits - 1 - i;
            int x = startX + col * cellW;
            int y = 4;
            boolean on = ((value >> i) & 1) == 1;
            g2.setColor(on ? new Color(onColor.getRed(), onColor.getGreen(), onColor.getBlue(), 40) : new Color(BG_r(), BG_g(), BG_b()));
            g2.fillRoundRect(x + 1, y + 1, cellW - 3, cellH - 2, 4, 4);
            g2.setColor(on ? onColor : offColor);
            g2.drawRoundRect(x + 1, y + 1, cellW - 3, cellH - 2, 4, 4);
            g2.setColor(on ? onColor : Theme.MUTED);
            g2.setFont(new Font("Consolas", Font.BOLD, 10));
            FontMetrics fm = g2.getFontMetrics();
            String b = on ? "1" : "0";
            g2.drawString(b, x + (cellW - fm.stringWidth(b)) / 2, y + (cellH + fm.getAscent()) / 2 - 1);
        }
    }

    private int BG_r() { return Theme.BG.getRed(); }
    private int BG_g() { return Theme.BG.getGreen(); }
    private int BG_b() { return Theme.BG.getBlue(); }
}

// ==================== RESULT PANEL ====================
class ResultPanel extends JPanel {
    private final JTextArea area;
    public ResultPanel() {
        setLayout(new BorderLayout());
        setBackground(Theme.BG);
        setBorder(BorderFactory.createLineBorder(Theme.BORDER, 1));
        area = new JTextArea();
        area.setEditable(false);
        area.setBackground(Theme.BG);
        area.setForeground(Theme.TEXT);
        area.setFont(new Font("Consolas", Font.PLAIN, 13));
        area.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
        area.setLineWrap(true);
        add(new JScrollPane(area), BorderLayout.CENTER);
    }
    public void setText(String t) { area.setText(t); area.setCaretPosition(0); }
    public String getText()       { return area.getText(); }
    public void clear()           { area.setText(""); }
}

// ==================== MAIN DASHBOARD ====================
public class BaseCalculatorDashboard extends JFrame {

    // ── Arithmetic fields
    private JTextField  arithN1, arithN2;
    private JComboBox<String> arithBase1, arithBase2, arithOp;
    private ResultPanel arithResult;
    private BitPanel    arithBits;
    private JLabel      n1Hint, n2Hint;

    // ── Converter fields
    private JTextField  convInput, convFrom, convTo, convCustomVal;
    private JComboBox<String> convFromBase;
    private JLabel      cvBin, cvOct, cvDec, cvHex, customResult;

    // ── Bitwise fields
    private JTextField  bwA, bwB, bwShift;
    private ResultPanel bwResult;
    private BitPanel    bwBitsA, bwBitsB, bwBitsR;

    // ── Base Table
    private JTextField  tblStart, tblEnd;
    private JTable      baseTable;
    private DefaultTableModel tableModel;

    // ── History
    private final HistoryManager historyManager = new HistoryManager();
    private DefaultListModel<String> historyModel = new DefaultListModel<>();
    private JList<String> historyList;
    private JLabel histCountLabel, statCalcs, statConvs, statLast;

    // ── Status
    private JLabel statusLabel;
    private JLabel clockLabel;
    private javax.swing.Timer clockTimer;

    // ── Stats
    private int calcCount = 0, convCount = 0;

    // ── Tabs
    private JTabbedPane mainTabs;

    public BaseCalculatorDashboard() {
        setTitle("BaseCalc — Arithmetic & Conversion Engine");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1400, 860);
        setMinimumSize(new Dimension(1100, 700));
        setLocationRelativeTo(null);
        getContentPane().setBackground(Theme.BG);

        setJMenuBar(buildMenuBar());
        setLayout(new BorderLayout(6, 6));

        add(buildHeader(),  BorderLayout.NORTH);
        add(buildCenter(),  BorderLayout.CENTER);
        add(buildStatusBar(), BorderLayout.SOUTH);

        startClock();
        buildBaseTable(0, 31);
        setVisible(true);
    }

    // ─────────────────────────────────────────────────────
    //  MENU BAR
    // ─────────────────────────────────────────────────────
    private JMenuBar buildMenuBar() {
        JMenuBar bar = new JMenuBar();
        bar.setBackground(Theme.PANEL);
        bar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.BORDER));

        JMenu file = darkMenu("File");
        JMenuItem exportItem = new JMenuItem("Export History to TXT");
        exportItem.addActionListener(e -> exportHistory());
        JMenuItem clearItem = new JMenuItem("Clear All History");
        clearItem.addActionListener(e -> clearHistory());
        JMenuItem exitItem  = new JMenuItem("Exit");
        exitItem.addActionListener(e -> System.exit(0));
        file.add(exportItem); file.add(clearItem); file.addSeparator(); file.add(exitItem);

        JMenu help = darkMenu("Help");
        JMenuItem about = new JMenuItem("About");
        about.addActionListener(e -> JOptionPane.showMessageDialog(this,
            "BaseCalc Dashboard  v2.0\n" +
            "OOP Final Project — Enhanced Edition\n\n" +
            "Features:\n" +
            "  • Mixed-base arithmetic (+, -, ×, ÷, %, ^)\n" +
            "  • Base converter (2–36)\n" +
            "  • Bitwise operations (AND, OR, XOR, NOT, NAND, NOR, shifts)\n" +
            "  • Interactive base reference table\n" +
            "  • Fraction converter\n" +
            "  • Bit visualizer\n" +
            "  • Timestamped history with export\n" +
            "  • Real-time input validation",
            "About BaseCalc", JOptionPane.INFORMATION_MESSAGE));
        help.add(about);

        bar.add(file); bar.add(help);
        return bar;
    }

    private JMenu darkMenu(String text) {
        JMenu m = new JMenu(text);
        m.setForeground(Theme.SOFT);
        m.setFont(Theme.MONO_PLAIN);
        return m;
    }

    // ─────────────────────────────────────────────────────
    //  HEADER
    // ─────────────────────────────────────────────────────
    private JPanel buildHeader() {
        JPanel p = new JPanel(new BorderLayout(12, 0));
        p.setBackground(Theme.PANEL);
        p.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.BORDER),
            BorderFactory.createEmptyBorder(12, 18, 12, 18)));

        // Left: logo
        JPanel logoBox = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        logoBox.setOpaque(false);
        JLabel logoMark = new JLabel("BC");
        logoMark.setFont(new Font("Segoe UI", Font.BOLD, 18));
        logoMark.setForeground(Color.WHITE);
        logoMark.setOpaque(true);
        logoMark.setBackground(Theme.ACCENT2);
        logoMark.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));
        JPanel titleBox = new JPanel(new GridLayout(2, 1, 0, 2));
        titleBox.setOpaque(false);
        JLabel title = new JLabel("BASE CALC DASHBOARD");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(Color.WHITE);
        JLabel sub = new JLabel("Arithmetic & Number Base Conversion Engine");
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        sub.setForeground(Theme.SOFT);
        titleBox.add(title); titleBox.add(sub);
        logoBox.add(logoMark); logoBox.add(titleBox);

        // Right: badges + clock
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        right.setOpaque(false);
        right.add(badge("MIXED BASES", Theme.ACCENT, new Color(0, 212, 255, 20)));
        right.add(badge("REAL-TIME",   Theme.GREEN,  new Color(16, 185, 129, 20)));
        right.add(badge("BITWISE",     Theme.ACCENT2, new Color(124, 58, 237, 20)));
        clockLabel = new JLabel("00:00:00");
        clockLabel.setFont(new Font("Consolas", Font.BOLD, 13));
        clockLabel.setForeground(Theme.MUTED);
        right.add(clockLabel);

        p.add(logoBox, BorderLayout.WEST);
        p.add(right,   BorderLayout.EAST);
        return p;
    }

    private JLabel badge(String text, Color fg, Color bg) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.BOLD, 10));
        l.setForeground(fg);
        l.setOpaque(true);
        l.setBackground(bg);
        l.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(fg, 1),
            BorderFactory.createEmptyBorder(3, 8, 3, 8)));
        return l;
    }

    // ─────────────────────────────────────────────────────
    //  CENTER (LEFT sidebar + Tabbed center + RIGHT sidebar)
    // ─────────────────────────────────────────────────────
    private JPanel buildCenter() {
        JPanel p = new JPanel(new BorderLayout(8, 0));
        p.setBackground(Theme.BG);
        p.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
        p.add(buildLeftSidebar(),  BorderLayout.WEST);
        p.add(buildTabbedCenter(), BorderLayout.CENTER);
        p.add(buildRightSidebar(), BorderLayout.EAST);
        return p;
    }

    // ─────────────────────────────────────────────────────
    //  LEFT SIDEBAR  (reference + quick tips)
    // ─────────────────────────────────────────────────────
    private JPanel buildLeftSidebar() {
        JPanel outer = new JPanel(new BorderLayout(0, 8));
        outer.setBackground(Theme.BG);
        outer.setPreferredSize(new Dimension(210, 0));

        // Reference card
        JPanel refCard = darkCard("REFERENCE");
        JTextArea ref = new JTextArea(
            "BIN (2)  → 0-1\n" +
            "OCT (8)  → 0-7\n" +
            "DEC (10) → 0-9\n" +
            "HEX (16) → 0-9, A-F\n\n" +
            "MAX VALUE:\n" +
            Long.MAX_VALUE + "\n\n" +
            "SHORTCUTS:\n" +
            "  ENTER  — Calculate\n" +
            "  Ctrl+C — Copy result\n" +
            "  Right-click history\n" +
            "  to remove entry\n\n" +
            "TIPS:\n" +
            "  • Mixed bases allowed\n" +
            "  • Real-time validation\n" +
            "  • Click table row to\n" +
            "    load into converter"
        );
        ref.setEditable(false);
        ref.setBackground(Theme.PANEL);
        ref.setForeground(Theme.SOFT);
        ref.setFont(new Font("Consolas", Font.PLAIN, 11));
        ref.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
        ((JPanel) refCard.getClientProperty("body")).setLayout(new BorderLayout());
        ((JPanel) refCard.getClientProperty("body")).add(new JScrollPane(ref, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER));

        // Stats card
        JPanel statsCard = darkCard("SESSION STATS");
        JPanel statsBody = (JPanel) statsCard.getClientProperty("body");
        statsBody.setLayout(new GridLayout(3, 1, 0, 6));
        statsBody.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));

        statCalcs = bigStatLabel("0", Theme.ACCENT);
        statConvs = bigStatLabel("0", Theme.ACCENT4);
        statLast  = bigStatLabel("—", Theme.ACCENT3);
        statsBody.add(miniStat("Calculations", statCalcs));
        statsBody.add(miniStat("Conversions",  statConvs));
        statsBody.add(miniStat("Last Result",  statLast));

        outer.add(refCard,   BorderLayout.CENTER);
        outer.add(statsCard, BorderLayout.SOUTH);
        return outer;
    }

    private JLabel bigStatLabel(String text, Color color) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Consolas", Font.BOLD, 18));
        l.setForeground(color);
        return l;
    }

    private JPanel miniStat(String name, JLabel valueLabel) {
        JPanel p = new JPanel(new BorderLayout(4, 0));
        p.setBackground(Theme.BG);
        p.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Theme.BORDER, 1),
            BorderFactory.createEmptyBorder(5, 8, 5, 8)));
        JLabel n = new JLabel(name.toUpperCase());
        n.setFont(new Font("Segoe UI", Font.BOLD, 9));
        n.setForeground(Theme.MUTED);
        p.add(n, BorderLayout.NORTH);
        p.add(valueLabel, BorderLayout.CENTER);
        return p;
    }

    // ─────────────────────────────────────────────────────
    //  TABBED CENTER
    // ─────────────────────────────────────────────────────
    private JTabbedPane buildTabbedCenter() {
        mainTabs = new JTabbedPane(JTabbedPane.TOP);
        mainTabs.setBackground(Theme.PANEL);
        mainTabs.setForeground(Theme.SOFT);
        mainTabs.setFont(new Font("Segoe UI", Font.BOLD, 12));
        mainTabs.setBorder(BorderFactory.createLineBorder(Theme.BORDER, 1));

        mainTabs.addTab("⊕  Arithmetic",  buildArithmeticTab());
        mainTabs.addTab("⇄  Converter",   buildConverterTab());
        mainTabs.addTab("⊗  Bitwise",     buildBitwiseTab());
        mainTabs.addTab("⊞  Base Table",  buildBaseTableTab());

        return mainTabs;
    }

    // ─────────────────────────────────────────────────────
    //  TAB 1: ARITHMETIC
    // ─────────────────────────────────────────────────────
    private JPanel buildArithmeticTab() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(Theme.PANEL);
        p.setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 16));
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(5, 5, 5, 5);
        g.fill   = GridBagConstraints.HORIZONTAL;

        // Row 0: First operand
        g.gridy = 0; g.gridx = 0; g.weightx = 0;
        p.add(Theme.sectionLabel("FIRST OPERAND"), g);
        arithN1 = Theme.styledField();
        arithN1.setToolTipText("Enter a number in the selected base");
        g.gridx = 1; g.weightx = 1;
        p.add(arithN1, g);
        arithBase1 = Theme.styledCombo("BIN (2)", "OCT (8)", "DEC (10)", "HEX (16)");
        arithBase1.setSelectedIndex(2);
        g.gridx = 2; g.weightx = 0;
        p.add(arithBase1, g);

        // Hint row
        n1Hint = new JLabel(" ");
        n1Hint.setFont(new Font("Consolas", Font.PLAIN, 10));
        n1Hint.setForeground(Theme.MUTED);
        g.gridy = 1; g.gridx = 1; g.gridwidth = 2;
        p.add(n1Hint, g);
        g.gridwidth = 1;

        // Row 2: Operation
        g.gridy = 2; g.gridx = 0; g.weightx = 0;
        p.add(Theme.sectionLabel("OPERATION"), g);
        arithOp = Theme.styledCombo("+  (Add)", "−  (Subtract)", "×  (Multiply)", "÷  (Divide)", "%  (Modulo)", "^  (Power)");
        g.gridx = 1; g.weightx = 1;
        p.add(arithOp, g);

        // Row 3: Second operand
        g.gridy = 3; g.gridx = 0; g.weightx = 0;
        p.add(Theme.sectionLabel("SECOND OPERAND"), g);
        arithN2 = Theme.styledField();
        arithN2.setToolTipText("Enter a number in the selected base");
        g.gridx = 1; g.weightx = 1;
        p.add(arithN2, g);
        arithBase2 = Theme.styledCombo("BIN (2)", "OCT (8)", "DEC (10)", "HEX (16)");
        arithBase2.setSelectedIndex(2);
        g.gridx = 2; g.weightx = 0;
        p.add(arithBase2, g);

        // Hint row 2
        n2Hint = new JLabel(" ");
        n2Hint.setFont(new Font("Consolas", Font.PLAIN, 10));
        n2Hint.setForeground(Theme.MUTED);
        g.gridy = 4; g.gridx = 1; g.gridwidth = 2;
        p.add(n2Hint, g);
        g.gridwidth = 1;

        // Row 5: Buttons
        g.gridy = 5; g.gridx = 0; g.gridwidth = 3;
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        btnRow.setOpaque(false);
        JButton calcBtn  = Theme.primaryBtn("▶ CALCULATE");
        JButton swapBtn  = Theme.secondaryBtn("⇅ SWAP");
        JButton clearBtn = Theme.secondaryBtn("✕ CLEAR");
        JButton copyBtn  = Theme.purpleBtn("⎘ COPY ALL");
        calcBtn.addActionListener(e  -> calculateArithmetic());
        swapBtn.addActionListener(e  -> swapArithOperands());
        clearBtn.addActionListener(e -> clearArithmetic());
        copyBtn.addActionListener(e  -> copyToClipboard(arithResult.getText()));
        btnRow.add(calcBtn); btnRow.add(swapBtn); btnRow.add(clearBtn); btnRow.add(copyBtn);
        p.add(btnRow, g);
        g.gridwidth = 1;

        // Row 6: Result
        g.gridy = 6; g.gridx = 0; g.gridwidth = 3; g.weighty = 1; g.fill = GridBagConstraints.BOTH;
        arithResult = new ResultPanel();
        arithResult.setText("   Enter values above and press CALCULATE (or press Enter)\n\n   Result will appear here in all bases.");
        p.add(arithResult, g);
        g.weighty = 0; g.fill = GridBagConstraints.HORIZONTAL;

        // Row 7: Bit visualizer
        g.gridy = 7;
        JPanel bitSection = new JPanel(new BorderLayout(6, 4));
        bitSection.setOpaque(false);
        JLabel bitLabel = Theme.sectionLabel("BIT VISUALIZER");
        arithBits = new BitPanel();
        bitSection.add(bitLabel,    BorderLayout.NORTH);
        bitSection.add(arithBits,   BorderLayout.CENTER);
        p.add(bitSection, g);
        g.gridwidth = 1;

        // Live validation listeners
        arithN1.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) { liveValidate(arithN1, arithBase1, n1Hint); }
            public void keyPressed(KeyEvent e)  { if (e.getKeyCode() == KeyEvent.VK_ENTER) calculateArithmetic(); }
        });
        arithN2.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) { liveValidate(arithN2, arithBase2, n2Hint); }
            public void keyPressed(KeyEvent e)  { if (e.getKeyCode() == KeyEvent.VK_ENTER) calculateArithmetic(); }
        });
        arithBase1.addActionListener(e -> liveValidate(arithN1, arithBase1, n1Hint));
        arithBase2.addActionListener(e -> liveValidate(arithN2, arithBase2, n2Hint));

        return p;
    }

    // ─────────────────────────────────────────────────────
    //  TAB 2: CONVERTER
    // ─────────────────────────────────────────────────────
    private JPanel buildConverterTab() {
        JPanel p = new JPanel(new BorderLayout(0, 14));
        p.setBackground(Theme.PANEL);
        p.setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 16));

        // ── Top: standard converter
        JPanel top = new JPanel(new GridBagLayout());
        top.setBackground(Theme.PANEL);
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(5, 5, 5, 5);
        g.fill = GridBagConstraints.HORIZONTAL;

        g.gridy = 0; g.gridx = 0; g.weightx = 0;
        top.add(Theme.sectionLabel("INPUT VALUE"), g);
        convInput = Theme.styledField();
        g.gridx = 1; g.weightx = 1;
        top.add(convInput, g);
        convFromBase = Theme.styledCombo("BIN (2)", "OCT (8)", "DEC (10)", "HEX (16)");
        convFromBase.setSelectedIndex(2);
        g.gridx = 2; g.weightx = 0;
        top.add(convFromBase, g);

        g.gridy = 1; g.gridx = 0; g.gridwidth = 3;
        JButton convBtn = Theme.primaryBtn("⇄ CONVERT ALL BASES");
        JPanel cb = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        cb.setOpaque(false);
        cb.add(convBtn);
        top.add(cb, g);
        g.gridwidth = 1;

        // All-bases result grid
        g.gridy = 2; g.gridx = 0; g.gridwidth = 3; g.fill = GridBagConstraints.BOTH; g.weighty = 0;
        JPanel baseGrid = new JPanel(new GridLayout(2, 2, 8, 8));
        baseGrid.setOpaque(false);
        cvBin = resultValueLabel("—", Theme.ACCENT2); baseGrid.add(baseResultCard("Binary  (2)",  cvBin));
        cvOct = resultValueLabel("—", Theme.ACCENT4); baseGrid.add(baseResultCard("Octal   (8)",  cvOct));
        cvDec = resultValueLabel("—", Theme.ACCENT3); baseGrid.add(baseResultCard("Decimal (10)", cvDec));
        cvHex = resultValueLabel("—", Theme.ACCENT);  baseGrid.add(baseResultCard("Hex     (16)", cvHex));
        top.add(baseGrid, g);
        g.gridwidth = 1; g.fill = GridBagConstraints.HORIZONTAL;

        // ── Bottom: custom base + fraction
        JPanel bottom = new JPanel(new GridLayout(1, 2, 12, 0));
        bottom.setOpaque(false);

        // Custom base panel
        JPanel custom = darkCard("CUSTOM BASE (2–36)");
        JPanel customBody = (JPanel) custom.getClientProperty("body");
        customBody.setLayout(new GridBagLayout());
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(4, 6, 4, 6); gc.fill = GridBagConstraints.HORIZONTAL;
        gc.gridy = 0; gc.gridx = 0; customBody.add(Theme.sectionLabel("VALUE"), gc);
        convCustomVal = Theme.styledField();
        gc.gridx = 1; gc.weightx = 1; customBody.add(convCustomVal, gc);
        gc.gridy = 1; gc.gridx = 0; gc.weightx = 0; customBody.add(Theme.sectionLabel("FROM BASE"), gc);
        convFrom = Theme.styledField(); convFrom.setPreferredSize(new Dimension(60, 30));
        gc.gridx = 1; gc.weightx = 0; customBody.add(convFrom, gc);
        gc.gridy = 2; gc.gridx = 0; customBody.add(Theme.sectionLabel("TO BASE"), gc);
        convTo = Theme.styledField(); convTo.setPreferredSize(new Dimension(60, 30));
        gc.gridx = 1; customBody.add(convTo, gc);
        gc.gridy = 3; gc.gridx = 0; gc.gridwidth = 2;
        JButton customConvBtn = Theme.primaryBtn("CONVERT");
        customBody.add(customConvBtn, gc);
        gc.gridy = 4;
        customResult = new JLabel(" ");
        customResult.setFont(new Font("Consolas", Font.BOLD, 13));
        customResult.setForeground(Theme.ACCENT);
        customBody.add(customResult, gc);

        // Fraction panel
        JPanel frac = darkCard("FRACTION CONVERTER");
        JPanel fracBody = (JPanel) frac.getClientProperty("body");
        fracBody.setLayout(new GridBagLayout());
        GridBagConstraints gf = new GridBagConstraints();
        gf.insets = new Insets(4, 6, 4, 6); gf.fill = GridBagConstraints.HORIZONTAL;

        JTextField fracIn = Theme.styledField(); fracIn.setToolTipText("e.g. 0.75 or 0.11 for binary");
        JComboBox<String> fracBase = Theme.styledCombo("DEC (10)", "BIN (2)", "HEX (16)");
        JLabel fracResult = new JLabel(" ");
        fracResult.setFont(new Font("Consolas", Font.PLAIN, 11));
        fracResult.setForeground(Theme.ACCENT3);

        gf.gridy = 0; gf.gridx = 0; fracBody.add(Theme.sectionLabel("FRACTION VALUE"), gf);
        gf.gridx = 1; gf.weightx = 1; fracBody.add(fracIn, gf);
        gf.gridy = 1; gf.gridx = 0; gf.weightx = 0; fracBody.add(Theme.sectionLabel("FROM BASE"), gf);
        gf.gridx = 1; gf.weightx = 1; fracBody.add(fracBase, gf);
        gf.gridy = 2; gf.gridx = 0; gf.gridwidth = 2;
        JButton fracBtn = Theme.primaryBtn("CONVERT FRACTION");
        fracBody.add(fracBtn, gf);
        gf.gridy = 3;
        fracBody.add(fracResult, gf);

        fracBtn.addActionListener(e -> convertFraction(fracIn.getText().trim(), fracBase.getSelectedIndex(), fracResult));
        fracIn.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) { if (e.getKeyCode() == KeyEvent.VK_ENTER) convertFraction(fracIn.getText().trim(), fracBase.getSelectedIndex(), fracResult); }
        });

        bottom.add(custom); bottom.add(frac);

        // Wire actions
        convBtn.addActionListener(e -> convertAllBases());
        convInput.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) { if (e.getKeyCode() == KeyEvent.VK_ENTER) convertAllBases(); }
        });
        customConvBtn.addActionListener(e -> performCustomConvert());

        p.add(top,    BorderLayout.CENTER);
        p.add(bottom, BorderLayout.SOUTH);
        return p;
    }

    // ─────────────────────────────────────────────────────
    //  TAB 3: BITWISE
    // ─────────────────────────────────────────────────────
    private JPanel buildBitwiseTab() {
        JPanel p = new JPanel(new BorderLayout(0, 12));
        p.setBackground(Theme.PANEL);
        p.setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 16));

        // Inputs
        JPanel inputs = new JPanel(new GridBagLayout());
        inputs.setBackground(Theme.PANEL);
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(5, 5, 5, 5); g.fill = GridBagConstraints.HORIZONTAL;

        g.gridy = 0; g.gridx = 0; g.weightx = 0;
        inputs.add(Theme.sectionLabel("OPERAND A (Decimal)"), g);
        bwA = Theme.styledField();
        g.gridx = 1; g.weightx = 1; inputs.add(bwA, g);

        g.gridy = 1; g.gridx = 0; g.weightx = 0;
        inputs.add(Theme.sectionLabel("OPERAND B (Decimal)"), g);
        bwB = Theme.styledField();
        g.gridx = 1; g.weightx = 1; inputs.add(bwB, g);

        g.gridy = 2; g.gridx = 0; g.weightx = 0;
        inputs.add(Theme.sectionLabel("SHIFT AMOUNT"), g);
        bwShift = Theme.styledField(); bwShift.setText("1");
        g.gridx = 1; g.weightx = 1; inputs.add(bwShift, g);

        // Operation buttons
        JPanel ops = new JPanel(new GridLayout(2, 4, 8, 8));
        ops.setBackground(Theme.PANEL);
        String[] opNames = {"AND", "OR", "XOR", "NOT A", "NAND", "NOR", "A << n", "A >> n"};
        Color[] opColors = {Theme.ACCENT, Theme.ACCENT3, Theme.ACCENT4, Theme.RED, Theme.ACCENT2, Theme.SOFT, Theme.ACCENT, Theme.ACCENT3};
        for (int i = 0; i < opNames.length; i++) {
            final String op = opNames[i];
            JButton btn = bitwiseOpBtn(op, opColors[i]);
            btn.addActionListener(e -> performBitwise(op));
            ops.add(btn);
        }

        // Result + Bit Rows
        bwResult = new ResultPanel();
        bwBitsA = new BitPanel(); bwBitsA.setPreferredSize(new Dimension(0, 32));
        bwBitsB = new BitPanel(); bwBitsB.setPreferredSize(new Dimension(0, 32));
        bwBitsR = new BitPanel(); bwBitsR.setPreferredSize(new Dimension(0, 32));

        JPanel bitRows = new JPanel(new GridLayout(3, 1, 0, 4));
        bitRows.setBackground(Theme.PANEL);
        bitRows.add(labeledBitRow("A  ", bwBitsA, Theme.ACCENT2));
        bitRows.add(labeledBitRow("B  ", bwBitsB, Theme.ACCENT4));
        bitRows.add(labeledBitRow("R  ", bwBitsR, Theme.ACCENT));

        p.add(inputs,  BorderLayout.NORTH);
        JPanel mid = new JPanel(new BorderLayout(0, 8));
        mid.setBackground(Theme.PANEL);
        mid.add(ops,      BorderLayout.NORTH);
        mid.add(bwResult, BorderLayout.CENTER);
        mid.add(bitRows,  BorderLayout.SOUTH);
        p.add(mid, BorderLayout.CENTER);
        return p;
    }

    private JPanel labeledBitRow(String label, BitPanel bp, Color color) {
        JPanel row = new JPanel(new BorderLayout(6, 0));
        row.setBackground(Theme.PANEL);
        JLabel l = new JLabel(label);
        l.setFont(new Font("Consolas", Font.BOLD, 12));
        l.setForeground(color);
        l.setPreferredSize(new Dimension(30, 0));
        row.add(l,  BorderLayout.WEST);
        row.add(bp, BorderLayout.CENTER);
        return row;
    }

    private JButton bitwiseOpBtn(String text, Color color) {
        JButton b = new JButton(text);
        b.setBackground(Theme.BG);
        b.setForeground(color);
        b.setFont(new Font("Consolas", Font.BOLD, 12));
        b.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Theme.BORDER, 1),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)));
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        Color hoverBg = new Color(color.getRed(), color.getGreen(), color.getBlue(), 25);
        b.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { b.setBackground(hoverBg); b.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(color, 1), BorderFactory.createEmptyBorder(8, 12, 8, 12))); }
            public void mouseExited(MouseEvent e)  { b.setBackground(Theme.BG); b.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Theme.BORDER, 1), BorderFactory.createEmptyBorder(8, 12, 8, 12))); }
        });
        return b;
    }

    // ─────────────────────────────────────────────────────
    //  TAB 4: BASE TABLE
    // ─────────────────────────────────────────────────────
    private JPanel buildBaseTableTab() {
        JPanel p = new JPanel(new BorderLayout(0, 10));
        p.setBackground(Theme.PANEL);
        p.setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 16));

        // Range controls
        JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        controls.setBackground(Theme.PANEL);
        controls.add(Theme.sectionLabel("FROM:"));
        tblStart = Theme.styledField(); tblStart.setText("0");  tblStart.setPreferredSize(new Dimension(80, 32));
        controls.add(tblStart);
        controls.add(Theme.sectionLabel("TO:"));
        tblEnd = Theme.styledField(); tblEnd.setText("31"); tblEnd.setPreferredSize(new Dimension(80, 32));
        controls.add(tblEnd);
        JButton buildBtn = Theme.primaryBtn("BUILD TABLE");
        buildBtn.addActionListener(e -> {
            try {
                int s = Integer.parseInt(tblStart.getText().trim());
                int en = Integer.parseInt(tblEnd.getText().trim());
                buildBaseTable(s, Math.min(en, s + 255));
            } catch (NumberFormatException ex) { setStatus("✗ Invalid range", Theme.RED); }
        });
        controls.add(buildBtn);
        JButton exportTblBtn = Theme.secondaryBtn("⬇ EXPORT CSV");
        exportTblBtn.addActionListener(e -> exportTableToCSV());
        controls.add(exportTblBtn);

        // Table
        String[] cols = {"DEC", "BIN", "OCT", "HEX", "BIN (padded 8)"};
        tableModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        baseTable = new JTable(tableModel);
        baseTable.setBackground(Theme.BG);
        baseTable.setForeground(Theme.TEXT);
        baseTable.setFont(new Font("Consolas", Font.PLAIN, 12));
        baseTable.setGridColor(Theme.BORDER);
        baseTable.setRowHeight(24);
        baseTable.setSelectionBackground(new Color(0, 212, 255, 30));
        baseTable.setSelectionForeground(Theme.TEXT);
        baseTable.getTableHeader().setBackground(Theme.CARD);
        baseTable.getTableHeader().setForeground(Theme.SOFT);
        baseTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 11));
        baseTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        // Color columns differently via renderer
        for (int i = 0; i < cols.length; i++) {
            final int col = i;
            Color[] colColors = {Theme.SOFT, Theme.ACCENT2, Theme.ACCENT4, Theme.ACCENT, Theme.ACCENT2};
            baseTable.getColumnModel().getColumn(i).setCellRenderer((t, val, sel, foc, row, c) -> {
                JLabel l = new JLabel(val != null ? val.toString() : "");
                l.setFont(new Font("Consolas", col == 0 ? Font.PLAIN : Font.BOLD, 12));
                l.setForeground(sel ? Theme.ACCENT : colColors[col]);
                l.setBackground(sel ? new Color(0, 212, 255, 20) : (row % 2 == 0 ? Theme.BG : new Color(13, 20, 32)));
                l.setOpaque(true);
                l.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
                return l;
            });
        }
        // Click row to load into converter
        baseTable.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                int row = baseTable.getSelectedRow();
                if (row >= 0) {
                    String dec = tableModel.getValueAt(row, 0).toString();
                    convInput.setText(dec);
                    convFromBase.setSelectedIndex(2); // DEC
                    convertAllBases();
                    mainTabs.setSelectedIndex(1);
                }
            }
        });

        p.add(controls, BorderLayout.NORTH);
        p.add(new JScrollPane(baseTable), BorderLayout.CENTER);
        return p;
    }

    // ─────────────────────────────────────────────────────
    //  RIGHT SIDEBAR  (History)
    // ─────────────────────────────────────────────────────
    private JPanel buildRightSidebar() {
        JPanel outer = new JPanel(new BorderLayout());
        outer.setBackground(Theme.BG);
        outer.setPreferredSize(new Dimension(295, 0));

        JPanel histCard = darkCard("HISTORY");
        histCountLabel = new JLabel("0 items");
        histCountLabel.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        histCountLabel.setForeground(Theme.MUTED);

        historyModel = new DefaultListModel<>();
        historyList  = new JList<>(historyModel);
        historyList.setBackground(Theme.PANEL);
        historyList.setForeground(Theme.SOFT);
        historyList.setFont(new Font("Consolas", Font.PLAIN, 11));
        historyList.setFixedCellHeight(48);
        historyList.setCellRenderer(new HistoryCellRenderer());
        historyList.setBorder(null);

        // Right-click menu
        JPopupMenu popup = new JPopupMenu();
        popup.setBackground(Theme.CARD);
        JMenuItem copyItem   = new JMenuItem("Copy Entry");
        JMenuItem deleteItem = new JMenuItem("Delete Entry");
        copyItem.addActionListener(e -> {
            int idx = historyList.getSelectedIndex();
            if (idx >= 0) copyToClipboard(historyManager.getHistory().get(idx).toString());
        });
        deleteItem.addActionListener(e -> {
            int idx = historyList.getSelectedIndex();
            if (idx >= 0) { historyManager.remove(idx); refreshHistory(); }
        });
        popup.add(copyItem); popup.add(deleteItem);
        historyList.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e)  { showPopup(e); }
            public void mouseReleased(MouseEvent e) { showPopup(e); }
            private void showPopup(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    historyList.setSelectedIndex(historyList.locationToIndex(e.getPoint()));
                    popup.show(historyList, e.getX(), e.getY());
                }
            }
        });

        JPanel body = (JPanel) histCard.getClientProperty("body");
        body.setLayout(new BorderLayout());
        body.add(new JScrollPane(historyList), BorderLayout.CENTER);

        JPanel btnRow = new JPanel(new GridLayout(1, 2, 6, 0));
        btnRow.setBackground(Theme.PANEL);
        btnRow.setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));
        JButton clearHistBtn  = Theme.dangerBtn("✕ CLEAR ALL");
        JButton exportHistBtn = Theme.secondaryBtn("⬇ EXPORT");
        clearHistBtn.addActionListener(e  -> clearHistory());
        exportHistBtn.addActionListener(e -> exportHistory());
        btnRow.add(clearHistBtn); btnRow.add(exportHistBtn);

        histCard.add(btnRow, BorderLayout.SOUTH);
        outer.add(histCard, BorderLayout.CENTER);
        return outer;
    }

    // ─────────────────────────────────────────────────────
    //  STATUS BAR
    // ─────────────────────────────────────────────────────
    private JPanel buildStatusBar() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(Theme.PANEL);
        p.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, Theme.BORDER),
            BorderFactory.createEmptyBorder(5, 14, 5, 14)));
        statusLabel = new JLabel("●  Ready — Mixed base arithmetic & conversion enabled");
        statusLabel.setFont(new Font("Consolas", Font.PLAIN, 11));
        statusLabel.setForeground(Theme.SOFT);
        JLabel ver = new JLabel("BaseCalc v2.0  |  OOP Final Project");
        ver.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        ver.setForeground(Theme.MUTED);
        p.add(statusLabel, BorderLayout.WEST);
        p.add(ver,         BorderLayout.EAST);
        return p;
    }

    // ─────────────────────────────────────────────────────
    //  HELPER BUILDERS
    // ─────────────────────────────────────────────────────
    private JPanel darkCard(String title) {
        JPanel outer = new JPanel(new BorderLayout());
        outer.setBackground(Theme.PANEL);
        outer.setBorder(BorderFactory.createLineBorder(Theme.BORDER, 1));
        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Theme.CARD);
        header.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.BORDER),
            BorderFactory.createEmptyBorder(7, 12, 7, 12)));
        JLabel dot = new JLabel("●");
        dot.setForeground(Theme.ACCENT);
        dot.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        JLabel titleLabel = new JLabel(" " + title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
        titleLabel.setForeground(Theme.SOFT);
        header.add(dot,        BorderLayout.WEST);
        header.add(titleLabel, BorderLayout.CENTER);
        // Body
        JPanel body = new JPanel();
        body.setBackground(Theme.PANEL);
        body.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        outer.add(header, BorderLayout.NORTH);
        outer.add(body,   BorderLayout.CENTER);
        outer.putClientProperty("body", body);
        return outer;
    }

    private JPanel baseResultCard(String label, JLabel valueLabel) {
        JPanel card = new JPanel(new BorderLayout(0, 4));
        card.setBackground(Theme.BG);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Theme.BORDER, 1),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)));
        JLabel lbl = new JLabel(label.toUpperCase());
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 9));
        lbl.setForeground(Theme.MUTED);
        card.add(lbl,        BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        return card;
    }

    private JLabel resultValueLabel(String text, Color color) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Consolas", Font.BOLD, 16));
        l.setForeground(color);
        return l;
    }

    // ─────────────────────────────────────────────────────
    //  LOGIC — ARITHMETIC
    // ─────────────────────────────────────────────────────
    private int selectedBase(JComboBox<String> combo) {
        return switch (combo.getSelectedIndex()) {
            case 0 -> 2; case 1 -> 8; case 2 -> 10; case 3 -> 16; default -> 10;
        };
    }

    private void calculateArithmetic() {
        String v1 = arithN1.getText().trim();
        String v2 = arithN2.getText().trim();
        if (v1.isEmpty() || v2.isEmpty()) {
            setStatus("⚠  Both operand fields are required", Theme.ACCENT4);
            return;
        }
        int b1 = selectedBase(arithBase1);
        int b2 = selectedBase(arithBase2);
        if (!BaseConverter.validateInput(v1, b1)) {
            setStatus("✗  First operand is invalid for base " + b1, Theme.RED);
            arithN1.setBorder(BorderFactory.createLineBorder(Theme.RED, 2));
            return;
        }
        if (!BaseConverter.validateInput(v2, b2)) {
            setStatus("✗  Second operand is invalid for base " + b2, Theme.RED);
            arithN2.setBorder(BorderFactory.createLineBorder(Theme.RED, 2));
            return;
        }
        String[] opSymbols = {"+", "−", "×", "÷", "%", "^"};
        String opStr = opSymbols[arithOp.getSelectedIndex()];
        String[] internalOps = {"+", "-", "×", "÷", "%", "^"};
        String intOp = internalOps[arithOp.getSelectedIndex()];
        try {
            long d1 = BaseConverter.convertToDecimal(v1, b1);
            long d2 = BaseConverter.convertToDecimal(v2, b2);
            long result = BaseConverter.performArithmetic(d1, d2, intOp);

            StringBuilder sb = new StringBuilder();
            sb.append("  Expression : ").append(v1).append(" (B").append(b1).append(")")
              .append("  ").append(opStr).append("  ")
              .append(v2).append(" (B").append(b2).append(")\n");
            sb.append("  ─────────────────────────────────────────\n");
            sb.append("  Decimal     : ").append(result).append("\n");
            sb.append("  Binary      : ").append(BaseConverter.convertFromDecimal(result, 2)).append("\n");
            sb.append("  Octal       : ").append(BaseConverter.convertFromDecimal(result, 8)).append("\n");
            sb.append("  Hex         : ").append(BaseConverter.convertFromDecimal(result, 16)).append("\n");
            sb.append("  ─────────────────────────────────────────\n");
            sb.append("  Operand A   : ").append(d1).append(" (decimal)\n");
            sb.append("  Operand B   : ").append(d2).append(" (decimal)\n");

            arithResult.setText(sb.toString());
            arithBits.setValue(result, 32);

            String expr = v1 + "(B" + b1 + ") " + opStr + " " + v2 + "(B" + b2 + ")";
            historyManager.add(expr, String.valueOf(result), "ARITH");
            calcCount++;
            statCalcs.setText(String.valueOf(calcCount));
            statLast.setText(String.valueOf(result));
            refreshHistory();
            setStatus("✓  Result: " + result + "  (decimal)", Theme.GREEN);

        } catch (ArithmeticException ex) {
            arithResult.setText("  ✗ ARITHMETIC ERROR\n\n  " + ex.getMessage());
            setStatus("✗  " + ex.getMessage(), Theme.RED);
        } catch (Exception ex) {
            arithResult.setText("  ✗ ERROR\n\n  " + ex.getMessage());
            setStatus("✗  " + ex.getMessage(), Theme.RED);
        }
    }

    private void swapArithOperands() {
        String v1 = arithN1.getText(); String v2 = arithN2.getText();
        int    b1 = arithBase1.getSelectedIndex(); int b2 = arithBase2.getSelectedIndex();
        arithN1.setText(v2); arithN2.setText(v1);
        arithBase1.setSelectedIndex(b2); arithBase2.setSelectedIndex(b1);
        liveValidate(arithN1, arithBase1, n1Hint);
        liveValidate(arithN2, arithBase2, n2Hint);
        setStatus("↕  Operands swapped", Theme.ACCENT4);
    }

    private void clearArithmetic() {
        arithN1.setText(""); arithN2.setText("");
        arithN1.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Theme.BORDER, 1), BorderFactory.createEmptyBorder(6, 10, 6, 10)));
        arithN2.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Theme.BORDER, 1), BorderFactory.createEmptyBorder(6, 10, 6, 10)));
        n1Hint.setText(" "); n2Hint.setText(" ");
        arithResult.setText("   Enter values above and press CALCULATE\n\n   Result will appear here.");
        arithBits.setValue(0, 8);
        setStatus("Fields cleared", Theme.SOFT);
    }

    private void liveValidate(JTextField field, JComboBox<String> baseCombo, JLabel hint) {
        String val = field.getText().trim();
        if (val.isEmpty()) {
            field.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Theme.BORDER, 1), BorderFactory.createEmptyBorder(6, 10, 6, 10)));
            hint.setText(" "); return;
        }
        int base = selectedBase(baseCombo);
        boolean valid = BaseConverter.validateInput(val, base);
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(valid ? Theme.GREEN : Theme.RED, 1),
            BorderFactory.createEmptyBorder(6, 10, 6, 10)));
        if (valid) {
            try {
                long dec = BaseConverter.convertToDecimal(val, base);
                hint.setText("  ✓  valid  →  decimal: " + dec);
                hint.setForeground(Theme.GREEN);
            } catch (Exception e) { hint.setText("  ✓  valid"); hint.setForeground(Theme.GREEN); }
        } else {
            String allowed = base <= 10 ? "0-" + (base - 1) : "0-9, A-" + "0123456789ABCDEF".charAt(base - 1);
            hint.setText("  ✗  invalid for base " + base + "  (allowed: " + allowed + ")");
            hint.setForeground(Theme.RED);
        }
    }

    // ─────────────────────────────────────────────────────
    //  LOGIC — CONVERTER
    // ─────────────────────────────────────────────────────
    private void convertAllBases() {
        String val = convInput.getText().trim();
        if (val.isEmpty()) return;
        int fromBase = selectedBase(convFromBase);
        if (!BaseConverter.validateInput(val, fromBase)) {
            setStatus("✗  Invalid number for base " + fromBase, Theme.RED);
            cvBin.setText("invalid"); cvOct.setText("invalid");
            cvDec.setText("invalid"); cvHex.setText("invalid");
            return;
        }
        try {
            long dec = BaseConverter.convertToDecimal(val, fromBase);
            cvBin.setText(BaseConverter.convertFromDecimal(dec, 2));
            cvOct.setText(BaseConverter.convertFromDecimal(dec, 8));
            cvDec.setText(String.valueOf(dec));
            cvHex.setText(BaseConverter.convertFromDecimal(dec, 16));
            convCount++;
            statConvs.setText(String.valueOf(convCount));
            statLast.setText(String.valueOf(dec));
            historyManager.add(val + " (B" + fromBase + ") → all bases", "dec=" + dec, "CONV");
            refreshHistory();
            setStatus("✓  Converted " + val + " (base " + fromBase + ") = " + dec + " decimal", Theme.GREEN);
        } catch (Exception ex) {
            setStatus("✗  " + ex.getMessage(), Theme.RED);
        }
    }

    private void performCustomConvert() {
        String val = convCustomVal.getText().trim();
        String fromStr = convFrom.getText().trim();
        String toStr   = convTo.getText().trim();
        if (val.isEmpty() || fromStr.isEmpty() || toStr.isEmpty()) {
            customResult.setText("⚠  Fill all three fields");
            customResult.setForeground(Theme.ACCENT4);
            return;
        }
        try {
            int from = Integer.parseInt(fromStr);
            int to   = Integer.parseInt(toStr);
            if (from < 2 || from > 36 || to < 2 || to > 36) {
                customResult.setText("✗  Base must be 2–36");
                customResult.setForeground(Theme.RED); return;
            }
            String result = BaseConverter.convertAnyBase(val, from, to);
            customResult.setText(val + " (B" + from + ")  →  " + result + " (B" + to + ")");
            customResult.setForeground(Theme.ACCENT);
            historyManager.add(val + "(B" + from + ") → B" + to, result, "CONV");
            convCount++; statConvs.setText(String.valueOf(convCount));
            refreshHistory();
            setStatus("✓  Custom base convert: " + result, Theme.GREEN);
        } catch (NumberFormatException ex) {
            customResult.setText("✗  Invalid base number");
            customResult.setForeground(Theme.RED);
        } catch (Exception ex) {
            customResult.setText("✗  " + ex.getMessage());
            customResult.setForeground(Theme.RED);
        }
    }

    private void convertFraction(String val, int baseIdx, JLabel resultLabel) {
        if (val.isEmpty()) { resultLabel.setText(""); return; }
        int[] bases = {10, 2, 16};
        int from = bases[baseIdx];
        try {
            double dec;
            if (from == 10) {
                dec = Double.parseDouble(val);
            } else if (from == 2) {
                String[] parts = val.split("\\.");
                dec = Integer.parseInt(parts[0], 2);
                if (parts.length > 1) {
                    for (int i = 0; i < parts[1].length(); i++)
                        dec += (parts[1].charAt(i) - '0') / Math.pow(2, i + 1);
                }
            } else {
                dec = Long.parseLong(val.replace(".", ""), 16);
                dec /= Math.pow(16, val.contains(".") ? val.length() - val.indexOf('.') - 1 : 0);
            }
            long whole = (long) dec; double frac = dec - whole;
            StringBuilder binFrac = new StringBuilder();
            double f2 = frac;
            for (int i = 0; i < 8 && f2 > 0; i++) {
                f2 *= 2; binFrac.append(f2 >= 1 ? "1" : "0"); if (f2 >= 1) f2 -= 1;
            }
            resultLabel.setText(
                "BIN: " + Long.toBinaryString(whole) + (binFrac.length() > 0 ? "." + binFrac : "") +
                "  |  OCT: " + Long.toOctalString(whole) +
                "  |  HEX: " + Long.toHexString(whole).toUpperCase()
            );
            resultLabel.setForeground(Theme.ACCENT3);
        } catch (Exception ex) {
            resultLabel.setText("✗  " + ex.getMessage());
            resultLabel.setForeground(Theme.RED);
        }
    }

    // ─────────────────────────────────────────────────────
    //  LOGIC — BITWISE
    // ─────────────────────────────────────────────────────
    private void performBitwise(String op) {
        long a, b = 0;
        int shiftAmt = 1;
        try { a = Long.parseLong(bwA.getText().trim().isEmpty() ? "0" : bwA.getText().trim()); }
        catch (Exception e) { setStatus("✗  Operand A must be a decimal integer", Theme.RED); return; }
        try { b = Long.parseLong(bwB.getText().trim().isEmpty() ? "0" : bwB.getText().trim()); }
        catch (Exception e) { /* B not always needed */ }
        try { shiftAmt = Integer.parseInt(bwShift.getText().trim().isEmpty() ? "1" : bwShift.getText().trim()); }
        catch (Exception e) { /* default to 1 */ }

        long result;
        String expr;
        switch (op) {
            case "AND"   -> { result = BaseConverter.bitwiseAnd(a, b);     expr = a + " AND " + b; }
            case "OR"    -> { result = BaseConverter.bitwiseOr(a, b);      expr = a + " OR "  + b; }
            case "XOR"   -> { result = BaseConverter.bitwiseXor(a, b);     expr = a + " XOR " + b; }
            case "NOT A" -> { result = BaseConverter.bitwiseNot(a);        expr = "NOT " + a; }
            case "NAND"  -> { result = BaseConverter.bitwiseNand(a, b);    expr = a + " NAND " + b; }
            case "NOR"   -> { result = BaseConverter.bitwiseNor(a, b);     expr = a + " NOR "  + b; }
            case "A << n"-> { result = BaseConverter.leftShift(a, shiftAmt);  expr = a + " << " + shiftAmt; }
            case "A >> n"-> { result = BaseConverter.rightShift(a, shiftAmt); expr = a + " >> " + shiftAmt; }
            default      -> { result = 0; expr = "?"; }
        }

        StringBuilder sb = new StringBuilder();
        sb.append("  Operation  : ").append(expr).append("\n");
        sb.append("  ─────────────────────────────────────────\n");
        sb.append("  Decimal     : ").append(result).append("\n");
        sb.append("  Binary      : ").append(BaseConverter.convertFromDecimal(result, 2)).append("\n");
        sb.append("  Octal       : ").append(BaseConverter.convertFromDecimal(result, 8)).append("\n");
        sb.append("  Hex         : ").append(BaseConverter.convertFromDecimal(result, 16)).append("\n");

        bwResult.setText(sb.toString());
        bwBitsA.setValue(a, 16);
        bwBitsB.setValue(b, 16);
        bwBitsR.setValue(result, 16);

        historyManager.add(expr, String.valueOf(result), "BITWISE");
        calcCount++; statCalcs.setText(String.valueOf(calcCount));
        statLast.setText(String.valueOf(result));
        refreshHistory();
        setStatus("✓  Bitwise " + op + " = " + result, Theme.GREEN);
    }

    // ─────────────────────────────────────────────────────
    //  LOGIC — BASE TABLE
    // ─────────────────────────────────────────────────────
    private void buildBaseTable(int start, int end) {
        tableModel.setRowCount(0);
        for (int i = start; i <= end; i++) {
            tableModel.addRow(new Object[]{
                String.valueOf(i),
                Integer.toBinaryString(i),
                Integer.toOctalString(i),
                Integer.toHexString(i).toUpperCase(),
                BaseConverter.toBinaryPadded(i, 8)
            });
        }
        setStatus("✓  Base table built: " + start + " to " + end + " (" + (end - start + 1) + " rows)", Theme.GREEN);
    }

    private void exportTableToCSV() {
        JFileChooser fc = new JFileChooser();
        fc.setSelectedFile(new File("base_table.csv"));
        if (fc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
        try (PrintWriter w = new PrintWriter(fc.getSelectedFile())) {
            w.println("DEC,BIN,OCT,HEX,BIN_PADDED");
            for (int i = 0; i < tableModel.getRowCount(); i++) {
                w.println(tableModel.getValueAt(i, 0) + "," +
                          tableModel.getValueAt(i, 1) + "," +
                          tableModel.getValueAt(i, 2) + "," +
                          tableModel.getValueAt(i, 3) + "," +
                          tableModel.getValueAt(i, 4));
            }
            JOptionPane.showMessageDialog(this, "Table exported to " + fc.getSelectedFile().getName(), "Export OK", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Export failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ─────────────────────────────────────────────────────
    //  HISTORY
    // ─────────────────────────────────────────────────────
    private void refreshHistory() {
        historyModel.clear();
        for (HistoryEntry e : historyManager.getHistory()) {
            historyModel.addElement(e.timestamp + "  " + e.type + "\n" + e.expression + " = " + e.result);
        }
        histCountLabel.setText(historyManager.size() + " items");
    }

    private void clearHistory() {
        int confirm = JOptionPane.showConfirmDialog(this, "Clear all history?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) { historyManager.clear(); refreshHistory(); setStatus("History cleared", Theme.SOFT); }
    }

    private void exportHistory() {
        if (historyManager.size() == 0) {
            JOptionPane.showMessageDialog(this, "No history to export.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        JFileChooser fc = new JFileChooser();
        fc.setSelectedFile(new File("calculator_history.txt"));
        if (fc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
        try {
            historyManager.exportToFile(fc.getSelectedFile().getAbsolutePath());
            JOptionPane.showMessageDialog(this, "Exported " + historyManager.size() + " entries to\n" + fc.getSelectedFile().getName(), "Export OK", JOptionPane.INFORMATION_MESSAGE);
            setStatus("✓  History exported", Theme.GREEN);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Export failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ─────────────────────────────────────────────────────
    //  UTILITIES
    // ─────────────────────────────────────────────────────
    private void copyToClipboard(String text) {
        if (text == null || text.isBlank()) { setStatus("⚠  Nothing to copy", Theme.ACCENT4); return; }
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(text), null);
        setStatus("⎘  Copied to clipboard", Theme.ACCENT3);
        JOptionPane.showMessageDialog(this, "Copied to clipboard!", "Copied", JOptionPane.INFORMATION_MESSAGE);
    }

    private void setStatus(String msg, Color color) {
        statusLabel.setText("  " + msg);
        statusLabel.setForeground(color != null ? color : Theme.SOFT);
    }

    private void startClock() {
        clockTimer = new javax.swing.Timer(1000, e -> {
            clockLabel.setText(LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
        });
        clockTimer.start();
        clockLabel.setText(LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
    }

    // ─────────────────────────────────────────────────────
    //  HISTORY CELL RENDERER
    // ─────────────────────────────────────────────────────
    class HistoryCellRenderer implements ListCellRenderer<String> {
        @Override
        public Component getListCellRendererComponent(JList<? extends String> list, String value,
                int index, boolean isSelected, boolean cellHasFocus) {
            JPanel cell = new JPanel(new BorderLayout(4, 2));
            cell.setBackground(isSelected ? new Color(0, 212, 255, 20) : (index % 2 == 0 ? Theme.PANEL : Theme.CARD));
            cell.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.BORDER),
                BorderFactory.createEmptyBorder(4, 10, 4, 10)));

            HistoryEntry entry = historyManager.getHistory().size() > index
                                 ? historyManager.getHistory().get(index) : null;
            if (entry == null) return cell;

            Color typeColor = switch (entry.type) {
                case "ARITH"   -> Theme.ACCENT;
                case "CONV"    -> Theme.ACCENT4;
                case "BITWISE" -> Theme.ACCENT2;
                default        -> Theme.SOFT;
            };

            JLabel typeBadge = new JLabel(entry.type);
            typeBadge.setFont(new Font("Segoe UI", Font.BOLD, 9));
            typeBadge.setForeground(typeColor);
            typeBadge.setOpaque(true);
            typeBadge.setBackground(new Color(typeColor.getRed(), typeColor.getGreen(), typeColor.getBlue(), 20));
            typeBadge.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(typeColor, 1),
                BorderFactory.createEmptyBorder(1, 5, 1, 5)));

            JLabel timeLabel = new JLabel(entry.timestamp);
            timeLabel.setFont(new Font("Consolas", Font.PLAIN, 9));
            timeLabel.setForeground(Theme.MUTED);

            JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
            top.setOpaque(false);
            top.add(typeBadge); top.add(timeLabel);

            JLabel exprLabel = new JLabel(entry.expression.length() > 36
                ? entry.expression.substring(0, 33) + "..." : entry.expression);
            exprLabel.setFont(new Font("Consolas", Font.PLAIN, 11));
            exprLabel.setForeground(Theme.SOFT);

            JLabel resultLabel = new JLabel("= " + entry.result);
            resultLabel.setFont(new Font("Consolas", Font.BOLD, 12));
            resultLabel.setForeground(isSelected ? Theme.ACCENT : Theme.TEXT);

            cell.add(top,         BorderLayout.NORTH);
            cell.add(exprLabel,   BorderLayout.CENTER);
            cell.add(resultLabel, BorderLayout.SOUTH);
            return cell;
        }
    }

    // ─────────────────────────────────────────────────────
    //  MAIN
    // ─────────────────────────────────────────────────────
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception ignored) {}
        SwingUtilities.invokeLater(BaseCalculatorDashboard::new);
    }
}