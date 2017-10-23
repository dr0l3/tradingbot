const gf = require('google-finance');

gf.historical({
	symbol: 'NASDAQ:AAPL',
	from: '2016-01-01',
	to: '2016-12-31'
}, (err, quotes) => {
	console.log(quotes);
});
