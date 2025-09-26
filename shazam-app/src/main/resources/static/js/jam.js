const applicationContext = {
  listening: false
}

function animateJamButton(jamButton) {
  $(jamButton).removeClass("pulse");
  $(jamButton).addClass("pulse");
}

function identifySong() {

  if (applicationContext.listening) {
    $("#inProgressCircles").toggle();
    $("#musicNotesIcon").fadeIn(500);
  }
  else {
    $("#musicNotesIcon").toggle();
    $("#inProgressCircles").fadeIn(1250);
  }

  applicationContext.listening = !applicationContext.listening;
}

function toggleUploadSongMenu() {
  $("#uploadSongFormContainer").toggleClass("active");
}

$("#jamButton").click(event => identifySong());
