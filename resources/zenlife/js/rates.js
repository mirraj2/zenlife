var SEX = -6;
var AGE = -5;
var SMOKING = -4;
var RATES = 0;

function showFinalRates() {
	row.empty();

	row.append($("<img id='ajax-loader' src='img/loading.gif'>"));

	$.getJSON("/getFinalRates", savedQuestions, function(data) {
		row.empty();

		var coverage = savedQuestions[0];
		var benchmark = data.benchmark_rate;

		row.append($("<h3>").html(
				"Good News. You've qualified for <b>$" + coverage.formatMoney(0)
						+ "</b> of life insurance at a low rate of <b>$" + data.rate.formatMoney(2) + "</b> / month"));

		row.append($("<h4>").text("You can purchase now, or come back when you're ready."));

		backButton();
		row.append($("<button id='next-button' type='button' class='btn btn-success'>").text("Purchase"));

		// row.append($("<h4>").text("benchmark: $" + benchmark.formatMoney(2)));
	});
}

function getRatesTable() {
	var ret = $("<div>");

	ret.append($("<img id='ajax-loader' src='img/loading.gif'>"));

	console.log("Showing rates table...");

	var inputs = {
		sex : savedAnswers[SEX][0],
		age : savedAnswers[AGE],
		smoking : savedAnswers[SMOKING][0],
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

			if (savedAnswers[RATES] == data[i]) {
				row.addClass("success");
			}

			row.data("coverage", data[i]);
		}
		table.append(body);

		ret.empty().append(table);

		syncNextButton();

		table.on('click', 'tbody tr', function(event) {
			$(this).addClass('success').siblings().removeClass('success');
			var rate = $("tr.success").data("coverage");
			if (rate) {
				changeAnswer(RATES, rate);
			}
			syncNextButton();
		});
	});

	return ret;
}

function isRatesQuestion(question) {
	return question.id == RATES;
}

Number.prototype.formatMoney = function(c, d, t) {
	var n = this, c = isNaN(c = Math.abs(c)) ? 2 : c, d = d == undefined ? "." : d, t = t == undefined ? "," : t, s = n < 0 ? "-"
			: "", i = parseInt(n = Math.abs(+n || 0).toFixed(c)) + "", j = (j = i.length) > 3 ? j % 3 : 0;
	return s + (j ? i.substr(0, j) + t : "") + i.substr(j).replace(/(\d{3})(?=\d)/g, "$1" + t)
			+ (c ? d + Math.abs(n - i).toFixed(c).slice(2) : "");
};