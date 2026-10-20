document.addEventListener("DOMContentLoaded", function () {

    const container =
        document.getElementById("permission-container");

    if (!container) {
        return;
    }

    const scope =
        (container.dataset.scope || "")
            .trim()
            .toUpperCase();

    const scopeMap = {
        MY: "Của tôi",
        TEAM: "Của nhóm tôi",
        ALL: "Tất cả"
    };

    const scopeText =
        scopeMap[scope] || "Chưa xác định";


    /*
     * Cập nhật nhãn phạm vi tại các loại dữ liệu.
     */
    const labels =
        document.querySelectorAll(".scope-label");

    labels.forEach(function (label) {
        label.textContent = scopeText;
    });


    /*
     * Cập nhật phần tóm tắt phạm vi hiện tại.
     */
    const currentScopeText =
        document.getElementById("current-scope-text");

    if (currentScopeText) {
        currentScopeText.textContent = scopeText;
    }


    /*
     * Tạo chữ cái đại diện từ tên người dùng.
     * Chỉ phục vụ giao diện, không liên quan phân quyền.
     */
    const avatar =
        document.getElementById("account-avatar");

    const accountName =
        document.querySelector(".account-name");

    if (avatar && accountName) {

        const name =
            accountName.textContent.trim();

        if (
            name &&
            name !== "Chưa có thông tin"
        ) {

            const words =
                name.split(/\s+/)
                    .filter(Boolean);

            let initials = "";

            if (words.length === 1) {
                initials =
                    words[0]
                        .substring(0, 2);
            } else {
                initials =
                    words[0].charAt(0) +
                    words[words.length - 1].charAt(0);
            }

            avatar.textContent =
                initials.toUpperCase();
        }
    }
});