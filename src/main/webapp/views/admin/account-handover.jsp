<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>Khóa tài khoản & Bàn giao dữ liệu</title>

    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css"
          rel="stylesheet">
</head>

<body class="bg-light">

<div class="container mt-5">
    <div class="row justify-content-center">
        <div class="col-md-8">

            <div class="card shadow border-danger">

                <div class="card-header bg-danger text-white">
                    <h4 class="mb-0">
                        Khóa tài khoản & Bàn giao dữ liệu khách hàng
                    </h4>
                </div>

                <div class="card-body">

                    <form action="${pageContext.request.contextPath}/users/handover"
                          method="POST">

                        <!-- ID tài khoản đang thao tác -->
                        <input type="hidden"
                               name="userId"
                               value="${user.id}">

                        <div class="alert alert-warning"
                             role="alert">

                            Bạn đang thao tác với tài khoản của nhân sự:

                            <strong>
                                <c:out value="${user.fullName}" />
                            </strong>.

                        </div>

                        <!-- Trạng thái tài khoản -->
                        <div class="mb-3">

                            <label for="status"
                                   class="form-label fw-bold">
                                Trạng thái tài khoản
                            </label>

                            <select class="form-select"
                                    id="status"
                                    name="status">

                                <option value="LOCKED"
                                    ${user.status == 'LOCKED' ? 'selected' : ''}>
                                    Khóa tài khoản (Lock)
                                </option>

                                <option value="ACTIVE"
                                    ${user.status == 'ACTIVE' ? 'selected' : ''}>
                                    Mở khóa (Active)
                                </option>

                            </select>

                        </div>

                        <!-- Người nhận bàn giao -->
                        <div class="mb-4"
                             id="handoverSection">

                            <label for="targetUserId"
                                   class="form-label fw-bold">
                                Chọn nhân sự nhận bàn giao dữ liệu
                            </label>

                            <select class="form-select"
                                    id="targetUserId"
                                    name="targetUserId">

                                <option value="">
                                    -- Chọn nhân sự thay thế --
                                </option>

                                <!-- Danh sách nhân sự do Backend truyền sang -->
                                <c:forEach var="targetUser"
                                           items="${targetUsers}">

                                    <c:if test="${targetUser.id != user.id}">
                                        <option value="${targetUser.id}">
                                            <c:out value="${targetUser.fullName}" />
                                        </option>
                                    </c:if>

                                </c:forEach>

                            </select>

                            <c:if test="${empty targetUsers}">
                                <div class="text-muted mt-2">
                                    Chưa có nhân sự phù hợp để nhận bàn giao.
                                </div>
                            </c:if>

                        </div>

                        <div class="d-flex justify-content-between">

                            <a href="${pageContext.request.contextPath}/users"
                               class="btn btn-secondary">
                                Quay lại
                            </a>

                            <button type="submit"
                                    id="submitButton"
                                    class="btn btn-danger">
                                Xác nhận khóa và bàn giao
                            </button>

                        </div>

                    </form>

                </div>
            </div>

        </div>
    </div>
</div>

<script>
    const statusSelect = document.getElementById("status");
    const targetUserSelect = document.getElementById("targetUserId");
    const handoverSection = document.getElementById("handoverSection");
    const submitButton = document.getElementById("submitButton");

    function updateHandoverForm() {
        const isLocked = statusSelect.value === "LOCKED";

        handoverSection.style.display = isLocked ? "block" : "none";
        targetUserSelect.required = isLocked;

        if (isLocked) {
            submitButton.textContent = "Xác nhận khóa và bàn giao";
            submitButton.className = "btn btn-danger";
        } else {
            targetUserSelect.value = "";
            submitButton.textContent = "Xác nhận mở khóa";
            submitButton.className = "btn btn-success";
        }
    }

    statusSelect.addEventListener("change", updateHandoverForm);

    updateHandoverForm();
</script>

</body>
</html>