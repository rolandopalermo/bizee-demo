(() => {
	function setSubmitLoading(button, loading) {
		if (!button) {
			return;
		}
		if (loading) {
			button.disabled = true;
			button.classList.add("app-btn-loading");
			if (!button.dataset.appOriginalHtml) {
				button.dataset.appOriginalHtml = button.innerHTML;
			}
			const label = button.dataset.appLoadingLabel || "Saving…";
			button.replaceChildren();
			const spinner = document.createElement("span");
			spinner.className = "spinner-border spinner-border-sm me-2";
			spinner.setAttribute("role", "status");
			spinner.setAttribute("aria-hidden", "true");
			button.append(spinner, document.createTextNode(label));
		} else {
			button.disabled = false;
			button.classList.remove("app-btn-loading");
			if (button.dataset.appOriginalHtml) {
				button.innerHTML = button.dataset.appOriginalHtml;
			}
		}
	}

	function enhanceValidatedForms() {
		document.querySelectorAll("form[data-app-validate]").forEach((form) => {
			form.addEventListener("submit", (event) => {
				if (!form.checkValidity()) {
					event.preventDefault();
					event.stopPropagation();
					form.classList.add("was-validated");
					const firstInvalid = form.querySelector(":invalid");
					if (firstInvalid && typeof firstInvalid.focus === "function") {
						firstInvalid.focus();
					}
					return;
				}

				const submitButton = form.querySelector('[type="submit"]');
				if (submitButton && !submitButton.disabled) {
					setSubmitLoading(submitButton, true);
				}
			});
		});
	}

	function enhanceConfirmForms() {
		const modalElement = document.getElementById("appConfirmModal");
		if (!modalElement || typeof bootstrap === "undefined") {
			return;
		}

		const modal = bootstrap.Modal.getOrCreateInstance(modalElement);
		const messageElement = modalElement.querySelector("[data-app-confirm-message]");
		const confirmButton = modalElement.querySelector("[data-app-confirm-submit]");
		let pendingForm = null;

		document.querySelectorAll("form[data-app-confirm]").forEach((form) => {
			form.addEventListener("submit", (event) => {
				if (form.dataset.appConfirmed === "true") {
					form.dataset.appConfirmed = "false";
					return;
				}
				event.preventDefault();
				pendingForm = form;
				if (messageElement) {
					messageElement.textContent = form.getAttribute("data-app-confirm") || "Are you sure?";
				}
				modal.show();
			});
		});

		if (confirmButton) {
			confirmButton.addEventListener("click", () => {
				if (!pendingForm) {
					return;
				}
				const form = pendingForm;
				pendingForm = null;
				form.dataset.appConfirmed = "true";
				modal.hide();
				if (typeof form.requestSubmit === "function") {
					form.requestSubmit();
				} else {
					form.submit();
				}
			});
		}

		modalElement.addEventListener("hidden.bs.modal", () => {
			pendingForm = null;
		});
	}

	function focusServerInvalid() {
		const invalidFeedback = document.querySelector(".invalid-feedback.d-block, .is-invalid");
		if (!invalidFeedback) {
			return;
		}
		const field =
			invalidFeedback.closest(".mb-3")?.querySelector(".form-control, .form-select, .form-check-input") ||
			document.querySelector(".is-invalid");
		if (field && typeof field.focus === "function") {
			field.focus();
		}
	}

	document.addEventListener("DOMContentLoaded", () => {
		enhanceValidatedForms();
		enhanceConfirmForms();
		focusServerInvalid();
	});
})();
