document.addEventListener("DOMContentLoaded", function () {
    var noticeDialog = document.getElementById("notice-dialog");
    if (noticeDialog) {
        noticeDialog.showModal();
        document.getElementById("notice-ok").addEventListener("click", function () {
            noticeDialog.close();
        });
    }

    var confirmDialog = document.getElementById("confirm-dialog");
    if (!confirmDialog) {
        return;
    }

    var pendingForm = null;

    document.querySelectorAll("form[data-confirm]").forEach(function (form) {
        form.addEventListener("submit", function (event) {
            if (form.dataset.confirmed === "true") {
                return;
            }
            event.preventDefault();
            pendingForm = form;
            document.getElementById("confirm-message").textContent = form.getAttribute("data-confirm");
            confirmDialog.showModal();
        });
    });

    document.getElementById("confirm-ok").addEventListener("click", function () {
        var form = pendingForm;
        pendingForm = null;
        confirmDialog.close();
        if (!form) {
            return;
        }
        form.dataset.confirmed = "true";
        form.requestSubmit();
    });

    document.getElementById("confirm-cancel").addEventListener("click", function () {
        pendingForm = null;
        confirmDialog.close();
    });
});
