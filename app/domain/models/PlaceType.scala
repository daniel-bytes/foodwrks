package domain.models

/**
 * The type of place
 */
sealed trait PlaceType {
  def id: String
  def text: String
  override def toString: String = this.id
}

object PlaceTypes {
  /**
   * Gets a PlaceType by its identifier, or else None
   */
  def byId(id: String): Option[PlaceType] = all.get(id)

  /**
   * Gets a PlaceType by its text description, or else None
   */
  def byName(text: String): Option[PlaceType] = all.collectFirst {
    case (_, v) if v.text == text => v
  }

  // see https://developers.google.com/places/web-service/supported_types#table1
  case object Accounting extends PlaceType {
    override val id: String = "accounting"
    override val text: String = "Accounting"
  }
  case object Airport extends PlaceType {
    override val id: String = "airport"
    override val text: String = "Airport"
  }
  case object AmusementPark extends PlaceType {
    override val id: String = "amusement_park"
    override val text: String = "Amusement Park"
  }
  case object Aquarium extends PlaceType {
    override val id: String = "aquarium"
    override val text: String = "Aquarium"
  }
  case object ArtGallery extends PlaceType {
    override val id: String = "art_gallery"
    override val text: String = "Art Gallery"
  }
  case object Atm extends PlaceType {
    override val id: String = "atm"
    override val text: String = "Atm"
  }
  case object Bakery extends PlaceType {
    override val id: String = "bakery"
    override val text: String = "Bakery"
  }
  case object Bank extends PlaceType {
    override val id: String = "bank"
    override val text: String = "Bank"
  }
  case object Bar extends PlaceType {
    override val id: String = "bar"
    override val text: String = "Bar"
  }
  case object BeautySalon extends PlaceType {
    override val id: String = "beauty_salon"
    override val text: String = "Beauty Salon"
  }
  case object BicycleStore extends PlaceType {
    override val id: String = "bicycle_store"
    override val text: String = "Bicycle Store"
  }
  case object BookStore extends PlaceType {
    override val id: String = "book_store"
    override val text: String = "Book Store"
  }
  case object BowlingAlley extends PlaceType {
    override val id: String = "bowling_alley"
    override val text: String = "Bowling Alley"
  }
  case object BusStation extends PlaceType {
    override val id: String = "bus_station"
    override val text: String = "Bus Station"
  }
  case object Cafe extends PlaceType {
    override val id: String = "cafe"
    override val text: String = "Cafe"
  }
  case object Campground extends PlaceType {
    override val id: String = "campground"
    override val text: String = "Campground"
  }
  case object CarDealer extends PlaceType {
    override val id: String = "car_dealer"
    override val text: String = "Car Dealer"
  }
  case object CarRental extends PlaceType {
    override val id: String = "car_rental"
    override val text: String = "Car Rental"
  }
  case object CarRepair extends PlaceType {
    override val id: String = "car_repair"
    override val text: String = "Car Repair"
  }
  case object CarWash extends PlaceType {
    override val id: String = "car_wash"
    override val text: String = "Car Wash"
  }
  case object Casino extends PlaceType {
    override val id: String = "casino"
    override val text: String = "Casino"
  }
  case object Cemetery extends PlaceType {
    override val id: String = "cemetery"
    override val text: String = "Cemetery"
  }
  case object Church extends PlaceType {
    override val id: String = "church"
    override val text: String = "Church"
  }
  case object CityHall extends PlaceType {
    override val id: String = "city_hall"
    override val text: String = "City Hall"
  }
  case object ClothingStore extends PlaceType {
    override val id: String = "clothing_store"
    override val text: String = "Clothing Store"
  }
  case object ConvenienceStore extends PlaceType {
    override val id: String = "convenience_store"
    override val text: String = "Convenience Store"
  }
  case object Courthouse extends PlaceType {
    override val id: String = "courthouse"
    override val text: String = "Courthouse"
  }
  case object Dentist extends PlaceType {
    override val id: String = "dentist"
    override val text: String = "Dentist"
  }
  case object DepartmentStore extends PlaceType {
    override val id: String = "department_store"
    override val text: String = "Department Store"
  }
  case object Doctor extends PlaceType {
    override val id: String = "doctor"
    override val text: String = "Doctor"
  }
  case object Drugstore extends PlaceType {
    override val id: String = "drugstore"
    override val text: String = "Drugstore"
  }
  case object Electrician extends PlaceType {
    override val id: String = "electrician"
    override val text: String = "Electrician"
  }
  case object ElectronicsStore extends PlaceType {
    override val id: String = "electronics_store"
    override val text: String = "Electronics Store"
  }
  case object Embassy extends PlaceType {
    override val id: String = "embassy"
    override val text: String = "Embassy"
  }
  case object FireStation extends PlaceType {
    override val id: String = "fire_station"
    override val text: String = "Fire Station"
  }
  case object Florist extends PlaceType {
    override val id: String = "florist"
    override val text: String = "Florist"
  }
  case object FuneralHome extends PlaceType {
    override val id: String = "funeral_home"
    override val text: String = "Funeral Home"
  }
  case object FurnitureStore extends PlaceType {
    override val id: String = "furniture_store"
    override val text: String = "Furniture Store"
  }
  case object GasStation extends PlaceType {
    override val id: String = "gas_station"
    override val text: String = "Gas Station"
  }
  case object Gym extends PlaceType {
    override val id: String = "gym"
    override val text: String = "Gym"
  }
  case object HairCare extends PlaceType {
    override val id: String = "hair_care"
    override val text: String = "Hair Care"
  }
  case object HardwareStore extends PlaceType {
    override val id: String = "hardware_store"
    override val text: String = "Hardware Store"
  }
  case object HinduTemple extends PlaceType {
    override val id: String = "hindu_temple"
    override val text: String = "Hindu Temple"
  }
  case object HomeGoodsStore extends PlaceType {
    override val id: String = "home_goods_store"
    override val text: String = "Home Goods Store"
  }
  case object Hospital extends PlaceType {
    override val id: String = "hospital"
    override val text: String = "Hospital"
  }
  case object InsuranceAgency extends PlaceType {
    override val id: String = "insurance_agency"
    override val text: String = "Insurance Agency"
  }
  case object JewelryStore extends PlaceType {
    override val id: String = "jewelry_store"
    override val text: String = "Jewelry Store"
  }
  case object Laundry extends PlaceType {
    override val id: String = "laundry"
    override val text: String = "Laundry"
  }
  case object Lawyer extends PlaceType {
    override val id: String = "lawyer"
    override val text: String = "Lawyer"
  }
  case object Library extends PlaceType {
    override val id: String = "library"
    override val text: String = "Library"
  }
  case object LightRailStation extends PlaceType {
    override val id: String = "light_rail_station"
    override val text: String = "Light Rail Station"
  }
  case object LiquorStore extends PlaceType {
    override val id: String = "liquor_store"
    override val text: String = "Liquor Store"
  }
  case object LocalGovernmentOffice extends PlaceType {
    override val id: String = "local_government_office"
    override val text: String = "Local Government Office"
  }
  case object Locksmith extends PlaceType {
    override val id: String = "locksmith"
    override val text: String = "Locksmith"
  }
  case object Lodging extends PlaceType {
    override val id: String = "lodging"
    override val text: String = "Lodging"
  }
  case object MealDelivery extends PlaceType {
    override val id: String = "meal_delivery"
    override val text: String = "Meal Delivery"
  }
  case object MealTakeaway extends PlaceType {
    override val id: String = "meal_takeaway"
    override val text: String = "Meal Takeaway"
  }
  case object Mosque extends PlaceType {
    override val id: String = "mosque"
    override val text: String = "Mosque"
  }
  case object MovieRental extends PlaceType {
    override val id: String = "movie_rental"
    override val text: String = "Movie Rental"
  }
  case object MovieTheater extends PlaceType {
    override val id: String = "movie_theater"
    override val text: String = "Movie Theater"
  }
  case object MovingCompany extends PlaceType {
    override val id: String = "moving_company"
    override val text: String = "Moving Company"
  }
  case object Museum extends PlaceType {
    override val id: String = "museum"
    override val text: String = "Museum"
  }
  case object NightClub extends PlaceType {
    override val id: String = "night_club"
    override val text: String = "Night Club"
  }
  case object Painter extends PlaceType {
    override val id: String = "painter"
    override val text: String = "Painter"
  }
  case object Park extends PlaceType {
    override val id: String = "park"
    override val text: String = "Park"
  }
  case object Parking extends PlaceType {
    override val id: String = "parking"
    override val text: String = "Parking"
  }
  case object PetStore extends PlaceType {
    override val id: String = "pet_store"
    override val text: String = "Pet Store"
  }
  case object Pharmacy extends PlaceType {
    override val id: String = "pharmacy"
    override val text: String = "Pharmacy"
  }
  case object Physiotherapist extends PlaceType {
    override val id: String = "physiotherapist"
    override val text: String = "Physiotherapist"
  }
  case object Plumber extends PlaceType {
    override val id: String = "plumber"
    override val text: String = "Plumber"
  }
  case object Police extends PlaceType {
    override val id: String = "police"
    override val text: String = "Police"
  }
  case object PostOffice extends PlaceType {
    override val id: String = "post_office"
    override val text: String = "Post Office"
  }
  case object PrimarySchool extends PlaceType {
    override val id: String = "primary_school"
    override val text: String = "Primary School"
  }
  case object RealEstateAgency extends PlaceType {
    override val id: String = "real_estate_agency"
    override val text: String = "Real Estate Agency"
  }
  case object Restaurant extends PlaceType {
    override val id: String = "restaurant"
    override val text: String = "Restaurant"
  }
  case object RoofingContractor extends PlaceType {
    override val id: String = "roofing_contractor"
    override val text: String = "Roofing Contractor"
  }
  case object RvPark extends PlaceType {
    override val id: String = "rv_park"
    override val text: String = "Rv Park"
  }
  case object School extends PlaceType {
    override val id: String = "school"
    override val text: String = "School"
  }
  case object SecondarySchool extends PlaceType {
    override val id: String = "secondary_school"
    override val text: String = "Secondary School"
  }
  case object ShoeStore extends PlaceType {
    override val id: String = "shoe_store"
    override val text: String = "Shoe Store"
  }
  case object ShoppingMall extends PlaceType {
    override val id: String = "shopping_mall"
    override val text: String = "Shopping Mall"
  }
  case object Spa extends PlaceType {
    override val id: String = "spa"
    override val text: String = "Spa"
  }
  case object Stadium extends PlaceType {
    override val id: String = "stadium"
    override val text: String = "Stadium"
  }
  case object Storage extends PlaceType {
    override val id: String = "storage"
    override val text: String = "Storage"
  }
  case object Store extends PlaceType {
    override val id: String = "store"
    override val text: String = "Store"
  }
  case object SubwayStation extends PlaceType {
    override val id: String = "subway_station"
    override val text: String = "Subway Station"
  }
  case object Supermarket extends PlaceType {
    override val id: String = "supermarket"
    override val text: String = "Supermarket"
  }
  case object Synagogue extends PlaceType {
    override val id: String = "synagogue"
    override val text: String = "Synagogue"
  }
  case object TaxiStand extends PlaceType {
    override val id: String = "taxi_stand"
    override val text: String = "Taxi Stand"
  }
  case object TouristAttraction extends PlaceType {
    override val id: String = "tourist_attraction"
    override val text: String = "Tourist Attraction"
  }
  case object TrainStation extends PlaceType {
    override val id: String = "train_station"
    override val text: String = "Train Station"
  }
  case object TransitStation extends PlaceType {
    override val id: String = "transit_station"
    override val text: String = "Transit Station"
  }
  case object TravelAgency extends PlaceType {
    override val id: String = "travel_agency"
    override val text: String = "Travel Agency"
  }
  case object University extends PlaceType {
    override val id: String = "university"
    override val text: String = "University"
  }
  case object VeterinaryCare extends PlaceType {
    override val id: String = "veterinary_care"
    override val text: String = "Veterinary Care"
  }
  case object Zoo extends PlaceType {
    override val id: String = "zoo"
    override val text: String = "Zoo"
  }

