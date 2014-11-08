var ws;
var lastId = null;
var connected = false;
var startingTime;

if (WebSocket) {
	var domain = "$DOMAIN";
	var port = $PORT;
	ws = new WebSocket("ws://" + domain + ":" + port + "/socket");
	ws.onopen = function() {
		connected = true;
		startingTime = Date.now();
		console.log("connected.");
		ws.send("s" + $.cookie("session"));
	};
	ws.onmessage = function(evt) {
		console.log(evt.data);
	};
	ws.onclose = function() {
		connected = false;
		console.log("disconnected");
	};
} else {
	console.log("sockets not supported.");
}

$(document).mousedown(function(e) {
	log("d", e);
});

$(document).mouseup(function(e) {
	log("u", e);
});

$(document).mousemove(function(e) {
	log("m", e);
});

function log(code, e) {
	if (connected) {
		logId(e);
		ws.send(code + (Date.now() - startingTime) + " " + e.pageX + " " + e.pageY);
	}
}

$(document).keydown(function(e) {
	if (connected) {
		logId(e);
		ws.send("k" + (Date.now() - startingTime) + " " + e.keyCode);
	}
});

function onAnswerChanged(questionId, answer, previousAnswer) {
	if (connected) {
		ws.send("a" + (Date.now() - startingTime) + " " + questionId + ":" + answer);
	}
	if (demoAnswerChanged) {
		demoAnswerChanged(questionId, answer, previousAnswer);
	}
}

function logId(e) {
	var id = getId(e.target);
	if (id == lastId) {
		return;
	}
	lastId = id;

	if (questionListener) {
		if (id.indexOf("q") != -1) {
			questionListener(e.target);
		}
	}

	ws.send(id);
}

function getQuestionId(question, element) {
	var id = question.data("id");
	var li = element.closest("li")[0];
	if (li) {
		var index = $(li).index();
		return "q" + id + ":" + index;
	} else {
		return "q" + id;
	}
	return id;
}

function getId(element) {
	var question = $(element).closest(".question");
	if (question[0]) {
		return getQuestionId($(question[0]), $(element));
	}

	var xpath = '';
	for (; element && element.nodeType == 1; element = element.parentNode) {
		var ee = $(element);
		var key = null;
		if (element.href) {
			key = element.href;
		} else if (element.id) {
			key = element.id;
		} else if (ee.data("id")) {
			key = ee.data("id");
		} else if (element.innerHTML.length < 32) {
			key = element.innerHTML;
		}
		if (!key) {
			key = element.tagName.toLowerCase();
		}
		xpath = "/" + key + xpath;
	}
	return "x" + xpath;
}
