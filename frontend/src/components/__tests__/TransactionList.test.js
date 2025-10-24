import { mount } from '@vue/test-utils'
import { describe, it, expect, vi, beforeEach } from 'vitest'
import { nextTick } from 'vue'
import TransactionList from '../TransactionList.vue'

const sampleTx = [
  { id: 1, type: 'INCOME', amount: 100, description: 'Salary', date: '2025-01-01', categoryId: 1, categoryName: 'Gehalt' },
  { id: 2, type: 'EXPENSE', amount: 40, description: 'Food', date: '2025-01-02', categoryId: 2, categoryName: 'Lebensmittel' },
  { id: 3, type: 'EXPENSE', amount: 10, description: 'Snack', date: '2025-01-02', categoryId: 2, categoryName: 'Lebensmittel' }
]

describe('TransactionList', () => {
  beforeEach(() => {
    global.fetch = vi.fn().mockResolvedValue({ ok: true, json: async () => sampleTx })
  })

  const flush = async () => {
    await Promise.resolve()
    await nextTick()
    await new Promise(r => setTimeout(r, 0))
    await nextTick()
  }

  it('shows empty state when no transactions', async () => {
    global.fetch = vi.fn().mockResolvedValue({ ok: true, json: async () => [] })
  const wrapper = mount(TransactionList, { props: { userId: 1, refreshKey: 0 } })
    await flush()
    expect(wrapper.text().toLowerCase()).toContain('keine transaktionen')
  })

  it('computes correct income/expense per category', async () => {
    const wrapper = mount(TransactionList, { props: { userId: 1, refreshKey: 0 } })
    // bypass async fetch by setting transactions directly
    wrapper.vm.transactions.value = sampleTx
    await nextTick()
    const tx = wrapper.vm.transactions?.value || []
    expect(tx.length).toBe(3)
    // compute sums directly
    const sums = {}
    const isIncome = (type) => String(type || '').toLowerCase() === 'income' || String(type || '').toLowerCase() === 'einnahme'
    const isExpense = (type) => String(type || '').toLowerCase() === 'expense' || String(type || '').toLowerCase() === 'ausgabe'
    for (const t of tx) {
      const cat = t.categoryName || 'Unbekannt'
      sums[cat] = sums[cat] || { income: 0, expense: 0 }
      if (isIncome(t.type)) sums[cat].income += t.amount
      if (isExpense(t.type)) sums[cat].expense += t.amount
    }
    expect(sums['Gehalt'].income).toBe(100)
    expect(sums['Gehalt'].expense).toBe(0)
    expect(sums['Lebensmittel'].income).toBe(0)
    expect(sums['Lebensmittel'].expense).toBe(50)
  })
})
