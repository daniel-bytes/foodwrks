/**
 * Manages wiring up editor forms and buttons in the application
 * via data attributes, delegating to the Forms class for issuing
 * actions to the backend server.
 */
class Editors {
  constructor(document, geolocation, confirm, forms) {
    this.document = document
    this.geolocation = geolocation
    this.confirm = confirm
    this.forms = forms
  }

  bindAll() {
    this.bindDeleteCommentButtons()
    this.bindSavePlaceButtons()
    this.bindLocationSearch()
    this.bindEditors()
  }

  bindDeleteCommentButtons() {
    this.bindActionButtons('button[data-action="delete-comment"]', dataset =>
      this.forms.deleteComment(
        dataset.placeid,
        dataset.commentid
      )
    )
  }

  bindSavePlaceButtons() {
    this.bindActionButtons('button[data-action="save-place"]', dataset =>
      this.forms.savePlace(
        dataset.placeid,
        dataset.externalid,
        dataset.visitstatus
      )
    )
  }

  bindLocationSearch() {
    const $searchButton = this.document.querySelector('button#search-nearby')
    if ($searchButton) {
      if (this.geolocation) {
        $searchButton.addEventListener('click', event => {
          $searchButton.classList.add('is-loading')

          this.geolocation.getCurrentPosition(
            position => {
              $searchButton.classList.remove('is-loading')
              const location = `${position.coords.latitude},${position.coords.longitude}`
              const searchRadius = this.document.getElementById('search-radius').value
              this.document.location.href = `/places/nearby?location=${location}&radius=${searchRadius}`
            }, error => {
              $searchButton.classList.remove('is-loading')
              console.error(error)

              if (error.code === 1) alert('Failed to get browser location: permission denied')
              else if (error.code === 1) alert('Failed to get browser location: position unavailable, try again')
              else if (error.code === 1) alert('Failed to get browser location: timed out, try again')
              else alert(`Failed to get browser location (error code ${error.code})`)
            }
          )
        })
      } else {
        $searchButton.disabled = true
        $searchButton.textContent = "Location search disabled"
      }
    }
  }

  bindEditors() {
    for(const $editor of this.document.querySelectorAll(`[data-action^="edit-"]`)) {
      const thing = $editor.dataset.action.split(/-(.+)/)[1]

      for(const $saveButton of this.document.querySelectorAll(`[data-action="save-${thing}"]`)) {
        $editor.addEventListener('keyup', () => {
          $saveButton.disabled = !$editor.value.trim()
        })
      }
    }
  }

  bindActionButtons(selector, asyncAction) {
    // findActionButton is needed due to some clicks happening on the surrounding icon image
    function findActionButton(element) {
      if (!element) return null
      if (element.dataset.action) return element
      if (element.parentElement) return findActionButton(element.parentElement)
      return null
    }

    for(const $button of this.document.querySelectorAll(selector)) {
        $button.addEventListener('click', async event => {
          const $element = findActionButton(event.srcElement)

          if ($element) {
            if (this.maybeConfirm($element.dataset.confirm)) {
              await asyncAction($element.dataset)
            }
            return false
          }
        })
      }
  }

  maybeConfirm(confirmation) {
    return !confirmation || this.confirm(confirmation)
  }
}