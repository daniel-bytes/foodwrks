/**
 * Manages wiring up editor forms and buttons in the application
 * via data attributes, delegating to the Forms class for issuing
 * actions to the backend server.
 */
class Editors {
  constructor(document, geolocation, confirm, forms, search) {
    this.document = document
    this.geolocation = geolocation
    this.confirm = confirm
    this.forms = forms
    this.search = search
  }

  bindAll() {
    this.bindDeleteCommentButtons()
    this.bindSavePlaceButtons()
    this.bindSearch()
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

  bindSearch() {
    const $document = this.document
    const $search = this.search

    function bind(selector, action, disableFor) {
      const $searchButton = $document.querySelector(selector)

      if ($searchButton) {
        $searchButton.addEventListener('click', event => {
          $search[action](
            () => $searchButton.classList.add('is-loading'),
            () => $searchButton.classList.remove('is-loading')
          )
        })

        if (disableFor) {
          const $editor = $document.querySelector(disableFor)

          if ($editor) {
            $editor.addEventListener('keyup', () => {
              $searchButton.disabled = !$editor.value.trim()
            })
          }
        }
      }
    }

    bind('button#search-nearby', 'searchNearby')
    bind('button#search-text', 'searchText', 'input#query')
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