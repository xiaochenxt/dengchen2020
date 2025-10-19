package io.github.dengchen2020.core.utils;

import org.springframework.util.FastByteArrayOutputStream;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;
import java.util.List;

/**
 * 图片处理器
 * <p>支持图片压缩、尺寸调整、Base64转换、文本水印（多位置+字号颜色可控）、图片水印（多位置+宽高可控）</p>
 * 参数设置非线程安全，仅以下示例可单例使用
 * <pre>
 * {@code
 *     ImageProcessor imageProcessor;
 *
 *     {
 *         try {
 *             try (InputStream inputStream = new FileInputStream("qrcode.jpg")) {
 *                 imageProcessor = ImageProcessor.create()
 *                         .resize(3200,3200)
 *                         .imageWatermarkSize(400,400)
 *                         .imageWatermarkAlpha(0.7f)
 *                         .imageWatermark(inputStream.readAllBytes(), ImageProcessor.WatermarkPosition.RIGHT_BOTTOM, 10)
 *                         .compress(true);
 *             }
 *         } catch (IOException e) {
 *             throw new RuntimeException(e);
 *         }
 *     }
 *
 *     @GetMapping("/image")
 *     public void image(HttpServletResponse response) throws IOException {
 *         File sourceImage = new File("img.jpg");
 *         imageProcessor.toStream(sourceImage, response.getOutputStream());
 *     }
 * }
 * </pre>
 * @author xiaochen
 * @since 2025/10/16
 */
public class ImageProcessor {

    // 水印位置
    public enum WatermarkPosition {
        LEFT_TOP,     // 左上角
        RIGHT_TOP,    // 右上角
        LEFT_BOTTOM,  // 左下角
        RIGHT_BOTTOM, // 右下角
        CENTER,        // 居中
        TOP_CENTER,   // 上中
        BOTTOM_CENTER, // 下中
        LEFT_CENTER,   // 左中
        RIGHT_CENTER,   // 右中
    }

    /**
     * 文本水印配置
     *
     * @param text     文本内容
     * @param font     字体
     * @param color    颜色
     * @param position 位置
     * @param margin   边缘间距（非居中生效）
     * @param offsetX  居中X偏移
     * @param offsetY  居中Y偏移
     */
    private record TextWatermarkConfig(String text, Font font, Color color, WatermarkPosition position, int margin,
                                       int offsetX, int offsetY) {

        TextWatermarkConfig(String text, Font font, Color color, WatermarkPosition position, int margin) {
            this(text, font, color, position, margin, 0, 0);
        }

        TextWatermarkConfig(String text, Font font, Color color, int offsetX, int offsetY) {
            this(text, font, color, WatermarkPosition.CENTER, 0, offsetX, offsetY);
        }

    }

    /**
     * 图片水印配置
     *
     * @param image 水印图片数据
     * @param position    位置
     * @param width       目标宽度
     * @param height      目标高度
     * @param margin      边缘间距（非居中生效）
     * @param offsetX     居中X偏移
     * @param offsetY     居中Y偏移
     */
    private record ImageWatermarkConfig(byte[] image, WatermarkPosition position, int width,
                                        int height, float alpha, int margin, int offsetX, int offsetY) {

        ImageWatermarkConfig(byte[] image, int width, int height, float alpha, WatermarkPosition position, int margin) {
            this(image, position, width, height, alpha, margin, 0, 0);
        }

        ImageWatermarkConfig(byte[] image, int width, int height, float alpha, int offsetX, int offsetY) {
            this(image, WatermarkPosition.CENTER, width, height, alpha, 0, offsetX, offsetY);
        }

    }

    // 功能配置参数
    private String outputFormat = "jpg";
    public static final Set<String> SUPPORT_FORMATS = Set.of("jpg", "png", "gif", "wbmp", "bmp", "jpeg", "tif", "tiff");
    private boolean compress = false;
    private float compressionQuality = 0.9f;          // 默认压缩质量（0.0-1.0）
    private static final Font DEFAULT_TEXT_WATERMARK_FONT = new Font("Arial, SimHei, Heiti SC, Ubuntu Sans", Font.BOLD, 20); //字体（字体大小默认20）
    private Font textWatermarkFont = DEFAULT_TEXT_WATERMARK_FONT; // 文本水印字体
    private Color textWatermarkColor = Color.RED; // 文本水印颜色
    private int width = 0;                      // 主图目标宽度
    private int height = 0;                     // 主图目标高度
    private int imageWatermarkWidth = 0;        // 图片水印宽度
    private int imageWatermarkHeight = 0;       // 图片水印高度
    private float imageWatermarkAlpha = 1.0f;   // 图片水印透明度

