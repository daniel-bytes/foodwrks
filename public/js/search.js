class Search {
  constructor(document, geolocation) {
    this.document = document
    this.geolocation = geolocation
  }

  searchNearby(beginLoading, doneLoading) {
    this.doSearch(
      position => {
        const location = `${position.coords.latitude},${position.coords.longitude}`
        const searchRadius = this.document.getElementById('search-radius').value
        const placeType = this.document.getElementById('place-type').value
        this.document.location.href = `/places/nearby?location=${location}&radius=${searchRadius}&place_type=${placeType}`
      },
      beginLoading,
      doneLoading
    )
  }

  searchText(beginLoading, doneLoading) {
    this.doSearch(
      position => {
        const location = `${position.coords.latitude},${position.coords.longitude}`
        const searchRadius = this.document.getElementById('search-radius').value
        const query = encodeURI(this.document.getElementById('query').value)
        this.document.location.href = `/places/search?location=${location}&radius=${searchRadius}&query=${query}`
      },
      beginLoading,
      doneLoading
    )
  }

  doSearch(action, beginLoading, doneLoading) {
    if (this.geolocation) {
      beginLoading()

      this.geolocation.getCurrentPosition(
        position => {
          doneLoading()
          action(position)
        }, error => {
          doneLoading()
          console.error(error)

          switch(error.code) {
            case error.PERMISSION_DENIED:
              alert('Failed to get browser location: permission denied')
              break
            case error.POSITION_UNAVAILABLE:
              alert('Failed to get browser location: position unavailable, try again')
              break
            case error.TIMEOUT:
              alert('Failed to get browser location: timed out, try again')
              break
            default:
              alert(`Failed to get browser location (error code ${error.code})`)
              break
          }
        }
      )
    } else {
      alert('Browser location search is disabled, please update your browser settings to enable')
    }
  }
}