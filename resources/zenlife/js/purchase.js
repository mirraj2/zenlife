$(showFinalRates);

$("#card-form").submit(function(){
	$("#card-container").hide();
	$("#congrats").removeClass("invisible");
	return false;
});

$('#card-form').card({
	container: '.card-wrapper',
	numberInput: 'input#number',
	expiryInput: 'input#expiry',
	cvcInput: 'input#cvc',
	nameInput: 'input#name',
	
	width: 350,
	
	// Strings for translation - optional
	messages: {
		validDate: 'valid\ndate',
		monthYear: 'mm/yy',
	},
	
	// Default values for rendered fields - options
	values: {
		number: '•••• •••• •••• ••••',
		name: 'Full Name',
		expiry: '••/••',
		cvc: '•••'
	}
});