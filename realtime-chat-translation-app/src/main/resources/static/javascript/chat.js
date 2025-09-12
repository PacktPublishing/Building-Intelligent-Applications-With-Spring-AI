// OpenAI ChatGPT (5.0) Generated JavaScript
//const $ = (selection, node = document) => node.querySelector(selection);
//const $$ = (selection, node = document) => [...node.querySelectorAll(selection)];

const messageInputElement = $("#messageInput");

const applicationContext = {
  users: new Map(),
  messages: []
};

const initApp = () => {
  requestMessages();
  requestUsers();
  messageInputElement.focus();
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

function log(obj) {
  console.log(toJson(obj));
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
    hue: generateHue,
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
      //log(users);
      users.forEach(chatUser => addUser(newUser(chatUser)));
    },
    error: function(xhr, status, errorMessage) {
      console.log(`Failed to get chat users from chat session [${sessionId}]: ${status} - ${errorMessage}`);
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
    const userChip = document.createElement("div");
    userChip.className = "userchip";

    const userStatusElement = document.createElement("div");
    userStatusElement.setAttribute("id", userId);
    userStatusElement.className = "green-status-dot"
    userChip.append(userStatusElement);

    const userNameElement = document.createElement("div");
    userNameElement.className = "chip";
    userNameElement.style.setProperty("--h", user.hue);
    userNameElement.textContent = user.name;
    userChip.append(userNameElement);

    userBar.append(userChip);
  });
}

function showUserStatus(user) {

  const userStatus = user.status;
  const userStatusElement = $(`#${user.id}`);

  //console.log(`USER [${user.name}] has Status [${userStatus}] and Class [${userStatusElement.attr("class")}]`);

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
  applicationContext.messages.push(message);
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
    success: function(messages) {
      //console.log(JSON.stringify(messages));

      messages.forEach(chatMessage => {

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
    },
    complete: function() {
      setTimeout(requestMessages, 1000);
    }
  });
}

function copyChatSessionUrlToClipboard(event, element, tooltip) {
  try {
    const chatSessionUrl = $("#chatSessionUrl").val();
    navigator.clipboard.writeText(chatSessionUrl);
    showTooltipOnClick(element, tooltip, "Copied!");
    messageInputElement.focus();
  }
  catch (ignore) {
  }
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

function hideTooltip(tooltip) {
  tooltip.removeClass("show");
}

function showTooltip(element, tooltip) {

  const elementWidth = element.outerWidth();
  const elementOffset = element.offset();
  const tooltipHeight = tooltip.outerHeight();
  const tooltipWidth = tooltip.outerWidth();

  tooltip.css({
    top: elementOffset.top - tooltipHeight - 32, // 30px gap
    left: elementOffset.left + elementWidth / 2 - tooltipWidth / 2
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

messageInputElement.keypress(event => {
  if (event.key === 'Enter' && !event.shiftKey) {
    event.preventDefault();
    postUserMessage(event);
  }
});

const sendButton = $("#sendButton")
sendButton.click(event => postUserMessage(event));

initApp();
