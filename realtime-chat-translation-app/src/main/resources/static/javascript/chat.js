// OpenAI ChatGPT (5.0) Generated JavaScript
//const $ = (selection, node = document) => node.querySelector(selection);
//const $$ = (selection, node = document) => [...node.querySelectorAll(selection)];

const messageInput = $("#messageInput");

let mediaRecorder;

var audioData = [];
var recordingAudio = false;

const applicationContext = {
  users: new Map(),
  messages: []
};

const initApp = () => {
  requestMessages();
  requestUsers();
  messageInput.focus();
}

const generateHue = () => Math.floor(Math.random() * 360);

function agentLanguage() {
  const agentLanguage = navigator.language;
  const agentLanguageCode = agentLanguage.split("-")[0].trim();
  return agentLanguageCode;
}

function escapeHtml(str) {
  return str.replace(/[&<>"]+/g, s => ({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;'}[s]));
}

function logInfo(text) {
  console.log(text);
}

function resolveLanguage(language) {
  return language ? language.code : agentLanguage;
}

function timeNow() {
  const date = new Date();
  return date.toLocaleTimeString([], {
    hour: '2-digit',
    minute: '2-digit'
  });
}

function toJson(obj) {
  return JSON.stringify(obj);
}

function addUser(user) {
  if (user) {
    const applicationUser = applicationContext.users.get(user.id);
    if (applicationUser) {
      applicationUser.status = user.status;
      showUserStatus(applicationUser);
    }
    else {
      applicationContext.users.set(user.id, user);
      showUsers();
    }
  }
}

function newUser(template) {

  const user = {
    id: template.id,
    name: template.name,
    language: resolveLanguage(template.language),
    hue: generateHue(),
    status: template.status
  }

  return user;
}

function requestUsers() {

  const userId = $("#chatUserId").val();
  const sessionId = $("#chatSessionId").val();

  const GetChatUsersRequest = {
    userId: userId
  }

  $.ajax({
    method: 'PUT',
    url: `/chat-app/api/${sessionId}/users`,
    contentType: "application/json; charset=utf-8",
    data: toJson(GetChatUsersRequest),
    dataType: "json",
    success: function(users) {
      //logInfo(toJson(users));
      users.forEach(chatUser => addUser(newUser(chatUser)));
    },
    error: function(xhr, status, errorMessage) {
      logInfo(`Failed to get chat users from chat session [${sessionId}]: ${status} - ${errorMessage}`);
    },
    complete: function() {
      setTimeout(requestUsers, 2000);
    }
  });
}

function resolveUser(message) {

  var applicationUser = applicationContext.users.get(message.user.id);

  if (!applicationUser) {
    applicationUser = newUser(message.user);
  }

  return applicationUser;
}

function showUsers() {

  const userBar = $("#userbar");

  userBar.empty();

  // iterating (value, key) entries
  applicationContext.users.forEach((user, userId) => {
    const userTile = document.createElement("div");
    userTile.className = "usertile";

    const userStatusElement = document.createElement("div");
    userStatusElement.setAttribute("id", userId);
    userStatusElement.className = "green-status-dot"
    userTile.append(userStatusElement);

    const userNameElement = document.createElement("div");
    userNameElement.className = "tile";
    userNameElement.style.setProperty("--h", user.hue);
    userNameElement.textContent = user.name;
    userTile.append(userNameElement);

    userBar.append(userTile);
  });
}

function showUserStatus(user) {

  const userStatus = user.status;
  const userStatusElement = $(`#${user.id}`);

  //logInfo(`USER [${user.name}] has Status [${userStatus}] and Class [${userStatusElement.attr("class")}]`);

  switch (userStatus) {
    case "ACTIVE":
      userStatusElement.attr("class", "green-status-dot");
      break;
    case "INACTIVE":
      userStatusElement.attr("class", "orange-status-dot");
      break;
    case "LEFT":
      userStatusElement.attr("class", "gray-status-dot");
      break;
    default:
  }
}

function postMessage(message) {

  if (!message.text) {
    logInfo(`Message [${message.id}] from user [${message.user.name}] has no text`);
    return;
  }

  saveMessage(message);
  showMessage(message);
}

function postAudioMessage(audioMessage, audioBlob) {

  const userId = $("#chatUserId").val();
  const username = $("#chatUserName").val();
  const sessionId = $("#chatSessionId").val();

  const message = {
    id: null,
    user: { id: userId, name: username },
    audio: audioBlob,
    text: audioMessage.text,
    time: timeNow()
  }

  const messageId = sendMessage(sessionId, message);

  message.id = messageId;

  postMessage(message);
  messageInput.focus();
}

function postTextMessage(event) {

  const messageText = messageInput.val().trim();

  if (!messageText) return;

  const userId = $("#chatUserId").val();
  const username = $("#chatUserName").val();
  const sessionId = $("#chatSessionId").val();

  const message = {
    id: null,
    user: { id: userId, name: username },
    audio: null,
    text: messageText,
    time: timeNow()
  }

  const messageId = sendMessage(sessionId, message);

  message.id = messageId;

  postMessage(message);
  messageInput.val("");
  messageInput.focus();
}

function recordMessage(event) {

  recordingAudio = !recordingAudio;

  if (recordingAudio) {
    try {
      const mediaStreamPromise = navigator.mediaDevices.getUserMedia({ audio: true });

      mediaStreamPromise.then(mediaStream => {
        mediaRecorder = new MediaRecorder(mediaStream);

        mediaRecorder.ondataavailable = event => {
          const eventData = event.data;
          if (eventData.size > 0) {
            audioData.push(eventData);
          }
        };

        mediaRecorder.onstop = event => {
          const audioBlob = new Blob(audioData, { type: "audio/webm" }); // audio/webm; codecs=opus
          const formData = new FormData();

          logInfo(`Audio Data size [${audioBlob.size}] and type [${audioBlob.type}}]`);

          formData.append("audioMessage", audioBlob, "audioMessage.webm");
          formData.append("fileName", "audioMessage.webm");

          $.ajax({
            method: "POST",
            url: "/chat-app/api/audio/transcription",
            data: formData,
            processData: false,
            contentType: false,
            enctype: "multipart/form-data",
            success: function(audioMessage) {
              postAudioMessage(audioMessage, audioBlob);
            },
            error: function(xhr, status, error) {
              logInfo(`Failed to transcribe audio: ${error}`);
            },
            complete: function() {
              //setTimeout(() => playAudio(audioBlob), 3000);
            }
          });
        };

        audioData = [];
        toggleMic();
        mediaRecorder.start();
      });
    }
    catch (error) {
      toggleMic();
      logInfo(`Microphone access denied: ${error}`);
    }
  }
  else {
    mediaRecorder.stop();
    toggleMic();
  }
}

function requestMessages() {

  const userId = $("#chatUserId").val();
  const sessionId = $("#chatSessionId").val();

  $.ajax({
    method: 'GET',
    url: `/chat-app/api/${sessionId}/users/${userId}/messages`,
    contentType: "application/json; charset=utf-8",
    dataType: "json",
    success: function(messages) {
      //logInfo(toJson(messages));

      messages.forEach(chatMessage => {

        const message = {
          id: chatMessage.id,
          user: { id: chatMessage.user.id, name: chatMessage.user.name },
          audio: null,
          text: chatMessage.message,
          time: timeNow()
        };

        postMessage(message);
      });
    },
    error: function(xhr, status, errorMessage) {
      logInfo(`Failed to request chat messages from chat session [${sessionId}]: ${status} - ${errorMessage}`);
    },
    complete: function() {
      setTimeout(requestMessages, 1000);
    }
  });
}

function saveMessage(message) {
  applicationContext.messages.push(message);
  return message;
}

function sendMessage(sessionId, message) {

  const PostChatMessageRequest = {
      userId: message.user.id,
      message: message.text
  }

  var messageId;

  $.ajax({
    method: 'POST',
    url: `/chat-app/api/${sessionId}/messages`,
    data: JSON.stringify(PostChatMessageRequest),
    contentType: "application/json; charset=utf-8",
    dataType: "json",
    success: function(response) {
      // logInfo(toJson(response));
      messageId = response.id;
    },
    error: function(xhr, status, errorMessage) {
      const messageJson = toJson(PostChatMessageRequest);
      logInfo(`Failed to post chat message [${messageJson}]: ${status} - ${errorMessage}`);
    }
  });

  return messageId;
}

function showMessage(message) {

  const user = resolveUser(message);
  const userId = $("#chatUserId").val();
  const isMe = user != null && user.id === userId;
  const row = document.createElement("div");

  row.className = "msg-row" + (isMe ? " me" : "");

  // User Message Element
  const messageElement = document.createElement("div");
  messageElement.id = message.id;
  messageElement.className = 'bubble';
  messageElement.style.setProperty('--h', user.hue);
  messageElement.innerHTML = `
    <div class="meta"><span class="name">${user.name}</span><span class="time">${message.time}</span></div>
    <div class="text">${escapeHtml(message.text).replace(/\n/g, '<br>')}</div>
  `;

  if (isMe) {
    row.appendChild(messageElement);
  }
  else {
    // User Avatar Element
    const userAvatarElement = document.createElement('div');
    userAvatarElement.className = 'avatar';
    userAvatarElement.style.background = `linear-gradient(180deg, hsla(${user.hue}, 80%, 65%, .35), hsla(${user.hue}, 70%, 50%, .25))`;
    userAvatarElement.style.borderColor = 'rgba(255,255,255,.2)';
    userAvatarElement.textContent = user.name.slice(0,2).toUpperCase();

    row.appendChild(userAvatarElement);
    row.appendChild(messageElement);
  }

  const messagesElement = $("#messages");

  messagesElement.append(row);
  messagesElement.animate({
    scrollTop: messagesElement.prop("scrollHeight"),
  }, 500);
}

function copyChatSessionUrlToClipboard(event, element, tooltip) {
  try {
    const chatSessionUrl = $("#chatSessionUrl").val();
    navigator.clipboard.writeText(chatSessionUrl);
    showTooltipOnClick(element, tooltip, "Copied!");
    messageInput.focus();
  }
  catch (ignore) {
  }
}

function playAudio(audioBlob) {
  const audio = new Audio();
  audio.src = URL.createObjectURL(audioBlob);
  audio.play();
}

function sendEmail(event) {

  const recipient = "";
  const subject = "Join My Chat";
  const encodedSubject = encodeURIComponent(subject);
  const emailBody = $("#chatSessionUrl").val();
  const encodedEmailBody = encodeURIComponent(emailBody);
  const mailToLink = `mailto:${recipient}?subject=${encodedSubject}&body=${encodedEmailBody}`;

  window.open(mailToLink);
  messageInput.focus();
}

function hideTooltip(tooltip) {
  tooltip.removeClass("show");
}

function showTooltip(element, tooltip) {

  const elementOffset = element.offset();
  const elementWidth = element.outerWidth();
  const elementTop = elementOffset.top;
  const elementLeft = elementOffset.left;
  const tooltipWidth = tooltip.outerWidth();
  const tooltipHeight = tooltip.outerHeight();
  const tooltipTop = elementTop - tooltipHeight - 32; // pixels
  const tooltipLeft = (elementLeft + (elementWidth / 2)) - (tooltipWidth / 2); // pixels

  tooltip.css({
    top: `${tooltipTop}px`,
    left: `${tooltipLeft}px`
  });

  tooltip.addClass('show');
  setTimeout(() => { hideTooltip(tooltip); }, 3000);
}

function showTooltipOnClick(element, tooltip, text) {
  hideTooltip(tooltip);
  const originalText = tooltip.html();
  tooltip.html(text);
  showTooltip(element, tooltip);
  setTimeout(() => { tooltip.html(originalText); }, 3000);
}

function showMicTooltipOnClick(element, tooltip) {
  hideTooltip(tooltip);
  const text = recordingAudio ? "Recording" : "Record Audio Message";
  tooltip.html(text);
  showTooltip(element, tooltip);
}

function toggleMic() {
  $("#micIconOff").toggle();
  $("#micIconOn").toggle();
}

const copyUrlButton = $("#copyUrlButton");
const copyUrlTooltip = $('<div id="copyUrlTooltip" class="tooltip">Copy Chat URL to Clipboard</div>').appendTo("body");
const copyUrlTooltipText = copyUrlTooltip.html();

copyUrlButton.click(event => copyChatSessionUrlToClipboard(event, copyUrlButton, copyUrlTooltip));
copyUrlButton.mouseenter(event => showTooltip(copyUrlButton, copyUrlTooltip));
copyUrlButton.mouseleave(event => {
  hideTooltip(copyUrlTooltip);
  copyUrlTooltip.html(copyUrlTooltipText);
});

const emailButton = $("#emailButton")
const emailTooltip = $('<div id="emailTooltip" class="tooltip">Send Chat URL in Email</div>').appendTo("body");

emailButton.click(event => sendEmail(event));
emailButton.mouseenter(event => showTooltip(emailButton, emailTooltip));
emailButton.mouseleave(event => hideTooltip(emailTooltip));

messageInput.keypress(event => {
  if (event.key === 'Enter' && !event.shiftKey) {
    event.preventDefault();
    postTextMessage(event);
  }
});

const micButton = $("#micButton");
const micTooltip = $('<div id="micTooltip" class="tooltip">Record Audio Message</div>').appendTo("body");
micButton.click(event => recordMessage(event));
micButton.mouseenter(event => showTooltip(micButton, micTooltip));

const sendButton = $("#sendButton");
sendButton.click(event => postTextMessage(event));

$(document).click(function(event) {
  logInfo(`X: ${event.clientX}, Y: ${event.clientY}`);
});

$(window).focus(function() {
  messageInput.focus();
});

initApp();
