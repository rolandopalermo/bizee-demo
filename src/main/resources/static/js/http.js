(() => {
	const Http = {
		async getJson(url, options = {}) {
			const response = await fetch(url, {
				method: "GET",
				headers: {
					Accept: "application/json",
					...(options.headers || {})
				},
				...options
			});
			if (!response.ok) {
				throw new Error(`Request failed: ${response.status}`);
			}
			return response.json();
		}
	};

	window.AppHttp = Http;
})();
