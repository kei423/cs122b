function getParameterByName(target) {
    // Get request URL
    let url = window.location.href;
    // Encode target parameter name to url encoding
    target = target.replace(/[\[\]]/g, "\\$&");

    // Use regular expression to find matched parameter value
    let regex = new RegExp("[?&]" + target + "(=([^&#]*)|&|#|$)"),
        results = regex.exec(url);
    if (!results) return null;
    if (!results[2]) return '';

    // Return the decoded parameter value
    return decodeURIComponent(results[2].replace(/\+/g, " "));
}

function handleResult(resultData) {
    console.log("handleResult @movie-list.js");
    console.log("length of resultData: " + resultData.length);

    jQuery("#pageHeader").append(resultData[resultData.length-1]["value"]);

    // Limit and Order Select Label
    $("#limitSelect").val(resultData[resultData.length-1]['limit']);
    $("#orderSelect").val(resultData[resultData.length-1]['order']);

    // Movie Container with Movie Cards
    let movieContainerElement = jQuery("#movie-container");
    let movieCardHTML = "";

    for (let i = 0; i < resultData.length - 1; i++) {
        movieCardHTML += '<div class="movie-card">';

        // Movie Title
        movieCardHTML += '<div class="movie-title">' +
            '<a href="single-movie.html?id=' + resultData[i]['movieId'] + '">' +
            resultData[i]["movieTitle"] + '</a>' +
            '</div>';

        // Movie Year
        movieCardHTML += '<div class="movie-info">Year: ' + resultData[i]["movieYear"] + '</div>';

        // Movie Director
        movieCardHTML += '<div class="movie-info">Director: ' + resultData[i]["movieDirector"] + '</div>';

        // Movie Genres
        let genreList = resultData[i]["movieGenres"].split(',').slice(0, 3);
        movieCardHTML += '<div class="movie-info">Genres: ';
        for (let j = 0; j < genreList.length; j++) {
            movieCardHTML += '<a href="movie-list.html?action=browseGenre&value=' + genreList[j] + '">' +
                genreList[j] + '</a>, ';
        }
        movieCardHTML = movieCardHTML.slice(0, -2); // Remove the trailing comma and space
        movieCardHTML += '</div>';

        // Movie Stars
        let starsIdList = resultData[i]['movieStarsId'].split(",").slice(0, 3);
        let starsList = resultData[i]['movieStars'].split(",").slice(0, 3);
        movieCardHTML += '<div class="movie-info">Stars: ';
        for (let j = 0; j < starsIdList.length; j++) {
            movieCardHTML += '<a href="single-star.html?id=' + starsIdList[j] + '">' +
                starsList[j] + '</a>, ';
        }
        movieCardHTML = movieCardHTML.slice(0, -2); // Remove the trailing comma and space
        movieCardHTML += '</div>';

        // Movie Rating
        let rating = resultData[i]["movieRating"] == null ? "N/A" : resultData[i]["movieRating"];
        movieCardHTML += '<div class="movie-info">Rating: ' + rating + '</div>';

        // Add to Cart Form
        movieCardHTML += '<div class="add-to-cart-button" data-movie-id="' + resultData[i]["movieId"] + '">' +
            '<form method="post">' +
            '<input name="action" type="hidden" id="add-cart" value="add">' +
            '<input name="movieId" type="hidden" value="' + resultData[i]["movieId"] + '">' +
            '<input name="movieTitle" type="hidden" value="' + resultData[i]["movieTitle"] + '">' +
            '<input type="submit" class="add-to-cart-button-inner" value="Add to Cart">' +
            '</form>' +
            '</div>';

        movieCardHTML += '</div>';
    }
    movieContainerElement.append(movieCardHTML);


    // Next button, Page number, Prev button,
    let changePageContainerElement = jQuery("#change-page-container");
    let componentHTML = '<form id="prev-form" action="#" method="get"><input type="hidden" name="action" id="qu-input" value="prev">';
    if (resultData[resultData.length-1]["offset"] > 0) {
        componentHTML += '<input type="submit" value="< Prev"></form>';
    } else {
        componentHTML += '<input type="submit" value="< Prev" disabled></form>';
    }
    changePageContainerElement.append(componentHTML);
    componentHTML = '<p>' + (resultData[resultData.length-1]["offset"]/resultData[resultData.length-1]["limit"]+1) + '</\p>';
    changePageContainerElement.append(componentHTML);
    componentHTML = '<form id="next-form" action="#" method="get"><input type="hidden" name="action" id="q-input" value="next">';
    if (resultData[resultData.length-1]["limit"] === resultData[resultData.length-1]["numResults"]) {
        componentHTML += '<input type="submit" value="Next >"></form>';
    } else {
        componentHTML += '<input type="submit" value="Next >" disabled></form>';
    }
    changePageContainerElement.append(componentHTML);

    $(document).ready(function () {
        let $movieCards = $(".movie-card"); // Store the selected elements
        let maxHeight = 0;
        $movieCards.each(function () {
            let currentHeight = $(this).height();
            if (currentHeight > maxHeight) {
                maxHeight = currentHeight;
            }
        });
        $movieCards.height(maxHeight+22);
    });
}

$(document).on('click', '#prev-form input[type="submit"]', function (event) {
    event.preventDefault();
    window.location.href = "movie-list.html?action=prev";
});

$(document).on('click', '#next-form input[type="submit"]', function (event) {
    event.preventDefault();
    window.location.href = "movie-list.html?action=next";
});

let action = getParameterByName('action');
if (action === "browseGenre" || action === "browseTitle" || action === "search") {
    jQuery.ajax({
        dataType: "json",
        method: "GET",
        url: "api/movie-list",
        data: {
            action: getParameterByName('action'),
            value: getParameterByName('value'),
            limit: getParameterByName('limit'),
            order: getParameterByName('order'),
        },
        success: (resultData) => handleResult(resultData)
    });
} else {
    $.ajax({
        dataType: "json",
        method: "GET",
        url: "api/movie-list?",
        data: {
            action: getParameterByName('action'),
            title: getParameterByName('title'),
            year: getParameterByName('year'),
            director: getParameterByName('director'),
            star: getParameterByName('star'),
            limit: getParameterByName('limit'),
            order: getParameterByName('order'),
        },
        success: (resultData) => handleResult(resultData)
    });
}


/**
 * Handle the items in item list
 * @param resultArray jsonObject, needs to be parsed to html
 */
function handleCartArray(resultArray) {
    let item_list = $("#item_list");
    let res = "<ul>";
    for (let i = 0; i < resultArray.length; i++) {
        res += "<li>" + resultArray[i] + "</li>";
    }
    res += "</ul>";

    // clear the old array and show the new array in the frontend
    item_list.html("");
    item_list.append(res);
}

$(document).on('submit', '.add-to-cart-button form', function(event) {
    event.preventDefault();
    const formData = $(this).serialize();
    console.log(formData);
    const params = new URLSearchParams(formData);
    const mTitle = params.get('movieTitle');

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

