/**
 * Before this .js is loaded, the html skeleton is created.
 *
 * This .js performs two steps:
 *      1. Use jQuery to talk to backend API to get the json data.
 *      2. Populate the data to correct html elements.
 */


/**
 * Handle the items in item list
 * @param resultData jsonObject, needs to be parsed to html
 */
function handleSaleArray(resultData) {
    // $("#sale_table_body tr").remove();
    console.log(resultData[0]["status"]);
    console.log(resultData[0]["message"]);
    let sale_list = $("#sale_table_body");
    let overallTotal = 0;
    // change it to html list
    for (let i = 0; i < resultData.length; i++) {
        console.log(resultData[i]["status"]);
        console.log(resultData[i]["message"]);
        if (resultData[i]["status"] === "success") {
            let res = "";
            res += "<tr>";
            res += "<td>" + resultData[i]["saleId"] + "</td>";
            res += "<td>" +
                '<a href="single-movie.html?id=' + resultData[i]["movieId"] + '">' +
                resultData[i]["movieName"] + '</a>' +
                "</td>";
            res += "<td>" +
                "<span>1</span>" +
                "</td>";
            res += "<td>$42</td>";
            res += "</tr>";
            overallTotal += 42;
            sale_list.append(res);
        }
    }
    let overallTotalRow = "<tr><td colspan='3'></td><td id='overall-total'>$" + overallTotal + "</td></tr>";
    sale_list.append(overallTotalRow);
}
/**
 * Once this .js is loaded, following scripts will be executed by the browser
 */

$.ajax({
    dataType: "json", // Setting return data type
    method: "POST", // Setting request method
    url: "api/confirmation",
    success: (resultData) => {
        handleSaleArray(resultData);
    }
});