/* =====================================================================
   CRUD Manager | scripts da interface (JavaScript puro, sem jQuery)
   Cada bloco procura os elementos pelos atributos data-* que as telas (JSP) trazem.
   Sem JavaScript o sistema continua funcionando: só perde a janela de confirmação de
   exclusão, o olho da senha, o aviso que some sozinho e o ajuste automático dos campos.
   ===================================================================== */
(function () {
	"use strict";

	/* ---------- aviso (toast): some sozinho e pode ser fechado ---------- */
	document.querySelectorAll("[data-toast]").forEach(function (toast) {
		function close() {
			toast.classList.add("is-leaving");
			setTimeout(function () {
				toast.remove();
			}, 320);
		}

		var closeButton = toast.querySelector("[data-toast-close]");
		if (closeButton) closeButton.addEventListener("click", close);

		// erros ficam mais tempo na tela do que os avisos de sucesso
		var time = toast.classList.contains("toast-error") ? 12000 : 6000;
		var timer = setTimeout(close, time);

		// se a pessoa estiver lendo (mouse em cima), não some
		toast.addEventListener("mouseenter", function () {
			clearTimeout(timer);
		});
	});

	/* ---------- janela de confirmação de exclusão ---------- */
	var dialog = document.getElementById("delete-dialog");

	if (dialog && typeof dialog.showModal === "function") {
		var form = dialog.querySelector("form");
		var idField = dialog.querySelector("[data-delete-id]");
		var nameBox = dialog.querySelector("[data-delete-text]");
		var kindBox = dialog.querySelector("[data-delete-kind]");
		var confirmButton = dialog.querySelector("[data-delete-confirm]");
		var cancelButton = dialog.querySelector("[data-dialog-close]");

		document.querySelectorAll("[data-delete]").forEach(function (button) {
			button.addEventListener("click", function () {
				form.action = button.dataset.action;
				idField.value = button.dataset.id;
				kindBox.textContent = button.dataset.kind || "item";

				// monta o texto com textContent (nunca innerHTML): nomes digitados pelo usuário
				// não podem virar HTML
				nameBox.textContent = "";
				var blocked = button.dataset.blocked;

				if (blocked) {
					nameBox.textContent = blocked;
					confirmButton.hidden = true;
					cancelButton.textContent = "Entendi";
				} else {
					var strong = document.createElement("strong");
					strong.textContent = button.dataset.name;
					nameBox.appendChild(document.createTextNode("Você vai excluir "));
					nameBox.appendChild(strong);
					nameBox.appendChild(document.createTextNode(". Esta ação não pode ser desfeita."));
					confirmButton.hidden = false;
					cancelButton.textContent = "Cancelar";
				}

				dialog.showModal();
			});
		});

		cancelButton.addEventListener("click", function () {
			dialog.close();
		});

		// clique no fundo escuro também fecha
		dialog.addEventListener("click", function (event) {
			if (event.target === dialog) dialog.close();
		});
	}

	/* ---------- olho da senha ---------- */
	document.querySelectorAll("[data-toggle-password]").forEach(function (button) {
		var input = document.getElementById(button.getAttribute("aria-controls"));
		if (!input) return;

		button.addEventListener("click", function () {
			var show = input.type === "password";
			input.type = show ? "text" : "password";
			button.setAttribute("aria-pressed", show ? "true" : "false");
			button.setAttribute("aria-label", show ? "Ocultar senha" : "Mostrar senha");
			button.querySelector("[data-icon-show]").hidden = show;
			button.querySelector("[data-icon-hide]").hidden = !show;
		});
	});

	/* ---------- "itens por página" envia o formulário ao mudar ---------- */
	document.querySelectorAll("select[data-autosubmit]").forEach(function (select) {
		select.addEventListener("change", function () {
			select.form.submit();
		});
	});

	/* ---------- botão de acesso de teste (tela de login) ---------- */
	document.querySelectorAll("[data-fill]").forEach(function (button) {
		button.addEventListener("click", function () {
			document.getElementById("email").value = button.dataset.email;
			document.getElementById("password").value = button.dataset.password;
			document.getElementById("password").focus();
		});
	});

	/* ---------- formulário de plantio: ajuda a preencher ---------- */
	// A data da colheita não pode ser antes do plantio: o navegador passa a avisar.
	var plantingDate = document.querySelector("[data-min-source]");
	var harvestDate = document.querySelector("[data-min-target]");

	if (plantingDate && harvestDate) {
		var syncMin = function () {
			harvestDate.min = plantingDate.value || "";
		};
		plantingDate.addEventListener("change", syncMin);
		syncMin();
	}

	// A área plantada não pode passar da área da propriedade escolhida.
	var propertySelect = document.querySelector("[data-area-source]");
	var plantedArea = document.querySelector("[data-area-target]");
	var areaNote = document.querySelector("[data-area-note]");

	if (propertySelect && plantedArea) {
		var syncArea = function () {
			var option = propertySelect.options[propertySelect.selectedIndex];
			var area = option ? option.dataset.area : "";

			if (area) {
				plantedArea.max = area;
				if (areaNote) areaNote.textContent = "A propriedade escolhida tem " + option.dataset.areaLabel + ".";
			} else {
				plantedArea.removeAttribute("max");
				if (areaNote) areaNote.textContent = "";
			}
		};
		propertySelect.addEventListener("change", syncArea);
		syncArea();
	}

	/* ---------- calendário da safra: no celular, abre já mostrando o "hoje" ---------- */
	var seasonScroll = document.querySelector(".season-scroll");
	var todayLine = document.querySelector(".today-line");

	if (seasonScroll && todayLine && seasonScroll.scrollWidth > seasonScroll.clientWidth) {
		var labelWidth = seasonScroll.querySelector(".season-label") ? seasonScroll.querySelector(".season-label").offsetWidth : 0;
		var todayX = todayLine.getBoundingClientRect().left - seasonScroll.getBoundingClientRect().left + seasonScroll.scrollLeft;
		// deixa o "hoje" mais ou menos no meio da parte visível da linha do tempo
		seasonScroll.scrollLeft = Math.max(0, todayX - labelWidth - (seasonScroll.clientWidth - labelWidth) * 0.45);
	}

	/* ---------- formulário com erro: leva o foco ao primeiro campo com problema ---------- */
	var firstInvalid = document.querySelector("[aria-invalid='true']");
	if (firstInvalid && typeof firstInvalid.focus === "function") firstInvalid.focus();
})();
