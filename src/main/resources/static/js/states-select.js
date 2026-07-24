(() => {
	function label(state) {
		return state.name ? state.code + " — " + state.name : state.code;
	}

	function populate(select, states) {
		const selected = select.getAttribute("data-selected") || select.value || "";
		while (select.options.length > 1) {
			select.remove(1);
		}
		states.forEach((state) => {
			const option = document.createElement("option");
			option.value = state.code;
			option.textContent = label(state);
			if (state.code === selected) {
				option.selected = true;
			}
			select.appendChild(option);
		});
	}

	async function loadStates() {
		const selects = document.querySelectorAll("select[data-states-select]");
		if (!selects.length) {
			return;
		}

		try {
			const states = window.AppHttp
				? await window.AppHttp.getJson("/api/states")
				: await fetch("/api/states").then((response) => {
					if (!response.ok) {
						throw new Error("Failed to load states");
					}
					return response.json();
				});
			selects.forEach((select) => populate(select, states));
		} catch {
			/* leave placeholder only */
		}
	}

	if (document.readyState === "loading") {
		document.addEventListener("DOMContentLoaded", loadStates);
	} else {
		loadStates();
	}
})();
