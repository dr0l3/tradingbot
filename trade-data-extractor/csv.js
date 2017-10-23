var fastcsv = require("fast-csv");
const csv= require('csvtojson');



fastcsv
    .fromPath("nysecompanies.csv")
    .on("data", (data) => {
        console.log(data);
        csv.fromString(data)
            .on('csv', (csvRow) => {console.log(csvRow)})
            .on('done',() => {console.log("done")});
    })
    .on("end", () => {
        console.log("done");
    });