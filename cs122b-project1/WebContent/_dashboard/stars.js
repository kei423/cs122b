let dashboard_insert_star_form = $("#dashboard_insert_star_form");

/**
 * Handle the data returned by LoginServlet
 * @param resultDataJson jsonObject
 */
function handleResult(resultDataJson) {
    console.log("@_dashboard/stars.js Response received: " + resultDataJson);
    $("#dashboard_insert_star_form :input[type='text'], #dashboard_insert_star_form :input[type='number']").val('');

    if (resultDataJson["status"] === "success") {
        console.log(resultDataJson["message"]);
        $("#dashboard_insert_star_msg").text(resultDataJson["message"]);
    } else {
        console.log("show error message");
        console.log(resultDataJson["message"]);
        $("#dashboard_insert_star_msg").text(resultDataJson["message"]);
    }
}

function getFullURL(path) {
    let pathArray = window.location.pathname.split('/');

    return '/' + pathArray[1] + "/_dashboard" + path;
}


function submitDashboardInsertStarForm(formSubmitEvent) {
    console.log("submit insert star form");

    formSubmitEvent.preventDefault();
    console.log(getFullURL("/insert"));
    console.log(dashboard_insert_star_form.serialize());

    $.ajax({
            url:getFullURL("/insert"),
            method: "POST",
            dataType: "json",
            data: dashboard_insert_star_form.serialize(),
            success: handleResult
        }
    );
}
console.log(getFullURL("/insert"));
dashboard_insert_star_form.submit(submitDashboardInsertStarForm);