var fm;
var mouseHistory = [];
var inspect = false;
var canvas, coords, sherlock, glass;
var eventBox, alertBox;

$(function() {
	fm = Boolean($.cookie("demo"));
	updateDemo();

	$(document).mousemove(function(e) {
		if (fm) {
			coords.text("x = " + e.pageX + ", y = " + e.pageY);
			if (inspect) {
				drawOval(e.pageX, e.pageY);
			}
			mouseHistory.push(e.pageX);
			mouseHistory.push(e.pageY);
		}
	});
});

$(document).keypress(function(e) {
	if (e.ctrlKey) {
		if (e.keyCode == 49) {
			fm = !fm;
			updateDemo();
		} else if (e.keyCode == 50) {
			toggleConsole();
		}
	}
});

function updateDemo() {
	if (fm) {
		initDemo();
	} else {
		disableDemo();
	}
}

function initDemo() {
	console.log("initDemo");

	$.cookie("demo", "true");

	glass = $("<img id='mag-glass' src='/img/search.png'>").css("position", "fixed").css("bottom", 0).css("right", 0);
	glass.css("width", "32px").css("height", "32px").css("opacity", ".4");
	$("body").append(glass);

	glass.click(toggleConsole);
	canvas = $("<canvas id='canvas'>").css("position", "absolute").css("top", 0).css("left", 0).css("overflow", "hidden")
			.css("pointer-events", "none")[0];
	$("body").append(canvas);

	coords = $("<p id='coords'>").css("position", "fixed").css("bottom", 0).css("left", 25).css("font-size", "30px");
	$("body").append(coords);

	sherlock = $("<img src='/img/sherlock.png'>").css("position", "fixed").css("bottom", 0).css("right", 10);
	$("body").append(sherlock);

	eventBox = $("<div>").css("position", "fixed").css("bottom", 0).css("right", 100).css("width", "600px").css("height",
			"300px").css("border-style", "solid").css("border-width", "1px").css("background", "rgba(0,0,0,.5)").css("color",
			"white").css("padding", "10px").css("overflow-y", "scroll");
	$("body").append(eventBox);

	alertBox = $("<div>").css("position", "fixed").css("bottom", 50).css("left", 20).css("width", "300px").css("height",
			"300px").css("border-style", "solid").css("border-width", "1px").css("color", "red").css("padding", "10px").css(
			"overflow-y", "auto");
	$("body").append(alertBox);

	updateConsole();
}

function toggleConsole() {
	inspect = !inspect;
	updateConsole();
}

function updateConsole() {
	if (inspect) {
		repaint();
		coords.show();
		sherlock.show();
		glass.hide();
		eventBox.show();
		alertBox.show();
	} else {
		canvas.width = canvas.width;
		coords.hide();
		sherlock.hide();
		glass.show();
		eventBox.hide();
		alertBox.hide();
	}
}

function onNextSection() {
	if (!fm) {
		return;
	}
	canvas.width = canvas.width;
}

function questionListener(element) {
	if (!fm) {
		return;
	}
	element = $(element);

	var question = element.closest(".question")[0];
	if (!question) {
		return;
	}
	question = $(question);

	var h2 = $(question.children("h2")[0]);

	var label = element.children("label");
	if (label.length == 1) {
		label = $(label[0]);
		eventLog("HOVER: " + h2.text() + " :: " + $(label).text());
	}

}

function demoAnswerChanged(question, answer, previous) {
	if (!fm) {
		return;
	}
	question = idQuestions[question];
	if (question.choices) {
		var choice = question.choices[answer];
		eventLog("ANSWERED: " + question.text + " :: " + choice.text);
	} else {
		eventLog("ANSWERED: " + question.text + " :: " + answer);
	}

	console.log(previous + " " + answer);
	if (previous && answer != previous) {
		if (question.choices) {
			alertBox.append($("<p>").text(
					"Changed " + question.text + "' FROM " + question.choices[previous].text + " TO "
							+ question.choices[answer].text));
		} else {
			alertBox.append($("<p>").text("Changed " + question.text + "' FROM " + previous + " TO " + answer));
		}
		alertBox.scrollTop(alertBox[0].scrollHeight);
	}
}

function eventLog(event) {
	eventBox.append($("<p>").text(event));
	eventBox.scrollTop(eventBox[0].scrollHeight);
}

function repaint() {
	canvas.width = $(document).width();
	canvas.height = $(document).height();

	ctx = canvas.getContext("2d");
	for (var i = 0; i < mouseHistory.length; i += 2) {
		drawOval(mouseHistory[i], mouseHistory[i + 1]);
	}
}

function drawOval(x, y) {
	ctx.fillStyle = "rgba(0, 0, 0, 0.1)";
	ctx.beginPath();
	ctx.arc(x, y, 10, 0, 2 * Math.PI);
	ctx.fill();
}

function disableDemo() {
	$.removeCookie("demo");
	$("#mag-glass").remove();
	$("#canvas").remove();
	coords.remove();
	coords = null;
}