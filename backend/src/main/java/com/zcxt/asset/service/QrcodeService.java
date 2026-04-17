package com.zcxt.asset.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.zcxt.config.AppProperties;
import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

@Service
public class QrcodeService {
    private final AppProperties appProperties;

    public QrcodeService(AppProperties appProperties) {
        this.appProperties = appProperties;
    }

    public QrcodeResult generateAndStore(String assetId, String encryptedContent) {
        String storage = appProperties.getQrcode().getStorage();
        byte[] png = generatePng(encryptedContent, 320, 320);
        if ("local".equalsIgnoreCase(storage)) {
            String baseUrl = appProperties.getQrcode().getBaseUrl();
            Path dir = Path.of(appProperties.getQrcode().getLocalDir());
            Path file = dir.resolve(assetId + ".png");
            try {
                Files.createDirectories(dir);
                Files.write(file, png);
            } catch (Exception e) {
                throw new IllegalStateException("二维码落盘失败");
            }

            String url = baseUrl + "/api/public/qrcodes/" + assetId + ".png";
            return new QrcodeResult(url, png.length);
        }

        if ("minio".equalsIgnoreCase(storage)) {
            var cfg = appProperties.getQrcode().getMinio();
            String bucket = cfg.getBucket();
            if (bucket == null || bucket.isBlank()) {
                throw new IllegalStateException("MinIO bucket 未配置");
            }
            String object = "qrcodes/" + assetId + ".png";
            MinioClient client = MinioClient.builder()
                    .endpoint(cfg.getEndpoint())
                    .credentials(cfg.getAccessKey(), cfg.getSecretKey())
                    .build();
            try {
                boolean exists = client.bucketExists(BucketExistsArgs.builder().bucket(bucket).build());
                if (!exists) {
                    client.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
                }
                client.putObject(PutObjectArgs.builder()
                        .bucket(bucket)
                        .object(object)
                        .contentType("image/png")
                        .stream(new ByteArrayInputStream(png), png.length, -1)
                        .build());
            } catch (Exception e) {
                throw new IllegalStateException("二维码上传失败");
            }

            String publicEndpoint = cfg.getPublicEndpoint() == null || cfg.getPublicEndpoint().isBlank()
                    ? cfg.getEndpoint()
                    : cfg.getPublicEndpoint();
            String url = publicEndpoint.replaceAll("/$", "") + "/" + bucket + "/" + object;
            return new QrcodeResult(url, png.length);
        }

        throw new IllegalStateException("当前运行模式不支持该二维码存储类型: " + storage);
    }

    public byte[] generatePngBytes(String content, int size) {
        int s = Math.max(size, 160);
        return generatePng(content, s, s);
    }

    private byte[] generatePng(String content, int width, int height) {
        QRCodeWriter writer = new QRCodeWriter();
        BitMatrix matrix;
        try {
            matrix = writer.encode(content, BarcodeFormat.QR_CODE, width, height, Map.of(
                    EncodeHintType.MARGIN, 1
            ));
        } catch (WriterException e) {
            throw new IllegalStateException("二维码生成失败");
        }

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            MatrixToImageWriter.writeToStream(matrix, "PNG", out);
            return out.toByteArray();
        } catch (Exception e) {
            throw new IllegalStateException("二维码输出失败");
        }
    }

    public record QrcodeResult(String url, long bytes) {
    }
}
