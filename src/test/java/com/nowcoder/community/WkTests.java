package com.nowcoder.community;

import java.io.IOException;

public class WkTests {
    public static void main(String[] args) {
        String cmd = "D:/wkhtml/wkhtmltopdf/bin/wkhtmltoimage --quality 75  https://www.nowcoder.com d:/wkhtml/work/data/wk-images/1.png";
        try {
            //ok会先执行
            Runtime.getRuntime().exec(cmd);
            System.out.println("ok.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
