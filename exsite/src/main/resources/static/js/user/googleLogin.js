window.onload = () => {
    ///////////////////// GOOGLE_ClIENT_ID, GOOGLE_REDIRECT_URL 넣는 자리 //////////////////////////////

    
    ////////////////////////////////////////////////////////////////////////////////////////////////////
    // btn-google이라는 아이디 속성을 가진 요소의 클릭 이벤트
    $("#google-signup-btn-img").click(() => {
        const url = "https://accounts.google.com/o/oauth2/v2/auth"
                    + "?client_id=" + GOOGLE_ClIENT_ID
                    + "&redirect_uri=" + GOOGLE_REDIRECT_URL
                    + "&response_type=code"
                    + "&scope=email profile";
        location.href = url;
    });

    $("#google-login-btn-img").click(() => {
        const url = "https://accounts.google.com/o/oauth2/v2/auth"
                    + "?client_id=" + GOOGLE_ClIENT_ID
                    + "&redirect_uri=" + GOOGLE_REDIRECT_URL
                    + "&response_type=code"
                    + "&scope=email profile";
        location.href = url;
    });
};

