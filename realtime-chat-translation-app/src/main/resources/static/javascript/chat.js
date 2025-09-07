// OpenAI ChatGPT (5.0) Generated JavaScript
//const $ = (selection, node = document) => node.querySelector(selection);
//const $$ = (selection, node = document) => [...node.querySelectorAll(selection)];

// Chat App Data Structures
const applicationState = {
  users: {},
  me: 'You',
  messages: []
};

// Chat UI HTML Elements
const email = $("#emailContainer")
const messages = $("#messages");
const messageInput = $("#messageInput");
const sendButton = $("#sendButton")

// Chat App Functions
const addUser = (name, hue = randomHue()) => {
  if (!name || applicationState.users[name]) return;
  applicationState.users[name] = { name, hue };
  showUsers();
};

const randomHue = () => Math.floor(Math.random() * 360);

const seedUsers = () => {
  addUser('Admin', 330);
  addUser('You', 210);
};

function timeNow() {
  const date = new Date();
  return date.toLocaleTimeString([], {
    hour: '2-digit',
    minute: '2-digit'
  });
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

function saveMessage(user, text) {
  const message = { user, text, time: timeNow() };
  applicationState.messages.push(message);
  const row = showMessage(message);
  messages.append(row);
  messages.animate({
    scrollTop: messages.prop("scrollHeight"),
  }, 500);
}

function sendMessage(event) {

  const me = applicationState.me;
  const myMessage = messageInput.val().trim();

  if (!myMessage) return;

  saveMessage(me, myMessage);
  messageInput.val("");
}

function showMessage({ user, text, time }) {

  const row = document.createElement('div');
  const isMe = user === applicationState.me;

  row.className = 'msg-row' + (isMe ? ' me' : '');

  // User Avatar Element
  const userAvatar = document.createElement('div');
  userAvatar.className = 'avatar';
  userAvatar.style.background = `linear-gradient(180deg, hsla(${applicationState.users[user].hue}, 80%, 65%, .35), hsla(${applicationState.users[user].hue}, 70%, 50%, .25))`;
  userAvatar.style.borderColor = 'rgba(255,255,255,.2)';
  userAvatar.textContent = user.slice(0,2).toUpperCase();

  // User Text Message Element
  const userTextMessage = document.createElement('div');
  userTextMessage.className = 'bubble';
  userTextMessage.style.setProperty('--h', applicationState.users[user].hue);
  userTextMessage.innerHTML = `
    <div class="meta"><span class="name">${user}</span><span class="time">${time}</span></div>
    <div class="text">${escapeHtml(text).replace(/\n/g, '<br>')}</div>
  `;

  if (isMe) {
    row.appendChild(userTextMessage);
    row.appendChild(userAvatar);
  }
  else {
    row.appendChild(userAvatar);
    row.appendChild(userTextMessage);
  }

  return row;
}

function escapeHtml(str) {
  return str.replace(/[&<>"]+/g, s => ({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;'}[s]));
}

function sendEmail(event) {

  const recipient = "";
  const subject = "Join My Chat";
  const encodedSubject = encodeURIComponent(subject);
  const emailBody = $("#chatSessionUrl").val();
  console.log("EMAIL BODY ["+emailBody+"]");
  const encodedEmailBody = encodeURIComponent(emailBody);
  const mailToLink = `mailto:${recipient}?subject=${encodedSubject}&body=${encodedEmailBody}`;

  window.open(mailToLink);
  messageInput.focus();
}

email.click(event => sendEmail(event));

// Chat App Event Handling
messageInput.keypress(event => {
  if (event.key === 'Enter' && !event.shiftKey) {
    event.preventDefault();
    sendMessage();
  }
});

sendButton.click(event => {
  sendMessage(event);
  messageInput.focus();
});

// Initialize Chat App
seedUsers();
showUsers();
messageInput.focus();
saveMessage('Admin', 'Welcome to Glass Chat! 💬');
