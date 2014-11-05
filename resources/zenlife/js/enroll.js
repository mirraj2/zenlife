var sections;
var sectionIndex = -1;
var section;
var idQuestions = {};

var savedAnswers;

var content = $("#questions-panel");
var loadingImage = $("#ajax-loader")

$.getJSON("/data/questions.json", function(data) {
	savedAnswers = loadSavedAnswers();
	sections = data;
	for (var i = 0; i < sections.length; i++) {
		var questions = sections[i].questions;
		index(questions);
	}

	nextSection();
});

function loadSavedAnswers() {
	var cookieData = $.cookie("questions")
	return cookieData == null ? {} : JSON.parse(cookieData);
}

function nextSection() {
	if (++sectionIndex == sections.length) {
		window.location.href = "/purchase";
	}

	section = sections[sectionIndex];

	content.empty();
	content.append($("<h1>").text(section.title));

	var questions = section.questions;
	for (var i = 0; i < questions.length; i++) {
		var question = questions[i];
		var panel = createQuestionPanel(question);
		content.append(panel);
		icheckify(panel);
		syncChildren(question.id);
	}

	var nextButtonText = section["next-button-text"];
	if (!nextButtonText) {
		nextButtonText = "Next Questions";
	}

	content.append($("<button id='back-button' type='button' class='btn btn-default'>").text("Back"));
	content.append($("<button id='next-button' type='button' class='btn btn-primary'>").text(nextButtonText));

	syncNextButton();

	$("#next-button").click(nextSection);
	$("#back-button").click(function() {
		sectionIndex -= 2;
		nextSection();
	});
	$("#back-button").toggleClass("disabled", sectionIndex == 0);
}

function icheckify(parent) {
	parent.find("input").iCheck({
		checkboxClass : 'icheckbox_square-purple',
		radioClass : 'iradio_square-purple',
		increaseArea : '20%'
	});
}

function syncNextButton() {
	$("#next-button").toggleClass("disabled", !isAllCompleted());
}

function index(questions) {
	for (var i = 0; i < questions.length; i++) {
		var q = questions[i];
		idQuestions[q.id] = q;
		if (q.choices) {
			for (var j = 0; j < q.choices.length; j++) {
				var choice = q.choices[j];
				if (choice.questions) {
					index(choice.questions);
				}
			}
		}
	}
}
