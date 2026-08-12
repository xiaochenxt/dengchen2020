package io.github.dengchen2020.core.utils.s3;

/**
 * S3预签名上传链接信息
 * @author xiaochen
 * @since 2025/12/1
 * @param accessUrl 访问链接
 * @param uploadUrl 上传链接
 */
public record S3PresignedUploadUrlInfo(String accessUrl, String uploadUrl) {



}
