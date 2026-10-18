package vn.edu.ictu.qlkh.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import vn.edu.ictu.qlkh.model.Product;
import vn.edu.ictu.qlkh.service.ProductService;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@WebServlet("/api/v1/products/*")
public class ProductServlet extends HttpServlet {

    private ProductService productService;

    @Override
    public void init() throws ServletException {
        productService = new ProductService();
    }


    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws IOException {

        if (!canViewProducts(request)) {

            sendError(
                    response,
                    HttpServletResponse.SC_FORBIDDEN,
                    "Bạn không có quyền xem sản phẩm/dịch vụ."
            );

            return;
        }

        try {

            String pathInfo =
                    request.getPathInfo();

            boolean showCostPrice =
                    canViewCostPrice(request);

            if (pathInfo == null
                    || "/".equals(pathInfo)) {

                List<Product> products =
                        productService.getAllProducts();

                sendProducts(
                        response,
                        products,
                        showCostPrice
                );

                return;
            }

            long id =
                    parseId(pathInfo);

            Product product =
                    productService.getProductById(id);

            if (product == null) {

                sendError(
                        response,
                        HttpServletResponse.SC_NOT_FOUND,
                        "Không tìm thấy sản phẩm/dịch vụ."
                );

                return;
            }

            sendProduct(
                    response,
                    product,
                    showCostPrice
            );

        } catch (NumberFormatException e) {

            sendError(
                    response,
                    HttpServletResponse.SC_BAD_REQUEST,
                    "ID sản phẩm/dịch vụ không hợp lệ."
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

        if (!canManageProducts(request)) {

            sendError(
                    response,
                    HttpServletResponse.SC_FORBIDDEN,
                    "Bạn không có quyền quản lý sản phẩm/dịch vụ."
            );

            return;
        }

        request.setCharacterEncoding("UTF-8");

        try {

            String body =
                    readBody(request);

            Product product =
                    readProduct(
                            body,
                            request,
                            false
                    );

            productService.addProduct(product);

            response.setStatus(
                    HttpServletResponse.SC_CREATED
            );

            sendProduct(
                    response,
                    product,
                    canViewCostPrice(request)
            );

        } catch (IllegalArgumentException e) {

            sendError(
                    response,
                    HttpServletResponse.SC_BAD_REQUEST,
                    e.getMessage()
            );

        } catch (SecurityException e) {

            sendError(
                    response,
                    HttpServletResponse.SC_FORBIDDEN,
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

        if (!canManageProducts(request)) {

            sendError(
                    response,
                    HttpServletResponse.SC_FORBIDDEN,
                    "Bạn không có quyền quản lý sản phẩm/dịch vụ."
            );

            return;
        }

        request.setCharacterEncoding("UTF-8");

        try {

            long id =
                    parseId(
                            request.getPathInfo()
                    );

            Product existing =
                    productService.getProductById(id);

            if (existing == null) {

                sendError(
                        response,
                        HttpServletResponse.SC_NOT_FOUND,
                        "Không tìm thấy sản phẩm/dịch vụ."
                );

                return;
            }

            String body =
                    readBody(request);

            Product product =
                    readProduct(
                            body,
                            request,
                            true
                    );

            productService.updateProduct(
                    id,
                    product
            );

            sendProduct(
                    response,
                    product,
                    canViewCostPrice(request)
            );

        } catch (NumberFormatException e) {

            sendError(
                    response,
                    HttpServletResponse.SC_BAD_REQUEST,
                    "ID sản phẩm/dịch vụ không hợp lệ."
            );

        } catch (IllegalArgumentException e) {

            sendError(
                    response,
                    HttpServletResponse.SC_BAD_REQUEST,
                    e.getMessage()
            );

        } catch (SecurityException e) {

            sendError(
                    response,
                    HttpServletResponse.SC_FORBIDDEN,
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

        if (!canManageProducts(request)) {

            sendError(
                    response,
                    HttpServletResponse.SC_FORBIDDEN,
                    "Bạn không có quyền quản lý sản phẩm/dịch vụ."
            );

            return;
        }

        try {

            long id =
                    parseId(
                            request.getPathInfo()
                    );

            boolean success =
                    productService
                            .deleteOrDeactivateProduct(id);

            if (!success) {

                sendError(
                        response,
                        HttpServletResponse.SC_NOT_FOUND,
                        "Không tìm thấy sản phẩm/dịch vụ."
                );

                return;
            }

            response.setStatus(
                    HttpServletResponse.SC_NO_CONTENT
            );

        } catch (NumberFormatException e) {

            sendError(
                    response,
                    HttpServletResponse.SC_BAD_REQUEST,
                    "ID sản phẩm/dịch vụ không hợp lệ."
            );

        } catch (SQLException e) {

            sendError(
                    response,
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Lỗi cơ sở dữ liệu."
            );
        }
    }


    private Product readProduct(
            String body,
            HttpServletRequest request,
            boolean updating) {

        if (body == null
                || body.isBlank()) {

            throw new IllegalArgumentException(
                    "Request body không được để trống."
            );
        }

        Product product =
                new Product();

        product.setCode(
                extractString(
                        body,
                        "code",
                        true
                )
        );

        product.setName(
                extractString(
                        body,
                        "name",
                        true
                )
        );

        product.setType(
                extractString(
                        body,
                        "type",
                        true
                )
        );

        product.setUnit(
                extractString(
                        body,
                        "unit",
                        true
                )
        );

        product.setBasePrice(
                extractDecimal(
                        body,
                        "basePrice",
                        true
                )
        );

        product.setStatus(
                extractString(
                        body,
                        "status",
                        false
                )
        );

        product.setDescription(
                extractString(
                        body,
                        "description",
                        false
                )
        );

        if (containsKey(
                body,
                "floorPrice")) {

            product.setFloorPrice(
                    extractDecimal(
                            body,
                            "floorPrice",
                            false
                    )
            );

        } else if (!updating) {

            product.setFloorPrice(
                    product.getBasePrice()
            );
        }

        if (containsKey(
                body,
                "costPrice")) {

            if (!canViewCostPrice(request)) {

                throw new SecurityException(
                        "Chỉ Giám đốc kinh doanh được xem và sửa giá vốn."
                );
            }

            product.setCostPrice(
                    extractDecimal(
                            body,
                            "costPrice",
                            false
                    )
            );
        }

        return product;
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
                    "Trường " + key
                            + " phải là chuỗi."
            );
        }

        String value =
                raw.substring(
                        1,
                        raw.length() - 1
                );

        return unescapeJson(value);
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
                    "Trường " + key
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


    private long parseId(
            String pathInfo) {

        if (pathInfo == null
                || pathInfo.isBlank()
                || "/".equals(pathInfo)) {

            throw new NumberFormatException();
        }

        String value =
                pathInfo.startsWith("/")
                        ? pathInfo.substring(1)
                        : pathInfo;

        if (value.contains("/")) {
            throw new NumberFormatException();
        }

        return Long.parseLong(value);
    }


    private boolean canViewProducts(
            HttpServletRequest request) {

        String role =
                getRole(request);

        return "SALES".equalsIgnoreCase(role)
                || "MANAGER".equalsIgnoreCase(role)
                || "ADMIN".equalsIgnoreCase(role);
    }


    private boolean canManageProducts(
            HttpServletRequest request) {

        String role =
                getRole(request);

        return "MANAGER".equalsIgnoreCase(role)
                || "ADMIN".equalsIgnoreCase(role);
    }


    private boolean canViewCostPrice(
            HttpServletRequest request) {

        return "MANAGER".equalsIgnoreCase(
                getRole(request)
        );
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


    private void sendProduct(
            HttpServletResponse response,
            Product product,
            boolean includeCostPrice)
            throws IOException {

        response.setCharacterEncoding("UTF-8");

        response.setContentType(
                "application/json;charset=UTF-8"
        );

        response.getWriter().write(
                productToJson(
                        product,
                        includeCostPrice
                )
        );
    }


    private void sendProducts(
            HttpServletResponse response,
            List<Product> products,
            boolean includeCostPrice)
            throws IOException {

        response.setCharacterEncoding("UTF-8");

        response.setContentType(
                "application/json;charset=UTF-8"
        );

        StringBuilder json =
                new StringBuilder("[");

        for (int i = 0;
             i < products.size();
             i++) {

            if (i > 0) {
                json.append(",");
            }

            json.append(
                    productToJson(
                            products.get(i),
                            includeCostPrice
                    )
            );
        }

        json.append("]");

        response.getWriter()
                .write(
                        json.toString()
                );
    }


    private String productToJson(
            Product product,
            boolean includeCostPrice) {

        StringBuilder json =
                new StringBuilder();

        json.append("{")
                .append("\"id\":")
                .append(product.getId())
                .append(",")
                .append("\"code\":")
                .append(jsonString(
                        product.getCode()))
                .append(",")
                .append("\"name\":")
                .append(jsonString(
                        product.getName()))
                .append(",")
                .append("\"type\":")
                .append(jsonString(
                        product.getType()))
                .append(",")
                .append("\"unit\":")
                .append(jsonString(
                        product.getUnit()))
                .append(",")
                .append("\"basePrice\":")
                .append(jsonNumber(
                        product.getBasePrice()))
                .append(",")
                .append("\"floorPrice\":")
                .append(jsonNumber(
                        product.getFloorPrice()));

        if (includeCostPrice) {

            json.append(",")
                    .append("\"costPrice\":")
                    .append(jsonNumber(
                            product.getCostPrice()));
        }

        json.append(",")
                .append("\"status\":")
                .append(jsonString(
                        product.getStatus()))
                .append(",")
                .append("\"description\":")
                .append(jsonString(
                        product.getDescription()))
                .append("}");

        return json.toString();
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


    private String jsonNumber(
            BigDecimal value) {

        return value == null
                ? "null"
                : value.toPlainString();
    }


    private void sendError(
            HttpServletResponse response,
            int status,
            String message)
            throws IOException {

        response.setStatus(status);

        response.setCharacterEncoding("UTF-8");

        response.setContentType(
                "application/json;charset=UTF-8"
        );

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