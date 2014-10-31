var SEX = -6;
var AGE = -5;
var SMOKING = -4;

function showRatesTable() {
	row.append($("<img id='ajax-loader' src='img/AjaxLoader.gif'>"));

	var div = $("<div>");
	row.append(div);

	console.log("Showing rates table...");

	var inputs = {
		sex : savedQuestions[SEX],
		age : savedQuestions[AGE],
		smoking : savedQuestions[SMOKING],
	};

	$.getJSON("/getRates", inputs, function(data) {
		var table = $("<table class='table table-hover'>");

		table.append($("<thead>").append(
				$("<tr>").append($("<th>").text("Coverage")).append($("<th>").text("Monthly Cost"))));

		var body = $("<tbody>");
		for (var i = 0; i < data.length; i += 2) {
			var row = $("<tr>");
			row.append($("<td>").text("$ " + data[i].formatMoney(0)));
			row.append($("<td>").text("$ " + data[i + 1].formatMoney(2) + " / month"));
			body.append(row);

			if (currentQuestion.answer == data[i]) {
				row.addClass("success");
			}

			row.data("coverage", data[i]);
		}
		table.append(body);

		$("#ajax-loader").hide();
		div.append(table);

		table.on('click', 'tbody tr', function(event) {
			$(this).addClass('success').siblings().removeClass('success');
			$("#next-button").removeClass("disabled");
		});

	});
}

function parseRate() {
	var rate = $("tr.success").data("coverage");
	if (rate) {
		currentQuestion.answer = rate;
	}
}

function isRatesQuestion() {
	return currentQuestion.text.indexOf("level of protection") != -1;
}

Number.prototype.formatMoney = function(c, d, t) {
	var n = this, c = isNaN(c = Math.abs(c)) ? 2 : c, d = d == undefined ? "." : d, t = t == undefined ? "," : t, s = n < 0 ? "-"
			: "", i = parseInt(n = Math.abs(+n || 0).toFixed(c)) + "", j = (j = i.length) > 3 ? j % 3 : 0;
	return s + (j ? i.substr(0, j) + t : "") + i.substr(j).replace(/(\d{3})(?=\d)/g, "$1" + t)
			+ (c ? d + Math.abs(n - i).toFixed(c).slice(2) : "");
};