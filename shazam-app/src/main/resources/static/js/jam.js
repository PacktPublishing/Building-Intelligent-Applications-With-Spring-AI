// WhatsMyJam JavaScript Controller logic

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

  if ($("#uploadSongFormContainer").hasClass("active")) {
    $("#artist").focus();
  }
  else {
    $("#jamButton").focus();
  }
}

$("#jamButton").click(event => identifySong());

$("#songFile").on("change", function(event) {

  const artist = $("#artist");
  const songTitle = $("#songTitle");

  if (isEmpty(artist.val()) || isEmpty(songTitle.val())) {
    const files = event.target.files;
    if (files.length > 0) {
      const file = files[0];
      const artistSongTitle = file.name.split("-");
      if (artistSongTitle.length > 1) {
        if (isEmpty(artist)) {
          artist.val(artistSongTitle[0]);
        }
        if (isEmpty(songTitle)) {
          songTitle.val(artistSongTitle[1]);
        }
      }
    }
  }
});

$("#uploadSongForm").on("submit", function(event) {

  event.preventDefault();

  const uploadSongButton = $("#uploadSongButton")
  const uploadSongFormData = new FormData(this);

  uploadSongButton.prop("disabled", true);
  uploadSongButton.html('<img src="../img/spinner.png" width="24" height="24" alt="✔️">');

  $.ajax({
    type: 'POST',
    url: '/music/api/songs',
    data: uploadSongFormData,
    processData: false, // prevent jQuery from transforming the data
    contentType: false, // let Web browser set multipart boundaries
    success: function(response) {
      console.log('Upload successful:', response);
      uploadSongButton.html('<img src="../img/checkmark-green-2.png" width="24" height="24" alt="✔️">');
    },
    error: function(xhr, status, error) {
      console.error('Upload failed:', error);
      uploadSongButton.html('<img src="../img/x-red.png" width="24" height="24" alt="Ⅹ️">');
    },
    complete: function() {
      setTimeout(function() {
        uploadSongButton.text("Upload");
        uploadSongButton.prop("disabled", false);
      }, 3000);
    }
  });
});

function isEmpty(string) {
  return !isNotEmpty(string);
}

function isNotEmpty(string) {
  return typeof string === "string" && string.trim().length > 0;
}
