/**
 * Dynamic forms manager class.
 * Uses the browser `fetch` API for calling forms,
 * redirecting to whatever URL the response contains.
 */
class Forms {
  constructor(redirect, showError) {
    this.redirect = redirect
    this.showError = showError
  }

  deleteComment(placeId, commentId) {
    return this.deleteEntity(
      `/places/${placeId}/comments/${commentId}`
    )
  }

  savePlace(placeId, externalId, visitStatus) {
    const formData = new FormData()
    formData.set('external_id', externalId)
    formData.set('visit_status', visitStatus)
    return this.postEntity(
       `/places/${placeId}`,
       formData
     )
  }

  async deleteEntity(url) {
    const response = await fetch(url, {
      method: 'DELETE',
      credentials: 'same-origin',
      headers: {
        'X-Requested-With': 'fetch'
      }
    })

    await this.handleResponse(response)
  }

  async postEntity(url, formData) {
    const response = await fetch(url, {
      method: 'POST',
      credentials: 'same-origin',
      headers: {
        'X-Requested-With': 'fetch'
      },
      body: formData
    })

    await this.handleResponse(response)
  }

  async handleResponse(response) {
    if (response.status < 400) {
      if (response.redirected) {
        this.redirect(response.url)
      }
    } else {
      console.error(response)

      const text = await response.text()
      console.error(text)

      this.showError(`${response.statusText}: ${text}`)
    }
  }
}