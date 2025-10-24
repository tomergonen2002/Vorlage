<script setup>
import { ref, onMounted, watch, computed } from 'vue'
import { apiUrl, withUserId } from '../lib/api'
import { computeSumsByCategory, isIncome, isExpense } from '../lib/sumUtils'

const props = defineProps({
  categories: { type: Array, default: () => [] },
  selectedCategory: { type: String, default: '' },
  userId: { type: [Number, String], default: null },
  // Used to trigger a manual refresh from parent after creating a transaction
  refreshKey: { type: Number, default: 0 }
})

const transactions = ref([])
const loading = ref(false)
const error = ref('')

async function load() {
  loading.value = true
  error.value = ''
  try {
  const url = withUserId(apiUrl('/transactions'), props.userId)
  const res = await fetch(url)
    if (!res.ok) throw new Error('HTTP ' + res.status)
    const data = await res.json()
    transactions.value = Array.isArray(data)
      ? data.map(t => ({
          id: t.id,
          type: t.type,
          amount: t.amount,
          description: t.description,
          date: t.date,
          categoryId: t.categoryId ?? null,
          categoryName: t.categoryName ?? null
        }))
      : []
  } catch (e) {
    error.value = 'Transaktionen konnten nicht geladen werden: ' + e.message
  } finally {
    loading.value = false
  }
}

onMounted(load)

// Watch userId changes and reload
watch(() => props.userId, (newVal, oldVal) => {
  if (newVal !== oldVal) {
    load()
  }
})

// Watch refreshKey changes to reload list after a new transaction is created
watch(() => props.refreshKey, (newVal, oldVal) => {
  if (newVal !== oldVal) {
    load()
  }
})

// Summen pro Kategorie (getrennt nach Einnahmen/Ausgaben + Netto)
function sumByCategory() {
  return computeSumsByCategory(transactions.value)
}

// Summen pro Datum (getrennt nach Einnahmen/Ausgaben + Netto)
function sumByDate() {
  const sums = {}
  for (const t of transactions.value) {
    const date = t.date || 'Unbekannt'
    if (!sums[date]) sums[date] = { income: 0, expense: 0, net: 0 }
    const amt = t.amount || 0
    if (isIncome(t.type)) sums[date].income += amt
    else if (isExpense(t.type)) sums[date].expense += amt
    sums[date].net = sums[date].income - sums[date].expense
  }
  return sums
}

// Summen für ausgewählte Kategorie
function sumForSelectedCategory() {
  if (!props.selectedCategory) return null
  let income = 0, expense = 0
  for (const t of transactions.value) {
  if (t.categoryName === props.selectedCategory) {
      if (isIncome(t.type)) income += t.amount || 0
      else if (isExpense(t.type)) expense += t.amount || 0
    }
  }
  return { income, expense }
}

// expose as computed for easier usage/testing
const sumsByCategory = computed(() => sumByCategory())

// expose for tests
defineExpose({ sumsByCategory, transactions })
</script>

<template>
  <section class="card">
    <h2>Transaktionen</h2>
    <p v-if="error" class="error">{{ error }}</p>
    <p v-else-if="loading">Lade Transaktionen…</p>
    <p v-else-if="transactions.length === 0">Keine Transaktionen vorhanden</p>
    <ul v-else class="tx-list">
      <li v-for="t in transactions" :key="t.id">
        <strong>{{ t.type }}</strong> {{ t.amount }} € –
        <span>{{ t.categoryName }}</span> am <span>{{ t.date }}</span>
        <span v-if="t.description">({{ t.description }})</span>
      </li>
    </ul>
    <div class="summary">
      <template v-if="selectedCategory">
        <h3>Gesamtsumme für Kategorie: {{ selectedCategory }}</h3>
        <div v-if="sumForSelectedCategory()">
          <p><strong>Einnahmen:</strong> {{ sumForSelectedCategory().income.toFixed(2) }} €</p>
          <p><strong>Ausgaben:</strong> {{ sumForSelectedCategory().expense.toFixed(2) }} €</p>
        </div>
      </template>
      <h3>Summe pro Kategorie</h3>
      <ul>
        <li v-for="(sum, cat) in sumByCategory()" :key="cat">
          {{ cat }}:
          Einnahmen {{ sum.income.toFixed(2) }} € · Ausgaben {{ sum.expense.toFixed(2) }} € · Netto {{ sum.net.toFixed(2) }} €
        </li>
      </ul>
      <h3>Summe pro Datum</h3>
      <ul>
        <li v-for="(sum, date) in sumByDate()" :key="date">
          {{ date }}:
          Einnahmen {{ sum.income.toFixed(2) }} € · Ausgaben {{ sum.expense.toFixed(2) }} € · Netto {{ sum.net.toFixed(2) }} €
        </li>
      </ul>
    </div>
  </section>
</template>

<style scoped>
h2, h3 {
  color: var(--text);
}
.tx-list { 
  list-style: none; 
  padding: 0; 
  margin: 0;
}
.tx-list li { 
  padding: .75rem;
  margin-bottom: .5rem;
  border-left: 3px solid var(--primary);
  background: var(--bg);
  border-radius: 6px;
}
.tx-list li strong {
  color: var(--primary);
  font-weight: 600;
}
.summary { 
  margin-top: 2rem; 
  padding-top: 1.5rem;
  border-top: 2px solid var(--border);
}
.summary ul {
  list-style: none;
  padding: 0;
}
.summary li {
  padding: .5rem;
  margin-bottom: .25rem;
  background: var(--bg);
  border-radius: 6px;
}
 
</style>
