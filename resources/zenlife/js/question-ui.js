function createQuestionPanel(question) {
	var ret = $("<div class='question' data-id='" + question.id + "'>");

	ret.append($("<h2>").text(question.text));

	var type = question.type;
	if (type == "single-choice") {
		addChoices(ret, question, question.choices, false);
	} else if (type == 'multi-choice') {
		addChoices(ret, question, question.choices, true);
	} else if (type == 'number') {
		addTextChoice(ret, question);
	} else {
		console.log("Unhandled Type: " + type);
	}

	ret.append($("<div class='children'>"));

	listen(ret);

	return ret;
}

function addChoices(panel, question, choices, multiSelection) {
	var type = multiSelection ? "checkbox" : "radio";

	var list = $("<ul class='choices'>");
	for (var i = 0; i < choices.length; i++) {
		var item = ($("<li>"))
		var id = question.id + "" + i;
		var input = $("<input type='" + type + "' name='question-" + question.id + "' id='option-" + id + "' value='" + i
				+ "'>");
		var label = $("<label for='option-" + id + "'>").text(choices[i].text);

		var array = savedAnswers[question.id];
		if ($.inArray(i, array)) {
			input.prop("checked", true);
		}

		input.on("ifToggled", function() {
			var answer = [];
			var inputs = list.find("input");
			for (var j = 0; j < inputs.length; j++) {
				if (inputs[j].checked) {
					answer.push($(inputs[j]).val());
				}
			}
			changeAnswer(question.id, answer);
		});

		item.append(input).append(label);
		list.append(item);
	}
	panel.append(list);
}

function addTextChoice(panel, question) {
	var input = $("<input type='number' min='0' max='99999'>");
	var units = "";
	panel.append(input).append($("<p style='display: inline; margin-left: 10px'>").text(units));
}

function isAllCompleted() {
	var questions = $(".question");
	for (var i = 0; i < questions.length; i++) {
		if (!isCompleted($(questions[i]))) {
			return false;
		}
	}
	return true;
}

function isCompleted(questionPanel) {
	var question = idQuestions[questionPanel.data("id")];
	var type = question.type;
	if (type == "single-choice" || type == "multi-choice") {
		var choices = questionPanel.children(".choices").find("input");
		for (var i = 0; i < choices.length; i++) {
			if (choices[i].checked) {
				return true;
			}
		}
		return false;
	} else if (type == 'number') {
		var input = questionPanel.find("input");
		if (!input.val()) {
			return false;
		}
	}
	return true;
}

function changeAnswer(questionId, answer) {
	savedAnswers[questionId] = answer;
	syncNextButton();
	console.log(savedAnswers);
	$.cookie("questions", JSON.stringify(savedAnswers), {
		expires : 14
	});
}
