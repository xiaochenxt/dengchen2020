package io.github.dengchen2020.core.utils;

import org.jspecify.annotations.NullMarked;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Base64;

/**
 * 图片工具类
 *
 * @author xiaochen
 * @since 2023/6/8
 */
@NullMarked
public abstract class ImageUtils {

    /**
     * 将图片压缩
     *
     * @param inputStream
     * @param quality
     */
    public static void compression(InputStream inputStream, float quality, boolean lossless, OutputStream outputStream) {
        try (inputStream; outputStream) {
            BufferedImage img = ImageIO.read(inputStream);
            compression(img, quality, lossless, outputStream);
        } catch (IOException e) {
            throw new RuntimeException("读取图片文件失败", e);
        }
    }

    /**
     * 压缩图片
     *
     * @param file
     * @param quality
     */
    public static void compression(File file, float quality, boolean lossless, OutputStream outputStream) {
        try (outputStream) {
            BufferedImage img = ImageIO.read(file);
            compression(img, quality, lossless, outputStream);
        } catch (IOException e) {
            throw new RuntimeException("读取图片文件失败", e);
        }
    }

    /**
     * 将图片流转Base64，会进行压缩
     *
     * @param inputStream {@link InputStream}
     * @return Base64字符串
     */
    public static String imgToBase64(InputStream inputStream, float quality, boolean lossless) {
        try (inputStream) {
            BufferedImage img = ImageIO.read(inputStream);
            return imgToBase64(img, quality, lossless);
        } catch (IOException e) {
            throw new RuntimeException("读取图片文件失败", e);
        }
    }

    /**
     * 将图片文件转Base64，会进行压缩
     *
     * @param imageFile {@link File}
     * @return Base64字符串
     */
    public static String imgToBase64(File imageFile, float quality, boolean lossless) {
        try {
            BufferedImage img = ImageIO.read(imageFile);
            return imgToBase64(img, quality, lossless);
        } catch (IOException e) {
            throw new RuntimeException("读取图片文件失败", e);
        }
    }

    /**
     * 将压缩质量设置为0和1之间的值。默认情况下仅支持单个压缩质量设置; 编写器可以提供ImageWriteParam扩展版本，提供更多控制。对于有损压缩方案，压缩质量应控制文件大小和图像质量之间的权衡 (例如，通过在编写JPEG图像时选择量化表)。对于无损方案，可以使用压缩质量来控制文件大小和执行压缩所花费的时间之间的权衡 (例如，通过优化行过滤器和在写入PNG图像时设置ZLIB压缩级别)。
     * 0.0的压缩质量设置最一般地解释为 “高压缩是重要的”，而1.0的设置最一般地解释为 “高图像质量是重要的”。
     * 如果有多个压缩类型但没有设置，则会引发IllegalStateException。
     * 默认实现检查是否支持压缩，且压缩模式为mode _explicit。如果是这样，如果getCompressionTypes() 返回null或compressionType为非null ，则设置compressionQuality实例变量。
     *
     * @param image    图片
     * @param quality  压缩质量
     * @param lossless 是否无损
     * @return Base64字符串
     */
    private static String imgToBase64(BufferedImage image, float quality, boolean lossless) {
        var baos = new ByteArrayOutputStream(256);
        compression(image, quality, lossless, baos);
        return Base64.getEncoder().encodeToString(baos.toByteArray());
    }

    /**
     * 将压缩质量设置为0和1之间的值。默认情况下仅支持单个压缩质量设置; 编写器可以提供ImageWriteParam扩展版本，提供更多控制。对于有损压缩方案，压缩质量应控制文件大小和图像质量之间的权衡 (例如，通过在编写JPEG图像时选择量化表)。对于无损方案，可以使用压缩质量来控制文件大小和执行压缩所花费的时间之间的权衡 (例如，通过优化行过滤器和在写入PNG图像时设置ZLIB压缩级别)。
     * 0.0的压缩质量设置最一般地解释为 “高压缩是重要的”，而1.0的设置最一般地解释为 “高图像质量是重要的”。
     * 如果有多个压缩类型但没有设置，则会引发IllegalStateException。
     * 默认实现检查是否支持压缩，且压缩模式为mode _explicit。如果是这样，如果getCompressionTypes() 返回null或compressionType为非null ，则设置compressionQuality实例变量。
     *
     * @param image    图片
     * @param quality  压缩质量
     * @param lossless 是否无损
     */
    private static void compression(BufferedImage image, float quality, boolean lossless, OutputStream outputStream) {
        if (quality >= 1.0) {
            quality = 0.9f;
        }
        if (quality < 0.0) {
            quality = 0.0f;
        }
        ImageWriter writer;
        if (lossless) {
            writer = ImageIO.getImageWritersByFormatName("png").next();
        } else {
            writer = ImageIO.getImageWritersByFormatName("jpg").next();
        }
        ImageWriteParam param = writer.getDefaultWriteParam();
        param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        if (lossless) {
            param.setCompressionType("Deflate");
        }
        param.setCompressionQuality(quality);
        try (ImageOutputStream ios = ImageIO.createImageOutputStream(outputStream)) {
            writer.setOutput(ios);
            writer.write(null, new IIOImage(image, null, null), param);
            writer.dispose();
        } catch (IOException e) {
            throw new RuntimeException("压缩图片失败", e);
        }
    }

}
