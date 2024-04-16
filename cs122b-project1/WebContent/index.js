/**
 * Before this .js is loaded, the html skeleton is created.
 *
 * This .js performs two steps:
 *      1. Use jQuery to talk to backend API to get the json data.
 *      2. Populate the data to correct html elements.
 */


function handleResult(resultData) {

    let genreContainer = jQuery("#genres-container");

    for (let i = 0; i < resultData.length; i++) {
        let genreHTML = '<a href="movie-list.html?action=browseGenre&value=' + resultData[i]['genreName'] + '">';
        genreHTML += resultData[i]['genreName'] + '</a>';
        genreContainer.append(genreHTML);
    }


    let titleContainer = jQuery("#title-container");
    let titles = [
        'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M',
        'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '*',
    ];

    for (let i = 0; i < titles.length; i++) {
        let titleHTML = '<a href="movie-list.html?action=browseTitle&value=' + titles[i] + '">';
        titleHTML += titles[i]+ '</a>';
        titleContainer.append(titleHTML)
    }
}


// Makes the HTTP GET request and registers on success callback function handleResult
jQuery.ajax({
    dataType: "json",
    method: "GET",
    url: "api/home",
    success: (resultData) => handleResult(resultData)
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
    // console.log("you select " + suggestion["value"] + " with ID " + suggestion["data"]["movieID"])
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
