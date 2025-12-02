package io.github.dengchen2020.core.utils.s3;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.DisposableBean;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.time.Duration;
import java.util.Objects;
import java.util.function.UnaryOperator;

/**
 * S3存储服务
 * @author xiaochen
 * @since 2025/12/1
 */
@NullMarked
public class S3Service implements DisposableBean {

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;
    private final String apiEndpointCname;

    public S3Client getS3Client() {
        return s3Client;
    }

    public S3Presigner getS3Presigner() {
        return s3Presigner;
    }

    public String getApiEndpointCname() {
        return apiEndpointCname;
    }

    /**
     * @param apiEndpoint 例如：<a href="https://s3.regru.cloud">api端点</a>
     * @param accessKeyId 密钥id
     * @param secretKey 密钥
     * @param region 地域
     * @param apiEndpointCname 访问域名，例如配置了cdn，则此处填写cdn域名
     */
    public S3Service(String apiEndpoint, String accessKeyId, String secretKey, Region region,@Nullable String apiEndpointCname) {
        this(apiEndpoint, StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKeyId, secretKey)), region, apiEndpointCname);
    }

    /**
     * @param apiEndpoint 例如：<a href="https://s3.regru.cloud">api端点</a>
     * @param credentialsProvider 凭证提供者
     * @param region 地域
     */
    public S3Service(String apiEndpoint, AwsCredentialsProvider credentialsProvider, Region region,@Nullable String apiEndpointCname) {
        this.apiEndpointCname = Objects.requireNonNullElse(apiEndpointCname, apiEndpoint);
        var endpoint = URI.create(apiEndpoint);
        var s3ClientBuilder = S3Client.builder()
                .credentialsProvider(credentialsProvider)
                .endpointOverride(endpoint)
                .region(region);
        this.s3Client = s3ClientBuilder.build();
        var s3PresignerBuilder = S3Presigner.builder()
                .credentialsProvider(credentialsProvider)
                .endpointOverride(endpoint)
                .region(region);
        this.s3Presigner = s3PresignerBuilder.build();
    }



    /**
     * 设置跨域 </br>
     * 调用示例：
     * <pre>
     * {@code
     * applyCors("bucketName", CORSRule.builder()
     * // 允许的前端域名（生产替换为实际域名，多个域名用Arrays.asList）
     * .allowedOrigins("*")
     * // 允许的HTTP方法（PUT为预签名上传核心方法，GET/HEAD为辅助）
     * .allowedMethods(HttpMethod.PUT.name(), HttpMethod.GET.name(), HttpMethod.HEAD.name())
     * // 允许的请求头（*表示所有，生产可指定具体头如Content-Type）
     * .allowedHeaders("*")
     * // 预检请求缓存时间（3000秒=50分钟）
     * .maxAgeSeconds(3000)
     * .build())
     * }
     * </pre>
     *
     * @param bucketName 存储桶名称
     * @return
     */
    public PutBucketCorsResponse applyCors(String bucketName, CORSRule... corsRules) {
        return s3Client.putBucketCors(PutBucketCorsRequest.builder()
                .bucket(bucketName)
                .corsConfiguration(CORSConfiguration.builder()
                        .corsRules(corsRules)
                        .build())
                .build());
    }

    /**
     * 查询跨域配置 </br>
     *
     * @param bucketName 存储桶名称
     * @return
     */
    public GetBucketCorsResponse getCors(String bucketName) {
        return s3Client.getBucketCors(GetBucketCorsRequest.builder()
                .bucket(bucketName)
                .build());
    }

    /**
     * 上传文件
     * @param bucketName 存储桶名称
     * @param key 文件名
     * @param file 文件
     * @return 上传后的文件url
     */
    public String uploadFile(String bucketName, String key, File file) {
        return uploadFile(bucketName, key, RequestBody.fromFile(file), null, null);
    }

    /**
     * 上传文件
     * @param bucketName 存储桶名称
     * @param key 文件名
     * @param inputStream 文件流
     * @param contentLength 文件长度
     * @param contentType 文件类型
     * @return 文件访问url
     */
    public String uploadFile(String bucketName, String key, InputStream inputStream, long contentLength,@Nullable String contentType) {
        return uploadFile(bucketName, key, RequestBody.fromInputStream(inputStream, contentLength), contentType, null);
    }

    /**
     * 上传文件
     * @param bucketName 存储桶名称
     * @param key 文件名
     * @param inputStream 文件流
     * @param contentLength 文件长度
     * @param contentType 文件类型
     * @param requestCustomizer 请求定制配置
     * @return 文件访问url
     */
    public String uploadFile(String bucketName, String key, InputStream inputStream, long contentLength,@Nullable String contentType,@Nullable UnaryOperator<PutObjectRequest.Builder> requestCustomizer) {
        return uploadFile(bucketName, key, RequestBody.fromInputStream(inputStream, contentLength), contentType, requestCustomizer);
    }

    /**
     * 上传文件
     * @param bucketName 存储桶名称
     * @param key 文件名
     * @param contentType 文件类型
     * @param requestBody 请求体
     * @param requestCustomizer 请求定制配置
     * @return 文件访问url
     */
    public String uploadFile(String bucketName, String key, RequestBody requestBody,@Nullable String contentType,@Nullable UnaryOperator<PutObjectRequest.Builder> requestCustomizer) {
        var putObjectRequestBuilder = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .contentType(contentType);
        if (contentType != null) putObjectRequestBuilder.contentType(contentType);
        PutObjectRequest putObjectRequest;
        if (requestCustomizer != null) {
            putObjectRequest = requestCustomizer.apply(putObjectRequestBuilder).build();
        }else {
            putObjectRequest = putObjectRequestBuilder.build();
        }
        s3Client.putObject(putObjectRequest, requestBody);
        return apiEndpointCname + "/" + bucketName + "/" + key;
    }

    /**
     * 下载文件 </br>
     * 示例
     * <pre>
     * {@code
     * try (var res = s3Service.downloadFile("dns", "img/a.png")) {
     *     res.transferTo(new FileOutputStream("a.png"));
     * } catch (IOException e) {
     *     throw new RuntimeException(e);
     * }
     * }
     * }
     * </pre>
     * @param bucketName 存储桶名称
     * @param key 文件名
     * @return 文件流
     */
    public ResponseInputStream<GetObjectResponse> downloadFile(String bucketName, String key) {
        return downloadFile(bucketName, key, null);
    }

    /**
     * 下载文件 </br>
     * 示例
     * <pre>
     * {@code
     * try (var res = s3Service.downloadFile("dns", "img/a.png"), null) {
     *     res.transferTo(new FileOutputStream("a.png"));
     * } catch (IOException e) {
     *     throw new RuntimeException(e);
     * }
     * }
     * }
     * </pre>
     * @param bucketName 存储桶名称
     * @param key 文件名
     * @param requestCustomizer 请求定制配置
     * @return 文件流
     */
    public ResponseInputStream<GetObjectResponse> downloadFile(String bucketName, String key, @Nullable UnaryOperator<GetObjectRequest.Builder> requestCustomizer) {
        var getObjectRequestBuilder = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(key);
        GetObjectRequest getObjectRequest;
        if (requestCustomizer != null) {
            getObjectRequest = requestCustomizer.apply(getObjectRequestBuilder).build();
        } else {
            getObjectRequest = getObjectRequestBuilder.build();
        }
        return s3Client.getObject(getObjectRequest);
    }

    /**
     * 生成预签名上传url
     * @param bucketName 存储桶名称
     * @param key 文件名
     * @param duration 签名有效期
     * @return 预签名上传链接
     */
    public S3PresignedUploadUrlInfo generatePresignedUploadUrl(String bucketName, String key, Duration duration) {
        return generatePresignedUploadUrl(bucketName, key, duration, null);
    }

    /**
     * 生成预签名上传url
     * @param bucketName 存储桶名称
     * @param key 文件名
     * @param duration 签名有效期
     * @param requestCustomizer 请求定制配置
     * @return 预签名上传链接
     */
    public S3PresignedUploadUrlInfo generatePresignedUploadUrl(String bucketName, String key, Duration duration,@Nullable UnaryOperator<PutObjectRequest.Builder> requestCustomizer) {
        var putObjectRequestBuilder = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key);
        PutObjectRequest putObjectRequest;
        if (requestCustomizer != null) {
            putObjectRequest = requestCustomizer.apply(putObjectRequestBuilder).build();
        } else {
            putObjectRequest = putObjectRequestBuilder.build();
        }
        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                .signatureDuration(duration)
                .putObjectRequest(putObjectRequest)
                .build();
        return new S3PresignedUploadUrlInfo(apiEndpointCname + "/"+bucketName+"/" + key, s3Presigner.presignPutObject(presignRequest).url().toString());
    }

    /**
     * 生成预签名下载url
     * @param bucketName 存储桶名称
     * @param key 文件名
     * @param duration 签名有效期
     * @return 预签名下载链接
     */
    public String generatePresignedDownloadUrl(String bucketName, String key, Duration duration) {
        return generatePresignedDownloadUrl(bucketName, key, duration, null);
    }

    /**
     * 生成预签名下载url
     * @param bucketName 存储桶名称
     * @param key 文件名
     * @param duration 签名有效期
     * @param requestCustomizer 请求定制配置
     * @return 预签名下载链接
     */
    public String generatePresignedDownloadUrl(String bucketName, String key, Duration duration,@Nullable UnaryOperator<GetObjectRequest.Builder> requestCustomizer) {
        var getObjectRequestBuilder = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(key);
        GetObjectRequest getObjectRequest;
        if (requestCustomizer != null) {
            getObjectRequest = requestCustomizer.apply(getObjectRequestBuilder).build();
        } else {
            getObjectRequest = getObjectRequestBuilder.build();
        }
        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(duration)
                .getObjectRequest(getObjectRequest)
                .build();
        return s3Presigner.presignGetObject(presignRequest).url().toString();
    }

    /**
     * 删除文件
     *
     * @param bucketName 存储桶名称
     * @param key        文件名
     * @return
     */
    public DeleteObjectResponse deleteFile(String bucketName, String key) {
        return deleteFile(bucketName, key, null);
    }

    /**
     * 删除文件
     *
     * @param bucketName        存储桶名称
     * @param key               文件名
     * @param requestCustomizer 删除请求定制配置
     * @return
     */
    public DeleteObjectResponse deleteFile(String bucketName, String key, @Nullable UnaryOperator<DeleteObjectRequest.Builder> requestCustomizer) {
        var deleteObjectRequestBuilder = DeleteObjectRequest.builder()
                .bucket(bucketName)
                .key(key);
        DeleteObjectRequest deleteObjectRequest;
        if (requestCustomizer != null) {
            deleteObjectRequest = requestCustomizer.apply(deleteObjectRequestBuilder).build();
        } else {
            deleteObjectRequest = deleteObjectRequestBuilder.build();
        }
        return s3Client.deleteObject(deleteObjectRequest);
    }

    @Override
    public void destroy() {
        s3Client.close();
        s3Presigner.close();
    }

}
