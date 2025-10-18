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
import java.io.*;
import java.util.HashMap;
import java.util.Map;

/**
 * 二维码生成器，支持自定义logo大小比例和颜色设置 </br>
 * 参数设置非线程安全，仅以下示例可单例使用
 * <pre>
 * {@code
 *     private final QRCodeGenerator qrCodeGenerator = QRCodeGenerator.create().logoPath("/logo.jpg");
 *
 *     @GetMapping(value = "/qrcode", produces = MediaType.IMAGE_JPEG_VALUE)
 *     public void qrcode(HttpServletResponse response) throws IOException {
 *         var text = RandomStringUtils.insecure().nextAlphanumeric(6);
 *         qrCodeGenerator.generate(response.getOutputStream(), text);
 *     }
 * }
 * </pre>
 *
 * @author xiaochen
 * @since 2025/10/16
 */
public class QRCodeGenerator {

    // 默认配置常量
    private static final int DEFAULT_PATTERN_COLOR = 0xFF000000; // 默认图案颜色：黑色
    private static final int DEFAULT_BACKGROUND_COLOR = 0xFFFFFFFF; // 默认背景色：白色
    private static final BarcodeFormat DEFAULT_FORMAT = BarcodeFormat.QR_CODE;
    private static final ErrorCorrectionLevel DEFAULT_ERROR_LEVEL = ErrorCorrectionLevel.H;
    private static final String DEFAULT_CHARSET = "utf-8";
    private static final int DEFAULT_SIZE = 140;
    private static final int DEFAULT_MARGIN = 0;
    private static final float DEFAULT_LOGO_SCALE = 0.2f;
    private static final int DEFAULT_LOGO_BORDER_COLOR = DEFAULT_BACKGROUND_COLOR;

    private String text;
    private BarcodeFormat format = DEFAULT_FORMAT;
    private int width = DEFAULT_SIZE;
    private int height = DEFAULT_SIZE;
    private byte[] logo;
    private ErrorCorrectionLevel errorLevel = DEFAULT_ERROR_LEVEL;
    private String charset = DEFAULT_CHARSET;
    private int margin = DEFAULT_MARGIN;
    private float logoScale = DEFAULT_LOGO_SCALE;
    private int patternColor = DEFAULT_PATTERN_COLOR;
    private int backgroundColor = DEFAULT_BACKGROUND_COLOR;
    private int logoBorderColor = DEFAULT_LOGO_BORDER_COLOR;

    // 私有构造函数：禁止外部直接实例化，必须通过create()获取
    private QRCodeGenerator() {}

    /**
     * 静态工厂方法：初始化生成器并设置核心内容
     * @return 生成器实例，用于链式配置
     */
    public static QRCodeGenerator create() {
        return new QRCodeGenerator();
    }

    /**
     * 静态工厂方法：初始化生成器并设置核心内容
     * @param text 二维码内容（不能为空）
     * @return 生成器实例，用于链式配置
     */
    public QRCodeGenerator text(String text) {
        if (text == null || text.isBlank()) throw new IllegalArgumentException("二维码内容不能为空");
        this.text = text;
        return this;
    }

    /**
     * 设置条形码/二维码格式（默认QR_CODE）
     */
    public QRCodeGenerator format(BarcodeFormat format) {
        this.format = format;
        return this;
    }

    /**
     * 设置正方形尺寸（宽=高）
     */
    public QRCodeGenerator size(int size) {
        if (size <= 0) return this;
        this.width = size;
        this.height = size;
        return this;
    }

    /**
     * 分别设置宽和高（非正方形场景）
     */
    public QRCodeGenerator size(int width, int height) {
        if (width <= 0 || height <= 0) return this;
        this.width = width;
        this.height = height;
        return this;
    }

    /**
     * 设置logo
     * @param logoPath 类路径下的logo资源路径
     */
    public QRCodeGenerator logoPath(String logoPath) {
        try (InputStream inputStream = getClass().getResourceAsStream(logoPath)) {
            if (inputStream == null) return this;
            this.logo = inputStream.readAllBytes();
        } catch (IOException e) {
            throw new IllegalArgumentException("logo资源读取失败"+ logoPath, e);
        }
        return this;
    }

    /**
     * 设置logo
     */
    public QRCodeGenerator logo(byte[] logoData) {
        this.logo = logoData;
        return this;
    }

    /**
     * 定义了二维码标准中规定的四种纠错等级：</br>
     * L：约 7% 的纠错能力，适用于数据量较大但对可靠性要求不高的场景 </br>
     * M：约 15% 的纠错能力，是默认的中等纠错等级 </br>
     * Q：约 25% 的纠错能力，适用于对可靠性有较高要求的场景 </br>
     * H：约 30% 的纠错能力，提供最高级别的容错，即使二维码部分被遮挡或损坏也能识别 </br>
     * 默认：H
     * @param errorLevel
     */
    public QRCodeGenerator errorLevel(ErrorCorrectionLevel errorLevel) {
        this.errorLevel = errorLevel;
        return this;
    }

    /**
     * 设置字符集（默认UTF-8）
     */
    public QRCodeGenerator charset(String charset) {
        this.charset = charset;
        return this;
    }

