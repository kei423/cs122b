function createTable(tableData) {
    let $table = $("<table class='table'>");

    $table.append($("<caption>" + tableData["tableName"]+ "</caption>"));

    let $thead = $("<thead>");
    let $headerRow = $("<tr>");
    $headerRow.append($("<th style='width: 50%;'>Attribute</th>"));
    $headerRow.append($("<th style='width: 50%;'>Type</th>"));
    $thead.append($headerRow);

    let $tbody = $("<tbody>");

    for (let i = 0; i < tableData["columns"].length; i++) {
        let rowData = tableData["columns"][i];
        let rowHTML = "<tr>";
        rowHTML += "<td style='width: 50%;'>" + rowData["Field"] + "</td>";
        rowHTML += "<td style='width: 50%;'>" + rowData["Type"] + "</td>";
        rowHTML += "</tr>";
        $tbody.append(rowHTML);
    }

    $table.append($thead);
    $table.append($tbody);

    return $table;
}

function fillPageWithTables(data) {
    console.log(data);
    const $tableContainer = $("#table_container");

    $tableContainer.empty();

    for (let index = 0; index < data.length; index++) {
        let table = data[index];
        let $table = createTable(table);

        $tableContainer.append($table);
    }
}

function getFullURL(path) {
    let pathArray = window.location.pathname.split('/');

    return '/' + pathArray[1] + "/_dashboard" + path;
}

$(document).ready(function () {
    $.ajax({
        url: getFullURL("/index"),
        method: "GET",
        dataType: "json",
        success: function (data) {
            fillPageWithTables(data);
        }
    });
});
