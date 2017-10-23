var mongo = require('mongodb').MongoClient

var url = 'mongodb://localhost:32768/example';

mongo.connect(url, function(err,db) {
	console.log("connected");
	
	var collection = db.collection('exampledoc');
	collection.insertMany([{a: 1}, {a:2}, {a:3}], function(err, result) {
		console.log(result);
	});
	db.close();
});
