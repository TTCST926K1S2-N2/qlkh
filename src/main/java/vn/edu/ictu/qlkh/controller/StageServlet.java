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
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

        prepareResponse(request, response);

        if (!canManageStages(request)) {
            sendError(
                    response,
                    HttpServletResponse.SC_FORBIDDEN,
                    "Bạn không có quyền quản lý pipeline."
            );
            return;
        }

        try {
            String pathInfo = request.getPathInfo();

            if (pathInfo == null || "/".equals(pathInfo)) {
                writeStages(
                        response,
                        stageService.getAllStages()
                );
                return;
            }

            Long id = parseId(pathInfo);

            if (id == null) {
                sendError(
                        response,
                        HttpServletResponse.SC_BAD_REQUEST,
                        "ID giai đoạn không hợp lệ."
                );
                return;
            }

            Stage stage =
                    stageService.getStageById(id);

            if (stage == null) {
                sendError(
                        response,
                        HttpServletResponse.SC_NOT_FOUND,
                        "Không tìm thấy giai đoạn."
                );
                return;
            }

            writeStage(response, stage);

        } catch (IllegalArgumentException e) {
            sendError(
                    response,
                    HttpServletResponse.SC_BAD_REQUEST,
                    e.getMessage()
            );

        } catch (SQLException e) {
            throw new ServletException(
                    "Không thể tải giai đoạn.",
                    e
            );
        }
    }

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        prepareResponse(request, response);

        if (!canManageStages(request)) {
            sendError(
                    response,
                    HttpServletResponse.SC_FORBIDDEN,
                    "Bạn không có quyền quản lý pipeline."
            );
            return;
        }

        try {
            Stage stage = readStage(request);

            stageService.addStage(stage);

            response.setStatus(
                    HttpServletResponse.SC_CREATED
            );

            writeStage(response, stage);

        } catch (IllegalArgumentException e) {
            sendError(
                    response,
                    HttpServletResponse.SC_BAD_REQUEST,
                    e.getMessage()
            );

        } catch (SQLException e) {
            throw new ServletException(
                    "Không thể thêm giai đoạn.",
                    e
            );
        }
    }

    @Override
    protected void doPut(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        prepareResponse(request, response);

        if (!canManageStages(request)) {
            sendError(
                    response,
                    HttpServletResponse.SC_FORBIDDEN,
                    "Bạn không có quyền quản lý pipeline."
            );
            return;
        }

        Long id =
                parseId(request.getPathInfo());

        if (id == null) {
            sendError(
                    response,
                    HttpServletResponse.SC_BAD_REQUEST,
                    "ID giai đoạn không hợp lệ."
            );
            return;
        }

        try {
            Stage existing =
                    stageService.getStageById(id);

            if (existing == null) {
                sendError(
                        response,
                        HttpServletResponse.SC_NOT_FOUND,
                        "Không tìm thấy giai đoạn."
                );
                return;
            }

            Stage stage = readStage(request);
            stage.setId(id);

            stageService.updateStage(stage);

            writeStage(response, stage);

        } catch (IllegalArgumentException e) {
            sendError(
                    response,
                    HttpServletResponse.SC_BAD_REQUEST,
                    e.getMessage()
            );

        } catch (SQLException e) {
            throw new ServletException(
                    "Không thể cập nhật giai đoạn.",
                    e
            );
        }
    }

    @Override
    protected void doDelete(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        prepareResponse(request, response);

        if (!canManageStages(request)) {
            sendError(
                    response,
                    HttpServletResponse.SC_FORBIDDEN,
                    "Bạn không có quyền quản lý pipeline."
            );
            return;
        }

        Long id =
                parseId(request.getPathInfo());

        if (id == null) {
            sendError(
                    response,
                    HttpServletResponse.SC_BAD_REQUEST,
                    "ID giai đoạn không hợp lệ."
            );
            return;
        }

        try {
            Stage existing =
                    stageService.getStageById(id);

            if (existing == null) {
                sendError(
                        response,
                        HttpServletResponse.SC_NOT_FOUND,
                        "Không tìm thấy giai đoạn."
                );
                return;
            }

            stageService.deactivateStage(id);

            response.setStatus(
                    HttpServletResponse.SC_NO_CONTENT
            );

        } catch (IllegalArgumentException e) {
            sendError(
                    response,
                    HttpServletResponse.SC_BAD_REQUEST,
                    e.getMessage()
            );

        } catch (SQLException e) {
            throw new ServletException(
                    "Không thể ngừng hoạt động giai đoạn.",
                    e
            );
        }
    }

    private Stage readStage(
            HttpServletRequest request)
            throws IOException {

        String contentType =
                request.getContentType();

        if (contentType != null
                && contentType
                .toLowerCase()
                .contains("application/json")) {

            String body =
                    request.getReader()
                            .lines()
                            .reduce(
                                    "",
                                    (a, b) -> a + b
                            );

            if (body.isBlank()) {
                throw new IllegalArgumentException(
                        "Request body không được để trống."
                );
            }

            return readJsonStage(body);
        }

        Stage stage = new Stage();

        stage.setName(
                request.getParameter("name")
        );

        stage.setCode(
                request.getParameter("code")
        );

        stage.setStageOrder(
                parseInt(
                        request.getParameter(
                                "stageOrder"
                        )
                )
        );

        stage.setWinProbability(
                parseDecimal(
                        request.getParameter(
                                "winProbability"
                        )
                )
        );

        stage.setExitCondition(
                request.getParameter(
                        "exitCondition"
                )
        );

        stage.setStatus(
                request.getParameter("status")
        );

        stage.setDescription(
                request.getParameter(
                        "description"
                )
        );

        return stage;
    }

    private Stage readJsonStage(String json) {

        Stage stage = new Stage();

        stage.setName(
                extractString(json, "name")
        );

        stage.setCode(
                extractString(json, "code")
        );

        stage.setStageOrder(
                extractInt(json, "stageOrder")
        );

        stage.setWinProbability(
                extractDecimal(
                        json,
                        "winProbability"
                )
        );

        stage.setExitCondition(
                extractString(
                        json,
                        "exitCondition"
                )
        );

        stage.setStatus(
                extractString(json, "status")
        );

        stage.setDescription(
                extractString(
                        json,
                        "description"
                )
        );

        return stage;
    }

    private String extractString(
            String json,
            String key) {

        String raw =
                extractRawValue(json, key);

        if (raw == null
                || "null".equals(raw)) {
            return null;
        }

        if (!raw.startsWith("\"")
                || !raw.endsWith("\"")) {

            throw new IllegalArgumentException(
                    "Trường " + key
                            + " phải là chuỗi."
            );
        }

        return unescapeJson(
                raw.substring(
                        1,
                        raw.length() - 1
                )
        );
    }

    private int extractInt(
            String json,
            String key) {

        return parseInt(
                extractRawValue(json, key)
        );
    }

    private BigDecimal extractDecimal(
            String json,
            String key) {

        return parseDecimal(
                extractRawValue(json, key)
        );
    }

    private String extractRawValue(
            String json,
            String key) {

        String regex =
                "\""
                + Pattern.quote(key)
                + "\"\\s*:\\s*"
                + "(null|"
                + "\"(?:\\\\.|[^\"\\\\])*\"|"
                + "-?\\d+(?:\\.\\d+)?)";

        Matcher matcher =
                Pattern.compile(regex)
                        .matcher(json);

        return matcher.find()
                ? matcher.group(1)
                : null;
    }

    private int parseInt(String value) {

        if (value == null
                || value.isBlank()
                || "null".equals(value)) {
            return 0;
        }

        try {
            return Integer.parseInt(
                    value.trim()
            );

        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                    "Thứ tự giai đoạn phải là số nguyên."
            );
        }
    }

    private BigDecimal parseDecimal(
            String value) {

        if (value == null
                || value.isBlank()
                || "null".equals(value)) {
            return null;
        }

        try {
            return new BigDecimal(
                    value.trim()
            );

        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                    "Xác suất thắng phải là số."
            );
        }
    }

    private Long parseId(String pathInfo) {

        if (pathInfo == null
                || pathInfo.isBlank()
                || "/".equals(pathInfo)) {
            return null;
        }

        String value =
                pathInfo.startsWith("/")
                        ? pathInfo.substring(1)
                        : pathInfo;

        if (value.contains("/")) {
            return null;
        }

        try {
            long id =
                    Long.parseLong(value);

            return id > 0
                    ? id
                    : null;

        } catch (NumberFormatException e) {
            return null;
        }
    }

    private boolean canManageStages(
            HttpServletRequest request) {

        HttpSession session =
                request.getSession(false);

        if (session == null) {
            return false;
        }

        Object role =
                session.getAttribute("userRole");

        if (role == null) {
            return false;
        }

        String roleName =
                role.toString().trim();

        return "MANAGER".equalsIgnoreCase(
                    roleName
                )
                || "ADMIN".equalsIgnoreCase(
                    roleName
                );
    }

    private void prepareResponse(
            HttpServletRequest request,
            HttpServletResponse response)
            throws IOException {

        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");

        response.setContentType(
                "application/json;charset=UTF-8"
        );
    }

    private void writeStages(
            HttpServletResponse response,
            List<Stage> stages)
            throws IOException {

        StringBuilder json =
                new StringBuilder("[");

        for (int i = 0;
             i < stages.size();
             i++) {

            if (i > 0) {
                json.append(",");
            }

            appendStageJson(
                    json,
                    stages.get(i)
            );
        }

        json.append("]");

        response.getWriter()
                .write(json.toString());
    }

    private void writeStage(
            HttpServletResponse response,
            Stage stage)
            throws IOException {

        StringBuilder json =
                new StringBuilder();

        appendStageJson(json, stage);

        response.getWriter()
                .write(json.toString());
    }

    private void appendStageJson(
            StringBuilder json,
            Stage stage) {

        json.append("{")
                .append("\"id\":")
                .append(stage.getId())

                .append(",\"name\":")
                .append(
                        jsonString(
                                stage.getName()
                        )
                )

                .append(",\"code\":")
                .append(
                        jsonString(
                                stage.getCode()
                        )
                )

                .append(",\"stageOrder\":")
                .append(
                        stage.getStageOrder()
                )

                .append(",\"winProbability\":")
                .append(
                        stage.getWinProbability()
                                == null
                                ? "null"
                                : stage
                                    .getWinProbability()
                                    .toPlainString()
                )

                .append(",\"exitCondition\":")
                .append(
                        jsonString(
                                stage.getExitCondition()
                        )
                )

                .append(",\"status\":")
                .append(
                        jsonString(
                                stage.getStatus()
                        )
                )

                .append(",\"description\":")
                .append(
                        jsonString(
                                stage.getDescription()
                        )
                )

                .append("}");
    }

    private void sendError(
            HttpServletResponse response,
            int status,
            String message)
            throws IOException {

        response.setStatus(status);

        response.getWriter().write(
                "{"
                + "\"success\":false,"
                + "\"message\":"
                + jsonString(message)
                + "}"
        );
    }

    private String jsonString(
            String value) {

        if (value == null) {
            return "null";
        }

        return "\""
                + escapeJson(value)
                + "\"";
    }

    private String escapeJson(
            String value) {

        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\r", "\\r")
                .replace("\n", "\\n")
                .replace("\t", "\\t");
    }

    private String unescapeJson(
            String value) {

        return value
                .replace("\\\"", "\"")
                .replace("\\\\", "\\")
                .replace("\\n", "\n")
                .replace("\\r", "\r")
                .replace("\\t", "\t");
    }
}
