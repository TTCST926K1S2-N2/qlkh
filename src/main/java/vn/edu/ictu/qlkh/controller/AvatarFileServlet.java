package vn.edu.ictu.qlkh.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import vn.edu.ictu.qlkh.util.AvatarStorage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

@WebServlet("/uploads/avatars/*")
public class AvatarFileServlet extends HttpServlet {

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        String pathInfo = request.getPathInfo();

        if (pathInfo == null
                || pathInfo.length() <= 1) {
            response.sendError(
                    HttpServletResponse.SC_NOT_FOUND
            );
            return;
        }

        String fileName = pathInfo.substring(1);

        File avatarFile;

        try {
            avatarFile = AvatarStorage.resolve(fileName);
        } catch (IOException e) {
            response.sendError(
                    HttpServletResponse.SC_BAD_REQUEST
            );
            return;
        }

        if (!avatarFile.isFile()) {
            response.sendError(
                    HttpServletResponse.SC_NOT_FOUND
            );
            return;
        }

        String contentType =
                getServletContext().getMimeType(
                        avatarFile.getName()
                );

        if (contentType == null) {
            contentType = "application/octet-stream";
        }

        response.setContentType(contentType);
        response.setContentLengthLong(
                avatarFile.length()
        );

        response.setHeader(
                "Cache-Control",
                "private, max-age=86400"
        );

        Files.copy(
                avatarFile.toPath(),
                response.getOutputStream()
        );
    }
}