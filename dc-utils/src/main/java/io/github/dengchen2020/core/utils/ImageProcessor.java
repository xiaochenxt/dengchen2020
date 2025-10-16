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
import java.util.ArrayList;
import java.util.Base64;
import java.util.Iterator;
import java.util.List;

/**
 * 图片处理器
 * <p>支持图片压缩、尺寸调整、Base64转换、文本水印（多位置+字号颜色可控）、图片水印（多位置+宽高可控）</p>
 *
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
     * @param inputStream 水印图片流
     * @param position    位置
     * @param width       目标宽度
     * @param height      目标高度
     * @param margin      边缘间距（非居中生效）
     * @param offsetX     居中X偏移
     * @param offsetY     居中Y偏移
     */
    private record ImageWatermarkConfig(InputStream inputStream, WatermarkPosition position, Integer width,
                                        Integer height, int margin, int offsetX, int offsetY) {

        ImageWatermarkConfig(InputStream inputStream, Integer width, Integer height, WatermarkPosition position, int margin) {
            this(inputStream, position, width, height, margin, 0, 0);
        }

        ImageWatermarkConfig(InputStream inputStream, Integer width, Integer height, int offsetX, int offsetY) {
            this(inputStream, WatermarkPosition.CENTER, width, height, 0, offsetX, offsetY);
        }

    }

    // 核心输入参数
    private InputStream stream;
    private File file;
    private BufferedImage image;

    // 功能配置参数
    private boolean compress = false;
    private float compressionQuality = 0.9f;          // 默认压缩质量（0.0-1.0）
    private boolean lossless = false;                 // 默认有损压缩（JPG）
    private static final Font DEFAULT_TEXT_WATERMARK = new Font("Arial, SimHei, Heiti SC, Ubuntu Sans", Font.BOLD, 40); //字体（字体大小默认40）
    private Font textWatermarkFont = DEFAULT_TEXT_WATERMARK; // 文本水印
    private Color textWatermarkColor = Color.RED; // 文本水印颜色
    private Integer width;                      // 主图目标宽度
    private Integer height;                     // 主图目标高度
    private Integer imageWatermarkWidth;        // 图片水印宽度
    private Integer imageWatermarkHeight;       // 图片水印高度

    // 多组水印配置
    private final List<TextWatermarkConfig> textWatermarkConfigs = new ArrayList<>();
    private final List<ImageWatermarkConfig> imageWatermarkConfigs = new ArrayList<>();

    private ImageProcessor(InputStream stream) {
        this.stream = stream;
    }

    private ImageProcessor(File file) {
        this.file = file;
    }

    private ImageProcessor(BufferedImage image) {
        this.image = image;
    }

    public static ImageProcessor inputStream(InputStream stream) {
        return new ImageProcessor(stream);
    }

    public static ImageProcessor file(File file) {
        return new ImageProcessor(file);
    }

    public static ImageProcessor image(BufferedImage image) {
        return new ImageProcessor(image);
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
     * 是否有损压缩
     * @param lossless
     * @return
     */
    public ImageProcessor lossless(boolean lossless) {
        this.lossless = lossless;
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
     * 设置后续的图片尺寸宽高
     * @param width
     * @param height
     * @return
     */
    public ImageProcessor imageWatermarkSize(int width, int height) {
        this.imageWatermarkWidth = width;
        this.imageWatermarkHeight = height;
        return this;
    }

    /**
     * 添加图片水印（支持多次调用，每次添加一组新的图片水印）
     * @param watermarkInputStream 图片水印输入流，使用{@link ByteArrayInputStream}可重用该输入流
     * @param x 图片水印居中时X轴偏移
     * @param y 图片水印居中时Y轴偏移
     * @return
     */
    public ImageProcessor imageWatermark(InputStream watermarkInputStream, int x, int y) {
        return imageWatermark(watermarkInputStream, this.imageWatermarkWidth, this.imageWatermarkHeight, x, y);
    }

    /**
     * 添加图片水印（支持多次调用，每次添加一组新的图片水印）
     * @param watermarkInputStream 图片水印输入流，使用{@link ByteArrayInputStream}可重用该输入流
     * @param x 图片水印居中时X轴偏移
     * @param y 图片水印居中时Y轴偏移
     * @return
     */
    public ImageProcessor imageWatermark(InputStream watermarkInputStream, int x, int y, Integer width, Integer height) {
        if (watermarkInputStream != null) {
            ImageWatermarkConfig config = new ImageWatermarkConfig(watermarkInputStream, width, height, x, y);
            this.imageWatermarkConfigs.add(config);
        }
        return this;
    }

    /**
     * 添加图片水印（支持多次调用，每次添加一组新的图片水印）
     * @param watermarkInputStream 图片水印输入流，使用{@link ByteArrayInputStream}可重用该输入流
     * @param position 图片水印位置
     * @param margin 图片水印与边缘间距
     * @return
     */
    public ImageProcessor imageWatermark(InputStream watermarkInputStream, WatermarkPosition position, int margin) {
        return imageWatermark(watermarkInputStream, position, margin, this.imageWatermarkWidth, this.imageWatermarkHeight);
    }

    /**
     * 添加图片水印（支持多次调用，每次添加一组新的图片水印）
     * @param watermarkInputStream 图片水印输入流，使用{@link ByteArrayInputStream}可重用该输入流
     * @param position 图片水印位置
     * @param margin 图片水印与边缘间距
     * @param width 图片水印宽度
     * @param height 图片水印高度
     * @return
     */
    public ImageProcessor imageWatermark(InputStream watermarkInputStream, WatermarkPosition position, int margin, Integer width, Integer height) {
        if (watermarkInputStream != null && position != null) {
            ImageWatermarkConfig config = new ImageWatermarkConfig(
                    watermarkInputStream, width, height, position, margin
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

    public void toStream(OutputStream outputStream) {
        try (outputStream) {
            BufferedImage sourceImage = readInputImage();
            // 主图尺寸调整
            if (width != null && height != null) sourceImage = doResize(sourceImage);
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
        }
    }

    public String toBase64() {
        try (FastByteArrayOutputStream baos = new FastByteArrayOutputStream()) {
            toStream(baos);
            return "data:image/png;base64," + Base64.getEncoder().encodeToString(baos.toByteArray());
        }
    }

    /**
     * 读取输入图片
     * @return
     * @throws IOException
     */
    private BufferedImage readInputImage() throws IOException {
        if (stream != null) {
            try {
                return ImageIO.read(stream);
            } finally {
                stream.close();
            }
        } else if (file != null) {
            return ImageIO.read(file);
        } else {
            return image;
        }
    }

    /**
     * 主图尺寸调整
     * @param sourceImage
     * @return
     */
    private BufferedImage doResize(BufferedImage sourceImage) {
        BufferedImage resizedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = resizedImage.createGraphics();
        g.drawImage(sourceImage, 0, 0, width, height, null);
        g.dispose();
        return resizedImage;
    }

    /**
     * 图片宽高调整，主要用于水印
     * @param source
     * @param targetWidth
     * @param targetHeight
     * @return
     */
    private BufferedImage resizeImage(BufferedImage source, int targetWidth, int targetHeight) {
        BufferedImage resized = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = resized.createGraphics();
        g.setComposite(AlphaComposite.Src); // 保留PNG透明通道
        g.drawImage(source, 0, 0, targetWidth, targetHeight, null);
        g.dispose();
        return resized;
    }

    /**
     * 执行多组文本水印添加（循环绘制所有添加的文本水印）
     * @param sourceImage
     * @return
     */
    private BufferedImage doAddMultiTextWatermark(BufferedImage sourceImage) {
        int imgWidth = sourceImage.getWidth();
        int imgHeight = sourceImage.getHeight();
        BufferedImage watermarkedImage = new BufferedImage(imgWidth, imgHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = (Graphics2D) watermarkedImage.getGraphics();

        // 1. 绘制原始图片
        g2d.drawImage(sourceImage, 0, 0, null);
        // 2. 配置文本样式（抗锯齿+中文支持）
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

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
        BufferedImage watermarkedImage = new BufferedImage(mainImgWidth, mainImgHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = (Graphics2D) watermarkedImage.getGraphics();

        // 1. 绘制主图
        g2d.drawImage(sourceImage, 0, 0, null);

        // 循环处理每一组图片水印
        for (ImageWatermarkConfig config : imageWatermarkConfigs) {
            // 2. 读取图片水印
            BufferedImage watermarkImg = ImageIO.read(config.inputStream);
            try {
                // 3. 调整图片水印宽高（如有配置）
                if (config.width != null && config.height != null) {
                    watermarkImg = resizeImage(watermarkImg, config.width, config.height);
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
                if (config.inputStream instanceof ByteArrayInputStream && config.inputStream.markSupported()) config.inputStream.reset();
            } finally {
                config.inputStream.close();
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
        String format = lossless ? "png" : "jpg";
        Iterator<ImageWriter> writerIt = ImageIO.getImageWritersByFormatName(format);
        if (!writerIt.hasNext()) {
            throw new IllegalArgumentException("不支持的图片格式：" + format);
        }
        ImageWriter writer = writerIt.next();

        // 配置压缩参数
        ImageWriteParam param = writer.getDefaultWriteParam();
        param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        if (lossless) param.setCompressionType("Deflate"); // PNG无损压缩
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
        ImageIO.write(image, "png", outputStream);
    }

}