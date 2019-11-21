$(document).ready(function () {
    var intervalMs = 5000;
    var zoneName = $('#zone_name').text();
    var attrName = $('#attribute').text();

    var ctx = $('#plot');
    var plotData = {
        type: 'line',
        data: {
            labels: [],
            datasets: [{
                label: 'Value',
                data: [],
                data_type: [],  // custom field
                borderWidth: 1,
                // showLine: false
            }]
        },
        options: {
            tooltips: {
                callbacks: {
                    afterBody: function (items, data) {
                        return data.data_type;
                    }
                }
            }
        }
    };
    var plot = new Chart(ctx, plotData);

    var successFun = function (data) {
        if (data.error) {
            $('#error').text("ERROR: Failed to fetch current data. " + data.error);
        } else {
            $('#error').text();
            plotData.data.datasets[0].data = data.data;
            plotData.data.datasets[0].data_type = data.data_type;
            plotData.data.labels = data.label;
            plot.update();
        }
    };

    var failureFun = function () {
        $('#error').text("ERROR: Data request to HTTP server failed.")
    };

    var intervalFun = function() {
        $.ajax('/data/attribute?zone=' + zoneName + '&attribute=' + attrName)
            .done(successFun)
            .fail(failureFun);
    };

    intervalFun();

    setInterval(
        intervalFun,
        intervalMs
    );
});