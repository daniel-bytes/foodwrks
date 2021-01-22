/**
 * Bulma interactivity scripts
 */

// see https://bulma.io/documentation/elements/notification/#javascript-example
function bulmaBindCloseNotifications() {
  (document.querySelectorAll('.notification .delete') || []).forEach(($delete) => {
    var $notification = $delete.parentNode

    $delete.addEventListener('click', () => {
      $notification.parentNode.removeChild($notification);
    })
  })
}

// see https://bulma.io/documentation/components/navbar/#navbar-menu
function bulmaBindNavBarLinks() {
  const $navbarBurgers = Array.prototype.slice.call(document.querySelectorAll('.navbar-burger'), 0)

  if ($navbarBurgers.length > 0) {
    $navbarBurgers.forEach( el => {
      el.addEventListener('click', () => {
        const $target = document.getElementById(el.dataset.target)
        el.classList.toggle('is-active')
        $target.classList.toggle('is-active')
      })
    })
  }
}

document.addEventListener('DOMContentLoaded', () => {
  bulmaBindCloseNotifications()
  bulmaBindNavBarLinks()
});
