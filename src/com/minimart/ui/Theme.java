package com.minimart.ui;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicSplitPaneDivider;
import javax.swing.plaf.basic.BasicSplitPaneUI;
import javax.swing.plaf.basic.BasicTabbedPaneUI;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/** Giao diện phẳng hiện đại: bảng màu, font, và các thành phần Swing đã tạo kiểu sẵn. */
public final class Theme {
    public static final Color BG = new Color(0xF3F4F6);
    public static final Color CARD = Color.WHITE;
    public static final Color DARK = new Color(0x0F172A);
    public static final Color PRIMARY = new Color(0x2563EB);
    public static final Color PRIMARY_HOVER = new Color(0x1D4ED8);
    public static final Color DANGER = new Color(0xDC2626);
    public static final Color DANGER_HOVER = new Color(0xB91C1C);
    public static final Color TEXT = new Color(0x111827);
    public static final Color MUTED = new Color(0x6B7280);
    public static final Color BORDER = new Color(0xE5E7EB);
    public static final Color BORDER_DARK = new Color(0xD1D5DB);
    public static final Color ZEBRA = new Color(0xF9FAFB);
    public static final Color SELECT = new Color(0xDBEAFE);

    private static final String FAMILY = pickFamily();
    public static final Font BASE = new Font(FAMILY, Font.PLAIN, 13);
    public static final Font BOLD = new Font(FAMILY, Font.BOLD, 13);
    public static final Font SMALL = new Font(FAMILY, Font.PLAIN, 12);
    public static final Font TITLE = new Font(FAMILY, Font.BOLD, 16);
    public static final Font BIG = new Font(FAMILY, Font.BOLD, 20);

    public enum Kind { PRIMARY, SECONDARY, DANGER }

    private Theme() {
    }

    private static String pickFamily() {
        Set<String> have = new HashSet<>(Arrays.asList(
                GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames()));
        for (String f : new String[]{"Segoe UI", "SF Pro Text", "Helvetica Neue", "Roboto", "Noto Sans", "DejaVu Sans"}) {
            if (have.contains(f)) {
                return f;
            }
        }
        return Font.SANS_SERIF;
    }

    /** Gọi một lần sau khi đặt Look and Feel, trước khi tạo giao diện. */
    public static void install() {
        for (String k : new String[]{"Label.font", "Button.font", "TextField.font", "TextArea.font",
                "ComboBox.font", "Spinner.font", "FormattedTextField.font", "OptionPane.messageFont",
                "OptionPane.buttonFont", "ToolTip.font", "Table.font"}) {
            UIManager.put(k, BASE);
        }
        UIManager.put("TableHeader.font", BOLD);
    }

    // ---------- Thành phần cơ bản ----------

