class Application {
  constructor(redirect, showError) {
    this.redirect = redirect
    this.showError = showError
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

  async postEntity(url, data) {
    const response = await fetch(url, {
      method: 'POST',
      credentials: 'same-origin',
      headers: {
        'Content-Type': 'application/x-www-form-urlencoded',
        'X-Requested-With': 'fetch'
      },
      body: data
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