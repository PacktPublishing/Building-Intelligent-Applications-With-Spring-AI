// OpenAI ChatGPT (5.0) Generated JavaScript
//const $ = (selection, node = document) => node.querySelector(selection);
//const $$ = (selection, node = document) => [...node.querySelectorAll(selection)];

const applicationState = {
  users: {},
  messages: []
};

const messageInputElement = $("#messageInput");

const addUser = (userId, username, hue = randomHue()) => {

  if (!userId || applicationState.users[userId]) return;

  const user = {
    id: userId,
    name: username,
    hue: hue
  };

  applicationState.users[userId] = user;
  showUsers();
};

const initApp = () => {
  initUsers();
  setInterval(requestMessages, 1000);
  messageInputElement.focus();
}

const initUsers = () => {
  const userId = $("#chatUserId").val();
  const username = $("#chatUserName").val();
  addUser(userId, username, 210);
};

const randomHue = () => Math.floor(Math.random() * 360);

function timeNow() {
  const date = new Date();
  return date.toLocaleTimeString([], {
    hour: '2-digit',
    minute: '2-digit'
  });
}

function resolveUser(message) {

  var applicationUser = applicationState.users[message.user.id];

  if (!applicationUser) {
    applicationUser = {
      id: message.user.id,
      name: message.user.name,
      hue: randomHue
    }
  }

  return applicationUser;
}

function showUsers() {
  const chipbar = $("#chipbar");
  chipbar.innerHTML = '';
  Object.values(applicationState.users).forEach(user => {
    const chip = document.createElement('div');
    chip.className = 'chip';
    chip.style.setProperty('--h', user.hue);
    chip.textContent = user.name;
    chipbar.append(chip);
  });
}

function postMessage(message) {

  if (!message.text) {
    console.log(`Message [${message.id}] from user [${message.user.name}] has no text`);
    return;
  }

  saveMessage(message);
  showMessage(message);
  messageInputElement.val("");
  messageInputElement.focus();
}

function postUserMessage(event) {

  const messageText = messageInputElement.val().trim();

  if (!messageText) return;

  const userId = $("#chatUserId").val();
  const username = $("#chatUserName").val();
  const sessionId = $("#chatSessionId").val();

  const message = {
    id: null,
    user: { id: userId, name: username },
    text: messageText,
    time: timeNow()
  }

  const messageId = sendMessage(sessionId, message);

  message.id = messageId;

  postMessage(message);
}

function saveMessage(message) {
  applicationState.messages.push(message);
  return message;
}

function sendMessage(sessionId, message) {

  const postChatMessageRequest = {
      userId: message.user.id,
      message: message.text
  }

  var messageId;

  $.ajax({
    method: 'POST',
    url: `/chat-app/api/${sessionId}/messages`,
    data: JSON.stringify(postChatMessageRequest),
    contentType: "application/json; charset=utf-8",
    dataType: "json",
    success: function(response) {
      // console.log(response);
      messageId = response.id;
    },
    error: function(xhr, status, errorMessage) {
      const messageJson = JSON.stringify(postChatMessageRequest);
      console.log(`Failed to post chat message [${messageJson}]: ${status} - ${errorMessage}`);
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

function requestMessages() {

  const userId = $("#chatUserId").val();
  const sessionId = $("#chatSessionId").val();

  $.ajax({
    method: 'GET',
    url: `/chat-app/api/${sessionId}/users/${userId}/messages`,
    contentType: "application/json; charset=utf-8",
    dataType: "json",
    success: function(response) {
      // console.log(response);

      Object.values(response).forEach(chatMessage => {

        const message = {
          id: chatMessage.id,
          user: { id: chatMessage.user.id, name: chatMessage.user.name },
          text: chatMessage.message,
          time: timeNow()
        };

        postMessage(message);
      });
    },
    error: function(xhr, status, errorMessage) {
      console.log(`Failed to request chat messages from chat session [${sessionId}]: ${status} - ${errorMessage}`);
    }
  });
}

function escapeHtml(str) {
  return str.replace(/[&<>"]+/g, s => ({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;'}[s]));
}

function sendEmail(event) {

  const recipient = "";
  const subject = "Join My Chat";
  const encodedSubject = encodeURIComponent(subject);
  const emailBody = $("#chatSessionUrl").val();
  const encodedEmailBody = encodeURIComponent(emailBody);
  const mailToLink = `mailto:${recipient}?subject=${encodedSubject}&body=${encodedEmailBody}`;

  window.open(mailToLink);
  messageInputElement.focus();
}

const emailButton = $("#emailButton")
emailButton.click(event => sendEmail(event));

messageInputElement.keypress(event => {
  if (event.key === 'Enter' && !event.shiftKey) {
    event.preventDefault();
    postUserMessage(event);
  }
});

const sendButton = $("#sendButton")
sendButton.click(event => postUserMessage(event));

initApp();
