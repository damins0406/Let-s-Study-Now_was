package com.team.LetsStudyNow_rg.global.s3;

import com.team.LetsStudyNow_rg.global.exception.S3Exception;
import io.awspring.cloud.s3.ObjectMetadata;
import io.awspring.cloud.s3.S3Template;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class S3Service {

    private final S3Template s3Template;

    @Value("${spring.cloud.aws.s3.bucket}")
    private String bucket;

    /**
     * 파일 업로드 메서드 (재사용 가능)
     *
     * @param file    업로드할 파일
     * @param dirName S3 내부 폴더 이름 (예: "profile", "chat")
     * @return 업로드된 파일의 전체 URL
     */
    public String uploadFile(MultipartFile file, String dirName) {
        // 파일 검증 (비어있는지 확인)
        if (file.isEmpty()) {
            throw new IllegalArgumentException("파일이 존재하지 않습니다.");
        }

        // 파일명 생성 (uuid, 중복 방지)
        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String s3FileName = dirName + "/" + UUID.randomUUID().toString().substring(0, 10) + extension;

        // 메타데이터 설정
        ObjectMetadata metadata = ObjectMetadata.builder()
                .contentType(file.getContentType())
                .contentLength(file.getSize())
                .build();

        // S3 업로드
        try (InputStream inputStream = file.getInputStream()) {
            s3Template.upload(bucket, s3FileName, inputStream, metadata);

            // URL 반환
            return s3Template.download(bucket, s3FileName).getURL().toString();
        } catch (IOException e) {
            log.error("S3 파일 업로드 실패: {}", s3FileName, e);
            throw new S3Exception("S3 파일 업로드 실패", e);
        }
    }

    /**
     * S3 파일 삭제 메서드
     *
     * @param fileUrl 삭제할 파일의 전체 URL
     */
    public void deleteFile(String fileUrl) {
        try {
            // 예: https://s3.../bucket/profile/abc.jpg -> profile/abc.jpg
            String splitStr = ".com/";
            int splitIndex = fileUrl.indexOf(splitStr);

            if (splitIndex != -1) {
                String fileName = fileUrl.substring(splitIndex + splitStr.length());

                String decodedFileName = URLDecoder.decode(fileName, StandardCharsets.UTF_8);

                s3Template.deleteObject(bucket, decodedFileName);
            }
        } catch (Exception e) {
            log.error("S3 파일 삭제 실패: url={}", fileUrl, e);
        }
    }
}