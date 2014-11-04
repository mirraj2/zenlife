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
	} else if (type == 'other') {
		if (isRatesQuestion(question)) {
			ret.append(getRatesTable());
		}
	} else {
		console.log("Unhandled Type: " + type);
	}

	ret.append($("<div class='children'>"));

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
		if ($.inArray(i, array) != -1) {
			input.prop("checked", true);
		}

		input.on("ifToggled", function() {
			var answer = [];
			var inputs = list.find("input");
			for (var j = 0; j < inputs.length; j++) {
				if (inputs[j].checked) {
					answer.push(parseInt($(inputs[j]).val()));
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

	input.val(savedAnswers[question.id]);

	var callback = function() {
		changeAnswer(question.id, input.val());
	};

	input.keyup(callback);
	input.change(callback);
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
		return Boolean(input.val());
	} else if (type == 'other') {
		if (isRatesQuestion(question)) {
			return $("tr.success").length > 0;
		}
	}
	return true;
}

function changeAnswer(questionId, answer) {
	console.log("Changing question " + questionId + "'s answer to: " + answer);
	savedAnswers[questionId] = answer;
	// console.log(JSON.stringify(savedAnswers));
	$.cookie("questions", JSON.stringify(savedAnswers), {
		expires : 14
	});

	syncChildren(questionId, answer);
	syncNextButton();
}

function syncChildren(questionId) {
	var answer = savedAnswers[questionId];
	var questionPanel = $(".question[data-id='" + questionId + "']");
	var question = idQuestions[questionId];
	var childrenPanel = questionPanel.children(".children");

	childrenPanel.empty();

	if (!answer) {
		return;
	}

	if (question.choices) {
		for (var i = 0; i < answer.length; i++) {
			var choice = question.choices[answer[i]];
			if (choice.questions) {
				for (var j = 0; j < choice.questions.length; j++) {
					var q = choice.questions[j];
					childrenPanel.append(createQuestionPanel(q));
					syncChildren(q.id);
				}
			}
		}
		icheckify(childrenPanel);
	}
}
