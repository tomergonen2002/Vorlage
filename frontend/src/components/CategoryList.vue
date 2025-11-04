<script setup>
import CategoryItem from './CategoryItem.vue'

const props = defineProps({
  items: { type: Array, default: () => [] },
  selectedCategory: { type: String, default: '' }
})
const emit = defineEmits(['select'])
</script>

<template>
  <ul class="grid">
    <CategoryItem
      v-for="(c, idx) in items"
      :key="(c.id ?? c.name) + '-' + idx"
      :id="c.id"
      :name="c.name"
      :description="c.description"
      :selected="selectedCategory === c.name"
      @select="emit('select', $event)"
      @delete="emit('delete', $event)"
    />
  </ul>
</template>

<style scoped>
.grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: 12px;
  padding: 0;
  list-style: none;
}
</style>
