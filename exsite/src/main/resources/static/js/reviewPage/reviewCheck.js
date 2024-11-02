$(document).ready(function () {
    $("#more").click(function () {
        const $textContent = $("#review-content");
        $textContent.toggleClass("expanded");

        if ($textContent.hasClass("expanded")) {
            $(this).text("간략히");
        } else {
            $(this).text("더보기");
        }
    });
});