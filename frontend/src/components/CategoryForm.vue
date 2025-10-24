<script setup>
import { ref } from 'vue'
import { apiUrl, withUserId } from '../lib/api'

const props = defineProps({
  onCreated: { type: Function, default: () => {} },
  userId: { type: [Number, String], default: null }
})

const name = ref('')
const description = ref('')
const error = ref('')
const loading = ref(false)

async function submit() {
  error.value = ''
  if (!name.value) {
    error.value = 'Name erforderlich'
    return
  }
  loading.value = true
  try {
  const url = withUserId(apiUrl('/categories'), props.userId)
  const res = await fetch(url, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ name: name.value, description: description.value })
    })
    if (!res.ok) throw new Error('HTTP ' + res.status)
    name.value = ''
    description.value = ''
    props.onCreated()
  } catch (e) {
    error.value = 'Fehler: ' + e.message
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <form class="card" @submit.prevent="submit">
    <h2>Kategorie anlegen</h2>
    <div>
      <input v-model="name" placeholder="Name" required />
    </div>
    <div>
      <input v-model="description" placeholder="Beschreibung" />
    </div>
    <button :disabled="loading">Speichern</button>
    <span v-if="error" class="error">{{ error }}</span>
  </form>
</template>

<style scoped>
h2 {
  color: var(--text);
  margin-bottom: 1rem;
}
input { 
  margin-bottom: .75rem; 
  width: 100%; 
  display: block;
}
</style>
