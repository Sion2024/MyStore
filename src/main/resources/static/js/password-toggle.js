document.addEventListener('DOMContentLoaded', () => {
    document.querySelectorAll('.toggle-password').forEach((button) => {
        button.addEventListener('click', () => {
            const input = button.closest('.password-field')?.querySelector('input');
            if (!input) {
                return;
            }

            const show = input.type === 'password';
            input.type = show ? 'text' : 'password';
            button.textContent = show ? '🙈' : '👁';
            button.setAttribute('aria-label', show ? 'Скриване на парола' : 'Показване на парола');
            button.setAttribute('aria-pressed', String(show));
        });
    });
});
