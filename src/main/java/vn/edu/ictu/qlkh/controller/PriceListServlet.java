package vn.edu.ictu.qlkh.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import vn.edu.ictu.qlkh.model.PriceList;
import vn.edu.ictu.qlkh.model.PriceListItem;
import vn.edu.ictu.qlkh.service.PriceListService;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@WebServlet("/api/v1/price-lists/*")
public class PriceListServlet extends HttpServlet {

    private PriceListService priceListService;

    @Override
    public void init() throws ServletException {
        priceListService =
                new PriceListService();
    }


    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws IOException {

        if (!canView(request)) {

            sendError(
                    response,
                    HttpServletResponse.SC_FORBIDDEN,
                    "Bạn không có quyền xem bảng giá."
            );

            return;
        }

        try {

            String[] segments =
                    getSegments(request);

            if (segments.length == 0) {

                sendPriceLists(
                        response,
                        priceListService
                                .getAllPriceLists()
                );

                return;
            }

            if (segments.length == 1) {

                long priceListId =
                        parseId(segments[0]);

                PriceList priceList =
                        priceListService
                                .getPriceListById(
                                        priceListId
                                );

                if (priceList == null) {

                    sendError(
                            response,
                            HttpServletResponse.SC_NOT_FOUND,
                            "Không tìm thấy bảng giá."
                    );

                    return;
                }

                sendPriceList(
                        response,
                        priceList
                );

                return;
            }

            if (segments.length == 2
                    && "items".equalsIgnoreCase(
                    segments[1])) {

                long priceListId =
                        parseId(segments[0]);

                List<PriceListItem> items =
                        priceListService
                                .getItems(
                                        priceListId
                                );

                sendItems(
                        response,
                        items
                );

                return;
            }

            sendError(
                    response,
                    HttpServletResponse.SC_NOT_FOUND,
                    "API không tồn tại."
            );

        } catch (NumberFormatException e) {

            sendError(
                    response,
                    HttpServletResponse.SC_BAD_REQUEST,
                    "ID không hợp lệ."
            );

        } catch (IllegalArgumentException e) {

            sendError(
                    response,
                    HttpServletResponse.SC_BAD_REQUEST,
                    e.getMessage()
            );

        } catch (SQLException e) {

            sendError(
                    response,
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Lỗi cơ sở dữ liệu."
            );
        }
    }


    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
            throws IOException {

        if (!canManage(request)) {

            sendError(
                    response,
                    HttpServletResponse.SC_FORBIDDEN,
                    "Bạn không có quyền quản lý bảng giá."
            );

            return;
        }

        request.setCharacterEncoding("UTF-8");

        try {

            String[] segments =
                    getSegments(request);

            String body =
                    readBody(request);

            if (segments.length == 0) {

                PriceList priceList =
                        readPriceList(
                                body,
                                null
                        );

                priceListService
                        .addPriceList(
                                priceList
                        );

                response.setStatus(
                        HttpServletResponse.SC_CREATED
                );

                sendPriceList(
                        response,
                        priceList
                );

                return;
            }

            if (segments.length == 2
                    && "items".equalsIgnoreCase(
                    segments[1])) {

                long priceListId =
                        parseId(
                                segments[0]
                        );

                PriceListItem item =
                        readItem(
                                body,
                                null
                        );

                PriceListItem created =
                        priceListService
                                .addItem(
                                        priceListId,
                                        item
                                );

                response.setStatus(
                        HttpServletResponse.SC_CREATED
                );

                sendItem(
                        response,
                        created
                );

                return;
            }

            sendError(
                    response,
                    HttpServletResponse.SC_NOT_FOUND,
                    "API không tồn tại."
            );

        } catch (NumberFormatException e) {

            sendError(
                    response,
                    HttpServletResponse.SC_BAD_REQUEST,
                    "ID không hợp lệ."
            );

        } catch (IllegalArgumentException e) {

            sendError(
                    response,
                    HttpServletResponse.SC_BAD_REQUEST,
                    e.getMessage()
            );

        } catch (SQLException e) {

            sendError(
                    response,
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Lỗi cơ sở dữ liệu."
            );
        }
    }


