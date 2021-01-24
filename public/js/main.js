/**
 * Top level wiring of the various JS components.
 * For the sake of simplicity, all pages get the same DOMContentLoaded;
 * JS components are responsible for ensuring they handle a control not
 * being available on a given page.
 */
document.addEventListener('DOMContentLoaded', () => {
  const bulma = new Bulma(document)
  const forms = new Forms(
    url => document.location.href = url,
    error => alert(error)
  )
  const search = new Search(
    document,
    navigator.geolocation
  )
  const editors = new Editors(
    document,
    navigator.geolocation,
    msg => confirm(msg),
    forms,
    search
  )

  bulma.bindAll()
  editors.bindAll()
})