    // 多组水印配置
    private final List<TextWatermarkConfig> textWatermarkConfigs = new ArrayList<>();
    private final List<ImageWatermarkConfig> imageWatermarkConfigs = new ArrayList<>();

    public static ImageProcessor create() {
        return new ImageProcessor();
    }

    public ImageProcessor outputFormat(String format) {
        if (format == null) return this;
        String formatLower = format.toLowerCase();
        if (SUPPORT_FORMATS.contains(formatLower)) this.outputFormat = formatLower;
        return this;
    }

    public ImageProcessor compress(boolean compress) {
        this.compress = compress;
        return this;
    }

    /**
     * 设置压缩质量
     * @param quality
     * @return
     */
    public ImageProcessor compressionQuality(float quality) {
        this.compressionQuality = quality < 0.0f ? 0.0f : (quality > 1.0f ? 0.9f : quality);
        return this;
    }

    /**
     * 文本水印字体大小
     * @param font 字体
     * @return
     */
    public ImageProcessor textWatermarkFont(Font font) {
        this.textWatermarkFont = font;
        return this;
    }

    /**
     * 文本水印颜色
     * @param color 颜色
     * @return
     */
    public ImageProcessor textWatermarkColor(Color color) {
        this.textWatermarkColor = color;
        return this;
    }

    /**
     * 添加文本水印（支持多次调用，每次添加一组新的文本水印）
     * @param text 文本内容
     * @param position 水印位置
     * @param margin 文本水印与边缘间距
     * @return
     */
    public ImageProcessor textWatermark(String text, WatermarkPosition position, int margin) {
        return textWatermark(text, position, margin, this.textWatermarkFont, this.textWatermarkColor);
    }

    /**
     * 添加文本水印（支持多次调用，每次添加一组新的文本水印）
     * @param text 文本内容
     * @param position 水印位置
     * @param margin 文本水印与边缘间距
     * @param font 字体
     * @param color 颜色
     * @return
     */
    public ImageProcessor textWatermark(String text, WatermarkPosition position, int margin, Font font, Color color) {
        if (text != null && position != null) {
            TextWatermarkConfig config = new TextWatermarkConfig(
                    text, font, color, position, margin
            );
            this.textWatermarkConfigs.add(config);
        }
        return this;
    }

    /**
     * 添加文本水印（支持多次调用，每次添加一组新的文本水印）
     * @param text 文本内容
     * @param x 文本水印居中时X轴偏移
     * @param y 文本水印居中时Y轴偏移
     * @return
     */
    public ImageProcessor textWatermark(String text, int x, int y) {
        return textWatermark(text, x, y, this.textWatermarkFont, this.textWatermarkColor);
    }

    /**
     * 添加文本水印（支持多次调用，每次添加一组新的文本水印）
     * @param text 文本内容
     * @param x 文本水印居中时X轴偏移
     * @param y 文本水印居中时Y轴偏移
     * @param font 字体
     * @param color 颜色
     * @return
     */
    public ImageProcessor textWatermark(String text, int x, int y, Font font, Color color) {
        if (text != null) {
            TextWatermarkConfig config = new TextWatermarkConfig(
                    text, font, color, x, y
            );
            this.textWatermarkConfigs.add(config);
        }
        return this;
    }

    /**
     * 设置后续的水印图片尺寸宽高
     * @param width
     * @param height
     * @return
     */
    public ImageProcessor imageWatermarkSize(int width, int height) {
        if (width > 0) this.imageWatermarkWidth = width;
        if (height > 0) this.imageWatermarkHeight = height;
        return this;
    }

    /**
     * 设置后续的水印图片透明度
     * @param alpha 透明度 0-完全透明 1-不透明
     * @return
     */
    public ImageProcessor imageWatermarkAlpha(float alpha) {
        if (alpha >= 0 && alpha <= 1) this.imageWatermarkAlpha = alpha;
        return this;
    }

