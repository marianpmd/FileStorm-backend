package com.marian.owncloudbackend.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.commons.io.FilenameUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import static com.marian.owncloudbackend.utils.VideoStreamConstants.ACCEPT_RANGES;
import static com.marian.owncloudbackend.utils.VideoStreamConstants.BYTES;
import static com.marian.owncloudbackend.utils.VideoStreamConstants.BYTE_RANGE;
import static com.marian.owncloudbackend.utils.VideoStreamConstants.CHUNK_SIZE;
import static com.marian.owncloudbackend.utils.VideoStreamConstants.CONTENT_LENGTH;
import static com.marian.owncloudbackend.utils.VideoStreamConstants.CONTENT_RANGE;
import static com.marian.owncloudbackend.utils.VideoStreamConstants.CONTENT_TYPE;
import static com.marian.owncloudbackend.utils.VideoStreamConstants.VIDEO_CONTENT;

import com.marian.owncloudbackend.entity.FileEntity;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class VideoService {
    private final FileStoreService fileStoreService;

    public ResponseEntity<byte[]> prepareContent(final Long fileId, final String range) {

        try {
            String userEmail = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            FileEntity fileEntity = this.fileStoreService.getFileEntityByIdAndUser(fileId, userEmail);
            long rangeStart = 0;
            long rangeEnd = CHUNK_SIZE;
            final Long fileSize = fileEntity.getSize().longValue();
            if (range == null) {
                return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT)
                        .header(CONTENT_TYPE, VIDEO_CONTENT + getFileType(fileEntity))
                        .header(ACCEPT_RANGES, BYTES)
                        .header(CONTENT_LENGTH, String.valueOf(rangeEnd))
                        .header(CONTENT_RANGE, BYTES + " " + rangeStart + "-" + rangeEnd + "/" + fileSize)
                        .header(CONTENT_LENGTH, String.valueOf(fileSize))
                        .body(readByteRange(Path.of(fileEntity.getPath()), rangeStart, rangeEnd));
            }
            String[] ranges = range.split("-");
            rangeStart = Long.parseLong(ranges[0].substring(6));
            if (ranges.length > 1) {
                rangeEnd = Long.parseLong(ranges[1]);
            } else {
                rangeEnd = rangeStart + CHUNK_SIZE;
            }

            rangeEnd = Math.min(rangeEnd, fileSize - 1);
            final byte[] data = readByteRange(Path.of(fileEntity.getPath()), rangeStart, rangeEnd);
            final String contentLength = String.valueOf((rangeEnd - rangeStart) + 1);
            HttpStatus httpStatus = HttpStatus.PARTIAL_CONTENT;
            if (rangeEnd >= fileSize) {
                httpStatus = HttpStatus.OK;
            }
            return ResponseEntity.status(httpStatus)
                    .header(CONTENT_TYPE, VIDEO_CONTENT + getFileType(fileEntity))
                    .header(ACCEPT_RANGES, BYTES)
                    .header(CONTENT_LENGTH, contentLength)
                    .header(CONTENT_RANGE, BYTES + " " + rangeStart + "-" + rangeEnd + "/" + fileSize)
                    .body(data);
        } catch (IOException e) {
            log.error("Exception while reading the file {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }


    }

    private String getFileType(FileEntity fileEntity) {
        return FilenameUtils.getExtension(fileEntity.getName());
    }

    /**
     * ready file byte by byte.
     *
     * @param start long.
     * @param end   long.
     * @return byte array.
     * @throws IOException exception.
     */
    public byte[] readByteRangeNew(Path path, long start, long end) throws IOException {
        byte[] data = Files.readAllBytes(path);
        byte[] result = new byte[(int) (end - start) + 1];
        System.arraycopy(data, (int) start, result, 0, (int) (end - start) + 1);
        return result;
    }


    public byte[] readByteRange(Path path, long start, long end) throws IOException {
        try (InputStream inputStream = (Files.newInputStream(path));
             ByteArrayOutputStream bufferedOutputStream = new ByteArrayOutputStream()) {
            byte[] data = new byte[BYTE_RANGE];
            int nRead;
            while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
                bufferedOutputStream.write(data, 0, nRead);
            }
            bufferedOutputStream.flush();
            byte[] result = new byte[(int) (end - start) + 1];
            System.arraycopy(bufferedOutputStream.toByteArray(), (int) start, result, 0, result.length);
            return result;
        }
    }

    /**
     * Getting the size from the path.
     *
     * @param path Path.
     * @return Long.
     */
    private Long sizeFromFile(Path path) {
        try {
            return Files.size(path);
        } catch (IOException ioException) {
            log.error("Error while getting the file size", ioException);
        }
        return 0L;
    }
}
