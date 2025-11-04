<script setup>
import { ref, onMounted } from 'vue'
import { apiUrl, withUserId } from './lib/api'

import CategoryList from './components/CategoryList.vue'
import CategoryForm from './components/CategoryForm.vue'
import TransactionForm from './components/TransactionForm.vue'
import TransactionList from './components/TransactionList.vue'
import UserAuthForm from './components/UserAuthForm.vue'



const categories = ref([])
const loading = ref(false)
const error = ref('')
const selectedCategory = ref('')
const userAuthenticated = ref(false)
const showAuth = ref(true)
const userName = ref('')
const userId = ref(null)
const isDummyUser = ref(false)
const txRefreshKey = ref(0)

function onTransactionCreated() {
  // Reload categories (names might be used in list) and force tx list refresh
  load()
  txRefreshKey.value++
}


function reloadCategories() {
  load()
}

async function handleCategoryDelete(id) {
  try {
    const res = await fetch(apiUrl(`/categories/${id}`), { method: 'DELETE' })
    if (!res.ok) throw new Error('HTTP ' + res.status)
    await load()
  } catch (e) {
    alert('Kategorie konnte nicht gelöscht werden: ' + e.message)
  }
}

function handleAuthSuccess(user) {
  userAuthenticated.value = true
  showAuth.value = false
  userName.value = user.name || 'Benutzer'
  userId.value = user.id || null
  isDummyUser.value = user.email === 'guest@finance.local'
}

async function continueWithoutUser() {
  userAuthenticated.value = false
  showAuth.value = false
  // Fetch or create dummy user
  try {
  const res = await fetch(apiUrl('/users/login'), {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ email: 'guest@finance.local', passwordHash: '' })
    })
    const user = await res.json()
    userName.value = user.name || 'Gast'
    userId.value = user.id || null
    isDummyUser.value = true
    // Reload categories and transactions after setting dummy user
    await load()
  } catch (e) {
    userName.value = 'Gast'
    userId.value = null
    isDummyUser.value = true
  }
}

function handleCategorySelect(name) {
  selectedCategory.value = name
}

async function load() {
  loading.value = true
  error.value = ''
  try {
    let url = withUserId(apiUrl('/categories'), userId.value)
    const res = await fetch(url)
    if (!res.ok) throw new Error('HTTP ' + res.status)
    const data = await res.json()
    // Map to plain objects
    categories.value = Array.isArray(data)
      ? data.map(c => ({ id: c.id, name: c.name, description: c.description }))
      : []
  } catch (e) {
    error.value = 'Kategorien konnten nicht geladen werden: ' + e.message
  } finally {
    loading.value = false
  }
}

onMounted(load)

async function resetDummy() {
  try {
  const res = await fetch(apiUrl('/reset-dummy'), { method: 'POST' })
    if (!res.ok) throw new Error('HTTP ' + res.status)
    await load()
    alert('App wurde für den Gast zurückgesetzt!')
  } catch (e) {
    alert('Fehler beim Zurücksetzen: ' + e.message)
  }
}
</script>

<template>
  <main class="container">
    <header class="row">
      <h1>FinanceMaster</h1>
      <button @click="load" :disabled="loading">{{ loading ? 'Laden…' : 'Neu laden' }}</button>
    </header>

    <section v-if="showAuth">
      <UserAuthForm @auth-success="handleAuthSuccess" />
      <div style="text-align:center; margin-top:1.5rem;">
        <button @click="continueWithoutUser" class="button-link">Ohne Konto fortfahren</button>
      </div>
    </section>

    <section v-else>
      <div v-if="userName" class="welcome-message">
        <h2>Willkommen, {{ userName }}! 👋</h2>
        <button v-if="isDummyUser" @click="resetDummy" class="reset-btn">App zurücksetzen</button>
      </div>

  <CategoryForm :onCreated="reloadCategories" :userId="userId" />
  <TransactionForm :categories="categories" :onCreated="onTransactionCreated" :userId="userId" />

      <p v-if="error" class="error">{{ error }}</p>
      <p v-else-if="loading">Lade Kategorien…</p>
      <CategoryList
        v-else
        :items="categories"
        :selectedCategory="selectedCategory"
        @select="handleCategorySelect"
         @delete="handleCategoryDelete"
      />

  <TransactionList :categories="categories" :selectedCategory="selectedCategory" :userId="userId" :refreshKey="txRefreshKey" />
    </section>
  </main>
</template>

<style scoped>
.container { 
  max-width: 1000px; 
  margin: 0 auto; 
  padding: 2rem 1rem; 
}
.row { 
  display: flex; 
  align-items: center; 
  justify-content: space-between; 
  gap: 1rem; 
  margin-bottom: 2rem;
  padding-bottom: 1rem;
  border-bottom: 2px solid var(--border);
}
.error { 
  color: var(--error); 
  background: var(--error-bg); 
  padding: .75rem 1rem; 
  border-radius: 8px; 
  border-left: 4px solid var(--error);
}
.welcome-message {
  background: var(--primary-bg);
  border: 1px solid var(--primary);
  border-radius: 12px;
  padding: 1.5rem;
  margin-bottom: 2rem;
  text-align: center;
}
.welcome-message h2 {
  color: var(--primary);
  margin: 0;
  font-size: 1.5rem;
}
.reset-btn {
  margin-top: 1rem;
  background: var(--error);
  color: #fff;
  border: none;
  padding: .5rem 1.5rem;
  border-radius: 8px;
  cursor: pointer;
  font-weight: 600;
}
</style>
