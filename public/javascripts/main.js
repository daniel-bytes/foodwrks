function maybeConfirm(confirmation) {
  return !confirmation || confirm(confirmation)
}

function findActionButton(element) {
  if (!element) return null
  if (element.dataset.action) return element
  if (element.parentElement) return findActionButton(element.parentElement)
  return null
}


function bindEditors() {
  for(const $editor of document.querySelectorAll(`[data-action^="edit-"]`)) {
    const thing = $editor.dataset.action.split(/-(.+)/)[1]

    for(const $saveButton of document.querySelectorAll(`[data-action="save-${thing}"]`)) {
      $editor.addEventListener('keyup', () => {
        $saveButton.disabled = !$editor.value.trim()
      })
    }
  }
}

function bindDeleteCommentLinks(app) {
  for(const deleteLink of document.querySelectorAll(`button[data-action="delete-comment"]`)) {
    deleteLink.addEventListener('click', async event => {
      const $element = findActionButton(event.srcElement)

      if ($element) {
        const placeId = $element.dataset.placeid
        const commentId = $element.dataset.commentid
        const confirmation = $element.dataset.confirm
        if (maybeConfirm(confirmation)) {
          await app.deleteEntity(`/places/${placeId}/comments/${commentId}`)
        }
        return false
      }
    })
  }
}

function bindSavePlaceButtons(app) {
  for(const saveButton of document.querySelectorAll(`button[data-action="save-place"]`)) {
    saveButton.addEventListener('click', async event => {
      const $element = findActionButton(event.srcElement)

      if ($element) {
        const placeId = $element.dataset.placeid
        const externalId = $element.dataset.externalid
        const visitStatus = $element.dataset.visitstatus
        const confirmation = $element.dataset.confirm
        if (maybeConfirm(confirmation)) {
          await app.postEntity(
            `/places/${placeId}`,
            `visit_status=${visitStatus}&external_id=${externalId}`
          )
        }

        return false
      }
    })
  }
}

function bindLocationSearch() {
  const $searchButton = document.querySelector('button#search-nearby')
  if ($searchButton) {
    if (navigator.geolocation) {
      $searchButton.addEventListener('click', event => {
        $searchButton.classList.add('is-loading')

        navigator.geolocation.getCurrentPosition(
          position => {
            $searchButton.classList.remove('is-loading')
            const location = `${position.coords.latitude},${position.coords.longitude}`
            const searchRadius = document.getElementById('search-radius').value
            document.location.href = `/places/nearby?location=${location}&radius=${searchRadius}`
          }, error => {
            $searchButton.classList.remove('is-loading')
            alert(`Failed to get browser location: ${error}`)
            console.error(error)
          }
        )
      })
    } else {
      $searchButton.disabled = true
      $searchButton.textContent = "Location search disabled"
    }
  }
}

document.addEventListener('DOMContentLoaded', () => {
  const app = new Application(
    url => document.location.href = url,
    error => alert(error)
  )

  bindEditors()
  bindDeleteCommentLinks(app)
  bindSavePlaceButtons(app)
  bindLocationSearch()
})

