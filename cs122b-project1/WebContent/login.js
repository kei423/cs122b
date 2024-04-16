/**
 * Handle the data returned by LoginServlet
 * @param resultDataJson jsonObject
 */
function handleResult(resultDataJson) {
    console.log("@Login.js Response received: " + resultDataJson);

    if (resultDataJson["status"] === "success") {
        localStorage.clear();
        window.location.replace("index.html");
    } else {
        console.log("login error: " + resultDataJson["message"]);
        $("#login_error_msg").text(resultDataJson["message"]);
        grecaptcha.reset();
    }
}

function submitLoginForm(formSubmitEvent) {
    formSubmitEvent.preventDefault();

    $.ajax(
        "api/login", {
            method: "POST",
            data: login_form.serialize(),
            success: handleResult
        }
    );
}

let login_form = $("#login_form");
login_form.submit(submitLoginForm);