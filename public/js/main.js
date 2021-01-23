document.addEventListener('DOMContentLoaded', () => {
  const api = new Api(
    url => document.location.href = url,
    error => alert(error)
  )
  const bulma = new Bulma(document)
  const editors = new Editors(
    document,
    navigator.geolocation,
    msg => confirm(msg),
    api
  )

  bulma.bindAll()
  editors.bindAll()
})
