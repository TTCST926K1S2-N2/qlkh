package vn.edu.ictu.qlkh.util;

import jakarta.servlet.http.Part;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.UUID;

public final class FileUploadUtil {

    public static final long MAX_FILE_SIZE = 2L * 1024 * 1024;
    public static final int THUMBNAIL_SIZE = 200;

    private FileUploadUtil() {
    }

    public static boolean isValidType(Part filePart) {
        if (filePart == null || filePart.getContentType() == null) {
            return false;
        }

        String contentType = filePart.getContentType()
                .toLowerCase(Locale.ROOT);

        return "image/jpeg".equals(contentType)
                || "image/png".equals(contentType);
    }

    public static boolean isValidSize(Part filePart) {
        return filePart != null
                && filePart.getSize() > 0
                && filePart.getSize() <= MAX_FILE_SIZE;
    }

    public static UploadResult saveAvatar(
            Part filePart,
            String uploadPath,
            long userId
    ) throws IOException {

        if (!isValidType(filePart)) {
            throw new IllegalArgumentException(
                    "Chỉ chấp nhận ảnh JPG hoặc PNG."
            );
        }

        if (!isValidSize(filePart)) {
            throw new IllegalArgumentException(
                    "Ảnh phải có kích thước tối đa 2MB."
            );
        }

        BufferedImage sourceImage;

        try (InputStream inputStream = filePart.getInputStream()) {
            sourceImage = ImageIO.read(inputStream);
        }

        if (sourceImage == null) {
            throw new IllegalArgumentException(
                    "File tải lên không phải ảnh hợp lệ."
            );
        }

        BufferedImage squareImage = cropSquare(sourceImage);

        String format = "image/png".equalsIgnoreCase(
                filePart.getContentType()
        ) ? "png" : "jpg";

        String uniqueName =
                "avatar-" + userId + "-" + UUID.randomUUID();

        String avatarFileName =
                uniqueName + "." + format;

        String thumbnailFileName =
                uniqueName + "-thumb." + format;

        File uploadDirectory = new File(uploadPath);

        if (!uploadDirectory.exists()
                && !uploadDirectory.mkdirs()) {
            throw new IOException(
                    "Không thể tạo thư mục lưu avatar."
            );
        }

        File avatarFile =
                new File(uploadDirectory, avatarFileName);

        File thumbnailFile =
                new File(uploadDirectory, thumbnailFileName);

        writeImage(squareImage, format, avatarFile);

        BufferedImage thumbnail =
                resizeSquare(squareImage, THUMBNAIL_SIZE);

        writeImage(thumbnail, format, thumbnailFile);

        return new UploadResult(
                "/uploads/avatars/" + avatarFileName,
                "/uploads/avatars/" + thumbnailFileName
        );
    }

    private static BufferedImage cropSquare(
            BufferedImage source
    ) {

        int size = Math.min(
                source.getWidth(),
                source.getHeight()
        );

        int x = (source.getWidth() - size) / 2;
        int y = (source.getHeight() - size) / 2;

        BufferedImage cropped =
                source.getSubimage(x, y, size, size);

        BufferedImage copy = new BufferedImage(
                size,
                size,
                BufferedImage.TYPE_INT_RGB
        );

        Graphics2D graphics = copy.createGraphics();
        graphics.drawImage(cropped, 0, 0, null);
        graphics.dispose();

        return copy;
    }

    private static BufferedImage resizeSquare(
            BufferedImage source,
            int size
    ) {

        BufferedImage resized = new BufferedImage(
                size,
                size,
                BufferedImage.TYPE_INT_RGB
        );

        Graphics2D graphics = resized.createGraphics();

        graphics.setRenderingHint(
                RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BILINEAR
        );

        graphics.setRenderingHint(
                RenderingHints.KEY_RENDERING,
                RenderingHints.VALUE_RENDER_QUALITY
        );

        graphics.drawImage(
                source,
                0,
                0,
                size,
                size,
                null
        );

        graphics.dispose();

        return resized;
    }

    private static void writeImage(
            BufferedImage image,
            String format,
            File destination
    ) throws IOException {

        if (!ImageIO.write(image, format, destination)) {
            throw new IOException(
                    "Không thể ghi file ảnh."
            );
        }
    }

    public record UploadResult(
            String avatarUrl,
            String thumbnailUrl
    ) {
    }
}