  val all: Map[String, PlaceType] = Map(
    "accounting" -> Accounting,
    "airport" -> Airport,
    "amusement_park" -> AmusementPark,
    "aquarium" -> Aquarium,
    "art_gallery" -> ArtGallery,
    "atm" -> Atm,
    "bakery" -> Bakery,
    "bank" -> Bank,
    "bar" -> Bar,
    "beauty_salon" -> BeautySalon,
    "bicycle_store" -> BicycleStore,
    "book_store" -> BookStore,
    "bowling_alley" -> BowlingAlley,
    "bus_station" -> BusStation,
    "cafe" -> Cafe,
    "campground" -> Campground,
    "car_dealer" -> CarDealer,
    "car_rental" -> CarRental,
    "car_repair" -> CarRepair,
    "car_wash" -> CarWash,
    "casino" -> Casino,
    "cemetery" -> Cemetery,
    "church" -> Church,
    "city_hall" -> CityHall,
    "clothing_store" -> ClothingStore,
    "convenience_store" -> ConvenienceStore,
    "courthouse" -> Courthouse,
    "dentist" -> Dentist,
    "department_store" -> DepartmentStore,
    "doctor" -> Doctor,
    "drugstore" -> Drugstore,
    "electrician" -> Electrician,
    "electronics_store" -> ElectronicsStore,
    "embassy" -> Embassy,
    "fire_station" -> FireStation,
    "florist" -> Florist,
    "funeral_home" -> FuneralHome,
    "furniture_store" -> FurnitureStore,
    "gas_station" -> GasStation,
    "gym" -> Gym,
    "hair_care" -> HairCare,
    "hardware_store" -> HardwareStore,
    "hindu_temple" -> HinduTemple,
    "home_goods_store" -> HomeGoodsStore,
    "hospital" -> Hospital,
    "insurance_agency" -> InsuranceAgency,
    "jewelry_store" -> JewelryStore,
    "laundry" -> Laundry,
    "lawyer" -> Lawyer,
    "library" -> Library,
    "light_rail_station" -> LightRailStation,
    "liquor_store" -> LiquorStore,
    "local_government_office" -> LocalGovernmentOffice,
    "locksmith" -> Locksmith,
    "lodging" -> Lodging,
    "meal_delivery" -> MealDelivery,
    "meal_takeaway" -> MealTakeaway,
    "mosque" -> Mosque,
    "movie_rental" -> MovieRental,
    "movie_theater" -> MovieTheater,
    "moving_company" -> MovingCompany,
    "museum" -> Museum,
    "night_club" -> NightClub,
    "painter" -> Painter,
    "park" -> Park,
    "parking" -> Parking,
    "pet_store" -> PetStore,
    "pharmacy" -> Pharmacy,
    "physiotherapist" -> Physiotherapist,
    "plumber" -> Plumber,
    "police" -> Police,
    "post_office" -> PostOffice,
    "primary_school" -> PrimarySchool,
    "real_estate_agency" -> RealEstateAgency,
    "restaurant" -> Restaurant,
    "roofing_contractor" -> RoofingContractor,
    "rv_park" -> RvPark,
    "school" -> School,
    "secondary_school" -> SecondarySchool,
    "shoe_store" -> ShoeStore,
    "shopping_mall" -> ShoppingMall,
    "spa" -> Spa,
    "stadium" -> Stadium,
    "storage" -> Storage,
    "store" -> Store,
    "subway_station" -> SubwayStation,
    "supermarket" -> Supermarket,
    "synagogue" -> Synagogue,
    "taxi_stand" -> TaxiStand,
    "tourist_attraction" -> TouristAttraction,
    "train_station" -> TrainStation,
    "transit_station" -> TransitStation,
    "travel_agency" -> TravelAgency,
    "university" -> University,
    "veterinary_care" -> VeterinaryCare,
    "zoo" -> Zoo
  )

  /* Scala shell to regenerate above
  scala> def transform(t: String): Unit = {
     |   val uppercaseWords = t.split("_").map(_.capitalize)
     |   val identifier = uppercaseWords.mkString("")
     |   val text = uppercaseWords.mkString(" ")
     |   println(s"case object $identifier extends PlaceType {")
     |   println(s"""  override val id: String = "$t"""")
     |   println(s"""  override val text: String = "$text"""")
     |   println(s"}")
     | }

     def item(t: String): (String, String) = {
     |   val uppercaseWords = t.split("_").map(_.capitalize)
     |   val identifier = uppercaseWords.mkString("")
     |   t -> identifier
     | }

     types.foreach { t =>
     | val i = item(t)
     | println(s""""${i._1}" -> ${i._2},""")
     | }
   */
}
