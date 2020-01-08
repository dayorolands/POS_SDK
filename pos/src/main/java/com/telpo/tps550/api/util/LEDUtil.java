package com.telpo.tps550.api.util;

import android.content.Context;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LEDUtil {
    private static final String ASC16 = "asc16";
    private static final String ENCODE = "GB2312";
    private static final String ZK16 = "hzk16";
    private int all_16 = 16;
    private int all_16_32 = 16;
    private int all_2_4 = 2;
    private int all_32_128 = 32;
    private byte[][] arr;
    byte[] bitmapC51 = new byte[512];
    private int font_height = 16;
    private int font_width = 8;
    Context mContext;

    public LEDUtil(Context context) {
        this.mContext = context;
    }

    private byte[][] resolveString(String str) {
        if (str.charAt(0) < 128) {
            this.arr = (byte[][]) Array.newInstance(Byte.TYPE, new int[]{this.font_height, this.font_width});
            byte[] data = read_a(str.charAt(0));
            int byteCount = 0;
            for (int line = 0; line < 16; line++) {
                int lCount = 0;
                for (int k = 0; k < 1; k++) {
                    for (int j = 0; j < 8; j++) {
                        if (((data[byteCount] >> (7 - j)) & 1) == 1) {
                            this.arr[line][lCount] = 1;
                        } else {
                            this.arr[line][lCount] = 0;
                        }
                        lCount++;
                    }
                    byteCount++;
                }
            }
        } else {
            this.arr = (byte[][]) Array.newInstance(Byte.TYPE, new int[]{this.all_16_32, this.all_16_32});
            int[] code = getByteCode(str.substring(0, 1));
            byte[] data2 = read(code[0], code[1]);
            int byteCount2 = 0;
            for (int line2 = 0; line2 < this.all_16_32; line2++) {
                int lCount2 = 0;
                for (int k2 = 0; k2 < this.all_2_4; k2++) {
                    for (int j2 = 0; j2 < 8; j2++) {
                        if (((data2[byteCount2] >> (7 - j2)) & 1) == 1) {
                            this.arr[line2][lCount2] = 1;
                        } else {
                            this.arr[line2][lCount2] = 0;
                        }
                        lCount2++;
                    }
                    byteCount2++;
                }
            }
        }
        return this.arr;
    }

    private byte[] read_a(char char_num) {
        byte[] data = null;
        int ascii = char_num;
        try {
            data = new byte[this.all_16];
            InputStream inputStream = this.mContext.getResources().getAssets().open(ASC16);
            inputStream.skip((long) (ascii * 16));
            inputStream.read(data, 0, this.all_16);
            inputStream.close();
            byte[] bArr = data;
            return data;
        } catch (IOException e) {
            e.printStackTrace();
            byte[] bArr2 = data;
            return data;
        }
    }

    private byte[] read(int areaCode, int posCode) {
        byte[] data = null;
        int area = areaCode - 160;
        int pos = posCode - 160;
        try {
            InputStream in = this.mContext.getResources().getAssets().open(ZK16);
            in.skip((long) (this.all_32_128 * ((((area - 1) * 94) + pos) - 1)));
            data = new byte[this.all_32_128];
            in.read(data, 0, this.all_32_128);
            in.close();
            return data;
        } catch (Exception e) {
            return data;
        }
    }

    private int[] getByteCode(String str) {
        int[] byteCode = new int[2];
        try {
            byte[] data = str.getBytes(ENCODE);
            byteCode[0] = data[0] < 0 ? data[0] + 256 : data[0];
            byteCode[1] = data[1] < 0 ? data[1] + 256 : data[1];
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return byteCode;
    }

    public byte[] getTextC51(String lcdText) {
        byte[][] num;
        int rowSize = 0;
        Pattern p = Pattern.compile("[一-龥]");
        int[][] pixel = (int[][]) Array.newInstance(Integer.TYPE, new int[]{32, 128});
        boolean secondBlock = false;
        for (int i = 0; i < lcdText.length(); i++) {
            String checkText = new StringBuilder(String.valueOf(lcdText.charAt(i))).toString();
            Matcher m = p.matcher(checkText);
            if (checkText.equals(ShellUtils.COMMAND_LINE_END) || (rowSize == 120 && (m.matches() || checkText.equals("√") || checkText.equals("π") || checkText.equals("÷") || checkText.equals("×") || checkText.equals("°") || checkText.equals("℅") || checkText.equals("¶") || checkText.equals("∆") || checkText.equals("©") || checkText.equals("®") || checkText.equals("™")))) {
                secondBlock = true;
                rowSize = 0;
            }
            if (checkText.equals("f")) {
                num = getSpecialChar("f");
            } else if (checkText.equals("j")) {
                num = getSpecialChar("j");
            } else if (checkText.equals("•")) {
                num = getSpecialSign("•");
            } else if (checkText.equals("∆")) {
                num = getSpecialSign("∆");
            } else if (checkText.equals("¥")) {
                num = getSpecialSign("¥");
            } else if (checkText.equals("¢")) {
                num = getSpecialSign("¢");
            } else if (checkText.equals("€")) {
                num = getSpecialSign("€");
            } else if (checkText.equals("£")) {
                num = getSpecialSign("£");
            } else {
                num = resolveString(checkText);
            }
            if (!secondBlock) {
                for (int a = 0; a < num.length; a++) {
                    for (int b = 0; b < num[a].length; b++) {
                        if (num[a][b] == 0) {
                            pixel[a][b + rowSize] = 0;
                        } else {
                            pixel[a][b + rowSize] = 1;
                        }
                    }
                }
            } else if (secondBlock) {
                for (int a2 = 0; a2 < num.length; a2++) {
                    for (int b2 = 0; b2 < num[a2].length; b2++) {
                        if (num[a2][b2] == 0) {
                            pixel[a2 + 16][b2 + rowSize] = 0;
                        } else {
                            pixel[a2 + 16][b2 + rowSize] = 1;
                        }
                    }
                }
            }
            if (m.matches()) {
                rowSize += 16;
            } else if (checkText.equals("√") || checkText.equals("π") || checkText.equals("÷") || checkText.equals("×") || checkText.equals("°") || checkText.equals("℅") || checkText.equals("¶") || checkText.equals("∆") || checkText.equals("©") || checkText.equals("®") || checkText.equals("™")) {
                rowSize += 16;
            } else {
                rowSize += 8;
            }
            if (rowSize == 128) {
                rowSize = 0;
                secondBlock = true;
            }
        }
        StringBuffer pageOne = new StringBuffer();
        byte[] bitmapC51One = new byte[128];
        for (int y = 0; y < 128; y++) {
            pageOne.setLength(0);
            for (int x = 7; x > -1; x--) {
                pageOne.append(pixel[x][y]);
            }
            bitmapC51One[y] = BToH(pageOne.toString());
        }
        StringBuffer pageTwo = new StringBuffer();
        byte[] bitmapC51Two = new byte[128];
        for (int y2 = 0; y2 < 128; y2++) {
            pageTwo.setLength(0);
            for (int x2 = 15; x2 > 7; x2--) {
                pageTwo.append(pixel[x2][y2]);
            }
            bitmapC51Two[y2] = BToH(pageTwo.toString());
        }
        StringBuffer pageThree = new StringBuffer();
        byte[] bitmapC51Three = new byte[128];
        for (int y3 = 0; y3 < 128; y3++) {
            pageThree.setLength(0);
            for (int x3 = 23; x3 > 15; x3--) {
                pageThree.append(pixel[x3][y3]);
            }
            bitmapC51Three[y3] = BToH(pageThree.toString());
        }
        StringBuffer pageFour = new StringBuffer();
        byte[] bitmapC51Four = new byte[128];
        for (int y4 = 0; y4 < 128; y4++) {
            pageFour.setLength(0);
            for (int x4 = 31; x4 > 23; x4--) {
                pageFour.append(pixel[x4][y4]);
            }
            bitmapC51Four[y4] = BToH(pageFour.toString());
        }
        System.arraycopy(bitmapC51One, 0, this.bitmapC51, 0, bitmapC51One.length);
        System.arraycopy(bitmapC51Two, 0, this.bitmapC51, 128, bitmapC51Two.length);
        System.arraycopy(bitmapC51Three, 0, this.bitmapC51, 256, bitmapC51Three.length);
        System.arraycopy(bitmapC51Four, 0, this.bitmapC51, 384, bitmapC51Four.length);
        return this.bitmapC51;
    }

    private byte BToH(String a) {
        String b = Integer.toHexString(Integer.valueOf(toD(a, 2)).intValue());
        if (b.length() == 1) {
            b = "0" + b;
        }
        return StringUtil.toBytes(b)[0];
    }

    private String toD(String a, int b) {
        int r = 0;
        for (int i = 0; i < a.length(); i++) {
            r = (int) (((double) r) + (((double) formatting(a.substring(i, i + 1))) * Math.pow((double) b, (double) ((a.length() - i) - 1))));
        }
        return String.valueOf(r);
    }

    private int formatting(String a) {
        int i = 0;
        for (int u = 0; u < 10; u++) {
            if (a.equals(String.valueOf(u))) {
                i = u;
            }
        }
        if (a.equals("a")) {
            i = 10;
        }
        if (a.equals("b")) {
            i = 11;
        }
        if (a.equals("c")) {
            i = 12;
        }
        if (a.equals("d")) {
            i = 13;
        }
        if (a.equals("e")) {
            i = 14;
        }
        if (a.equals("f")) {
            return 15;
        }
        return i;
    }

    private byte[][] getSpecialChar(String character) {
        int[][] blackSign = (int[][]) Array.newInstance(Integer.TYPE, new int[]{16, 8});
        byte[][] num_char = resolveString(character);
        for (int i = 0; i < num_char.length; i++) {
            for (int j = 0; j < num_char[i].length; j++) {
                if (num_char[i][j] == 1) {
                    num_char[i][j] = 0;
                    if (character.equals("f")) {
                        blackSign[i][j + 2] = 1;
                    } else if (character.equals("j")) {
                        blackSign[i][j - 1] = 1;
                    }
                }
            }
        }
        for (int i2 = 0; i2 < blackSign.length; i2++) {
            for (int j2 = 0; j2 < blackSign[i2].length; j2++) {
                if (blackSign[i2][j2] == 1) {
                    num_char[i2][j2] = 1;
                }
            }
        }
        return num_char;
    }

    private byte[][] getSpecialSign(String character) {
        if (character.equals("•")) {
            byte[][] num_sign = (byte[][]) Array.newInstance(Byte.TYPE, new int[]{16, 8});
            byte[] bArr = num_sign[5];
            byte[] bArr2 = num_sign[5];
            byte[] bArr3 = num_sign[5];
            byte[] bArr4 = num_sign[6];
            byte[] bArr5 = num_sign[6];
            byte[] bArr6 = num_sign[6];
            byte[] bArr7 = num_sign[6];
            byte[] bArr8 = num_sign[6];
            byte[] bArr9 = num_sign[7];
            byte[] bArr10 = num_sign[7];
            byte[] bArr11 = num_sign[7];
            byte[] bArr12 = num_sign[7];
            byte[] bArr13 = num_sign[7];
            byte[] bArr14 = num_sign[8];
            byte[] bArr15 = num_sign[8];
            byte[] bArr16 = num_sign[8];
            byte[] bArr17 = num_sign[8];
            byte[] bArr18 = num_sign[8];
            byte[] bArr19 = num_sign[9];
            byte[] bArr20 = num_sign[9];
            num_sign[9][4] = 1;
            bArr20[3] = 1;
            bArr19[2] = 1;
            bArr18[5] = 1;
            bArr17[4] = 1;
            bArr16[3] = 1;
            bArr15[2] = 1;
            bArr14[1] = 1;
            bArr13[5] = 1;
            bArr12[4] = 1;
            bArr11[3] = 1;
            bArr10[2] = 1;
            bArr9[1] = 1;
            bArr8[5] = 1;
            bArr7[4] = 1;
            bArr6[3] = 1;
            bArr5[2] = 1;
            bArr4[1] = 1;
            bArr3[4] = 1;
            bArr2[3] = 1;
            bArr[2] = 1;
            return num_sign;
        } else if (character.equals("∆")) {
            byte[][] num_sign2 = (byte[][]) Array.newInstance(Byte.TYPE, new int[]{16, 16});
            byte[] bArr21 = num_sign2[3];
            byte[] bArr22 = num_sign2[4];
            byte[] bArr23 = num_sign2[4];
            byte[] bArr24 = num_sign2[5];
            byte[] bArr25 = num_sign2[5];
            byte[] bArr26 = num_sign2[6];
            byte[] bArr27 = num_sign2[6];
            byte[] bArr28 = num_sign2[7];
            byte[] bArr29 = num_sign2[7];
            byte[] bArr30 = num_sign2[8];
            byte[] bArr31 = num_sign2[8];
            byte[] bArr32 = num_sign2[9];
            num_sign2[9][13] = 1;
            bArr32[1] = 1;
            bArr31[12] = 1;
            bArr30[2] = 1;
            bArr29[11] = 1;
            bArr28[3] = 1;
            bArr27[10] = 1;
            bArr26[4] = 1;
            bArr25[9] = 1;
            bArr24[5] = 1;
            bArr23[8] = 1;
            bArr22[6] = 1;
            bArr21[7] = 1;
            for (int y = 0; y < 15; y++) {
                num_sign2[10][y] = 1;
            }
            return num_sign2;
        } else if (character.equals("¥")) {
            byte[][] num_sign3 = resolveString("Y");
            for (int y2 = 2; y2 < 6; y2++) {
                num_sign3[7][y2] = 1;
            }
            for (int y3 = 2; y3 < 6; y3++) {
                num_sign3[9][y3] = 1;
            }
            byte[] bArr33 = num_sign3[11];
            num_sign3[11][5] = 0;
            bArr33[2] = 0;
            byte[] bArr34 = num_sign3[6];
            num_sign3[6][5] = 0;
            bArr34[2] = 0;
            return num_sign3;
        } else if (character.equals("¢")) {
            byte[][] num_sign4 = resolveString("c");
            for (int y4 = 2; y4 < 4; y4++) {
                num_sign4[3][y4] = 1;
                num_sign4[4][y4] = 1;
                num_sign4[12][y4] = 1;
                num_sign4[13][y4] = 1;
            }
            return num_sign4;
        } else if (character.equals("€")) {
            byte[][] num_sign5 = (byte[][]) Array.newInstance(Byte.TYPE, new int[]{16, 8});
            for (int x = 2; x < 11; x++) {
                num_sign5[x][1] = 1;
                num_sign5[x][2] = 1;
            }
            byte[] bArr35 = num_sign5[1];
            byte[] bArr36 = num_sign5[1];
            num_sign5[1][5] = 1;
            bArr36[4] = 1;
            bArr35[3] = 1;
            byte[] bArr37 = num_sign5[11];
            byte[] bArr38 = num_sign5[11];
            num_sign5[11][5] = 1;
            bArr38[4] = 1;
            bArr37[3] = 1;
            byte[] bArr39 = num_sign5[5];
            byte[] bArr40 = num_sign5[5];
            num_sign5[5][4] = 1;
            bArr40[3] = 1;
            bArr39[0] = 1;
            byte[] bArr41 = num_sign5[7];
            byte[] bArr42 = num_sign5[7];
            num_sign5[7][4] = 1;
            bArr42[3] = 1;
            bArr41[0] = 1;
            return num_sign5;
        } else if (!character.equals("£")) {
            return null;
        } else {
            byte[][] num_sign6 = (byte[][]) Array.newInstance(Byte.TYPE, new int[]{16, 8});
            for (int x2 = 2; x2 < 10; x2++) {
                num_sign6[x2][1] = 1;
                num_sign6[x2][2] = 1;
            }
            byte[] bArr43 = num_sign6[1];
            num_sign6[1][4] = 1;
            bArr43[3] = 1;
            num_sign6[2][5] = 1;
            byte[] bArr44 = num_sign6[6];
            byte[] bArr45 = num_sign6[6];
            num_sign6[6][4] = 1;
            bArr45[3] = 1;
            bArr44[0] = 1;
            num_sign6[10][1] = 1;
            for (int y5 = 0; y5 < 6; y5++) {
                num_sign6[11][y5] = 1;
            }
            return num_sign6;
        }
    }
}
