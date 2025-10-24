<script setup>
import { ref, computed } from 'vue'
import { apiUrl, withUserId } from '../lib/api'

const props = defineProps({
  categories: { type: Array, default: () => [] },
  onCreated: { type: Function, default: () => {} },
  userId: { type: [Number, String], default: null }
})

const type = ref('Einnahme')
const amount = ref('')
const description = ref('')
const date = ref('')
const categoryId = ref('')
const error = ref('')
const loading = ref(false)

const sortedCategories = computed(() => [...props.categories].sort((a, b) => a.name.localeCompare(b.name)))

async function submit() {
  error.value = ''
  if (!amount.value || !date.value || !categoryId.value) {
    error.value = 'Alle Felder außer Beschreibung sind Pflicht.'
    return
  }
  loading.value = true
  try {
  const url = withUserId(apiUrl('/transactions'), props.userId)
  const res = await fetch(url, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        type: type.value,
        amount: parseFloat(amount.value),
        description: description.value,
        date: date.value,
        categoryId: parseInt(categoryId.value)
      })
    })
    if (!res.ok) throw new Error('HTTP ' + res.status)
    // Reset
    type.value = 'Einnahme'
    amount.value = ''
    description.value = ''
    date.value = ''
    categoryId.value = ''
    // Lade die Transaktionsliste neu, ignoriere die Response
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
    <h2>Transaktion anlegen</h2>
    <div>
      <select v-model="type">
        <option value="Einnahme">Einnahme</option>
        <option value="Ausgabe">Ausgabe</option>
      </select>
    </div>
    <div>
      <input v-model="amount" type="number" step="0.01" min="0" placeholder="Betrag (€)" required />
    </div>
    <div>
      <input v-model="date" type="date" required />
    </div>
    <div>
      <select v-model="categoryId" required>
        <option value="" disabled>Kategorie wählen</option>
        <option v-for="cat in sortedCategories" :key="cat.id" :value="cat.id">{{ cat.name }}</option>
      </select>
    </div>
    <div>
      <input v-model="description" placeholder="Beschreibung (optional)" />
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
input, select { 
  margin-bottom: .75rem; 
  width: 100%; 
  display: block;
}
</style>
