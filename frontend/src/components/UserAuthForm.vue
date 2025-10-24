<script setup>
import { ref } from 'vue'
import { apiUrl } from '../lib/api'

const emit = defineEmits(['auth-success'])

const mode = ref('login') // 'login' or 'register'
const name = ref('')
const email = ref('')
const password = ref('')
const error = ref('')
const success = ref('')

async function submit() {
  error.value = ''
  success.value = ''
  const url = mode.value === 'register'
    ? apiUrl('/users/register')
    : apiUrl('/users/login')
  const body = mode.value === 'register'
    ? { name: name.value, email: email.value, passwordHash: password.value }
    : { email: email.value, passwordHash: password.value }
  try {
    const res = await fetch(url, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(body)
    })
    const data = await res.json()
    if (!res.ok) {
      if (res.status === 401 && mode.value === 'login') {
        error.value = 'User not found';
      } else {
        error.value = data || 'Fehler beim ' + (mode.value === 'register' ? 'Registrieren' : 'Anmelden');
      }
    } else {
      success.value = mode.value === 'register' ? 'Registrierung erfolgreich!' : 'Login erfolgreich!';
      // Hier könnte man z.B. ein Token speichern oder den User im State halten
      emit('auth-success', data);
    }
  } catch (e) {
    error.value = e.message
  }
}
</script>

<template>
  <section class="auth-card">
    <h2>{{ mode === 'register' ? 'Konto erstellen' : 'Anmelden' }}</h2>
    <form @submit.prevent="submit">
      <div v-if="mode === 'register'">
        <label>Name:<br>
          <input v-model="name" type="text" required />
        </label>
      </div>
      <label>Email:<br>
        <input v-model="email" type="email" required />
      </label>
      <label>Passwort:<br>
        <input v-model="password" type="password" required />
      </label>
      <button type="submit">{{ mode === 'register' ? 'Registrieren' : 'Login' }}</button>
    </form>
    <p v-if="error" class="error">{{ error }}</p>
    <p v-if="success" class="success">{{ success }}</p>
    <div class="switch-mode">
      <button @click="mode = mode === 'login' ? 'register' : 'login'">
        {{ mode === 'login' ? 'Konto erstellen' : 'Zur Anmeldung' }}
      </button>
    </div>
  </section>
</template>

<style scoped>
.auth-card {
  background: var(--surface);
  border: 1px solid var(--border);
  border-radius: 12px;
  padding: 2rem;
  max-width: 400px;
  margin: 2rem auto;
  box-shadow: 0 2px 8px rgba(0,0,0,0.07);
}
form {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}
input {
  padding: .5rem;
  border-radius: 6px;
  border: 1px solid var(--border);
  width: 100%;
}
button[type="submit"] {
  background: var(--primary);
  color: #fff;
  border: none;
  padding: .75rem 1.5rem;
  border-radius: 8px;
  cursor: pointer;
  font-weight: 600;
}
.switch-mode {
  margin-top: 1rem;
  text-align: center;
}
.switch-mode button {
  background: none;
  border: none;
  color: var(--primary);
  cursor: pointer;
  font-size: 1em;
  text-decoration: underline;
}
.error {
  color: var(--error);
  background: var(--error-bg);
  padding: .5rem 1rem;
  border-radius: 8px;
  margin-top: .5rem;
}
.success {
  color: var(--success);
  background: var(--success-bg);
  padding: .5rem 1rem;
  border-radius: 8px;
  margin-top: .5rem;
}
</style>