    @Override
    protected void doPut(
            HttpServletRequest request,
            HttpServletResponse response)
            throws IOException {

        if (!canManage(request)) {

            sendError(
                    response,
                    HttpServletResponse.SC_FORBIDDEN,
                    "Bạn không có quyền quản lý bảng giá."
            );

            return;
        }

        request.setCharacterEncoding("UTF-8");

        try {

            String[] segments =
                    getSegments(request);

            String body =
                    readBody(request);

            if (segments.length == 1) {

                long priceListId =
                        parseId(
                                segments[0]
                        );

                PriceList existing =
                        priceListService
                                .getPriceListById(
                                        priceListId
                                );

                if (existing == null) {

                    sendError(
                            response,
                            HttpServletResponse.SC_NOT_FOUND,
                            "Không tìm thấy bảng giá."
                    );

                    return;
                }

                PriceList input =
                        readPriceList(
                                body,
                                existing
                        );

                PriceList updated =
                        priceListService
                                .updatePriceList(
                                        priceListId,
                                        input
                                );

                sendPriceList(
                        response,
                        updated
                );

                return;
            }

            if (segments.length == 3
                    && "items".equalsIgnoreCase(
                    segments[1])) {

                long priceListId =
                        parseId(
                                segments[0]
                        );

                long itemId =
                        parseId(
                                segments[2]
                        );

                PriceListItem input =
                        readItem(
                                body,
                                null
                        );

                PriceListItem updated =
                        priceListService
                                .updateItem(
                                        priceListId,
                                        itemId,
                                        input
                                );

                sendItem(
                        response,
                        updated
                );

                return;
            }

            sendError(
                    response,
                    HttpServletResponse.SC_NOT_FOUND,
                    "API không tồn tại."
            );

        } catch (NumberFormatException e) {

            sendError(
                    response,
                    HttpServletResponse.SC_BAD_REQUEST,
                    "ID không hợp lệ."
            );

        } catch (IllegalArgumentException e) {

            sendError(
                    response,
                    HttpServletResponse.SC_BAD_REQUEST,
                    e.getMessage()
            );

        } catch (SQLException e) {

            sendError(
                    response,
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Lỗi cơ sở dữ liệu."
            );
        }
    }


    @Override
    protected void doDelete(
            HttpServletRequest request,
            HttpServletResponse response)
            throws IOException {

        if (!canManage(request)) {

            sendError(
                    response,
                    HttpServletResponse.SC_FORBIDDEN,
                    "Bạn không có quyền quản lý bảng giá."
            );

            return;
        }

        try {

            String[] segments =
                    getSegments(request);

            if (segments.length == 1) {

                long priceListId =
                        parseId(
                                segments[0]
                        );

                boolean success =
                        priceListService
                                .deleteOrDeactivatePriceList(
                                        priceListId
                                );

                if (!success) {

                    sendError(
                            response,
                            HttpServletResponse.SC_NOT_FOUND,
                            "Không tìm thấy bảng giá."
                    );

                    return;
                }

                response.setStatus(
                        HttpServletResponse.SC_NO_CONTENT
                );

                return;
            }

            if (segments.length == 3
                    && "items".equalsIgnoreCase(
                    segments[1])) {

                long priceListId =
                        parseId(
                                segments[0]
                        );

                long itemId =
                        parseId(
                                segments[2]
                        );

                boolean success =
                        priceListService
                                .deleteItem(
                                        priceListId,
                                        itemId
                                );

                if (!success) {

                    sendError(
                            response,
                            HttpServletResponse.SC_NOT_FOUND,
                            "Không tìm thấy chi tiết bảng giá."
                    );

                    return;
                }

                response.setStatus(
                        HttpServletResponse.SC_NO_CONTENT
                );

                return;
            }

            sendError(
                    response,
                    HttpServletResponse.SC_NOT_FOUND,
                    "API không tồn tại."
            );

        } catch (NumberFormatException e) {

            sendError(
                    response,
                    HttpServletResponse.SC_BAD_REQUEST,
                    "ID không hợp lệ."
            );

        } catch (IllegalArgumentException e) {

            sendError(
                    response,
                    HttpServletResponse.SC_BAD_REQUEST,
                    e.getMessage()
            );

        } catch (SQLException e) {

            sendError(
                    response,
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Lỗi cơ sở dữ liệu."
            );
        }
    }


    private PriceList readPriceList(
            String body,
            PriceList existing) {

        PriceList priceList =
                new PriceList();

        String code =
                extractString(
                        body,
                        "code",
                        false
                );

        String name =
                extractString(
                        body,
                        "name",
                        false
                );

        String startDate =
                extractString(
                        body,
                        "startDate",
                        false
                );

        String status =
                extractString(
                        body,
                        "status",
                        false
                );

        priceList.setCode(
                code != null
                        ? code
                        : existing == null
                        ? null
                        : existing.getCode()
        );

        priceList.setName(
                name != null
                        ? name
                        : existing == null
                        ? null
                        : existing.getName()
        );

        if (startDate != null) {

            priceList.setStartDate(
                    parseDate(
                            startDate,
                            "startDate"
                    )
            );

        } else if (existing != null) {

            priceList.setStartDate(
                    existing.getStartDate()
            );
        }

        if (containsKey(
                body,
                "endDate")) {

            String endDate =
                    extractString(
                            body,
                            "endDate",
                            false
                    );

            if (endDate != null
                    && !endDate.isBlank()) {

                priceList.setEndDate(
                        parseDate(
                                endDate,
                                "endDate"
                        )
                );
            }

        } else if (existing != null) {

            priceList.setEndDate(
                    existing.getEndDate()
            );
        }

        priceList.setStatus(
                status != null
                        ? status
                        : existing == null
                        ? null
                        : existing.getStatus()
        );

        if (containsKey(
                body,
                "description")) {

            priceList.setDescription(
                    extractString(
                            body,
                            "description",
                            false
                    )
            );

        } else if (existing != null) {

            priceList.setDescription(
                    existing.getDescription()
            );
        }

        return priceList;
    }


