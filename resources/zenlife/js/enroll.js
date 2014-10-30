var questions;
var questionIndex = -1;

var currentQuestion;
var row;

var queuedQuestions = [];
var questionHistory = [];

$.getJSON("/data/questions.json", function(data) {
	questions = data["questions"];
	row = $("#questions-row");
	nextQuestion();
});

function nextQuestion() {
	if (queuedQuestions.length == 0) {
		currentQuestion = questions[++questionIndex];
	} else {
		currentQuestion = queuedQuestions.shift();
	}

	showQuestion();
}

function showQuestion() {
	row.empty();

	row.append($("<h2>").text(currentQuestion.text));

	addChoices();

	row.append($("<button id='back-button' type='button' class='btn btn-default'>").text("Back"));
	row.append($("<button id='next-button' type='button' class='btn btn-primary disabled'>").text("Next Question"));

	if (questionHistory.length == 0) {
		$("#back-button").addClass("disabled");
	}

	$('#next-button').click(function() {
		questionHistory.push(currentQuestion);

		var index = $("input:checked").val();
		if (index) {
			var links = currentQuestion.choices[index].links;
			if (links) {
				for (var i = 0; i < links.length; i++) {
					var link = links[i];
					queuedQuestions.push(link.question);
				}
			}
		}
		nextQuestion();
	});

	$("#back-button").click(function() {
		currentQuestion = questionHistory.pop();
		var rootIndex = questions.indexOf(currentQuestion);
		if (rootIndex >= 0) {
			questionIndex = rootIndex;
		}
		showQuestion();
	});
}

function addSpecialInput() {
	var text = currentQuestion.text;
	if (text.indexOf("How old are you") != -1) {
		row.append($("<input type='number' min='0' max='150'>")).append(
				$("<p style='display: inline; margin-left: 10px'>").text("years old"));
	} else if (text.indexOf("How tall are you") != -1) {
		row.append($("<input type='number' min='0' max='9'>")).append(
				$("<p style='display: inline; margin-left: 10px'>").text("ft"));
		row.append($("<input type='number' min='0' max='11' style='margin-left: 20px;'>")).append(
				$("<p style='display: inline; margin-left: 10px'>").text("inches"));
	} else if (text.indexOf("How much do you weigh") != -1) {
		row.append($("<input type='number' min='0' max='2000'>")).append(
				$("<p style='display: inline; margin-left: 10px'>").text("lbs"));
	}
	row.append("<br>");

	$("input[type='number']").change(function() {
		$("#next-button").removeClass("disabled");
	});
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
