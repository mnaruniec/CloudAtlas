$(document).ready(function () {
    var ctr = 0;
    setInterval(
        function () {
            $("#some").text((ctr++).toString(10));
        },
        1000
    );
});