    private PriceListItem readItem(
            String body,
            PriceListItem existing) {

        PriceListItem item =
                new PriceListItem();

        Long productId =
                extractLong(
                        body,
                        "productId",
                        false
                );

        BigDecimal listPrice =
                extractDecimal(
                        body,
                        "listPrice",
                        false
                );

        BigDecimal floorPrice =
                extractDecimal(
                        body,
                        "floorPrice",
                        false
                );

        item.setProductId(
                productId != null
                        ? productId
                        : existing == null
                        ? null
                        : existing.getProductId()
        );

        item.setListPrice(
                listPrice != null
                        ? listPrice
                        : existing == null
                        ? null
                        : existing.getListPrice()
        );

        item.setFloorPrice(
                floorPrice != null
                        ? floorPrice
                        : existing == null
                        ? null
                        : existing.getFloorPrice()
        );

        return item;
    }


    private LocalDate parseDate(
            String value,
            String fieldName) {

        try {

            return LocalDate.parse(value);

        } catch (DateTimeParseException e) {

            throw new IllegalArgumentException(
                    fieldName
                            + " phải có định dạng yyyy-MM-dd."
            );
        }
    }


    private String readBody(
            HttpServletRequest request)
            throws IOException {

        String contentType =
                request.getContentType();

        if (contentType == null
                || !contentType
                .toLowerCase()
                .contains("application/json")) {

            throw new IllegalArgumentException(
                    "Content-Type phải là application/json."
            );
        }

        return request.getReader()
                .lines()
                .reduce(
                        "",
                        (a, b) -> a + b
                );
    }


    private String[] getSegments(
            HttpServletRequest request) {

        String pathInfo =
                request.getPathInfo();

        if (pathInfo == null
                || pathInfo.isBlank()
                || "/".equals(pathInfo)) {

            return new String[0];
        }

        return Arrays.stream(
                        pathInfo.split("/")
                )
                .filter(
                        value ->
                                value != null
                                        && !value.isBlank()
                )
                .toArray(String[]::new);
    }


    private long parseId(String value) {

        long id =
                Long.parseLong(value);

        if (id <= 0) {
            throw new NumberFormatException();
        }

        return id;
    }


