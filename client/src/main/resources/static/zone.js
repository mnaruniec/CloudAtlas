$(document).ready(function () {
    var intervalMs = 5000;
    var zoneName = $('#zone_name').text();

    var successFun = function (data) {
        if (data.error) {
            $('#error').text("ERROR: Failed to fetch current data. " + data.error);
        } else {
            $('#error').text("");
            var table = $('#attribute_table');
            table.empty();
            for (var i = 0; i < data.data.length; i++) {
                table.append(
                    '<tr>' +
                    '<td id="name_' + i.toString() + '"></td>' +
                    '<td id="type_' + i.toString() + '"></td>' +
                    '<td id="value_' + i.toString() + '"></td>' +
                    '</tr>'
                );
                $('#name_' + i.toString()).text(data.data[i].name);
                $('#type_' + i.toString()).text(data.data[i].type);
                $('#value_' + i.toString()).text(data.data[i].value);
            }
        }
    };

    var failureFun = function () {
        $('#error').text("ERROR: Data request to HTTP server failed.")
    };

    var intervalFun = function() {
        $.ajax('/data/zone?name=' + zoneName)
            .done(successFun)
            .fail(failureFun);
    };

    intervalFun();

    setInterval(
        intervalFun,
        intervalMs
    );
});