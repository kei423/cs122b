/**
 * Before this .js is loaded, the html skeleton is created.
 *
 * This .js performs three steps:
 *      1. Get parameter from request URL, so it know which id to look for
 *      2. Use jQuery to talk to backend API to get the json data.
 *      3. Populate the data to correct html elements.
 */


/**
 * Retrieve parameter from request URL, matching by parameter name
 * @param target String
 * @returns {*}
 */
function getParameterByName(target) {
    // Get request URL
    let url = window.location.href;
    // Encode target parameter name to url encoding
    target = target.replace(/[\[\]]/g, "\\$&");

    // Ues regular expression to find matched parameter value
    let regex = new RegExp("[?&]" + target + "(=([^&#]*)|&|#|$)"),
        results = regex.exec(url);
    if (!results) return null;
    if (!results[2]) return '';

    // Return the decoded parameter value
    return decodeURIComponent(results[2].replace(/\+/g, " "));
}


function handleResult(resultData) {
    let pageTitleElement = jQuery("#page_title");
    pageTitleElement.append(resultData[0]["starName"]);

    let starNameElement = jQuery("#star_name");
    starNameElement.append("Actor: " + resultData[0]["starName"]);

    let starDobElement = jQuery("#star_dob");
    let birthYear = resultData[0]["starDoB"] == null ? "N/A" : resultData[0]["starDoB"];
    starDobElement.append("Birth Year: " + birthYear);

    let movieContainerElement = jQuery("#movie-info-container");
    let movieCardHTML = "";
    for (let i = 0; i < resultData.length; i++) {
        movieCardHTML += '<div class="single-star-container"> <div class="single-star-container-circle"></div>';
        movieCardHTML += '<div class="single-star-container-content">';
        movieCardHTML += '<h2><a href="single-movie.html?id=' + resultData[i]['movieId'] + '">' +
            resultData[i]["movieTitle"] + '</a></h2>';
        movieCardHTML += "<p>" + resultData[i]["movieTitle"] + " was released in " + resultData[i]["movieYear"] +
            " and was directed by " + resultData[i]["movieDirector"] + "</p>";
        movieCardHTML += '<div class="add-to-cart-button" data-movie-id="' + resultData[i]["movieId"] + '">' +
            '<form method="post">' +
            '<input name="action" type="hidden" id="add-cart" value="add">' +
            '<input name="movieId" type="hidden" value="' + resultData[i]["movieId"] + '">' +
            '<input name="movieTitle" type="hidden" value="' + resultData[i]["movieTitle"] + '">' +
            '<input type="submit" class="add-to-cart-button-inner" value="Add to Cart">' +
            '</form>' +
            '</div>';
        movieCardHTML += '</div></div>';
    }
    movieContainerElement.append(movieCardHTML);
}


// Makes the HTTP GET request and registers on success callback function handleResult
jQuery.ajax({
    dataType: "json",  // Setting return data type
    method: "GET",// Setting request method
    url: "api/single-star?id=" + getParameterByName('id'),
    success: (resultData) => handleResult(resultData)
});


function handleCartArray(resultArray) {
    let item_list = $("#item_list");
    // change it to html list
    let res = "<ul>";
    for (let i = 0; i < resultArray.length; i++) {
        // each item will be in a bullet point
        res += "<li>" + resultArray[i] + "</li>";
    }
    res += "</ul>";

    // clear the old array and show the new array in the frontend
    item_list.html("");
    item_list.append(res);
}

$(document).on('submit', '.add-to-cart-button form', function(event) {
    event.preventDefault(); // Prevent the default form submission behavior

    // Collect form data
    const formData = $(this).serialize();
    console.log(formData);
    const params = new URLSearchParams(formData);

    const mTitle = params.get('movieTitle');

    // Make an AJAX post request to the cart
    $.ajax({
        method: 'POST',
        url: 'api/cart',
        data: formData,
        success: resultDataString => {
            let resultDataJson = JSON.parse(resultDataString);
            handleCartArray(resultDataJson["previousItems"]);
            window.alert("Successfully added " + mTitle);
        }
    });
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
