const gf = require('yahoo-finance');
const mongo = require('mongodb').MongoClient;
const url = 'mongodb://localhost:32768/example';



mongo.connect(url, (err,db) => {
    console.log("connected");

    const collection = db.collection('prices');

    gf.historical({
        symbol: "fb",
        from: '2014-01-01',
        to: '2014-12-31'
    }, (err, quotes) => {
        console.log("fetched quotes");
        // console.log(quotes);
        collection.insertMany(quotes, (err, result) => {
            console.log("Inserted stuff to monog");
            console.log(result);
            if(err !== null){
                console.log(err);
            }
        });
    });


});










