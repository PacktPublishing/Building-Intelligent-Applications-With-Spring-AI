// OpenAI ChatGPT (5.0) Generated JavaScript
const $ = (selection, node = document) => node.querySelector(selection);
const $$ = (selection, node = document) => [...node.querySelectorAll(selection)];

const addUser = (name, hue = randomHue()) => {
  if (!name || state.users[name]) return;
  state.users[name] = { name, hue };
  renderUsers();
};

// Chat UI HTML Elements
const messagesBox = $('#messages');
const messageInput = $('#messageInput');
const sendButton = $('#sendButton')

// Chat App Functions & Data Structures
const state = {
  users: {},
  me: 'You',
  messages: []
};

const randomHue = () => Math.floor(Math.random() * 360);

const seedUsers = () => {
  addUser('You', 210);
  addUser('Admin', 330);
};

function timeNow() {
  const date = new Date();
  return date.toLocaleTimeString([], {
    hour:'2-digit',
    minute:'2-digit'
  });
}

function renderUsers() {
  const chipbar = $('#chipbar');
  chipbar.innerHTML = '';
  Object.values(state.users).forEach(user => {
    const chip = document.createElement('div');
    chip.className = 'chip';
    chip.style.setProperty('--h', user.hue);
    chip.textContent = user.name;
    chipbar.appendChild(chip);
  });
}

function escapeHtml(str) {
  return str.replace(/[&<>"]+/g, s => ({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;'}[s]));
}

function messageRow({ user, text, time }){

  const row = document.createElement('div');
  const isMe = user === state.me;

  row.className = 'msg-row' + (isMe ? ' me' : '');

  // User Avatar
  const userAvatar = document.createElement('div');
  userAvatar.className = 'avatar';
  userAvatar.style.background = `linear-gradient(180deg, hsla(${state.users[user].hue},80%,65%,.35), hsla(${state.users[user].hue},70%,50%,.25))`;
  userAvatar.style.borderColor = 'rgba(255,255,255,.2)';
  userAvatar.textContent = user.slice(0,2).toUpperCase();

  // User Text Message Bubble
  const userTextMessageBubble = document.createElement('div');
  userTextMessageBubble.className = 'bubble';
  userTextMessageBubble.style.setProperty('--h', state.users[user].hue);
  userTextMessageBubble.innerHTML = `
    <div class="meta"><span class="name">${user}</span><span class="time">${time}</span></div>
    <div class="text">${escapeHtml(text).replace(/\n/g, '<br>')}</div>
  `;

  if (isMe) {
    row.appendChild(userTextMessageBubble);
    row.appendChild(userAvatar);
  }
  else {
    row.appendChild(userAvatar);
    row.appendChild(userTextMessageBubble);
  }

  return row;
}

function pushMessage(user, text) {
  const message = { user, text, time: timeNow() };
  state.messages.push(message);
  const row = messageRow(message);
  messagesBox.appendChild(row);
  messagesBox.scrollTo({
    top: messagesBox.scrollHeight,
    behavior: 'smooth'
  });
}

function send(event) {

  const me = state.me;
  const myMessage = messageInput.value.trim();

  if (!myMessage) return;

  pushMessage(me, myMessage);
  messageInput.value = '';
  mockMessage(me, myMessage);
}

function mockMessage(user = state.me, reply = "Duh!") {
  if (user === state.me) {
    // playful auto-reply from a random other user
    const others = Object.keys(state.users).filter(other => other !== state.me);
    if (others.length) {
      const from = others[Math.floor(Math.random()*others.length)];
      const cannedMessage = [ "Copy that!" ];
      const fromMessage = cannedMessage[Math.floor(Math.random() * cannedMessage.length)]
      mockTyping(from, fromMessage);
    }
  }
}

function mockTyping(from='Alex', reply="Got it!") {

  const hue = state.users[from]?.hue ?? 210;
  const row = document.createElement('div');

  row.className = 'msg-row';

  const userAvatar = document.createElement('div');

  userAvatar.className = 'avatar';
  userAvatar.style.background = `linear-gradient(180deg, hsla(${hue}, 80%, 65%, .35), hsla(${hue}, 70%, 50%, .25))`;
  userAvatar.textContent = from.slice(0,2).toUpperCase();

  const userTextMessageBubble = document.createElement('div');

  userTextMessageBubble.className = 'bubble';
  userTextMessageBubble.style.setProperty('--h', hue);
  userTextMessageBubble.innerHTML = `
    <div class="meta"><span class="name">${from}</span><span class="time">${timeNow()}</span></div>
    <div class="typing" aria-label="${from} is typing">
      <span class="dot"></span><span class="dot"></span><span class="dot"></span>
    </div>
  `;

  row.appendChild(userAvatar);
  row.appendChild(userTextMessageBubble);
  messagesBox.appendChild(row);
  messagesBox.scrollTo({ top: messagesBox.scrollHeight, behavior: 'smooth' });

  setTimeout(() => {
    row.remove();
    pushMessage(from, reply);
  }, 1200 + Math.random()*800);
}

// Chat App Event Handling
messageInput.addEventListener('keydown', event => {
  if (event.key === 'Enter' && !event.shiftKey) {
    event.preventDefault();
    send();
  }
});

sendButton.addEventListener('click', event => {
  send(event);
  messageInput.focus();
});

// Initialize Chat App
seedUsers();
renderUsers();
messageInput.focus();
pushMessage('Admin', 'Welcome to Glass Chat! 💬');
