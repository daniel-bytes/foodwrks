/**
 * Bulma interactivity
 */
class Bulma {
  constructor(document) {
    this.document = document
  }

  bindAll() {
    this.bulmaBindCloseNotifications()
    this.bulmaBindNavBarLinks()
  }

  // see https://bulma.io/documentation/elements/notification/#javascript-example
  bulmaBindCloseNotifications() {
    (this.document.querySelectorAll('.notification .delete') || []).forEach(($delete) => {
      var $notification = $delete.parentNode

      $delete.addEventListener('click', () => {
        $notification.parentNode.removeChild($notification);
      })
    })
  }

  // see https://bulma.io/documentation/components/navbar/#navbar-menu
  bulmaBindNavBarLinks() {
    const $navbarBurgers = Array.prototype.slice.call(this.document.querySelectorAll('.navbar-burger'), 0)

    if ($navbarBurgers.length > 0) {
      $navbarBurgers.forEach( el => {
        el.addEventListener('click', () => {
          const $target = this.document.getElementById(el.dataset.target)
          el.classList.toggle('is-active')
          $target.classList.toggle('is-active')
        })
      })
    }
  }
}

