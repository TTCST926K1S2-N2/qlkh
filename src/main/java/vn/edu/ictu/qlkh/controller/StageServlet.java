package vn.edu.ictu.qlkh.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import vn.edu.ictu.qlkh.model.Stage;
import vn.edu.ictu.qlkh.service.StageService;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

@WebServlet("/stages/*")
public class StageServlet extends HttpServlet {

    private StageService stageService;

    @Override
    public void init() {
        stageService = new StageService();
    }

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");

        if (!isAdmin(request)) {
            response.sendError(
                    HttpServletResponse.SC_FORBIDDEN,
                    "Bạn không có quyền thực hiện chức năng này."
            );
            return;
        }

        String pathInfo = request.getPathInfo();

        try {
            if (pathInfo == null || "/".equals(pathInfo)) {
                writeStages(response, stageService.getAllStages());
                return;
            }

            Long id = parseLong(
                    pathInfo.substring(1)
            );

            if (id == null) {
                response.sendError(
                        HttpServletResponse.SC_BAD_REQUEST,
                        "ID stage không hợp lệ."
                );
                return;
            }

            Stage stage = stageService.getStageById(id);

            if (stage == null) {
                response.sendError(
                        HttpServletResponse.SC_NOT_FOUND,
                        "Không tìm thấy stage."
                );
                return;
            }

            writeStage(response, stage);

        } catch (SQLException e) {
            throw new ServletException(
                    "Không thể tải stage.",
                    e
            );
        }
    }

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");

        if (!isAdmin(request)) {
            response.sendError(
                    HttpServletResponse.SC_FORBIDDEN,
                    "Bạn không có quyền thực hiện chức năng này."
            );
            return;
        }

        Stage stage = readStage(request);

        try {
            stageService.addStage(stage);

            response.setStatus(
                    HttpServletResponse.SC_CREATED
            );

            writeStage(response, stage);

        } catch (IllegalArgumentException e) {

            response.sendError(
                    HttpServletResponse.SC_BAD_REQUEST,
                    e.getMessage()
            );

        } catch (SQLException e) {

            throw new ServletException(
                    "Không thể thêm stage.",
                    e
            );
        }
    }

    @Override
    protected void doPut(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");

        if (!isAdmin(request)) {
            response.sendError(
                    HttpServletResponse.SC_FORBIDDEN,
                    "Bạn không có quyền thực hiện chức năng này."
            );
            return;
        }

        Long id = parseLong(
                getIdFromPath(request)
        );

        if (id == null) {
            response.sendError(
                    HttpServletResponse.SC_BAD_REQUEST,
                    "ID stage không hợp lệ."
            );
            return;
        }

        Stage stage = readStage(request);
        stage.setId(id);

        try {
            stageService.updateStage(stage);
            writeStage(response, stage);

        } catch (IllegalArgumentException e) {

            response.sendError(
                    HttpServletResponse.SC_BAD_REQUEST,
                    e.getMessage()
            );

        } catch (SQLException e) {

            throw new ServletException(
                    "Không thể cập nhật stage.",
                    e
            );
        }
    }

    @Override
    protected void doDelete(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");

        if (!isAdmin(request)) {
            response.sendError(
                    HttpServletResponse.SC_FORBIDDEN,
                    "Bạn không có quyền thực hiện chức năng này."
            );
            return;
        }

        Long id = parseLong(
                getIdFromPath(request)
        );

        if (id == null) {
            response.sendError(
                    HttpServletResponse.SC_BAD_REQUEST,
                    "ID stage không hợp lệ."
            );
            return;
        }

        try {
            stageService.deleteStage(id);
            response.setStatus(
                    HttpServletResponse.SC_NO_CONTENT
            );

        } catch (IllegalArgumentException e) {

            response.sendError(
                    HttpServletResponse.SC_NOT_FOUND,
                    e.getMessage()
            );

        } catch (SQLException e) {

            throw new ServletException(
                    "Không thể xóa stage.",
                    e
            );
        }
    }

    private Stage readStage(HttpServletRequest request) {

        Stage stage = new Stage();

        stage.setName(
                request.getParameter("name")
        );

        stage.setCode(
                request.getParameter("code")
        );

        stage.setStatus(
                request.getParameter("status")
        );

        stage.setDescription(
                request.getParameter("description")
        );

        return stage;
    }

    private String getIdFromPath(
            HttpServletRequest request) {

        String pathInfo = request.getPathInfo();

        if (pathInfo == null
                || "/".equals(pathInfo)
                || pathInfo.length() <= 1) {
            return null;
        }

        return pathInfo.substring(1);
    }

    private Long parseLong(String value) {

        if (value == null || value.isBlank()) {
            return null;
        }

        try {
            long number = Long.parseLong(
                    value.trim()
            );

            return number > 0 ? number : null;

        } catch (NumberFormatException e) {
            return null;
        }
    }

    private boolean isAdmin(
            HttpServletRequest request) {

        HttpSession session =
                request.getSession(false);

        if (session == null) {
            return false;
        }

        Object role =
                session.getAttribute("userRole");

        return role != null
                && "ADMIN".equalsIgnoreCase(
                        role.toString().trim()
                );
    }

    private void writeStages(
            HttpServletResponse response,
            List<Stage> stages)
            throws IOException {

        StringBuilder json =
                new StringBuilder("[");

        for (int i = 0; i < stages.size(); i++) {

            if (i > 0) {
                json.append(",");
            }

            appendStageJson(
                    json,
                    stages.get(i)
            );
        }

        json.append("]");

        response.getWriter().write(
                json.toString()
        );
    }

    private void writeStage(
            HttpServletResponse response,
            Stage stage)
            throws IOException {

        StringBuilder json =
                new StringBuilder();

        appendStageJson(json, stage);

        response.getWriter().write(
                json.toString()
        );
    }

    private void appendStageJson(
            StringBuilder json,
            Stage stage) {

        json.append("{")
                .append("\"id\":")
                .append(stage.getId())
                .append(",")
                .append("\"name\":")
                .append(jsonString(stage.getName()))
                .append(",")
                .append("\"code\":")
                .append(jsonString(stage.getCode()))
                .append(",")
                .append("\"status\":")
                .append(jsonString(stage.getStatus()))
                .append(",")
                .append("\"description\":")
                .append(jsonString(stage.getDescription()))
                .append("}");
    }

    private String jsonString(String value) {

        if (value == null) {
            return "null";
        }

        return "\""
                + value
                    .replace("\\", "\\\\")
                    .replace("\"", "\\\"")
                    .replace("\r", "\\r")
                    .replace("\n", "\\n")
                + "\"";
    }
}