    /**
     * 设置边距（默认0，不能为负）
     */
    public QRCodeGenerator margin(int margin) {
        if (margin < 0) return this;
        this.margin = margin;
        return this;
    }

    /**
     * 设置logo相对于二维码的比例（0-1之间，默认0.2）
     */
    public QRCodeGenerator logoScale(float scale) {
        if (scale <= 0 || scale >= 1) return this;
        this.logoScale = scale;
        return this;
    }

    /**
     * 设置图案颜色（ARGB格式，如0xFFFF0000=红色）
     */
    public QRCodeGenerator patternColor(int color) {
        this.patternColor = color;
        return this;
    }

    /**
     * 设置图案颜色（Color对象）
     */
    public QRCodeGenerator patternColor(Color color) {
        this.patternColor = color.getRGB();
        return this;
    }

    /**
     * 设置背景颜色（ARGB格式，如0xFF00FF00=绿色）
     */
    public QRCodeGenerator backgroundColor(int color) {
        this.backgroundColor = color;
        return this;
    }

    /**
     * 设置背景颜色（Color对象）
     */
    public QRCodeGenerator backgroundColor(Color color) {
        this.backgroundColor = color.getRGB();
        return this;
    }

    /**
     * 设置logo边框颜色（ARGB格式）
     */
    public QRCodeGenerator logoBorderColor(int color) {
        this.logoBorderColor = color;
        return this;
    }

    /**
     * 设置logo边框颜色（Color对象）
     */
    public QRCodeGenerator logoBorderColor(Color color) {
        this.logoBorderColor = color.getRGB();
        return this;
    }

    /**
     * 生成二维码并写入输出流
     */
    public void generate(OutputStream outputStream) {
        generate(outputStream, text);
    }

    /**
     * 生成内容为{@code text}的二维码并写入输出流
     */
    public void generate(OutputStream outputStream, String text) {
        if (text == null) throw new IllegalArgumentException("未设置二维码内容");
        try {
            // 1. 生成二维码矩阵
            BitMatrix matrix = createBitMatrix(text);
            // 2. 矩阵转图片
            BufferedImage image = matrixToImage(matrix);
            // 3. 添加logo
            addLogo(image);
            // 4. 写入输出流
            ImageIO.write(image, "png", outputStream);
        } catch (Exception e) {
            throw new IllegalArgumentException("生成二维码失败", e);
        }
    }

    /**
     * 生成二维码数据矩阵
     */
    private BitMatrix createBitMatrix(String text) throws WriterException {
        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.ERROR_CORRECTION, errorLevel);
        hints.put(EncodeHintType.CHARACTER_SET, charset);
        hints.put(EncodeHintType.MARGIN, margin);
        hints.put(EncodeHintType.DATA_MATRIX_COMPACT, true);

        return new MultiFormatWriter().encode(text, format, width, height, hints);
    }

    /**
     * 矩阵转换为图片
     */
    private BufferedImage matrixToImage(BitMatrix matrix) {
        int width = matrix.getWidth();
        int height = matrix.getHeight();
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = image.createGraphics();

        // 填充背景
        g2.setColor(new Color(backgroundColor));
        g2.fillRect(0, 0, width, height);

        // 绘制码点
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(new Color(patternColor));
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (matrix.get(x, y)) {
                    g2.fillRect(x, y, 1, 1);
                }
            }
        }

        g2.dispose();
        return image;
    }

    /**
     * 添加logo到二维码
     */
    private void addLogo(BufferedImage image) throws IOException {
        if (logo != null) {
            try (InputStream is = new ByteArrayInputStream(logo)) {
                drawLogo(image, is);
            }
        }
    }

    /**
     * 绘制logo到二维码（计算尺寸、居中、加边框）
     */
    private void drawLogo(BufferedImage qrImage, InputStream logoIs) throws IOException {
        BufferedImage logo = ImageIO.read(logoIs);
        if (logo == null) {
            throw new IOException("无法读取logo图片（格式不支持或流为空）");
        }

        Graphics2D g2 = qrImage.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // 计算logo尺寸（按比例+保持宽高比）
        int qrWidth = qrImage.getWidth();
        int qrHeight = qrImage.getHeight();
        int logoWidth = (int) (qrWidth * logoScale);
        int logoHeight = (int) (qrHeight * logoScale);
        double logoRatio = (double) logo.getWidth() / logo.getHeight();

        // 调整尺寸以保持宽高比
        if (logoWidth / (double) logoHeight > logoRatio) {
            logoWidth = (int) (logoHeight * logoRatio);
        } else {
            logoHeight = (int) (logoWidth / logoRatio);
        }

        // 居中位置
        int x = (qrWidth - logoWidth) / 2;
        int y = (qrHeight - logoHeight) / 2;

        // 绘制logo
        g2.drawImage(logo, x, y, logoWidth, logoHeight, null);

        // 绘制边框
        BasicStroke stroke = new BasicStroke(2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
        g2.setStroke(stroke);
        g2.setColor(new Color(logoBorderColor, true));
        g2.drawRect(x, y, logoWidth, logoHeight);

        g2.dispose();
    }
}
