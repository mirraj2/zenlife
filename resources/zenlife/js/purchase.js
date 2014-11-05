$(function(){
	$('#card-form').card({
		// a selector or jQuery object for the container
		// where you want the card to appear
		container: '.card-wrapper', // *required*
		numberInput: 'input#number', // optional — default input[name="number"]
		expiryInput: 'input#expiry', // optional — default input[name="expiry"]
		cvcInput: 'input#cvc', // optional — default input[name="cvc"]
		nameInput: 'input#name', // optional - defaults input[name="name"]
		
		width: 350, // optional — default 350px
		formatting: true, // optional - default true
		
		// Strings for translation - optional
		messages: {
			validDate: 'valid\ndate', // optional - default 'valid\nthru'
			monthYear: 'mm/yyyy', // optional - default 'month/year'
		},
		
		// Default values for rendered fields - options
		values: {
			number: '•••• •••• •••• ••••',
			name: 'Full Name',
			expiry: '••/••',
			cvc: '•••'
		}
	});
});