    /**
     * 添加图片水印（支持多次调用，每次添加一组新的图片水印）
     * @param image 水印图片
     * @param x 图片水印居中时X轴偏移
     * @param y 图片水印居中时Y轴偏移
     * @return
     */
    public ImageProcessor imageWatermark(byte[] image, int x, int y) {
        return imageWatermark(image, this.imageWatermarkWidth, this.imageWatermarkHeight, x, y, imageWatermarkAlpha);
    }

    /**
     * 添加图片水印（支持多次调用，每次添加一组新的图片水印）
     * @param image 水印图片
     * @param x 图片水印居中时X轴偏移
     * @param y 图片水印居中时Y轴偏移
     * @return
     */
    public ImageProcessor imageWatermark(byte[] image, int x, int y, int width, int height, float alpha) {
        if (image != null) {
            ImageWatermarkConfig config = new ImageWatermarkConfig(image, width, height, alpha, x, y);
            this.imageWatermarkConfigs.add(config);
        }
        return this;
    }

    /**
     * 添加图片水印（支持多次调用，每次添加一组新的图片水印）
     * @param image 水印图片
     * @param position 图片水印位置
     * @param margin 图片水印与边缘间距
     * @return
     */
    public ImageProcessor imageWatermark(byte[] image, WatermarkPosition position, int margin) {
        return imageWatermark(image, position, margin, this.imageWatermarkWidth, this.imageWatermarkHeight, imageWatermarkAlpha);
    }

    /**
     * 添加图片水印（支持多次调用，每次添加一组新的图片水印）
     * @param image 水印图片
     * @param position 图片水印位置
     * @param margin 图片水印与边缘间距
     * @param width 图片水印宽度
     * @param height 图片水印高度
     * @return
     */
    public ImageProcessor imageWatermark(byte[] image, WatermarkPosition position, int margin, int width, int height, float alpha) {
        if (image != null && position != null) {
            ImageWatermarkConfig config = new ImageWatermarkConfig(
                    image, width, height, alpha, position, margin
            );
            this.imageWatermarkConfigs.add(config);
        }
        return this;
    }

    /**
     * 主图尺寸调整
     * @param width 新的主图宽度
     * @param height 新的主图高度
     * @return
     */
    public ImageProcessor resize(int width, int height) {
        if (width > 0 && height > 0) {
            this.width = width;
            this.height = height;
        }
        return this;
    }

    /**
     * 处理 源图片{@code inputStream} 并将新图片数据输出到 {@code outputStream}
     * @param outputStream 输出流
     * @param inputStream 源图片输入流
     */
    public void toStream(InputStream inputStream, OutputStream outputStream) {
        BufferedImage sourceImage;
        try (inputStream) {
            sourceImage = ImageIO.read(inputStream);
        } catch (IOException e) {
            throw new IllegalArgumentException("源图片读取失败", e);
        }
        toStream(sourceImage, outputStream);
    }

    /**
     * 处理 源图片{@code file} 并将新图片数据输出到 {@code outputStream}
     * @param outputStream 输出流
     * @param file 源图片文件
     */
    public void toStream(File file, OutputStream outputStream) {
        BufferedImage sourceImage;
        try {
            sourceImage = ImageIO.read(file);
        } catch (IOException e) {
            throw new IllegalArgumentException("源图片读取失败", e);
        }
        toStream(sourceImage,  outputStream);
    }

    /**
     * 处理 源图片{@code image} 并将新图片数据输出到 {@code outputStream}
     * @param sourceImage 源图片
     * @param outputStream 输出流
     */
    public void toStream(BufferedImage sourceImage, OutputStream outputStream) {
        try (outputStream) {
            // 主图尺寸调整
            if (width > 0 && height > 0) sourceImage = doResize(sourceImage);
            // 文本水印（循环处理所有添加的文本水印）
            if (!textWatermarkConfigs.isEmpty()) sourceImage = doAddMultiTextWatermark(sourceImage);
            // 图片水印（循环处理所有添加的图片水印）
            if (!imageWatermarkConfigs.isEmpty()) sourceImage = doAddMultiImageWatermark(sourceImage);
            // 压缩输出
            if (compress) {
                doCompress(sourceImage, outputStream);
            }else {
                doDirectOutput(sourceImage, outputStream);
            }
        } catch (IOException e) {
            throw new IllegalArgumentException("图片处理失败", e);
        }finally {
            sourceImage.flush();
        }
    }

