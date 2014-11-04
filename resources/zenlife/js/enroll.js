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
	section = sections[++sectionIndex];

	content.empty();
	content.append($("<h1>").text(section.title));

	var questions = section.questions;
	for (var i = 0; i < questions.length; i++) {
		var question = questions[i];
		var panel = createQuestionPanel(question);
		content.append(panel);
		icheckify(panel);
	}
	content.append($("<button id='back-button' type='button' class='btn btn-default'>").text("Back"));
	content.append($("<button id='next-button' type='button' class='btn btn-primary'>").text("Next Questions"));

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

function listen(parent) {
	parent.find("input:radio,input:checkbox").on("ifClicked", function() {
		var questionPanel = $(this).closest(".question");
		var questionId = questionPanel.data("id");
		var question = idQuestions[questionPanel.data("id")];
		var choice = question.choices[$(this).val()];

		var childrenPanel = questionPanel.children(".children");
		childrenPanel.empty();

		if (choice.questions) {
			for (var i = 0; i < choice.questions.length; i++) {
				childrenPanel.append(createQuestionPanel(choice.questions[i]));
			}
			icheckify(childrenPanel);
		}
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
