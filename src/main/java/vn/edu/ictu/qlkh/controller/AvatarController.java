package vn.edu.ictu.qlkh.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;
import vn.edu.ictu.qlkh.dao.UserDAO;
import vn.edu.ictu.qlkh.util.FileUploadUtil;

import java.io.IOException;
import java.sql.SQLException;

@WebServlet("/api/users/avatar")
@MultipartConfig(
        fileSizeThreshold = 1024 * 1024,
        maxFileSize = 2L * 1024 * 1024,
        maxRequestSize = 3L * 1024 * 1024
)
public class AvatarController extends HttpServlet {

    private final UserDAO userDAO = new UserDAO();

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        response.setCharacterEncoding("UTF-8");
        response.setContentType(
                "application/json;charset=UTF-8"
        );

        HttpSession session =
                request.getSession(false);

        Long userId = getSessionUserId(session);

        if (userId == null) {
            response.setStatus(
                    HttpServletResponse.SC_UNAUTHORIZED
            );
            response.getWriter().write(
                    "{\"success\":false,\"message\":\"Phiên đăng nhập không hợp lệ.\"}"
            );
            return;
        }

        try {
            Part filePart =
                    request.getPart("avatar");

            if (filePart == null
                    || filePart.getSize() == 0) {

                response.setStatus(
                        HttpServletResponse.SC_BAD_REQUEST
                );
                response.getWriter().write(
                        "{\"success\":false,\"message\":\"Vui lòng chọn ảnh.\"}"
                );
                return;
            }

            if (!FileUploadUtil.isValidType(filePart)) {
                response.setStatus(
                        HttpServletResponse.SC_BAD_REQUEST
                );
                response.getWriter().write(
                        "{\"success\":false,\"message\":\"Chỉ chấp nhận ảnh JPG hoặc PNG.\"}"
                );
                return;
            }

            if (!FileUploadUtil.isValidSize(filePart)) {
                response.setStatus(
                        HttpServletResponse.SC_BAD_REQUEST
                );
                response.getWriter().write(
                        "{\"success\":false,\"message\":\"Ảnh phải có kích thước tối đa 2MB.\"}"
                );
                return;
            }

            String uploadPath =
                    getServletContext().getRealPath(
                            "/uploads/avatars"
                    );

            if (uploadPath == null) {
                throw new IOException(
                        "Không xác định được thư mục lưu avatar."
                );
            }

            String oldAvatarUrl =
                    userDAO.getAvatarByUserId(userId);

            FileUploadUtil.UploadResult result =
                    FileUploadUtil.saveAvatar(
                            filePart,
                            uploadPath,
                            userId
                    );

            boolean updated =
                    userDAO.updateAvatar(
                            userId,
                            result.avatarUrl()
                    );

            if (!updated) {
                FileUploadUtil.deleteAvatarFiles(
                        uploadPath,
                        result.avatarUrl()
                );
                response.setStatus(
                        HttpServletResponse.SC_NOT_FOUND
                );
                response.getWriter().write(
                        "{\"success\":false,\"message\":\"Không tìm thấy người dùng.\"}"
                );
                return;
            }

            FileUploadUtil.deleteAvatarFiles(
                    uploadPath,
                    oldAvatarUrl
            );

            response.setStatus(
                    HttpServletResponse.SC_OK
            );

            response.getWriter().write(
                    "{\"success\":true,"
                            + "\"avatarUrl\":\""
                            + result.avatarUrl()
                            + "\","
                            + "\"thumbnailUrl\":\""
                            + result.thumbnailUrl()
                            + "\"}"
            );

        } catch (IllegalArgumentException e) {

            response.setStatus(
                    HttpServletResponse.SC_BAD_REQUEST
            );

            response.getWriter().write(
                    "{\"success\":false,\"message\":\"Dữ liệu ảnh không hợp lệ.\"}"
            );

        } catch (SQLException e) {

            response.setStatus(
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR
            );

            response.getWriter().write(
                    "{\"success\":false,\"message\":\"Không thể cập nhật avatar.\"}"
            );
        }
    }

    /**
     * Doc userId tu session an toan.
     */
    private Long getSessionUserId(
            HttpSession session) {

        if (session == null) {
            return null;
        }

        Object value =
                session.getAttribute("userId");

        if (value instanceof Number number) {
            return number.longValue();
        }

        return null;
    }
}