    /**
     * 处理 源图片{@code inputStream} 并返回base64编码数据
     * @param inputStream 源图片输入流
     */
    public String toBase64(InputStream inputStream) {
        try (FastByteArrayOutputStream baos = new FastByteArrayOutputStream()) {
            toStream(inputStream, baos);
            return "data:image/png;base64," + Base64.getEncoder().encodeToString(baos.toByteArray());
        }
    }

    /**
     * 处理 源图片{@code file} 并返回base64编码数据
     * @param file 源图片文件
     */
    public String toBase64(File file) {
        try (FastByteArrayOutputStream baos = new FastByteArrayOutputStream()) {
            toStream(file, baos);
            return "data:image/png;base64," + Base64.getEncoder().encodeToString(baos.toByteArray());
        }
    }

    /**
     * 处理 源图片{@code image} 并返回base64编码数据
     * @param image 源图片
     */
    public String toBase64(BufferedImage image) {
        try (FastByteArrayOutputStream baos = new FastByteArrayOutputStream()) {
            toStream(image, baos);
            return "data:image/png;base64," + Base64.getEncoder().encodeToString(baos.toByteArray());
        }
    }

    /**
     * 读取输入图片
     * @return
     * @throws IOException
     */
    private BufferedImage readInputImage(InputStream stream, File file, BufferedImage image) throws IOException {
        if (stream != null) {
            try (stream) {
                return ImageIO.read(stream);
            }
        } else if (file != null) {
            return ImageIO.read(file);
        } else {
            return image;
        }
    }

    /**
     * 分阶段调整图片尺寸（如果直接调整，会因为尺寸相差过大导致失真）
     * @param sourceImage 源图片
     * @return 缩放后的图片
     */
    private BufferedImage doResize(BufferedImage sourceImage) {
        // 初始当前尺寸为原图尺寸
        int currentWidth = sourceImage.getWidth();
        int currentHeight = sourceImage.getHeight();
        BufferedImage currentImage = sourceImage;
        // 分阶段缩小：每次缩放到当前尺寸的一半左右（直到接近目标尺寸）
        while (currentWidth > width * 1.8 && currentHeight > height * 1.8) {
            currentWidth = (int) Math.max(currentWidth / 1.8, width); // 确保不小于目标尺寸
            currentHeight = (int) Math.max(currentHeight / 1.8, height);
            currentImage = resizeStep(currentImage, currentWidth, currentHeight, 1);
        }

        // 最后一步缩放到目标尺寸
        return resizeStep(currentImage, width, height, 1);
    }

    /**
     * 单步缩放（复用原缩放逻辑）
     */
    private BufferedImage resizeStep(BufferedImage image, int w, int h, float alpha) {
        boolean arbg = alpha >= 0 && alpha < 1;
        BufferedImage resized = new BufferedImage(w, h,arbg ? BufferedImage.TYPE_INT_ARGB : image.getType());
        Graphics2D g2d = resized.createGraphics();
        // 保持原有的插值算法和配置，也可根据需要改为BICUBIC
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        if (arbg) {
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
        }else {
            g2d.setComposite(AlphaComposite.Src);
        }
        g2d.drawImage(image, 0, 0, w, h, null);
        g2d.dispose();
        return resized;
    }

    /**
     * 图片宽高调整，主要用于水印
     * @param width
     * @param height
     * @return
     */
    private BufferedImage resizeImage(BufferedImage sourceImage, int width, int height, float alpha) {
        // 初始当前尺寸为原图尺寸
        int currentWidth = sourceImage.getWidth();
        int currentHeight = sourceImage.getHeight();
        BufferedImage currentImage = sourceImage;
        // 分阶段缩小：每次缩放到当前尺寸的一半左右（直到接近目标尺寸）
        while (currentWidth > width * 1.8 && currentHeight > height * 1.8) {
            currentWidth = (int) Math.max(currentWidth / 1.8, width); // 确保不小于目标尺寸
            currentHeight = (int) Math.max(currentHeight / 1.8, height);
            currentImage = resizeStep(currentImage, currentWidth, currentHeight, 1);
        }
        // 最后一步缩放到目标尺寸
        return resizeStep(currentImage, width, height, alpha);
    }

