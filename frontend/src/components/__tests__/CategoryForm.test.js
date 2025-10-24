import { mount } from '@vue/test-utils'
import { describe, it, expect, vi } from 'vitest'
import CategoryForm from '../CategoryForm.vue'

describe('CategoryForm', () => {
  it('shows error when name is empty', async () => {
    const wrapper = mount(CategoryForm, { props: { onCreated: () => {}, userId: 1 } })
    await wrapper.find('form').trigger('submit.prevent')
    expect(wrapper.text()).toContain('Name erforderlich')
  })

  it('calls onCreated after successful submit', async () => {
    global.fetch = vi.fn().mockResolvedValue({ ok: true, json: async () => ({}) })
    const onCreated = vi.fn()
    const wrapper = mount(CategoryForm, { props: { onCreated, userId: 1 } })

    await wrapper.find('input[placeholder="Name"]').setValue('Test')
    await wrapper.find('form').trigger('submit.prevent')

    expect(onCreated).toHaveBeenCalled()
  })
})
