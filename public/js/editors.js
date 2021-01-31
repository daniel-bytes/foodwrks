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

    function bind(args) {
      const { selector, action, disableFor, pageButton } = args
      const $searchButton = $document.querySelector(selector)
      const $pageButton = pageButton ? $document.querySelector(pageButton) : null
      const pageCursor = $pageButton ? $pageButton.dataset.pagecursor : null

      if ($searchButton) {
        const handler = event => {
          $search[action](
            () => {
              $searchButton.classList.add('is-loading')
              if ($pageButton) $pageButton.classList.add('is-loading')
            },
            () => {
              $searchButton.classList.remove('is-loading')
              if ($pageButton) $pageButton.classList.remove('is-loading')
            },
            pageCursor
          )
        }

        $searchButton.addEventListener('click', handler)
        if ($pageButton) $pageButton.addEventListener('click', handler)

        if (disableFor) {
          const $editor = $document.querySelector(disableFor)

          if ($editor) {
            $editor.addEventListener('keyup', () => {
              $searchButton.disabled = !$editor.value.trim()
            })
            $searchButton.disabled = !$editor.value.trim()
          }
        }
      }
    }

    bind({
      selector: 'button#search-nearby',
      action: 'searchNearby',
      pageButton: 'button#next-page'
    })
    bind({
      selector: 'button#search-text',
      action: 'searchText',
      disableFor: 'input#query',
      pageButton: 'button#next-page'
    })
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