    private String extractString(
            String json,
            String key,
            boolean required) {

        String raw =
                extractRawValue(
                        json,
                        key
                );

        if (raw == null) {

            if (required) {
                throw new IllegalArgumentException(
                        "Thiếu trường " + key + "."
                );
            }

            return null;
        }

        if ("null".equals(raw)) {
            return null;
        }

        if (!raw.startsWith("\"")
                || !raw.endsWith("\"")) {

            throw new IllegalArgumentException(
                    "Trường "
                            + key
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


    private Long extractLong(
            String json,
            String key,
            boolean required) {

        String raw =
                extractRawValue(
                        json,
                        key
                );

        if (raw == null) {

            if (required) {
                throw new IllegalArgumentException(
                        "Thiếu trường " + key + "."
                );
            }

            return null;
        }

        if ("null".equals(raw)) {
            return null;
        }

        try {

            return Long.valueOf(raw);

        } catch (NumberFormatException e) {

            throw new IllegalArgumentException(
                    "Trường "
                            + key
                            + " phải là số nguyên."
            );
        }
    }


    private BigDecimal extractDecimal(
            String json,
            String key,
            boolean required) {

        String raw =
                extractRawValue(
                        json,
                        key
                );

        if (raw == null) {

            if (required) {
                throw new IllegalArgumentException(
                        "Thiếu trường " + key + "."
                );
            }

            return null;
        }

        if ("null".equals(raw)) {
            return null;
        }

        try {

            return new BigDecimal(raw);

        } catch (NumberFormatException e) {

            throw new IllegalArgumentException(
                    "Trường "
                            + key
                            + " phải là số."
            );
        }
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

        if (!matcher.find()) {
            return null;
        }

        return matcher.group(1);
    }


    private boolean containsKey(
            String json,
            String key) {

        String regex =
                "\""
                        + Pattern.quote(key)
                        + "\"\\s*:";

        return Pattern.compile(regex)
                .matcher(json)
                .find();
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


    private boolean canView(
            HttpServletRequest request) {

        String role =
                getRole(request);

        return "ADMIN".equalsIgnoreCase(role)
                || "MANAGER".equalsIgnoreCase(role)
                || "SALES".equalsIgnoreCase(role);
    }


    private boolean canManage(
            HttpServletRequest request) {

        String role =
                getRole(request);

        return "ADMIN".equalsIgnoreCase(role)
                || "MANAGER".equalsIgnoreCase(role);
    }


    private String getRole(
            HttpServletRequest request) {

        HttpSession session =
                request.getSession(false);

        if (session == null) {
            return null;
        }

        Object role =
                session.getAttribute("userRole");

        return role == null
                ? null
                : role.toString();
    }


    private void sendPriceList(
            HttpServletResponse response,
            PriceList priceList)
            throws IOException {

        prepareJsonResponse(response);

        response.getWriter().write(
                priceListToJson(
                        priceList
                )
        );
    }


    private void sendPriceLists(
            HttpServletResponse response,
            List<PriceList> priceLists)
            throws IOException {

        prepareJsonResponse(response);

        StringBuilder json =
                new StringBuilder("[");

        for (int i = 0;
             i < priceLists.size();
             i++) {

            if (i > 0) {
                json.append(",");
            }

            json.append(
                    priceListToJson(
                            priceLists.get(i)
                    )
            );
        }

        json.append("]");

        response.getWriter().write(
                json.toString()
        );
    }


    private void sendItem(
            HttpServletResponse response,
            PriceListItem item)
            throws IOException {

        prepareJsonResponse(response);

        response.getWriter().write(
                itemToJson(item)
        );
    }


    private void sendItems(
            HttpServletResponse response,
            List<PriceListItem> items)
            throws IOException {

        prepareJsonResponse(response);

        StringBuilder json =
                new StringBuilder("[");

        for (int i = 0;
             i < items.size();
             i++) {

            if (i > 0) {
                json.append(",");
            }

            json.append(
                    itemToJson(
                            items.get(i)
                    )
            );
        }

        json.append("]");

        response.getWriter().write(
                json.toString()
        );
    }


    private String priceListToJson(
            PriceList priceList) {

        return "{"
                + "\"id\":"
                + priceList.getId()
                + ","
                + "\"code\":"
                + jsonString(
                priceList.getCode())
                + ","
                + "\"name\":"
                + jsonString(
                priceList.getName())
                + ","
                + "\"startDate\":"
                + jsonString(
                priceList.getStartDate() == null
                        ? null
                        : priceList
                        .getStartDate()
                        .toString())
                + ","
                + "\"endDate\":"
                + jsonString(
                priceList.getEndDate() == null
                        ? null
                        : priceList
                        .getEndDate()
                        .toString())
                + ","
                + "\"status\":"
                + jsonString(
                priceList.getStatus())
                + ","
                + "\"description\":"
                + jsonString(
                priceList.getDescription())
                + "}";
    }


    private String itemToJson(
            PriceListItem item) {

        return "{"
                + "\"id\":"
                + item.getId()
                + ","
                + "\"priceListId\":"
                + item.getPriceListId()
                + ","
                + "\"productId\":"
                + item.getProductId()
                + ","
                + "\"productCode\":"
                + jsonString(
                item.getProductCode())
                + ","
                + "\"productName\":"
                + jsonString(
                item.getProductName())
                + ","
                + "\"productType\":"
                + jsonString(
                item.getProductType())
                + ","
                + "\"unit\":"
                + jsonString(
                item.getUnit())
                + ","
                + "\"listPrice\":"
                + jsonNumber(
                item.getListPrice())
                + ","
                + "\"floorPrice\":"
                + jsonNumber(
                item.getFloorPrice())
                + "}";
    }


    private String jsonNumber(
            BigDecimal value) {

        return value == null
                ? "null"
                : value.toPlainString();
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


    private void prepareJsonResponse(
            HttpServletResponse response) {

        response.setCharacterEncoding("UTF-8");

        response.setContentType(
                "application/json;charset=UTF-8"
        );
    }


    private void sendError(
            HttpServletResponse response,
            int status,
            String message)
            throws IOException {

        response.setStatus(status);

        prepareJsonResponse(response);

        response.getWriter().write(
                "{"
                        + "\"success\":false,"
                        + "\"message\":"
                        + jsonString(message)
                        + "}"
        );
    }


    private String escapeJson(
            String value) {

        if (value == null) {
            return "";
        }

        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\r", "\\r")
                .replace("\n", "\\n")
                .replace("\t", "\\t");
    }
}