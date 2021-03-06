const gf = require('yahoo-finance');
const mongo = require('mongodb').MongoClient;
const url = 'mongodb://localhost:32768/example';
const csv=require('csvtojson');



mongo.connect(url, (err,db) => {
    console.log("connected");

    const collection = db.collection('prices');

    console.log(process.argv[2]);

    let line = process.argv[2];
    let symbol = line.split(",")[0].replace(/["]+/g, '');

    console.log("Symbol" + symbol);

    gf.historical({
        symbol: symbol,
        from: '2014-01-01',
        to: '2014-12-31'
    }, (err, quotes) => {
        console.log(quotes);
        if(err !== null){
            console.log("Error while contacting yahoo");
            console.log(err);
        }

        let promise = collection.insertMany(quotes, (errm, result) => {
            console.log("Mongoresult");
            console.log(result);
            if(err !== null){
                console.log("Error with mongo");
                console.log(errm);
            }
            db.close();
        });
    });

    /*csv()
        .fromFile("nysecompanies-small.csv")
        .on('json',(jsonObj)=>{
            // combine csv header row and csv line to a json object
            // jsonObj.a ==> 1 or 4
            console.log("--------------------");
            console.log(jsonObj);
            console.log("--------------------");

            gf.historical({
                symbol: jsonObj.Symbol,
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
        })
        .on('done',(error)=>{
            console.log('end');
            if(error !== null){
                console.log(error)
            }
        });*/


});










