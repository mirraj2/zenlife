var questions;
var questionIndex = -1;

var currentQuestion;
var row;

var queuedQuestions = [];
var questionHistory = [];

var savedQuestions;

$.getJSON("/data/questions.json", function(data) {
	questions = data["questions"];
	row = $("#questions-row");

	var cookieData = $.cookie("questions")
	if (cookieData == null) {
		savedQuestions = {};
	} else {
		savedQuestions = JSON.parse(cookieData);
	}

	nextQuestion();
	while (currentQuestion.answer) {
		nextButtonClicked();
	}
});

function nextQuestion() {
	if (queuedQuestions.length == 0) {
		currentQuestion = questions[++questionIndex];
	} else {
		currentQuestion = queuedQuestions.shift();
	}

	if (savedQuestions[currentQuestion.id]) {
		console.log(savedQuestions[currentQuestion.id]);
		currentQuestion.answer = savedQuestions[currentQuestion.id];
	}

	showQuestion();
}

function showQuestion() {
	row.empty();

	console.log(currentQuestion);
	row.append($("<h2>").text(currentQuestion.text));

	addChoices();

	if (isRatesQuestion()) {
		showRatesTable();
	}

	row.append($("<button id='back-button' type='button' class='btn btn-default'>").text("Back"));
	row.append($("<button id='next-button' type='button' class='btn btn-primary'>").text("Next Question"));

	if (questionHistory.length == 0) {
		$("#back-button").addClass("disabled");
	}

	if (!currentQuestion.answer) {
		$("#next-button").addClass("disabled");
	}

	$('#next-button').click(nextButtonClicked);

	$("#back-button").click(goBack);
}

function addSpecialInput() {
	var text = currentQuestion.text;

	if (text.indexOf("How old are you") != -1) {
		var input = $("<input type='number' min='0' max='150'>");
		row.append(input).append($("<p style='display: inline; margin-left: 10px'>").text("years old"));
		input.val(currentQuestion.answer);
		listen(input, inputChanged);
	} else if (text.indexOf("How much do you weigh") != -1) {
		var input = $("<input type='number' min='0' max='2000'>");
		row.append(input).append($("<p style='display: inline; margin-left: 10px'>").text("lbs"));
		input.val(currentQuestion.answer);
		listen(input, inputChanged);
	} else if (text.indexOf("How tall are you") != -1) {
		var feet = $("<input type='number' min='0' max='9'>");
		var inches = $("<input type='number' min='0' max='11' style='margin-left: 20px;'>");
		row.append(feet).append($("<p style='display: inline; margin-left: 10px'>").text("ft"));
		row.append(inches).append($("<p style='display: inline; margin-left: 10px'>").text("inches"));

		if (currentQuestion.answer) {
			var arr = currentQuestion.answer.split(".");
			feet.val(arr[0]);
			inches.val(arr[1]);
		}

		listen(feet, heightChanged);
		listen(inches, heightChanged);
	}
	row.append("<br>");
}

function listen(input, callback) {
	input.keyup(callback);
	input.change(callback);
}

function inputChanged() {
	currentQuestion.answer = $(this).val();
	$("#next-button").removeClass("disabled");
}

function heightChanged() {
	var inputs = $("input[type=number]");
	currentQuestion.answer = $(inputs[0]).val() + "." + $(inputs[1]).val();
	$("#next-button").removeClass("disabled");
}

function nextButtonClicked() {
	questionHistory.push(currentQuestion);
	history.pushState(null, null, null);

	var index = $("input:checked").val();
	if (index) {
		currentQuestion.answer = index;
		var links = currentQuestion.choices[index].links;
		if (links) {
			for (var i = 0; i < links.length; i++) {
				var link = links[i];
				queuedQuestions.push(link.question);
			}
		}
	} else if (isRatesQuestion()) {
		parseRate();
	}
	saveState();
	nextQuestion();
}

function addChoices() {
	if (currentQuestion.choices.length == 0) {
		addSpecialInput();
		return;
	}

	var list = $("<ul>");
	row.append(list);

	for (var i = 0; i < currentQuestion.choices.length; i++) {
		var item = $("<li>");
		list.append(item);

		var choice = currentQuestion.choices[i];
		var ui = $("<input type='radio' name='choice' id='option-" + i + "' value='" + i + "'>");
		var label = $("<label for='option-" + i + "'>").text(choice.text);
		item.append(ui).append(label);
		if (currentQuestion.answer == i) {
			ui.prop("checked", true);
		}

		ui.iCheck({
			checkboxClass : 'icheckbox_square-purple',
			radioClass : 'iradio_square-purple',
			increaseArea : '20%'
		});
	}

	var radios = $("input:radio");
	radios.on('ifClicked', function() {
		$("#next-button").removeClass("disabled");
	});
}

window.addEventListener("popstate", function(e) {
	goBack();
});

function goBack() {
	currentQuestion = questionHistory.pop();
	var rootIndex = questions.indexOf(currentQuestion);
	if (rootIndex >= 0) {
		questionIndex = rootIndex;
	}
	showQuestion();
}

function saveState() {
	savedQuestions[currentQuestion.id] = currentQuestion.answer;
	$.cookie("questions", JSON.stringify(savedQuestions));
}