    public static JButton button(String text, Kind kind) {
        JButton b = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                boolean hot = getModel().isRollover() || getModel().isPressed();
                Color bg;
                Color fg;
                if (!isEnabled()) {
                    bg = BG;
                    fg = MUTED;
                } else if (kind == Kind.PRIMARY) {
                    bg = hot ? PRIMARY_HOVER : PRIMARY;
                    fg = Color.WHITE;
                } else if (kind == Kind.DANGER) {
                    bg = hot ? DANGER_HOVER : DANGER;
                    fg = Color.WHITE;
                } else {
                    bg = hot ? BORDER : Color.WHITE;
                    fg = TEXT;
                }
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
                if (kind == Kind.SECONDARY) {
                    g2.setColor(BORDER_DARK);
                    g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
                }
                g2.dispose();
                setForeground(fg);
                super.paintComponent(g);
            }
        };
        b.setFont(BOLD);
        b.setOpaque(false);
        b.setContentAreaFilled(false);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setRolloverEnabled(true);
        b.setBorder(new EmptyBorder(7, 14, 7, 14));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    /** Ô nhập có chữ gợi ý mờ khi trống. */
    public static JTextField field(String hint, int columns) {
        JTextField f = new JTextField(columns) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (hint != null && getText().isEmpty()) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                            RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                    g2.setColor(new Color(0x9CA3AF));
                    g2.setFont(getFont());
                    FontMetrics fm = g2.getFontMetrics();
                    Insets in = getInsets();
                    g2.drawString(hint, in.left, (getHeight() - fm.getHeight()) / 2 + fm.getAscent());
                    g2.dispose();
                }
            }
        };
        return style(f);
    }

    public static <T extends JTextField> T style(T f) {
        Border normal = BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_DARK), new EmptyBorder(5, 8, 5, 8));
        Border focus = BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(PRIMARY), new EmptyBorder(5, 8, 5, 8));
        f.setFont(BASE);
        f.setBorder(normal);
        f.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                f.setBorder(focus);
            }

            @Override
            public void focusLost(FocusEvent e) {
                f.setBorder(normal);
            }
        });
        return f;
    }

    public static JLabel label(String text, Font font, Color color) {
        JLabel l = new JLabel(text);
        l.setFont(font);
        l.setForeground(color);
        return l;
    }

    public static JLabel heading(String text) {
        return label(text, BOLD, TEXT);
    }

    /** Thẻ trắng bo góc. */
    public static JPanel card(int padding) {
        JPanel p = new JPanel(new BorderLayout(0, 10)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(CARD);
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                g2.setColor(BORDER);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                g2.dispose();
            }
        };
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(padding, padding, padding, padding));
        return p;
    }

    public static JPanel transparent(LayoutManager lm) {
        JPanel p = new JPanel(lm);
        p.setOpaque(false);
        return p;
    }

    public static JPanel page() {
        JPanel p = new JPanel(new BorderLayout(0, 10));
        p.setBackground(BG);
        p.setBorder(new EmptyBorder(12, 12, 12, 12));
        return p;
    }

    public static JScrollPane scroll(Component c) {
        JScrollPane s = new JScrollPane(c);
        s.setBorder(BorderFactory.createEmptyBorder());
        s.setViewportBorder(null);
        s.getViewport().setBackground(CARD);
        s.getVerticalScrollBar().setUnitIncrement(16);
        return s;
    }

    public static void split(JSplitPane sp) {
        sp.setBorder(null);
        sp.setOpaque(false);
        sp.setDividerSize(12);
        sp.setContinuousLayout(true);
        sp.setUI(new BasicSplitPaneUI() {
            @Override
            public BasicSplitPaneDivider createDefaultDivider() {
                return new BasicSplitPaneDivider(this) {
                    @Override
                    public void paint(Graphics g) {
                        g.setColor(BG);
                        g.fillRect(0, 0, getWidth(), getHeight());
                    }
                };
            }
        });
        sp.setBorder(null);
        sp.setDividerSize(12);
    }

    // ---------- Bảng ----------

    /** @param rightCols chỉ số cột (theo model) cần căn phải, thường là cột số/tiền. */
    public static void style(JTable t, int... rightCols) {
        Set<Integer> right = new HashSet<>();
        for (int c : rightCols) {
            right.add(c);
        }
        t.setRowHeight(30);
        t.setShowGrid(false);
        t.setIntercellSpacing(new Dimension(0, 0));
        t.setFillsViewportHeight(true);
        t.setFont(BASE);
        t.setBackground(CARD);
        t.setSelectionBackground(SELECT);
        t.setSelectionForeground(TEXT);
        t.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        t.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean sel,
                                                           boolean focus, int row, int col) {
                super.getTableCellRendererComponent(table, value, sel, false, row, col);
                setBorder(new EmptyBorder(0, 10, 0, 10));
                setForeground(TEXT);
                if (!sel) {
                    setBackground(row % 2 == 0 ? CARD : ZEBRA);
                }
                int mc = table.convertColumnIndexToModel(col);
                setHorizontalAlignment(right.contains(mc) ? SwingConstants.RIGHT : SwingConstants.LEFT);
                return this;
            }
        });

        JTableHeader h = t.getTableHeader();
        h.setReorderingAllowed(false);
        h.setPreferredSize(new Dimension(0, 34));
        h.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean sel,
                                                           boolean focus, int row, int col) {
                super.getTableCellRendererComponent(table, value, false, false, row, col);
                setFont(new Font(FAMILY, Font.BOLD, 12));
                setBackground(ZEBRA);
                setForeground(MUTED);
                setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER),
                        new EmptyBorder(0, 10, 0, 10)));
                int mc = table.convertColumnIndexToModel(col);
                setHorizontalAlignment(right.contains(mc) ? SwingConstants.RIGHT : SwingConstants.LEFT);
                return this;
            }
        });
    }

    // ---------- Tab ----------

    public static JTabbedPane tabs() {
        JTabbedPane tp = new JTabbedPane();
        tp.setUI(new TabsUI());
        tp.setFont(BOLD);
        tp.setOpaque(true);
        tp.setBackground(CARD);
        return tp;
    }

    /** Tab phẳng: chữ xám, tab đang chọn có chữ xanh và gạch chân. */
    private static final class TabsUI extends BasicTabbedPaneUI {
        @Override
        protected void installDefaults() {
            super.installDefaults();
            tabInsets = new Insets(9, 18, 9, 18);
            selectedTabPadInsets = new Insets(0, 0, 0, 0);
            tabAreaInsets = new Insets(0, 8, 0, 0);
            contentBorderInsets = new Insets(0, 0, 0, 0);
        }

        @Override
        protected void paintTabBackground(Graphics g, int tp, int idx, int x, int y, int w, int h, boolean sel) {
        }

        @Override
        protected void paintTabBorder(Graphics g, int tp, int idx, int x, int y, int w, int h, boolean sel) {
            if (sel) {
                g.setColor(PRIMARY);
                g.fillRect(x + 8, y + h - 3, w - 16, 3);
            }
        }

        @Override
        protected void paintFocusIndicator(Graphics g, int tp, Rectangle[] rects, int idx,
                                           Rectangle icon, Rectangle text, boolean sel) {
        }

        @Override
        protected void paintContentBorder(Graphics g, int tp, int sel) {
            int y = tabPane.getInsets().top + calculateTabAreaHeight(tp, runCount, maxTabHeight) - 1;
            g.setColor(BORDER);
            g.drawLine(0, y, tabPane.getWidth(), y);
        }

        @Override
        protected void paintText(Graphics g, int tp, Font font, FontMetrics fm, int idx,
                                 String title, Rectangle r, boolean sel) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2.setFont(font);
            g2.setColor(sel ? PRIMARY : MUTED);
            g2.drawString(title, r.x, r.y + fm.getAscent());
            g2.dispose();
        }
    }
}
