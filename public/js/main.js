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
  const editors = new Editors(
    document,
    navigator.geolocation,
    msg => confirm(msg),
    forms
  )

  bulma.bindAll()
  editors.bindAll()
})
