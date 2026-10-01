// HVTC BAKERY - AI Chatbot Assistant Widget
document.addEventListener('DOMContentLoaded', function () {
    const triggerBtn = document.getElementById('chatbotTrigger');
    const chatbotBox = document.getElementById('chatbotBox');
    const closeBtn = document.getElementById('chatbotClose');
    const sendBtn = document.getElementById('chatSendBtn');
    const chatInput = document.getElementById('chatInput');
    const messagesContainer = document.getElementById('chatMessages');

    if (!triggerBtn || !chatbotBox) return;

    // Toggle Chatbox
    triggerBtn.addEventListener('click', function () {
        chatbotBox.classList.toggle('is-open');
        if (chatbotBox.classList.contains('is-open')) {
            chatInput.focus();
        }
    });

    closeBtn.addEventListener('click', function () {
        chatbotBox.classList.remove('is-open');
    });

    // Send Message Handler
    function sendMessage() {
        const text = chatInput.value.trim();
        if (!text) return;

        // Append User Message
        appendMessage(text, 'user');
        chatInput.value = '';

        // Show Typing Indicator
        const typingId = appendTypingIndicator();

        // Call REST API Endpoint
        fetch('/api/chatbot/message', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ message: text })
        })
        .then(response => response.json())
        .then(data => {
            removeTypingIndicator(typingId);
            appendMessage(data.reply, 'bot', data.suggestedProducts);
        })
        .catch(err => {
            removeTypingIndicator(typingId);
            appendMessage('Xin lỗi bạn, trợ lý AI HVTC đang bận một chút. Bạn vui lòng thử lại sau nhé!', 'bot');
        });
    }

    sendBtn.addEventListener('click', sendMessage);
    chatInput.addEventListener('keypress', function (e) {
        if (e.key === 'Enter') {
            sendMessage();
        }
    });

    function appendMessage(text, sender, products = []) {
        const msgDiv = document.createElement('div');
        msgDiv.classList.add('chat-msg', sender);
        msgDiv.innerHTML = `<div>${text}</div>`;

        if (products && products.length > 0) {
            const suggestionsWrap = document.createElement('div');
            suggestionsWrap.style.marginTop = '8px';
            products.forEach(p => {
                const card = document.createElement('a');
                card.href = p.slug.startsWith('workshop/') ? `/${p.slug}` : (p.slug.startsWith('/') ? p.slug : `/product/${p.slug}`);
                card.classList.add('chat-suggestion-card');
                card.innerHTML = `
                    <img src="${p.image}" style="width: 40px; height: 40px; border-radius: 6px; object-fit: cover;" alt="${p.name}">
                    <div style="flex-grow:1; font-size: 0.8rem;">
                        <div style="font-weight: 600;">${p.name}</div>
                        <div style="color: var(--hvtc-primary); font-weight:700;">${p.priceFormatted}</div>
                    </div>
                `;
                suggestionsWrap.appendChild(card);
            });
            msgDiv.appendChild(suggestionsWrap);
        }

        messagesContainer.appendChild(msgDiv);
        messagesContainer.scrollTop = messagesContainer.scrollHeight;
    }

    function appendTypingIndicator() {
        const id = 'typing_' + Date.now();
        const msgDiv = document.createElement('div');
        msgDiv.id = id;
        msgDiv.classList.add('chat-msg', 'bot');
        msgDiv.innerHTML = '<span style="font-style: italic; color: #888;">HVTC AI đang suy nghĩ...</span>';
        messagesContainer.appendChild(msgDiv);
        messagesContainer.scrollTop = messagesContainer.scrollHeight;
        return id;
    }

    function removeTypingIndicator(id) {
        const elem = document.getElementById(id);
        if (elem) elem.remove();
    }
});
