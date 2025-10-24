import { describe, it, expect } from 'vitest'
import { computeSumsByCategory } from '../../lib/sumUtils'

describe('computeSumsByCategory', () => {
  it('computes income and expense per category and net', () => {
    const tx = [
      { type: 'INCOME', amount: 100, categoryName: 'Gehalt' },
      { type: 'EXPENSE', amount: 40, categoryName: 'Lebensmittel' },
      { type: 'Ausgabe', amount: 10, categoryName: 'Lebensmittel' }
    ]
    const sums = computeSumsByCategory(tx)
    expect(sums['Gehalt'].income).toBe(100)
    expect(sums['Gehalt'].expense).toBe(0)
    expect(sums['Gehalt'].net).toBe(100)
    expect(sums['Lebensmittel'].income).toBe(0)
    expect(sums['Lebensmittel'].expense).toBe(50)
    expect(sums['Lebensmittel'].net).toBe(-50)
  })
})
