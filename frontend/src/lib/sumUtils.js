export function isIncome(type) {
  if (!type) return false
  const v = String(type).toLowerCase()
  return v === 'income' || v === 'einnahme'
}

export function isExpense(type) {
  if (!type) return false
  const v = String(type).toLowerCase()
  return v === 'expense' || v === 'ausgabe'
}

export function computeSumsByCategory(transactions = []) {
  const sums = {}
  for (const t of transactions) {
    const cat = (t.categoryName || (t.category && t.category.name) || 'Unbekannt')
    if (!sums[cat]) sums[cat] = { income: 0, expense: 0, net: 0 }
    const amt = t.amount || 0
    if (isIncome(t.type)) sums[cat].income += amt
    else if (isExpense(t.type)) sums[cat].expense += amt
    sums[cat].net = sums[cat].income - sums[cat].expense
  }
  return sums
}
