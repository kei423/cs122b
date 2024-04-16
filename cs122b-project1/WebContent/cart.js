

/**
 * Handle the items in item list
 * @param resultItems jsonObject, needs to be parsed to html
 * @param resultTitles jsonObject, needs to be parsed to html
 * @param resultCounts jsonObject, needs to be parsed to html
 */
function handleCartArray(resultItems, resultTitles, resultCounts) {
    console.log("Cart Results: Items=" + resultItems + " Titles=" + resultTitles + " Counts=" + resultCounts);
    $("#cart_table_body tr").remove();
    let item_list = $("#cart_table_body");
    let overallTotal = 0;
    // change it to html list
    for (let i = 0; i < resultItems.length; i++) {
        console.log(resultTitles[i]);
        console.log(resultItems[i]);
        let res = "";
        res += "<tr>";
        res += "<td>" +
            '<a href="single-movie.html?id=' + resultItems[i] + '">' +
            resultTitles[i] + '</a>' +
            "</td>";
        res += "<td>" +
            "<button class = 'quantity-button decrease-quantity'>-</button>" +
            // stores id here to update the backend whenever the movie quantity is changed
            "<span style='margin: auto 5px;' class='quantity-display' data-movie-id=" + resultItems[i] + ">" + resultCounts[i] + "</span>" +
            "<button class='quantity-button increase-quantity'>+</button>" +
            "</td>";
        res += "<td>" +
            "<button class='delete-button' value=" + resultItems[i] + ">Delete</button>" +
            "</td>";
        res += "<td>$42</td>";
        res += "<td>$"  + (42 * resultCounts[i]) + "</td>";
        res += "</tr>";
        overallTotal += 42 * resultCounts[i];
        item_list.append(res);
    }
    if (overallTotal > 0) {
        let overallTotalRow = "<tr><td colspan='3'></td><td><button class='checkout-button'>Checkout</button></td><td id='overall-total'>$" + overallTotal + "</td></tr>";
        item_list.append(overallTotalRow);
    }
    else {
        let overallTotalRow = "<tr><td colspan='2'></td><td>Cart is Empty.</td><td colspan='2'></td></tr>";
        item_list.append(overallTotalRow);
    }
}


$.ajax({
    dataType: "json", // Setting return data type
    method: "GET", // Setting request method
    url: "api/cart?",
    data: {
    },
    success: (resultData) => {
        handleCartArray(resultData["previousItems"], resultData["previousTitles"], resultData["previousCounts"]);
    }
});

$(document).on('click', '.quantity-button', function () {
    const quantityDisplay = $(this).siblings('.quantity-display');
    const movieId = quantityDisplay.data('movie-id');
    let currentAmount = parseInt(quantityDisplay.text(), 10);

    if ($(this).hasClass('decrease-quantity')) {
        // Decrement button clicked
        if (currentAmount > 1) {
            currentAmount--;
        }
    } else if ($(this).hasClass('increase-quantity')) {
        // Increment button clicked
        currentAmount++;
    }
    $.ajax({
        method: 'GET',
        url: 'api/cart',
        data: {
            amount: currentAmount,
            movieId : movieId,
            action: "modifyAmount"
        },
        success: (resultDatas) => {
            let resultData = JSON.parse(resultDatas);
            handleCartArray(resultData["previousItems"], resultData["previousTitles"], resultData["previousCounts"]);
        }
    });
});

$(document).on('click', '.delete-button', function () {
    const movieId = $(this).val();
    $.ajax({
        method: 'GET',
        url: 'api/cart',
        data: {
            movieId : movieId,
            action: "deleteMovie"
        },
        success: (resultDatas) => {
            let resultData = JSON.parse(resultDatas);
            handleCartArray(resultData["previousItems"], resultData["previousTitles"], resultData["previousCounts"]);
        }
    });
});

$(document).on('click', '.checkout-button', function () {
    console.log("Success: proceeding to checkout");
    window.location.replace("payment.html");
});


// Autocomplete
// This Javascript code uses this library: https://github.com/devbridge/jQuery-Autocomplete

/*
 * This function is called by the library when it needs to lookup a query.
 *
 * The parameter query is the query string.
 * The doneCallback is a callback function provided by the library, after you get the
 *   suggestion list from AJAX, you need to call this function to let the library know.
 */
function handleLookup(query, doneCallback) {
    console.log("autocomplete initiated")

    // for (i = 0; i < localStorage.length; i++)   {
    //     console.log(localStorage.key(i) + "=[" + localStorage.getItem(localStorage.key(i)) + "]");
    // }
    let cachedData = localStorage.getItem(query)
    if (cachedData) {
        console.log("using cached results from local storage");
        console.log("cached data: " + cachedData);
        handleLookupAjaxSuccess(cachedData, query, doneCallback);
    } else {
        console.log("sending AJAX request to backend Java Servlet")
        jQuery.ajax({
            "method": "GET",
            // escape the query string to avoid errors caused by special characters
            "url": "api/movie-suggestion?query=" + escape(query),
            "success": function(data) {
                // pass the data, query, and doneCallback function into the success handler
                console.log("data from jquery: " + JSON.stringify(data));
                handleLookupAjaxSuccess(JSON.stringify(data), query, doneCallback)
            },
            "error": function(errorData) {
                console.log("lookup ajax error")
                console.log(errorData)
            }
        })
    }
}

/*
 * This function is used to handle the ajax success callback function.
 * It is called by our own code upon the success of the AJAX request
 * data is the JSON data string you get from your Java Servlet
 *
 */
function handleLookupAjaxSuccess(data, query, doneCallback) {
    // console.log("query: " + query + " data: " + data);
    let jsonData = JSON.parse(data);

    localStorage.setItem(query, data);

    // call the callback function provided by the autocomplete library
    // add "{suggestions: jsonData}" to satisfy the library response format according to
    //   the "Response Format" section in documentation
    doneCallback( { suggestions: jsonData } );
}


/*
 * This function is the select suggestion handler function.
 * When a suggestion is selected, this function is called by the library.
 *
 * You can redirect to the page you want using the suggestion data.
 */
function handleSelectSuggestion(suggestion) {
    console.log("you select " + suggestion["value"] + " with ID " + suggestion["data"]["movieID"])
    window.location.href = "single-movie.html?id=" + suggestion["data"]["movieID"];

}


/*
 * This statement binds the autocomplete library with the input box element and
 *   sets necessary parameters of the library.
 *
 * The library documentation can be find here:
 *   https://github.com/devbridge/jQuery-Autocomplete
 *   https://www.devbridge.com/sourcery/components/jquery-autocomplete/
 *
 */
// $('#autocomplete') is to find element by the ID "autocomplete"
$('#autocomplete').autocomplete({
    // documentation of the lookup function can be found under the "Custom lookup function" section
    lookup: function (query, doneCallback) {
        handleLookup(query, doneCallback);
    },
    onSelect: function(suggestion) {
        handleSelectSuggestion(suggestion);
    },
    deferRequestBy: 300,
    minChars: 3,
    lookupLimit: 10,
});

/*
 * do normal full text search if no suggestion is selected
 */
function handleNormalSearch(query) {
    console.log("doing normal search with query: " + query);
    window.location.href = "movie-list.html?action=search&value=" + query;
}

// bind pressing enter key to a handler function
$('#autocomplete').keypress(function(event) {
    // keyCode 13 is the enter key
    if (event.keyCode === 13) {
        // pass the value of the input box to the handler function
        handleNormalSearch($('#autocomplete').val())
    }
})

$(document).on('click', '.search-button', function () {
    handleNormalSearch($('#autocomplete').val())
});
