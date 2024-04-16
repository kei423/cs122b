let dashboard_insert_genre_form = $("#dashboard_insert_genre_form");

/**
 * Handle the data returned by LoginServlet
 * @param resultDataJson jsonObject
 */
function handleResult(resultDataJson) {
    console.log("@_dashboardLogin.js Response received: " + resultDataJson);
    $("#dashboard_insert_genre_form :input[type='text']").val('');

    if (resultDataJson["status"] === "success") {
        console.log(resultDataJson["message"]);
        $("#dashboard_insert_genre_msg").text(resultDataJson["message"]);
    } else {
        console.log("show error message");
        console.log(resultDataJson["message"]);
        $("#dashboard_insert_genre_msg").text(resultDataJson["message"]);
    }
}

function getFullURL(path) {
    let pathArray = window.location.pathname.split('/');

    return '/' + pathArray[1] + "/_dashboard" + path;
}


function submitDashboardInsertGenreForm(formSubmitEvent) {
    console.log("submit insert genre form");

    formSubmitEvent.preventDefault();
    console.log(dashboard_insert_genre_form.serialize());

    $.ajax({
            url:getFullURL("/insert"),
            method: "POST",
            dataType: "json",
            data: dashboard_insert_genre_form.serialize(),
            success: handleResult
        }
    );
}
console.log(getFullURL("/insert"));
// Bind the submit action of the form to a handler function
dashboard_insert_genre_form.submit(submitDashboardInsertGenreForm);