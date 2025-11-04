<script setup>
const props = defineProps({
  id: { type: [Number, String], required: false },
  name: { type: String, required: true },
  description: { type: String, default: '' },
  selected: { type: Boolean, default: false },
})
const emit = defineEmits(['select','delete'])

function handleClick() {
  emit('select', props.name)
}

function handleDelete(e) {
  e.stopPropagation()
  if (props.id == null) return
  emit('delete', props.id)
}
</script>

<template>
  <li class="item" :class="{ selected }" @click="handleClick">
    <strong>{{ name }}</strong>
    <div v-if="description" class="desc">{{ description }}</div>
    <button class="del" @click="handleDelete" title="Löschen">🗑️</button>
  </li>
  
</template>

<style scoped>
.item {
  padding: 1rem;
  border: 1px solid var(--border);
  border-radius: 10px;
  background: var(--surface);
  box-shadow: 0 1px 2px rgba(0,0,0,0.05);
  transition: all 0.2s;
  cursor: pointer;
}
.item.selected {
  border-color: var(--primary);
  background: var(--primary-bg);
}
.item:hover {
  box-shadow: 0 4px 6px rgba(0,0,0,0.1);
  border-color: var(--primary);
}
strong {
  color: var(--text);
  font-size: 1.1em;
}
.desc { 
  color: var(--text-light); 
  font-size: .9rem; 
  margin-top: .25rem;
}
</style>
