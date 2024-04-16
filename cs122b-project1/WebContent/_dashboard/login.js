/**
 * Handle the data returned by DashboardLoginServlet
 * @param resultDataJson jsonObject
 */
function handleResult(resultDataJson) {
    console.log("@_dashboardLogin.js Response received: " + resultDataJson);

    if (resultDataJson["status"] === "success") {
        window.location.replace("home.html");
    } else {
        console.log("show error message");
        console.log(resultDataJson["message"]);
        $("#dashboard_login_error_msg").text(resultDataJson["message"]);
        grecaptcha.reset();
    }
}

function getFullURL(path) {
    let pathArray = window.location.pathname.split('/');
    return '/' + pathArray[1] + "/api/_dashboard" + path;
}

function submitDashboardLoginForm(formSubmitEvent) {
    formSubmitEvent.preventDefault();
    console.log(getFullURL("/login"));

    $.ajax({
        url:getFullURL("/login"),
        method: "POST",
        dataType: "json",
        data: dashboard_login_form.serialize(),
        success: handleResult
        }
    );
}

let dashboard_login_form = $("#dashboard_login_form");
dashboard_login_form.submit(submitDashboardLoginForm);