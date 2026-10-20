(function () {

    const modal = document.getElementById("session-warning-modal");

    if (!modal) return;


    // Lấy contextPath động từ JSP
    const rawContextPath =
        modal.getAttribute("data-context-path");

    const contextPath =
        (rawContextPath && rawContextPath !== "/")
            ? rawContextPath
            : "";


    const countdownEl =
        document.getElementById("session-countdown");

    const alertEl =
        document.getElementById("session-modal-alert");

    const btnExtend =
        document.getElementById("btn-extend-session");

    const btnLogout =
        document.getElementById("btn-logout-session");


    /*
     * HTQLKH-2
     *
     * 14 phút không hoạt động -> hiện cảnh báo.
     * Sau đó đếm ngược thêm 60 giây.
     *
     * Tổng cộng:
     * 14 phút + 60 giây = 15 phút.
     */
    const IDLE_TIMEOUT_MS = 14 * 60 * 1000;

    const COUNTDOWN_SECONDS = 60;


    /*
     * Khi người dùng còn hoạt động,
     * tối đa khoảng 1 request keep-alive/phút.
     *
     * Không gửi request liên tục theo từng mousemove.
     */
    const KEEP_ALIVE_INTERVAL_MS = 60 * 1000;


    let idleTimer = null;

    let countdownInterval = null;

    let keepAliveTimer = null;

    let secondsLeft = COUNTDOWN_SECONDS;

    let lastKeepAliveAt = Date.now();

    let keepAliveInFlight = false;


    /*
     * =========================================================
     * HIỂN THỊ THÔNG BÁO TRONG MODAL
     * =========================================================
     */

    function showAlert(message) {

        if (!alertEl) return;

        alertEl.textContent = message;

        alertEl.classList.remove(
            "session-modal--hidden"
        );
    }


    function clearAlert() {

        if (!alertEl) return;

        alertEl.textContent = "";

        alertEl.classList.add(
            "session-modal--hidden"
        );
    }


    /*
     * =========================================================
     * RESET BỘ ĐẾM IDLE
     * =========================================================
     */

    function resetIdleTimer() {

        /*
         * Nếu modal cảnh báo đang mở
         * thì không tự reset nữa.
         */
        if (
            !modal.classList.contains(
                "session-modal--hidden"
            )
        ) {
            return;
        }

        clearTimeout(idleTimer);

        idleTimer =
            setTimeout(
                showWarningModal,
                IDLE_TIMEOUT_MS
            );
    }


    /*
     * =========================================================
     * TỰ ĐỘNG GIA HẠN SESSION
     * =========================================================
     *
     * Hàm này được gọi khi người dùng vẫn còn hoạt động.
     *
     * Backend:
     * POST /extend-session
     */

    async function sendKeepAlive() {

        /*
         * Không gửi request thứ hai nếu request trước
         * vẫn chưa hoàn thành.
         */
        if (keepAliveInFlight) {
            return;
        }


        /*
         * Nếu modal hết phiên đang mở
         * thì không tự gia hạn nữa.
         */
        if (
            !modal.classList.contains(
                "session-modal--hidden"
            )
        ) {
            return;
        }


        keepAliveInFlight = true;


        try {

            const response =
                await fetch(
                    `${contextPath}/extend-session`,
                    {
                        method: "POST",

                        headers: {
                            "X-Requested-With":
                                "XMLHttpRequest",

                            "Content-Type":
                                "application/x-www-form-urlencoded"
                        },

                        cache: "no-store"
                    }
                );


            /*
             * Gia hạn thành công.
             */
            if (response.ok) {

                lastKeepAliveAt =
                    Date.now();

                return;
            }


            /*
             * Backend trả 401:
             * session đã hết hạn hoặc không hợp lệ.
             */
            if (response.status === 401) {

                window.location.href =
                    `${contextPath}/login?expired=1`;

                return;
            }


        } catch (error) {

            /*
             * Không tự logout chỉ vì lỗi mạng tạm thời.
             *
             * Server vẫn có timeout 15 phút.
             */
            console.warn(
                "Không thể tự động gia hạn phiên.",
                error
            );


        } finally {

            keepAliveInFlight = false;
        }
    }


    /*
     * =========================================================
     * XỬ LÝ HOẠT ĐỘNG CỦA NGƯỜI DÙNG
     * =========================================================
     */

    function handleUserActivity() {

        /*
         * Modal đang hiện thì không tính hoạt động
         * phía sau modal.
         */
        if (
            !modal.classList.contains(
                "session-modal--hidden"
            )
        ) {
            return;
        }


        /*
         * Người dùng vừa thao tác:
         * reset bộ đếm 14 phút.
         */
        resetIdleTimer();


        /*
         * Đã có keep-alive được lên lịch
         * thì không tạo thêm timer.
         */
        if (keepAliveTimer !== null) {
            return;
        }


        const elapsed =
            Date.now() - lastKeepAliveAt;


        const delay =
            Math.max(
                0,
                KEEP_ALIVE_INTERVAL_MS - elapsed
            );


        /*
         * Giới hạn request keep-alive:
         * tối đa khoảng 1 lần/phút.
         */
        keepAliveTimer =
            setTimeout(
                async function () {

                    keepAliveTimer = null;

                    await sendKeepAlive();

                },
                delay
            );
    }


    /*
     * =========================================================
     * KẾT THÚC SESSION DO HẾT THỜI GIAN
     * =========================================================
     *
     * Khi người dùng:
     *
     * 14 phút không hoạt động
     *          +
     * 60 giây cảnh báo
     *
     * => kết thúc phiên.
     *
     * Không chỉ redirect phía trình duyệt.
     * Phải yêu cầu backend invalidate HttpSession.
     */

    async function expireSession() {

        /*
         * Dừng tất cả timer phía client.
         */
        clearTimeout(idleTimer);

        clearTimeout(keepAliveTimer);

        clearInterval(countdownInterval);

        keepAliveTimer = null;


        try {

            /*
             * Logout phía server để session
             * mất hiệu lực ngay lập tức.
             */
            await fetch(
                `${contextPath}/logout`,
                {
                    method: "POST",

                    headers: {
                        "X-Requested-With":
                            "XMLHttpRequest"
                    },

                    cache: "no-store"
                }
            );


        } catch (error) {

            /*
             * Nếu mạng lỗi thì không thể yêu cầu logout.
             *
             * Tuy nhiên backend vẫn có
             * maxInactiveInterval = 15 phút.
             */
            console.warn(
                "Không thể gửi yêu cầu kết thúc phiên.",
                error
            );


        } finally {

            /*
             * Luôn đưa người dùng về trang đăng nhập
             * với thông báo hết hạn phiên.
             */
            window.location.href =
                `${contextPath}/login?expired=1`;
        }
    }


    /*
     * =========================================================
     * HIỆN MODAL CẢNH BÁO
     * =========================================================
     */

    function showWarningModal() {

        /*
         * Đã bước vào trạng thái idle.
         *
         * Không được gửi keep-alive tự động nữa.
         */
        clearTimeout(keepAliveTimer);

        keepAliveTimer = null;


        secondsLeft =
            COUNTDOWN_SECONDS;


        if (countdownEl) {

            countdownEl.textContent =
                secondsLeft;
        }


        clearAlert();


        if (btnExtend) {

            btnExtend.disabled =
                false;
        }


        if (btnLogout) {

            btnLogout.disabled =
                false;
        }


        /*
         * Hiện modal.
         */
        modal.classList.remove(
            "session-modal--hidden"
        );


        clearInterval(
            countdownInterval
        );


        /*
         * Đếm ngược 60 giây.
         */
        countdownInterval =
            setInterval(
                function () {

                    secondsLeft--;


                    if (countdownEl) {

                        countdownEl.textContent =
                            secondsLeft;
                    }


                    /*
                     * Hết 60 giây.
                     */
                    if (secondsLeft <= 0) {

                        clearInterval(
                            countdownInterval
                        );


                        /*
                         * HTQLKH-2:
                         *
                         * Hủy session phía server
                         * rồi mới chuyển về login.
                         */
                        expireSession();
                    }

                },
                1000
            );
    }


    /*
     * =========================================================
     * ẨN MODAL SAU KHI GIA HẠN THÀNH CÔNG
     * =========================================================
     */

    function hideModal() {

        modal.classList.add(
            "session-modal--hidden"
        );


        clearInterval(
            countdownInterval
        );


        clearAlert();


        /*
         * Bắt đầu lại bộ đếm idle 14 phút.
         */
        resetIdleTimer();
    }


    /*
     * =========================================================
     * NGƯỜI DÙNG CHỦ ĐỘNG GIA HẠN PHIÊN
     * =========================================================
     */

    async function extendSession() {

        if (btnExtend) {

            btnExtend.disabled =
                true;
        }


        clearAlert();


        try {

            const response =
                await fetch(
                    `${contextPath}/extend-session`,
                    {
                        method: "POST",

                        headers: {
                            "X-Requested-With":
                                "XMLHttpRequest",

                            "Content-Type":
                                "application/x-www-form-urlencoded"
                        },

                        cache: "no-store"
                    }
                );


            /*
             * Backend xác nhận session còn hợp lệ.
             */
            if (response.ok) {

                lastKeepAliveAt =
                    Date.now();


                hideModal();

                return;
            }


            /*
             * Session đã hết hạn.
             */
            if (response.status === 401) {

                window.location.href =
                    `${contextPath}/login?expired=1`;

                return;
            }


            throw new Error(
                `Gia hạn thất bại (Mã lỗi: ${response.status}).`
            );


        } catch (error) {

            showAlert(
                error.message ||
                "Không thể kết nối đến máy chủ để gia hạn phiên."
            );


        } finally {

            /*
             * Nếu modal vẫn đang mở
             * thì bật lại nút gia hạn.
             */
            if (
                btnExtend &&
                !modal.classList.contains(
                    "session-modal--hidden"
                )
            ) {

                btnExtend.disabled =
                    false;
            }
        }
    }


    /*
     * =========================================================
     * ĐĂNG XUẤT CHỦ ĐỘNG
     * =========================================================
     *
     * Backend phải invalidate HttpSession.
     */

    async function performLogout() {

        if (btnLogout) {

            btnLogout.disabled =
                true;
        }


        if (btnExtend) {

            btnExtend.disabled =
                true;
        }


        clearAlert();


        try {

            const response =
                await fetch(
                    `${contextPath}/logout`,
                    {
                        method: "POST",

                        headers: {
                            "X-Requested-With":
                                "XMLHttpRequest"
                        },

                        cache: "no-store"
                    }
                );


            /*
             * Backend đã invalidate session.
             */
            if (response.ok) {

                window.location.href =
                    `${contextPath}/login`;

                return;
            }


            throw new Error(
                `Đăng xuất thất bại từ phía máy chủ (Mã lỗi: ${response.status}).`
            );


        } catch (error) {

            showAlert(
                error.message ||
                "Không thể kết nối máy chủ để đăng xuất. Vui lòng thử lại."
            );


            /*
             * Cho phép người dùng thử lại.
             */
            if (btnLogout) {

                btnLogout.disabled =
                    false;
            }


            if (btnExtend) {

                btnExtend.disabled =
                    false;
            }
        }
    }


    /*
     * =========================================================
     * BUTTON EVENTS
     * =========================================================
     */

    if (btnExtend) {

        btnExtend.addEventListener(
            "click",
            extendSession
        );
    }


    if (btnLogout) {

        btnLogout.addEventListener(
            "click",
            performLogout
        );
    }


    /*
     * =========================================================
     * USER ACTIVITY EVENTS
     * =========================================================
     */

    const userEvents = [
        "mousemove",
        "keydown",
        "click",
        "scroll",
        "touchstart"
    ];


    userEvents.forEach(
        function (eventName) {

            window.addEventListener(
                eventName,
                handleUserActivity,
                {
                    passive: true
                }
            );
        }
    );


    /*
     * =========================================================
     * KHỞI ĐỘNG
     * =========================================================
     */

    resetIdleTimer();


    /*
     * Chỉ phục vụ test thủ công trên trình duyệt.
     */
    window.showSessionWarning =
        showWarningModal;

})();