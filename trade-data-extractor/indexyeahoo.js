const gf = require('yahoo-finance');

gf.historical({
	symbol: 'AAPL',
	from: '2014-01-01',
	to: '2014-12-31'
}, (err, quotes) => {
	console.log(quotes);
});
