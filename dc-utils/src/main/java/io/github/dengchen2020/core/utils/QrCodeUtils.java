package io.github.dengchen2020.core.utils;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Hashtable;

/**
 * 二维码工具类
 *
 * @author xiaochen
 * @since 2023/7/28
 */
public abstract class QrCodeUtils {

    private static final int BLACK = 0xFF000000;//用于设置图案的颜色
    private static final int WHITE = 0xFFFFFFFF; //用于背景色

    /**
     * 生成二维码写入{@link OutputStream}
     *
     * @param text         二维码内容
     * @param width        二维码宽度
     * @param height       二维码高度
     * @param outputStream {@link OutputStream}
     */
    public static void generate(String text, int width, int height, OutputStream outputStream) {
        write(createBitMatrix(text, width, height), outputStream);
    }

    /**
     * 生成二维码写入{@link OutputStream}
     *
     * @param text         二维码内容
     * @param width        二维码宽度
     * @param height       二维码高度
     * @param logoPath     logo图所在路径
     * @param outputStream {@link OutputStream}
     */
    public static void generate(String text, int width, int height, String logoPath, OutputStream outputStream) {
        write(createBitMatrix(text, width, height), QrCodeUtils.class.getResourceAsStream(logoPath), outputStream);
    }

    /**
     * 生成二维码写入{@link OutputStream}
     *
     * @param text            二维码内容
     * @param width           二维码宽度
     * @param height          二维码高度
     * @param logoInputStream logo输入流
     * @param outputStream    {@link OutputStream}
     */
    public static void generate(String text, int width, int height, InputStream logoInputStream, OutputStream outputStream) {
        write(createBitMatrix(text, width, height), logoInputStream, outputStream);
    }

    /**
     * 将二维码写入{@link OutputStream}
     * <p>写入{@link OutputStream}后会将其关闭</p>
     *
     * @param matrix       二维码
     * @param outputStream {@link OutputStream}
     */
    private static void write(BitMatrix matrix, OutputStream outputStream) {
        try (outputStream) {
            ImageIO.write(matrixToImage(matrix), "png", outputStream);
        } catch (IOException e) {
            throw new RuntimeException("生成二维码失败", e);
        }
    }

    /**
     * 对二维码加入logo写入{@link OutputStream}
     * <p>写入{@link OutputStream}后会将其关闭</p>
     *
     * @param matrix          二维码
     * @param logoInputStream logo输入流
     * @param outputStream    {@link OutputStream}
     */
    private static void write(BitMatrix matrix, InputStream logoInputStream, OutputStream outputStream) {
        try (logoInputStream;outputStream) {
            ImageIO.write(logoMatrix(matrixToImage(matrix), logoInputStream), "png", outputStream);
        } catch (IOException e) {
            throw new RuntimeException("生成二维码失败", e);
        }
    }

    /**
     * 将二维码数据转换成图片
     *
     * @param matrix 二维码
     * @return {@link BufferedImage}
     */
    private static BufferedImage matrixToImage(BitMatrix matrix) {
        int width = matrix.getWidth();
        int height = matrix.getHeight();
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                image.setRGB(x, y, (matrix.get(x, y) ? BLACK : WHITE));
            }
        }
        return image;
    }

    /**
     * 生成二维码数据
     *
     * @param text   二维码内容
     * @param width  二维码宽度
     * @param height 二维码高度
     * @return {@link BitMatrix}
     */
    private static BitMatrix createBitMatrix(String text, int width, int height) {
        Hashtable<EncodeHintType, Object> hints = new Hashtable<>();
        // 指定纠错等级,纠错级别（L 7%、M 15%、Q 25%、H 30%）
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
        // 内容所使用字符集编码
        hints.put(EncodeHintType.CHARACTER_SET, "utf-8");
        //hints.put(EncodeHintType.MAX_SIZE, 350);//设置图片的最大值
        //hints.put(EncodeHintType.MIN_SIZE, 100);//设置图片的最小值
        hints.put(EncodeHintType.MARGIN, 0);//设置二维码边的空度，非负数
        hints.put(EncodeHintType.DATA_MATRIX_COMPACT, true);
        //生成条形码时的一些配置,此项可选
        try {
            return new MultiFormatWriter().encode(text,//要编码的内容
                    //编码类型，目前zxing支持：Aztec 2D,CODABAR 1D format,Code 39 1D,Code 93 1D ,Code 128 1D,
                    //Data Matrix 2D , EAN-8 1D,EAN-13 1D,ITF (Interleaved Two of Five) 1D,
                    //MaxiCode 2D barcode,PDF417,QR Code 2D,RSS 14,RSS EXPANDED,UPC-A 1D,UPC-E 1D,UPC/EAN extension,UPC_EAN_EXTENSION
                    BarcodeFormat.QR_CODE,
                    width, //条形码的宽度
                    height, //条形码的高度
                    hints);
        } catch (WriterException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 不设置logo
     *
     * @param matrixImage 源二维码图片
     * @return 返回带有logo的二维码图片
     * @author Administrator sangwenhao
     */
    private static BufferedImage logoMatrix(BufferedImage matrixImage) throws IOException {
        return logoMatrix(matrixImage, null);
    }

    /**
     * 设置 logo
     *
     * @param matrixImage 源二维码图片
     * @return 返回带有logo的二维码图片
     * @author Administrator sangwenhao
     */
    private static BufferedImage logoMatrix(BufferedImage matrixImage, InputStream logoInputStream) throws IOException {
        //读取二维码图片，并构建绘图对象
        Graphics2D g2 = matrixImage.createGraphics();

        int matrixWidth = matrixImage.getWidth();
        int matrixHeigh = matrixImage.getHeight();
        if (logoInputStream != null) {
            //读取Logo图片
            BufferedImage logo = ImageIO.read(logoInputStream);
            //开始绘制图片
            g2.drawImage(logo, matrixWidth / 5 * 2, matrixHeigh / 5 * 2, matrixWidth / 5, matrixHeigh / 5, null);//绘制
        }
        BasicStroke stroke = new BasicStroke(5, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
        g2.setStroke(stroke);// 设置笔画对象
        //指定弧度的圆角矩形
//        RoundRectangle2D.Float round = new RoundRectangle2D.Float((float) matrixWidth /5*2, (float) matrixHeigh /5*2, (float) matrixWidth /5, (float) matrixHeigh /5,1,1);
//        g2.setColor(Color.white);
//        g2.draw(round);// 绘制圆弧矩形

        //设置logo 有一道灰色边框
        BasicStroke stroke2 = new BasicStroke(1, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
        g2.setStroke(stroke2);// 设置笔画对象
//        RoundRectangle2D.Float round2 = new RoundRectangle2D.Float((float) matrixWidth /5*2+2, (float) matrixHeigh /5*2+2, (float) matrixWidth /5-4, (float) matrixHeigh /5-4,1,1);
//        g2.setColor(new Color(128,128,128));
//        g2.draw(round2);// 绘制圆弧矩形

        g2.dispose();
        matrixImage.flush();
        return matrixImage;
    }

}