    /**
     * 执行多组文本水印添加（循环绘制所有添加的文本水印）
     * @param sourceImage
     * @return
     */
    private BufferedImage doAddMultiTextWatermark(BufferedImage sourceImage) {
        int imgWidth = sourceImage.getWidth();
        int imgHeight = sourceImage.getHeight();
        BufferedImage watermarkedImage = new BufferedImage(imgWidth, imgHeight, sourceImage.getType());
        Graphics2D g2d = (Graphics2D) watermarkedImage.getGraphics();

        // 1. 绘制原始图片
        g2d.drawImage(sourceImage, 0, 0, null);
        // 2. 配置文本样式（抗锯齿+中文支持）
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR); // 插值算法（提升缩放清晰度）

        // 循环处理每一组文本水印
        for (TextWatermarkConfig config : textWatermarkConfigs) {
            // 设置当前文本的字体和颜色
            g2d.setFont(config.font);
            g2d.setColor(config.color);
            // 3. 计算文本宽高（避免位置偏移）
            FontMetrics fontMetrics = g2d.getFontMetrics(config.font);
            int textWidth = fontMetrics.stringWidth(config.text);
            int textHeight = fontMetrics.getHeight();

            // 4. 按选择的位置计算坐标
            int textX;
            int textY = switch (config.position) {
                case LEFT_TOP -> {
                    textX = config.margin;
                    yield config.margin + textHeight;
                }
                case RIGHT_TOP -> {
                    textX = imgWidth - textWidth - config.margin;
                    yield config.margin + textHeight;
                }
                case LEFT_BOTTOM -> {
                    textX = config.margin;
                    yield imgHeight - config.margin;
                }
                case RIGHT_BOTTOM -> {
                    textX = imgWidth - textWidth - config.margin;
                    yield imgHeight - config.margin;
                }
                case CENTER -> {
                    textX = imgWidth / 2 - config.offsetX;
                    yield imgHeight / 2 - config.offsetY + textHeight;
                }
                case TOP_CENTER -> {
                    // 上中：X=图片宽/2 - 文本宽/2，Y=边距+文本高
                    textX = (imgWidth - textWidth) / 2;
                    yield config.margin + textHeight;
                }
                case BOTTOM_CENTER -> {
                    // 下中：X=图片宽/2 - 文本宽/2，Y=图片高-边距
                    textX = (imgWidth - textWidth) / 2;
                    yield imgHeight - config.margin;
                }
                case LEFT_CENTER -> {
                    // 左中：X=边距，Y=图片高/2 + 文本高/2 - 边距
                    textX = config.margin;
                    yield (imgHeight + textHeight) / 2 - config.margin;
                }
                case RIGHT_CENTER -> {
                    // 右中：X=图片宽-文本宽-边距，Y=图片高/2 + 文本高/2 - 边距
                    textX = imgWidth - textWidth - config.margin;
                    yield (imgHeight + textHeight) / 2 - config.margin;
                }
            };

            // 5. 绘制当前文本水印
            g2d.drawString(config.text, textX, textY);
        }

