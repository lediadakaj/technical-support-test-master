export async function fetchJson(url) {
  const response = await fetch(url)
  if (!response.ok) {
    throw new Error(`Request failed with status ${response.status}`)
  }
  return response.json()
}

export function formatMoney(value) {
  return `€${Number(value).toFixed(2)}`
}
