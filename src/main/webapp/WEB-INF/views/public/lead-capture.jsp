<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Đăng ký tư vấn</title>
    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/assets/css/lead-capture.css">
</head>
<body>
    <main class="lead-page">
        <section class="lead-card">
            <div class="lead-brand">
                <span class="lead-logo">Q</span>
                <span>HỆ THỐNG QLKH</span>
            </div>

            <header class="lead-heading">
                <h1>Đăng ký nhận tư vấn</h1>
                <p>Vui lòng để lại thông tin để đội ngũ tư vấn liên hệ với bạn.</p>
            </header>

            <form id="leadCaptureForm" novalidate>
                <input type="hidden"
                       id="formId"
                       name="formId"
                       value="public-consultation">
                <div class="form-group">
                    <label for="fullName">Họ và tên <span>*</span></label>
                    <input id="fullName" name="fullName"
                           type="text" maxlength="150"
                           autocomplete="name"
                           placeholder="Nhập họ và tên" required>
                </div>

                <div class="form-row">
                    <div class="form-group">
                        <label for="email">Email <span>*</span></label>
                        <input id="email" name="email"
                               type="email" maxlength="254"
                               autocomplete="email"
                               placeholder="example@gmail.com" required>
                    </div>

                    <div class="form-group">
                        <label for="phone">Số điện thoại <span>*</span></label>
                        <input id="phone" name="phone"
                               type="tel" maxlength="15"
                               autocomplete="tel"
                               placeholder="Nhập số điện thoại" required>
                    </div>
                </div>

                <div class="form-group">
                    <label for="company">Công ty</label>
                    <input id="company" name="company"
                           type="text" maxlength="200"
                           autocomplete="organization"
                           placeholder="Tên công ty (nếu có)">
                </div>

                <div class="form-group">
                    <label for="interest">Nhu cầu quan tâm <span>*</span></label>
                    <textarea id="interest" name="interest"
                              rows="4" maxlength="2000"
                              placeholder="Mô tả nhu cầu của bạn"
                              required></textarea>
                </div>

                <p class="required-note">* Thông tin bắt buộc</p>

                <div id="leadMessage" class="lead-message"
                     role="status" aria-live="polite" hidden></div>

                <button type="submit"
                        id="leadSubmitButton"
                        class="submit-btn">
                    <span id="leadSubmitText">Gửi yêu cầu tư vấn</span>
                </button>
            </form>

            <section class="lead-embed" aria-labelledby="embedHeading">
                <h2 id="embedHeading">Lấy mã nhúng biểu mẫu</h2>
                <p>Sao chép mã HTML để nhúng biểu mẫu vào website khác.</p>

                <textarea id="embedCode"
                          rows="4"
                          readonly
                          aria-label="Mã nhúng iframe"></textarea>

                <button type="button"
                        id="copyEmbedCode"
                        class="submit-btn">
                    Sao chép mã nhúng
                </button>

                <p id="embedMessage"
                   role="status"
                   aria-live="polite"></p>
            </section>
        </section>
    </main>

    <script src="${pageContext.request.contextPath}/assets/js/lead-capture.js"></script>
</body>
</html>