        g2d.dispose();
        return watermarkedImage;
    }

    /**
     * 执行多组图片水印添加（循环绘制所有添加的图片水印）
     * @param sourceImage
     * @return
     * @throws IOException
     */
    private BufferedImage doAddMultiImageWatermark(BufferedImage sourceImage) throws IOException {
        int mainImgWidth = sourceImage.getWidth();
        int mainImgHeight = sourceImage.getHeight();
        BufferedImage watermarkedImage = new BufferedImage(mainImgWidth, mainImgHeight, sourceImage.getType());
        Graphics2D g2d = (Graphics2D) watermarkedImage.getGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR); // 插值算法（提升缩放清晰度）

        // 1. 绘制主图
        g2d.drawImage(sourceImage, 0, 0, null);

        // 循环处理每一组图片水印
        for (ImageWatermarkConfig config : imageWatermarkConfigs) {
            // 2. 读取图片水印
            BufferedImage watermarkImg;
            try (ByteArrayInputStream imageInputStream = new ByteArrayInputStream(config.image)) {
                watermarkImg = ImageIO.read(imageInputStream);
            }
            try {
                // 3. 调整图片水印宽高（如有配置）
                if (config.width > 0 && config.height > 0) {
                    watermarkImg = resizeImage(watermarkImg, config.width, config.height, config.alpha);
                }
                int watermarkWidth = watermarkImg.getWidth();
                int watermarkHeight = watermarkImg.getHeight();

                // 4. 按选择的位置计算水印坐标
                int watermarkX;
                int watermarkY = switch (config.position) {
                    case LEFT_TOP -> {
                        watermarkX = config.margin;
                        yield config.margin;
                    }
                    case RIGHT_TOP -> {
                        watermarkX = mainImgWidth - watermarkWidth - config.margin;
                        yield config.margin;
                    }
                    case LEFT_BOTTOM -> {
                        watermarkX = config.margin;
                        yield mainImgHeight - watermarkHeight - config.margin;
                    }
                    case RIGHT_BOTTOM -> {
                        watermarkX = mainImgWidth - watermarkWidth - config.margin;
                        yield mainImgHeight - watermarkHeight - config.margin;
                    }
                    case CENTER -> {
                        watermarkX = mainImgWidth / 2 - config.offsetX;
                        yield mainImgHeight / 2 - config.offsetY;
                    }
                    case TOP_CENTER -> {
                        // 上中：X=图片宽/2 - 水印宽/2，Y=边距
                        watermarkX = (mainImgWidth - watermarkWidth) / 2;
                        yield config.margin;
                    }
                    case BOTTOM_CENTER -> {
                        // 下中：X=图片宽/2 - 水印宽/2，Y=图片高-水印高-边距
                        watermarkX = (mainImgWidth - watermarkWidth) / 2;
                        yield mainImgHeight - watermarkHeight - config.margin;
                    }
                    case LEFT_CENTER -> {
                        // 左中：X=边距，Y=图片高/2 - 水印高/2
                        watermarkX = config.margin;
                        yield (mainImgHeight - watermarkHeight) / 2;
                    }
                    case RIGHT_CENTER -> {
                        // 右中：X=图片宽-水印宽-边距，Y=图片高/2 - 水印高/2
                        watermarkX = mainImgWidth - watermarkWidth - config.margin;
                        yield (mainImgHeight - watermarkHeight) / 2;
                    }
                };

                // 5. 绘制当前图片水印
                g2d.drawImage(watermarkImg, watermarkX, watermarkY, watermarkWidth, watermarkHeight, null);
            } finally {
                watermarkImg.flush();
            }
        }

        g2d.dispose();
        return watermarkedImage;
    }

    /**
     * 执行图片压缩
     * @param image
     * @param outputStream
     * @throws IOException
     */
    private void doCompress(BufferedImage image, OutputStream outputStream) throws IOException {
        Iterator<ImageWriter> writerIt = ImageIO.getImageWritersByFormatName(outputFormat);
        if (!writerIt.hasNext()) throw new IllegalArgumentException("不支持的图片格式：" + outputFormat);
        ImageWriter writer = writerIt.next();

        // 配置压缩参数
        ImageWriteParam param = writer.getDefaultWriteParam();
        param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        switch (outputFormat) {
            case "jpg","jpeg" -> param.setCompressionType("JPEG");
            case "gif" -> param.setCompressionType("LZW");
            case "bmp" -> param.setCompressionType("BI_RGB");
            case "tif","tiff" -> param.setCompressionType("CCITT RLE");
            default -> param.setCompressionType("Deflate");
        }
        param.setCompressionQuality(compressionQuality);

        // 写入输出流
        try (ImageOutputStream ios = ImageIO.createImageOutputStream(outputStream)) {
            writer.setOutput(ios);
            writer.write(null, new IIOImage(image, null, null), param);
        } finally {
            writer.dispose();
        }
    }

    /**
     * 直接输出图片（无压缩，保留原图格式和质量）
     * @param image 处理后的图片
     * @param outputStream 输出流
     * @throws IOException
     */
    private void doDirectOutput(BufferedImage image, OutputStream outputStream) throws IOException {
        ImageIO.write(image, outputFormat, outputStream);
    }

}