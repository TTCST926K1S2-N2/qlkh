(() => {
    "use strict";

    const contextPath =
        document.body.dataset.contextPath || "";

    const form =
        document.getElementById("profileForm");

    const fullName =
        document.getElementById("fullName");

    const email =
        document.getElementById("email");

    const phone =
        document.getElementById("phone");

    const role =
        document.getElementById("role");

    const emailSignature =
        document.getElementById("emailSignature");

    const saveButton =
        document.getElementById("saveProfileButton");

    const cancelButton =
        document.getElementById("cancelProfileButton");

    const alertBox =
        document.getElementById("profileAlert");

    const loadingBox =
        document.getElementById("profileLoading");

    const formContent =
        document.getElementById("profileFormContent");

    let initialProfile = null;


    function normalizePhone(value) {
        return value
            .trim()
            .replace(/[\s.-]/g, "");
    }


    function showAlert(message, type) {

        alertBox.textContent = message;

        alertBox.className =
            "profile-alert " +
            (type === "success"
                ? "is-success"
                : "is-error");

        alertBox.hidden = false;
    }


    function hideAlert() {

        alertBox.hidden = true;
        alertBox.textContent = "";

    }


    function setFieldError(
        input,
        errorId,
        message
    ) {

        const error =
            document.getElementById(errorId);

        input.classList.toggle(
            "is-invalid",
            Boolean(message)
        );

        error.textContent =
            message || "";

    }


    function validateForm() {

        let valid = true;

        const nameValue =
            fullName.value.trim();

        const phoneValue =
            normalizePhone(phone.value);


        setFieldError(
            fullName,
            "fullNameError",
            ""
        );

        setFieldError(
            phone,
            "phoneError",
            ""
        );


        if (!nameValue) {

            setFieldError(
                fullName,
                "fullNameError",
                "Họ và tên không được để trống."
            );

            valid = false;
        }


        if (
            phoneValue &&
            !/^(0|\+84)[35789]\d{8}$/.test(phoneValue)
        ) {

            setFieldError(
                phone,
                "phoneError",
                "Số điện thoại Việt Nam không đúng định dạng."
            );

            valid = false;
        }


        return valid;
    }


    function roleLabel(value) {

        const labels = {
            ADMIN: "Quản trị viên",
            USER: "Người dùng",
            MANAGER: "Quản lý",
            SALES: "Nhân viên"
        };

        return labels[value] || value || "";
    }


    function fillForm(data) {

        fullName.value =
            data.fullName || "";

        email.value =
            data.email || "";

        phone.value =
            data.phone || "";

        role.value =
            roleLabel(data.role);

        emailSignature.value =
            data.emailSignature || "";

    }


    async function loadProfile() {

        hideAlert();

        loadingBox.hidden = false;
        formContent.hidden = true;


        try {

            const response =
                await fetch(
                    contextPath + "/profile",
                    {
                        method: "GET",
                        credentials: "same-origin",
                        headers: {
                            "Accept": "application/json"
                        }
                    }
                );


            const result =
                await response.json();


            if (response.status === 401) {

                window.location.href =
                    contextPath + "/login";

                return;
            }


            if (
                !response.ok ||
                !result.success ||
                !result.data
            ) {

                throw new Error(
                    result.message ||
                    "Không thể tải hồ sơ cá nhân."
                );
            }


            initialProfile = {
                ...result.data
            };


            fillForm(initialProfile);

            formContent.hidden = false;

        }
        catch (error) {

            showAlert(
                error.message ||
                "Không thể tải hồ sơ cá nhân.",
                "error"
            );

        }
        finally {

            loadingBox.hidden = true;

        }

    }


    form.addEventListener(
        "submit",
        async (event) => {

            event.preventDefault();

            hideAlert();


            if (!validateForm()) {
                return;
            }


            const oldText =
                saveButton.textContent;


            saveButton.disabled = true;
            cancelButton.disabled = true;

            saveButton.textContent =
                "Đang lưu...";


            try {

                const body =
                    new URLSearchParams({
                        fullName:
                            fullName.value.trim(),

                        phone:
                            normalizePhone(
                                phone.value
                            ),

                        emailSignature:
                            emailSignature.value.trim()
                    });


                const response =
                    await fetch(
                        contextPath + "/profile",
                        {
                            method: "POST",

                            credentials:
                                "same-origin",

                            headers: {
                                "Content-Type":
                                    "application/x-www-form-urlencoded;charset=UTF-8",

                                "Accept":
                                    "application/json"
                            },

                            body: body
                        }
                    );


                const result =
                    await response.json();


                if (
                    !response.ok ||
                    !result.success
                ) {

                    throw new Error(
                        result.message ||
                        "Cập nhật hồ sơ thất bại."
                    );
                }


                initialProfile = {
                    ...result.data
                };


                fillForm(initialProfile);


                showAlert(
                    "Cập nhật hồ sơ thành công.",
                    "success"
                );

            }
            catch (error) {

                showAlert(
                    error.message ||
                    "Cập nhật hồ sơ thất bại.",
                    "error"
                );

            }
            finally {

                saveButton.disabled = false;
                cancelButton.disabled = false;

                saveButton.textContent =
                    oldText;

            }

        }
    );


    cancelButton.addEventListener(
        "click",
        () => {

            if (initialProfile) {
                fillForm(initialProfile);
            }

            setFieldError(
                fullName,
                "fullNameError",
                ""
            );

            setFieldError(
                phone,
                "phoneError",
                ""
            );

            hideAlert();

        }
    );


    fullName.addEventListener(
        "input",
        () => {

            setFieldError(
                fullName,
                "fullNameError",
                ""
            );

        }
    );


    phone.addEventListener(
        "input",
        () => {

            setFieldError(
                phone,
                "phoneError",
                ""
            );

        }
    );


    loadProfile();

})();