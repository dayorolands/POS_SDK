package com.telpo.tps550.api.printer;

public class StyleConfig {
    public Align align = Align.LEFT;
    public FontFamily fontFamily = FontFamily.DEFAULT;
    public FontSize fontSize = FontSize.F2;
    public FontStyle fontStyle = FontStyle.NORMAL;
    public int gray = 4;
    public int lineSpace = 16;
    public boolean newLine = true;

    public enum Align {
        LEFT,
        CENTER,
        RIGHT
    }

    public enum FontFamily {
        DEFAULT
    }

    public enum FontSize {
        F1,
        F2,
        F3,
        F4
    }

    public enum FontStyle {
        NORMAL,
        BOLD
    }
}
