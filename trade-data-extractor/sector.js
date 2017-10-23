const gf = require('google-finance');
const mongo = require('mongodb').MongoClient;
const url = 'mongodb://localhost:32768/example';
const csv=require('csvtojson');

mongo.connect(url, (err,db) => {
    console.log("connected");

    const collection = db.collection('sectorinfo');

    csv()
        .fromFile("nysecompanies.csv")
        .on('json',(jsonObj)=>{
            // combine csv header row and csv line to a json object
            // jsonObj.a ==> 1 or 4
            console.log(jsonObj);

            collection.insertOne(jsonObj, (err, result) => {
                console.log(result);
                if(err !== null){
                    console.log(err);
                }
            });
        })
        .on('done',(error)=>{
            console.log('end');
            if(error !== null){
                console.log(error)
            }
            db.close();
        })
});










