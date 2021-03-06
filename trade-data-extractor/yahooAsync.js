const gf = require('yahoo-finance');
const mongo = require('mongodb').MongoClient;
const url = 'mongodb://localhost:32768/example';
const csv=require('csvtojson');



mongo.connect(url, (err,db) => {
    console.log("connected");

    const collection = db.collection('prices');

    let res = csv()
        .fromFile("nasdaqcompanies.csv")
        .on('json',(jsonObj)=>{
            // combine csv header row and csv line to a json object
            // jsonObj.a ==> 1 or 4
            console.log("--------------------");
            console.log(jsonObj.Symbol);

            gf.historical({
                symbol: jsonObj.Symbol,
                from: '2014-01-01',
                to: '2014-12-31'
            }, (err, quotes) => {
                console.log("fetched quotes");
                console.log(quotes);
                // collection.insertMany(quotes, (err, result) => {
                //     console.log("Inserted stuff to monog");
                //     console.log(result);
                //     if(err !== null){
                //         console.log(err);
                //     }
                // });
            });
        })
        .on('done',(error)=>{
            console.log('end');
            db.close();
            if(error !== null){
                console.log(error)
            }
        });

    Promise.all(res).then(()=> db.close())

});










